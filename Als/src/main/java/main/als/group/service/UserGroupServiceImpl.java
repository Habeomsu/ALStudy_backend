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

        UserGroup userGroup = UserGroup.builder()
                .user(user)
                .group(group)
                .userDepositAmount(group.getDepositAmount())
                .build();

        user.getUserGroups().add(userGroup);
        group.getUserGroups().add(userGroup);
        userGroupRepository.save(userGroup);

    }
}
