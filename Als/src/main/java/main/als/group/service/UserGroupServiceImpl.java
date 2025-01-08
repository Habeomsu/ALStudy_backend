package main.als.group.service;

import main.als.apiPayload.code.status.ErrorStatus;
import main.als.apiPayload.exception.GeneralException;
import main.als.group.entity.Group;
import main.als.group.entity.UserGroup;
import main.als.group.repository.GroupRepository;
import main.als.group.repository.UserGroupRepository;
import main.als.user.entity.User;
import main.als.user.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
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
                .userDepositAmount(group.getDepositAmount())
                .build();

        user.getUserGroups().add(userGroup);
        group.getUserGroups().add(userGroup);
        userGroupRepository.save(userGroup);

    }

    @Override
    public List<User> getUsersByGroupId(Long groupId) {

        List<UserGroup> userGroups = userGroupRepository.findByGroupId(groupId);

        if (userGroups == null || userGroups.isEmpty()) {
            throw new GeneralException(ErrorStatus._NOT_FOUND_GROUP); // 그룹이 존재하지 않을 때 예외 발생
        }

        return userGroups.stream()
                .map(UserGroup::getUser)
                .collect(Collectors.toList());
    }
}
