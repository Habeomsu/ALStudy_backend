package main.als.group.service;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import main.als.apiPayload.code.status.ErrorStatus;
import main.als.apiPayload.exception.GeneralException;
import main.als.group.dto.UserGroupResponseDto;
import main.als.group.entity.Group;
import main.als.group.entity.UserGroup;
import main.als.group.repository.GroupRepository;
import main.als.group.repository.UserGroupRepository;
import main.als.page.PostPagingDto;
import main.als.user.converter.UserConverter;
import main.als.user.dto.UserDto;
import main.als.user.entity.User;
import main.als.user.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class UserGroupServiceImpl implements UserGroupService {

    private final GroupRepository groupRepository;
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final UserGroupRepository userGroupRepository;

    public UserGroupServiceImpl(GroupRepository groupRepository, UserRepository userRepository,
                                UserGroupRepository userGroupRepository,BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.groupRepository = groupRepository;
        this.userRepository = userRepository;
        this.userGroupRepository = userGroupRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    @Override
    public void joinUserGroup(Long groupId, String password, String username) {

        Group group = groupRepository.findById(groupId)
                .orElseThrow(()->new GeneralException(ErrorStatus._NOT_FOUND_GROUP));
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new GeneralException(ErrorStatus._USERNAME_NOT_FOUND);
        }

        if (!bCryptPasswordEncoder.matches(password, group.getPassword())) {
            throw new GeneralException(ErrorStatus._NOT_MATCH_GROUPPASSWORD);
        }

        // Deadline 확인
        if (group.getDeadline() != null && group.getDeadline().isBefore(LocalDateTime.now())) {
            throw new GeneralException(ErrorStatus._DEADLINE_EXCEEDED); // Deadline 초과 예외 처리
        }

        // 사용자 그룹에 이미 존재하는지 확인
        boolean userInGroup = user.getUserGroups().stream()
                .anyMatch(userGroup -> userGroup.getGroup().getId().equals(groupId));
        if (userInGroup) {
            throw new GeneralException(ErrorStatus._USER_ALREADY_IN_GROUP); // 이미 그룹에 존재하는 경우
        }


        UserGroup userGroup = UserGroup.builder()
                .user(user)
                .group(group)
                .userDepositAmount(BigDecimal.ZERO)
                .refunded(false)
                .charged(false)
                .paymentKey(null)
                .build();

        user.getUserGroups().add(userGroup);
        group.getUserGroups().add(userGroup);
        userGroupRepository.save(userGroup);

    }

    @Override
    public UserDto.SearchUsers getUsersByGroupId(Long groupId, PostPagingDto.PagingDto pagingDto) {

        Sort sort = Sort.by(Sort.Direction.fromString(pagingDto.getSort()),"userDepositAmount");


        Pageable pageable = PageRequest.of(pagingDto.getPage(), pagingDto.getSize(), sort);
        Page<UserGroup> userGroupsPage = userGroupRepository.findByGroupId(groupId, pageable);

        if (userGroupsPage == null || userGroupsPage.isEmpty()) {
            throw new GeneralException(ErrorStatus._NOT_FOUND_GROUP); // 그룹이 존재하지 않을 때 예외 발생
        }


        return UserConverter.toUserDtos(userGroupsPage);
    }

    @Override
    public void resignGroup(Long groupId, String username) {
        UserGroup userGroup = userGroupRepository.findByGroupIdAndUserUsername(groupId,username)
                .orElseThrow(()->new GeneralException(ErrorStatus._NOT_FOUND_USERGROUP));

        //리더인 경우 그룹 탈퇴 x
//        String leader = userGroup.getGroup().getLeader();
//        if (leader.equals(username)) {
//            throw new GeneralException(ErrorStatus._LEADER_NOT_RESIGN);
//        }
        userGroupRepository.delete(userGroup);

    }

    @Transactional
    public void checkCharged(){
        List<UserGroup> userGroups = userGroupRepository.findByChargedFalse();
        LocalDateTime now = LocalDateTime.now();
        for (UserGroup userGroup : userGroups) {
            // 모집 마감일 확인
            if (userGroup.getGroup().getDeadline().isBefore(now)) {
                // 모집 기간이 지나면 그룹에서 탈퇴 처리
                userGroupRepository.delete(userGroup); // 그룹에서 탈퇴
                log.info("User {} has been removed from the group due to expired recruitment period.", userGroup.getUser().getUsername());
            }
        }
    }
}

