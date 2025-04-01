package main.als.grouptest;

import main.als.apiPayload.code.status.ErrorStatus;
import main.als.apiPayload.exception.GeneralException;
import main.als.group.converter.GroupConverter;
import main.als.group.dto.GroupRequestDto;
import main.als.group.dto.GroupResponseDto;
import main.als.group.entity.Group;
import main.als.group.entity.UserGroup;
import main.als.group.repository.GroupRepository;
import main.als.group.repository.UserGroupRepository;
import main.als.group.service.GroupService;
import main.als.group.service.GroupServiceImpl;
import main.als.page.PostPagingDto;
import main.als.payment.dto.PaymentRequestDto;
import main.als.payment.entity.Payment;
import main.als.payment.repository.PaymentRepository;
import main.als.payment.util.PaymentUtil;
import main.als.user.entity.Role;
import main.als.user.entity.User;
import main.als.user.repository.UserRepository;
import org.json.simple.JSONObject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class GroupServiceTest {

    @InjectMocks
    private GroupServiceImpl groupService;

    @Mock
    private GroupRepository groupRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserGroupRepository userGroupRepository;

    @Mock
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Mock
    private PaymentRepository paymentRepository;

    BigDecimal amount  = BigDecimal.valueOf(1000);
    LocalDateTime now = LocalDateTime.now();

    @Test
    @DisplayName("그룹 생성 테스트")
    public void createGroupSuccess() {

        String username = "username";

        GroupRequestDto.CreateGroupDto groupDto = GroupRequestDto.CreateGroupDto.builder()
                .groupname("groupname")
                .password("password")
                .depositAmount(amount)
                .deadline(now.plusDays(1))
                .studyEndDate(now.plusDays(3))
                .build();

        User user = User.builder()
                .username(username)
                .password("password")
                .role(Role.ROLE_USER)
                .customerId("customerId")
                .build();

        Group group = Group.builder()
                .name(groupDto.getGroupname())
                .password("password")
                .depositAmount(groupDto.getDepositAmount())
                .leader(user.getUsername())
                .deadline(groupDto.getDeadline())
                .studyEndDate(groupDto.getStudyEndDate())
                .createdAt(now)
                .build();

        when(userRepository.findByUsername(username)).thenReturn(user);
        when(bCryptPasswordEncoder.encode(groupDto.getPassword())).thenReturn(groupDto.getPassword());
        when(groupRepository.save(any(Group.class))).thenReturn(group);

        Group createdGroup = groupService.createGroup(groupDto,username);

        assertNotNull(createdGroup);
        assertEquals(groupDto.getGroupname(), createdGroup.getName());
        assertEquals(user.getUsername(), createdGroup.getLeader());
        assertEquals(groupDto.getPassword(), createdGroup.getPassword());
        assertEquals(groupDto.getDepositAmount(), createdGroup.getDepositAmount());


        verify(userGroupRepository,times(1)).save(any(UserGroup.class));

        assertEquals(1, createdGroup.getUserGroups().size());
        assertEquals(1, user.getUserGroups().size());

        UserGroup userGroup = createdGroup.getUserGroups().get(0);
        assertEquals(user, userGroup.getUser());
        assertEquals(createdGroup, userGroup.getGroup());
        assertFalse(userGroup.isRefunded());
        assertFalse(userGroup.isCharged());

    }

    @Test
    @DisplayName("그룹생성 사용자 없음 테스트")
    public void createGroupFailTest(){

        String username = "username";

        GroupRequestDto.CreateGroupDto groupDto = GroupRequestDto.CreateGroupDto.builder()
                .groupname("groupname")
                .password("password")
                .depositAmount(amount)
                .deadline(now.plusDays(1))
                .studyEndDate(now.plusDays(3))
                .build();

        when(userRepository.findByUsername(username)).thenReturn(null);

        GeneralException exception = assertThrows(GeneralException.class,()->groupService.createGroup(groupDto,username));

        assertEquals(false,exception.getErrorReasonHttpStatus().getIsSuccess());
        assertEquals("USER400_2",exception.getErrorReasonHttpStatus().getCode());
        assertEquals("회원가입된 아이디가 아닙니다.",exception.getErrorReasonHttpStatus().getMessage());
        assertEquals(HttpStatus.NOT_FOUND,exception.getErrorReasonHttpStatus().getHttpStatus());

    }

    @Test
    @DisplayName("검색어 없이 그룹 검색 테스트")
    public void getAllGroupsTest1() {

        PostPagingDto.PagingDto pagingDto = PostPagingDto.PagingDto.builder()
                .page(0)
                .size(10)
                .sort("ASC")
                .build();

        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "deadline"));

        Page<Group> mockGroupPage = new PageImpl<>(List.of(
                Group.builder().name("스터디1")
                        .password("password")
                        .depositAmount(amount)
                        .deadline(now.plusDays(1))
                        .studyEndDate(now.plusDays(3))
                        .build(),
                Group.builder().name("스터디2")
                        .password("password2")
                        .depositAmount(amount)
                        .deadline(now.plusDays(1))
                        .studyEndDate(now.plusDays(3))
                        .build()
        ));

        when(groupRepository.findAllByDeadlineAfter(any(LocalDateTime.class), eq(pageable))).thenReturn(mockGroupPage);

        GroupResponseDto.SearchGroups mockResponse = GroupResponseDto.SearchGroups.builder()
                .groupResDtos(List.of())
                .isFirst(true)
                .isLast(false)
                .listSize(2)
                .totalElements(10L)
                .build();

        try (MockedStatic<GroupConverter> mockedStatic = mockStatic(GroupConverter.class)) {
            mockedStatic.when(() -> GroupConverter.toSearchGroupDto(mockGroupPage))
                    .thenReturn(mockResponse);

            // when
            GroupResponseDto.SearchGroups result = groupService.getAllGroups(pagingDto, null);

            // then
            assertNotNull(result);
            assertTrue(result.isFirst());
            assertFalse(result.isLast());
            assertEquals(2, result.getListSize());
            assertEquals(10L, result.getTotalElements());

        }

    }

    @Test
    @DisplayName("검색어 존재 그룹 검색 테스트")
    public void getAllGroupsTest2() {

        PostPagingDto.PagingDto pagingDto = PostPagingDto.PagingDto.builder()
                .page(0)
                .size(10)
                .sort("ASC")
                .build();

        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "deadline"));

        Page<Group> mockGroupPage = new PageImpl<>(List.of(
                Group.builder().name("스터디1")
                        .password("password")
                        .depositAmount(amount)
                        .deadline(now.plusDays(1))
                        .studyEndDate(now.plusDays(3))
                        .build(),
                Group.builder().name("스터디2")
                        .password("password2")
                        .depositAmount(amount)
                        .deadline(now.plusDays(1))
                        .studyEndDate(now.plusDays(3))
                        .build()
        ));

        when(groupRepository.findByNameContainingAndDeadlineAfter(any(String.class), any(LocalDateTime.class), eq(pageable))).thenReturn(mockGroupPage);

        GroupResponseDto.SearchGroups mockResponse = GroupResponseDto.SearchGroups.builder()
                .groupResDtos(List.of())
                .isFirst(true)
                .isLast(false)
                .listSize(2)
                .totalElements(10L)
                .build();

        try (MockedStatic<GroupConverter> mockedStatic = mockStatic(GroupConverter.class)) {
            mockedStatic.when(() -> GroupConverter.toSearchGroupDto(mockGroupPage))
                    .thenReturn(mockResponse);

            // when
            GroupResponseDto.SearchGroups result = groupService.getAllGroups(pagingDto, "search");

            // then
            assertNotNull(result);
            assertTrue(result.isFirst());
            assertFalse(result.isLast());
            assertEquals(2, result.getListSize());
            assertEquals(10L, result.getTotalElements());

        }
    }

    @Test
    @DisplayName("그룹 아이디로 그룹찾기 테스트")
    public void getGroupSuccessTest(){

        Group group1 = Group.builder()
                .id(1L)
                .name("group")
                .password("password")
                .depositAmount(amount)
                .createdAt(now)
                .deadline(now.plusDays(1))
                .studyEndDate(now.plusDays(3))
                .build();

        GroupResponseDto.AllGroupDto allGroupDto = GroupResponseDto.AllGroupDto.builder()
                .id(1L)
                .groupname("group")
                .username("username")
                .depositAmount(amount)
                .createdAt(now)
                .deadline(now.plusDays(1))
                .stutyEndDate(now.plusDays(3))
                .build();

        Long groupId = 1L;

        when(groupRepository.findById(1L)).thenReturn(Optional.of(group1));

        try (MockedStatic<GroupConverter> mockedStatic = mockStatic(GroupConverter.class)) {
            mockedStatic.when(() -> GroupConverter.toAllGroupDto(group1))
                    .thenReturn(allGroupDto);

            // when
            GroupResponseDto.AllGroupDto groupDto= groupService.getGroup(1L);

            // then
            assertNotNull(groupDto);
            assertEquals(1L, groupDto.getId());
            assertEquals("group", groupDto.getGroupname());
            assertEquals("username", groupDto.getUsername());
            assertEquals(amount, groupDto.getDepositAmount());
            assertEquals(now, groupDto.getCreatedAt());
            assertEquals(now.plusDays(1), groupDto.getDeadline());
            assertEquals(now.plusDays(3), groupDto.getStutyEndDate());

        }

    }

    @Test
    @DisplayName("그룹 아이디 없을 때 찾기 테스트")
    public void getGroupFailTest(){

        Long groupId = 1L;

        when(groupRepository.findById(groupId)).thenReturn(Optional.empty());

        GeneralException exception = assertThrows(GeneralException.class, () ->
                groupService.getGroup(groupId)
        );

        assertEquals(false,exception.getErrorReasonHttpStatus().getIsSuccess());
        assertEquals(HttpStatus.NOT_FOUND,exception.getErrorReasonHttpStatus().getHttpStatus());
        assertEquals("GROUP400_2",exception.getErrorReasonHttpStatus().getCode());
        assertEquals("그룹이 존재하지 않습니다.",exception.getErrorReasonHttpStatus().getMessage());

    }

    @Test
    @DisplayName("그룹 삭제 성공 테스트")
    public void deleteGroupSuccessTest(){

        Long groupId = 7L;
        String username = "username";
        String password = "password";

        Group group1 = Group.builder()
                .id(7L)
                .name("group")
                .password("password")
                .leader(username)
                .depositAmount(amount)
                .createdAt(now)
                .deadline(now.plusDays(1))
                .studyEndDate(now.plusDays(3))
                .build();

        when(groupRepository.findById(groupId)).thenReturn(Optional.of(group1));
        when(bCryptPasswordEncoder.matches(password, group1.getPassword())).thenReturn(true);

        groupService.deleteGroup(groupId, username, password);

        verify(groupRepository, times(1)).deleteById(groupId);

    }

    @Test
    @DisplayName("그룹 삭제 실패 테스트 - (그룹 아이디 없음)")
    public void deleteGroupFailTest1(){

        Long groupId = 7L;
        String username = "username";
        String password = "password";

        when(groupRepository.findById(groupId)).thenReturn(Optional.empty());

        GeneralException exception = assertThrows(GeneralException.class, () ->groupService.deleteGroup(groupId, username, password));

        assertFalse(exception.getErrorReasonHttpStatus().getIsSuccess());
        assertEquals(HttpStatus.NOT_FOUND,exception.getErrorReasonHttpStatus().getHttpStatus());
        assertEquals("GROUP400_2",exception.getErrorReasonHttpStatus().getCode());
        assertEquals("그룹이 존재하지 않습니다.",exception.getErrorReasonHttpStatus().getMessage());

    }

    @Test
    @DisplayName("그룹 삭제 실패 테스트 - (리더 불일치)")
    public void deleteGroupFailTest2(){

        Long groupId = 7L;
        String username = "username1";
        String password = "password";

        Group group1 = Group.builder()
                .id(7L)
                .name("group")
                .password("password")
                .leader("username2")
                .depositAmount(amount)
                .createdAt(now)
                .deadline(now.plusDays(1))
                .studyEndDate(now.plusDays(3))
                .build();

        when(groupRepository.findById(groupId)).thenReturn(Optional.of(group1));

        GeneralException exception = assertThrows(GeneralException.class, () ->groupService.deleteGroup(groupId, username, password));

        assertFalse(exception.getErrorReasonHttpStatus().getIsSuccess());
        assertEquals(HttpStatus.BAD_REQUEST,exception.getErrorReasonHttpStatus().getHttpStatus());
        assertEquals("GROUP400_4",exception.getErrorReasonHttpStatus().getCode());
        assertEquals("리더가 일치하지 않습니다.",exception.getErrorReasonHttpStatus().getMessage());
    }

    @Test
    @DisplayName("그룹 삭제 실패 테스트 - (비밀번호 불일치)")
    public void deleteGroupFailTest3(){

        Long groupId = 7L;
        String username = "username1";
        String password = "password";

        Group group1 = Group.builder()
                .id(7L)
                .name("group")
                .password("password")
                .leader("username1")
                .depositAmount(amount)
                .createdAt(now)
                .deadline(now.plusDays(1))
                .studyEndDate(now.plusDays(3))
                .build();

        when(groupRepository.findById(groupId)).thenReturn(Optional.of(group1));
        when(bCryptPasswordEncoder.matches(password, group1.getPassword())).thenReturn(false);

        GeneralException exception = assertThrows(GeneralException.class, () ->groupService.deleteGroup(groupId, username, password));
        assertFalse(exception.getErrorReasonHttpStatus().getIsSuccess());
        assertEquals(HttpStatus.BAD_REQUEST,exception.getErrorReasonHttpStatus().getHttpStatus());
        assertEquals("GROUP400_3",exception.getErrorReasonHttpStatus().getCode());
        assertEquals("그룹 비밀번호가 일치하지 않습니다.",exception.getErrorReasonHttpStatus().getMessage());
    }

    @Test
    @DisplayName("만료된 그룹 삭제 테스트")
    public void deleteExpiredGroupsTest(){

        Group group1 = Group.builder()
                .id(1L)
                .name("group")
                .password("password")
                .leader("username2")
                .depositAmount(amount)
                .createdAt(now)
                .deadline(now.plusDays(1))
                .studyEndDate(now.plusDays(3))
                .build();
        Group group2 = Group.builder()
                .id(2L)
                .name("group2")
                .password("password2")
                .leader("username2")
                .depositAmount(amount)
                .createdAt(now)
                .deadline(now.plusDays(1))
                .studyEndDate(now.plusDays(3))
                .build();

        List<Group> groups = Arrays.asList(group1, group2);
        when(groupRepository.findExpiredGroups(any(LocalDateTime.class))).thenReturn(groups);

        groupService.deleteExpiredGroups();

        verify(groupRepository, times(1)).deleteById(1L);
        verify(groupRepository, times(1)).deleteById(2L);
    }

    @Test
    @DisplayName("결제를 통해 그룹 생성 성공 테스트")
    public void createGroupWithPaymentTest(){

        String username = "username1";
        String password = "password";

        User user = User.builder()
                .id(1L)
                .username(username)
                .password(password)
                .customerId("customerId")
                .role(Role.ROLE_USER)
                .build();

        Group group = Group.builder()
                .id(1L)
                .name("group")
                .password("password")
                .leader("username1")
                .depositAmount(amount)
                .createdAt(now)
                .deadline(now.plusDays(1))
                .studyEndDate(now.plusDays(3))
                .build();

        PaymentRequestDto.GroupPaymentDto groupPaymentDto = PaymentRequestDto.GroupPaymentDto.builder()
                .orderId("orderId")
                .amount(String.valueOf(amount))
                .paymentKey("paymentKey")
                .build();

        GroupRequestDto.CreateWithPaymentDto createWithPaymentDto = GroupRequestDto.CreateWithPaymentDto.builder()
                .groupname("group")
                .password("password")
                .depositAmount(amount)
                .deadline(now.plusDays(1))
                .studyEndDate(now.plusDays(3))
                .GroupPaymentDto(groupPaymentDto)
                .build();

        JSONObject mockJsonResponse = new JSONObject();
        mockJsonResponse.put("paymentKey", "paymentKey");
        mockJsonResponse.put("orderId", "orderId");
        mockJsonResponse.put("totalAmount", String.valueOf(amount));
        mockJsonResponse.put("requestedAt", now.plusDays(1));

        Payment payment = Payment.builder()
                .paymentKey(mockJsonResponse.get("paymentKey").toString())
                .orderId(mockJsonResponse.get("orderId").toString())
                .requestedAt(mockJsonResponse.get("requestedAt").toString())
                .totalAmount(mockJsonResponse.get("totalAmount").toString())
                .build();

        UserGroup useGroup = UserGroup.builder()
                .id(1L)
                .user(user)
                .group(group)
                .userDepositAmount(amount)
                .refunded(false)
                .charged(true)
                .paymentKey(mockJsonResponse.get("paymentKey").toString())
                .build();

        when(userRepository.findByUsername(username)).thenReturn(user);
        when(bCryptPasswordEncoder.encode(anyString())).thenReturn(password);
        when(groupRepository.save(any(Group.class))).thenReturn(group);

        try(MockedStatic<PaymentUtil> mockedStatic = mockStatic(PaymentUtil.class)){
            mockedStatic.when(() -> PaymentUtil.confirmPayment(groupPaymentDto.getOrderId(),groupPaymentDto.getAmount(),groupPaymentDto.getPaymentKey())).thenReturn(mockJsonResponse);

            groupService.createGroupWithPayment(createWithPaymentDto,username);

            verify(groupRepository, times(1)).save(any(Group.class));
            verify(paymentRepository, times(1)).save(any(Payment.class));
            verify(userGroupRepository, times(1)).save(any(UserGroup.class));

        }

    }

    @Test
    @DisplayName("결제를 통해 그룹 생성 실패 테스트 - (리더 불일치)")
    public void createGroupWithPaymentFailTest1(){

        String username = "username1";
        String password = "password";

        PaymentRequestDto.GroupPaymentDto groupPaymentDto = PaymentRequestDto.GroupPaymentDto.builder()
                .orderId("orderId")
                .amount(String.valueOf(amount))
                .paymentKey("paymentKey")
                .build();

        GroupRequestDto.CreateWithPaymentDto createWithPaymentDto = GroupRequestDto.CreateWithPaymentDto.builder()
                .groupname("group")
                .password("password")
                .depositAmount(amount)
                .deadline(now.plusDays(1))
                .studyEndDate(now.plusDays(3))
                .GroupPaymentDto(groupPaymentDto)
                .build();

        when(userRepository.findByUsername(username)).thenReturn(null);

        GeneralException exception = assertThrows(GeneralException.class,()->groupService.createGroupWithPayment(createWithPaymentDto,username));

        assertFalse(exception.getErrorReasonHttpStatus().getIsSuccess());
        assertEquals("USER400_2",exception.getErrorReason().getCode());
        assertEquals("회원가입된 아이디가 아닙니다.",exception.getErrorReason().getMessage());

    }

    @Test
    @DisplayName("결제를 통해 그룹 생성 실패 테스트 - (토스페이먼츠 인증 실패)")
    public void createGroupWithPaymentFailTest2(){

        String username = "username1";
        String password = "password";

        User user = User.builder()
                .id(1L)
                .username(username)
                .password(password)
                .customerId("customerId")
                .role(Role.ROLE_USER)
                .build();

        Group group = Group.builder()
                .id(1L)
                .name("group")
                .password("password")
                .leader("username1")
                .depositAmount(amount)
                .createdAt(now)
                .deadline(now.plusDays(1))
                .studyEndDate(now.plusDays(3))
                .build();

        PaymentRequestDto.GroupPaymentDto groupPaymentDto = PaymentRequestDto.GroupPaymentDto.builder()
                .orderId("orderId")
                .amount(String.valueOf(amount))
                .paymentKey("paymentKey")
                .build();

        GroupRequestDto.CreateWithPaymentDto createWithPaymentDto = GroupRequestDto.CreateWithPaymentDto.builder()
                .groupname("group")
                .password("password")
                .depositAmount(amount)
                .deadline(now.plusDays(1))
                .studyEndDate(now.plusDays(3))
                .GroupPaymentDto(groupPaymentDto)
                .build();

        when(userRepository.findByUsername(username)).thenReturn(user);
        when(bCryptPasswordEncoder.encode(anyString())).thenReturn(password);
        when(groupRepository.save(any(Group.class))).thenReturn(group);

        try(MockedStatic<PaymentUtil> mockedStatic = mockStatic(PaymentUtil.class)){
            mockedStatic.when(() -> PaymentUtil.confirmPayment(groupPaymentDto.getOrderId(),groupPaymentDto.getAmount(),groupPaymentDto.getPaymentKey())).thenReturn(null);

            GeneralException exception = assertThrows(GeneralException.class,()->groupService.createGroupWithPayment(createWithPaymentDto,username));

            assertEquals(ErrorStatus._TOSS_CONFIRM_FAIL, exception.getCode());
            assertFalse(exception.getErrorReasonHttpStatus().getIsSuccess());
            assertEquals("PAYMENT400_1",exception.getErrorReason().getCode());
            assertEquals("결제 실패입니다.",exception.getErrorReason().getMessage());

            verify(groupRepository, times(1)).save(any(Group.class));

        }

    }

    @Test
    @DisplayName("결제를 통해 그룹 생성 실패 테스트 - (토스페이먼츠 결제 실패)")
    public void createGroupWithPaymentFailTest3(){
        String username = "username1";
        String password = "password";

        User user = User.builder()
                .id(1L)
                .username(username)
                .password(password)
                .customerId("customerId")
                .role(Role.ROLE_USER)
                .build();

        Group group = Group.builder()
                .id(1L)
                .name("group")
                .password("password")
                .leader("username1")
                .depositAmount(amount)
                .createdAt(now)
                .deadline(now.plusDays(1))
                .studyEndDate(now.plusDays(3))
                .build();

        PaymentRequestDto.GroupPaymentDto groupPaymentDto = PaymentRequestDto.GroupPaymentDto.builder()
                .orderId("orderId")
                .amount(String.valueOf(amount))
                .paymentKey("paymentKey")
                .build();

        GroupRequestDto.CreateWithPaymentDto createWithPaymentDto = GroupRequestDto.CreateWithPaymentDto.builder()
                .groupname("group")
                .password("password")
                .depositAmount(amount)
                .deadline(now.plusDays(1))
                .studyEndDate(now.plusDays(3))
                .GroupPaymentDto(groupPaymentDto)
                .build();

        JSONObject mockJsonResponse = new JSONObject();
        mockJsonResponse.put("paymentKey", "paymentKey");
        mockJsonResponse.put("orderId", "orderId");
        mockJsonResponse.put("totalAmount", String.valueOf(amount));
        mockJsonResponse.put("requestedAt", now.plusDays(1));

        Payment payment = Payment.builder()
                .paymentKey(mockJsonResponse.get("paymentKey").toString())
                .orderId(mockJsonResponse.get("orderId").toString())
                .requestedAt(mockJsonResponse.get("requestedAt").toString())
                .totalAmount(mockJsonResponse.get("totalAmount").toString())
                .build();

        when(userRepository.findByUsername(username)).thenReturn(user);
        when(bCryptPasswordEncoder.encode(anyString())).thenReturn(password);
        when(groupRepository.save(any(Group.class))).thenReturn(group);
        when(paymentRepository.save(any(Payment.class))).thenThrow(new RuntimeException("Error"));

        try (MockedStatic<PaymentUtil> mockedStatic = mockStatic(PaymentUtil.class)) {
            mockedStatic.when(() -> PaymentUtil.confirmPayment(
                            groupPaymentDto.getOrderId(), groupPaymentDto.getAmount(), groupPaymentDto.getPaymentKey()))
                    .thenReturn(mockJsonResponse);

            mockedStatic.when(() -> PaymentUtil.processRefund("paymentKey", amount))
                    .thenReturn(mockJsonResponse);

            // when & then
            GeneralException exception = assertThrows(GeneralException.class, () ->
                    groupService.createGroupWithPayment(createWithPaymentDto, username)
            );

            assertEquals(ErrorStatus._TOSS_SAVE_FAIL, exception.getCode());
            assertFalse(exception.getErrorReasonHttpStatus().getIsSuccess());
            assertEquals("PAYMENT400_2",exception.getErrorReason().getCode());
            assertEquals("결제 정보 저장 에러입니다.",exception.getErrorReason().getMessage());

            mockedStatic.verify(() -> PaymentUtil.processRefund("paymentKey", amount), times(1));
        }
    }

}
