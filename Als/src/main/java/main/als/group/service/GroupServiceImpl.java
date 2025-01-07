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
import main.als.user.entity.User;
import main.als.user.repository.UserRepository;
import org.springframework.cglib.core.Local;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class GroupServiceImpl implements GroupService {

    private final GroupRepository groupRepository;
    private final UserRepository userRepository;
    private final UserGroupRepository userGroupRepository;

    public GroupServiceImpl(GroupRepository groupRepository, UserRepository userRepository, UserGroupRepository userGroupRepository) {
        this.groupRepository = groupRepository;
        this.userRepository = userRepository;
        this.userGroupRepository = userGroupRepository;
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
                .password(groupRequestDto.getPassword())
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
                .userDepositAmount(savedGroup.getDepositAmount())
                .build();

        userGroupRepository.save(userGroup);
        leader.getUserGroups().add(userGroup);
        savedGroup.getUserGroups().add(userGroup);

        return savedGroup;
    }

    @Override
    public List<GroupResponseDto.AllGroupDto> getAllGroups() {
        List<Group> groups = groupRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));
        return GroupConverter.toAllGroupDto(groups);
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

}
