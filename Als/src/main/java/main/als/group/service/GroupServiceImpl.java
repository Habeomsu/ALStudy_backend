package main.als.group.service;

import jakarta.transaction.Transactional;
import main.als.apiPayload.code.status.ErrorStatus;
import main.als.apiPayload.exception.GeneralException;
import main.als.group.converter.GroupConverter;
import main.als.group.dto.GroupRequestDto;
import main.als.group.dto.GroupResponseDto;
import main.als.group.entity.Group;
import main.als.group.entity.UserGroup;
import main.als.group.repository.GroupRepository;
import main.als.group.repository.UserGroupRepository;
import main.als.page.PostPagingDto;
import main.als.payment.dto.PaymentRequestDto;
import main.als.payment.entity.Payment;
import main.als.payment.repository.PaymentRepository;
import main.als.payment.util.PaymentUtil;
import main.als.user.entity.User;
import main.als.user.repository.UserRepository;
import org.json.simple.JSONObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class GroupServiceImpl implements GroupService {

    private final GroupRepository groupRepository;
    private final UserRepository userRepository;
    private final UserGroupRepository userGroupRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final PaymentRepository paymentRepository;

    public GroupServiceImpl(GroupRepository groupRepository, UserRepository userRepository,
                            UserGroupRepository userGroupRepository, BCryptPasswordEncoder bCryptPasswordEncoder,
                            PaymentRepository paymentRepository) {
        this.groupRepository = groupRepository;
        this.userRepository = userRepository;
        this.userGroupRepository = userGroupRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.paymentRepository = paymentRepository;
    }

    @Override
    @Transactional
    public Group createGroup(GroupRequestDto.CreateGroupDto groupRequestDto,String username){

        User leader = userRepository.findByUsername(username);
        if (leader == null) {
            throw new GeneralException(ErrorStatus._USERNAME_NOT_FOUND);
        }
        Group group = Group.builder()
                .name(groupRequestDto.getGroupname())
                .password(bCryptPasswordEncoder.encode(groupRequestDto.getPassword()))
                .leader(leader.getUsername())
                .depositAmount(groupRequestDto.getDepositAmount())
                .createdAt(LocalDateTime.now())
                .deadline(groupRequestDto.getDeadline())
                .studyEndDate(groupRequestDto.getStudyEndDate())
                .build();
        Group savedGroup = groupRepository.save(group);

        UserGroup userGroup = UserGroup.builder()
                .user(leader)
                .group(savedGroup)
                .userDepositAmount(BigDecimal.ZERO)
                .refunded(false)
                .charged(false)
                .paymentKey(null)
                .build();

        userGroupRepository.save(userGroup);
        leader.getUserGroups().add(userGroup);
        savedGroup.getUserGroups().add(userGroup);

        return savedGroup;
    }

    @Override
    public GroupResponseDto.SearchGroups getAllGroups(PostPagingDto.PagingDto pagingDto,String search) {
        Sort sort = Sort.by(Sort.Direction.fromString(pagingDto.getSort()), "deadline");
        Pageable pageable = PageRequest.of(pagingDto.getPage(), pagingDto.getSize(), sort);
        // 현재 시간 가져오기
        LocalDateTime now = LocalDateTime.now();

        Page<Group> groupPages;
        if (search != null && !search.isEmpty()) {
            groupPages = groupRepository.findByNameContainingAndDeadlineAfter(search, now, pageable);
        } else {
            groupPages = groupRepository.findAllByDeadlineAfter(now, pageable);
        }

        return GroupConverter.toSearchGroupDto(groupPages);
    }

    @Override
    public GroupResponseDto.AllGroupDto getGroup(Long GroupId) {
        Group group = groupRepository.findById(GroupId)
                .orElseThrow(()->new GeneralException(ErrorStatus._NOT_FOUND_GROUP));
        return GroupConverter.toAllGroupDto(group);
    }



    @Override
    public boolean validateGroupPassword(GroupRequestDto.ValidPasswordDto validPasswordDto) {
        String password = validPasswordDto.getPassword();
        Group group = groupRepository.findById(validPasswordDto.getId())
                .orElseThrow(() -> new GeneralException(ErrorStatus._NOT_FOUND_GROUP));
        if (!bCryptPasswordEncoder.matches(password, group.getPassword())) {
            throw new GeneralException(ErrorStatus._NOT_MATCH_GROUPPASSWORD);
        }
        return true;
    }

    @Override
    public void deleteGroup(Long id, String username, String password) {
        Group group = groupRepository.findById(id)
                .orElseThrow(() -> new GeneralException(ErrorStatus._NOT_FOUND_GROUP));

        // 리더 검증
        if (!group.getLeader().equals(username)) {
            throw new GeneralException(ErrorStatus._NOT_MATCH_LEADER);
        }

        // 비밀번호 검증
        if (!bCryptPasswordEncoder.matches(password, group.getPassword())) {
            throw new GeneralException(ErrorStatus._NOT_MATCH_GROUPPASSWORD);
        }

        groupRepository.deleteById(id);
    }



    @Override
    @Transactional
    @Scheduled(cron = "0 0 0 * * ?")
    public void deleteExpiredGroups() {

        LocalDateTime now = LocalDateTime.now();
        List<Long> expiredGroupIds = groupRepository.findExpiredGroups(now).stream()
                .map(Group::getId)
                .collect(Collectors.toList());

        for (Long id : expiredGroupIds) {
            groupRepository.deleteById(id);
        }

    }

    @Override
    @Transactional
    public void createGroupWithPayment(GroupRequestDto.CreateWithPaymentDto createWithPaymentDto, String username) {

        User leader = userRepository.findByUsername(username);
        if (leader == null) {
            throw new GeneralException(ErrorStatus._USERNAME_NOT_FOUND);
        }

        Group group = Group.builder()
                .name(createWithPaymentDto.getGroupname())
                .password(bCryptPasswordEncoder.encode(createWithPaymentDto.getPassword()))
                .depositAmount(createWithPaymentDto.getDepositAmount())
                .leader(leader.getUsername())
                .createdAt(LocalDateTime.now())
                .deadline(createWithPaymentDto.getDeadline())
                .studyEndDate(createWithPaymentDto.getStudyEndDate())
                .build();

        Group savedGroup = groupRepository.save(group);

        PaymentRequestDto.GroupPaymentDto paymentDto = createWithPaymentDto.getGroupPaymentDto(); // 결제 정보 포함
        JSONObject jsonResponse = PaymentUtil.confirmPayment(paymentDto.getOrderId(), paymentDto.getAmount(), paymentDto.getPaymentKey());

        if (jsonResponse == null) {
            throw new GeneralException(ErrorStatus._TOSS_CONFIRM_FAIL);
        }

        Payment payment = Payment.builder()
                .paymentKey(jsonResponse.get("paymentKey").toString())
                .orderId(jsonResponse.get("orderId").toString())
                .requestedAt(jsonResponse.get("requestedAt").toString())
                .totalAmount(jsonResponse.get("totalAmount").toString())
                .build();

        try {
            paymentRepository.save(payment); // 결제 정보 저장

            UserGroup userGroup = UserGroup.builder()
                    .user(leader)
                    .group(savedGroup)
                    .userDepositAmount(new BigDecimal(payment.getTotalAmount())) // 결제 금액 설정
                    .refunded(false)
                    .charged(true) // 결제 완료 상태
                    .paymentKey(payment.getPaymentKey())
                    .build();

            userGroupRepository.save(userGroup);

            leader.getUserGroups().add(userGroup);
            savedGroup.getUserGroups().add(userGroup);

        } catch (Exception e) {
            // 결제 저장 또는 UserGroup 업데이트 중 에러 발생 시 환불 처리
            BigDecimal refundAmount = new BigDecimal(payment.getTotalAmount());
            JSONObject refundResponse = PaymentUtil.processRefund(payment.getPaymentKey(), refundAmount);
            throw new GeneralException(ErrorStatus._TOSS_SAVE_FAIL); // 예외 던지기
        }

    }

}
