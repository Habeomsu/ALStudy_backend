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
import main.als.user.entity.User;
import main.als.user.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

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

    public GroupServiceImpl(GroupRepository groupRepository, UserRepository userRepository, UserGroupRepository userGroupRepository, BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.groupRepository = groupRepository;
        this.userRepository = userRepository;
        this.userGroupRepository = userGroupRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
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
                .userDepositAmount(savedGroup.getDepositAmount())
                .build();

        userGroupRepository.save(userGroup);
        leader.getUserGroups().add(userGroup);
        savedGroup.getUserGroups().add(userGroup);

        return savedGroup;
    }

    @Override
    public GroupResponseDto.SearchGroups getAllGroups(PostPagingDto.PagingDto pagingDto) {
        Sort sort = Sort.by(Sort.Direction.fromString(pagingDto.getSort()), "id");
        Pageable pageable = PageRequest.of(pagingDto.getPage(), pagingDto.getSize(), sort);
        Page<Group> groupPages = groupRepository.findAll(pageable);
        return GroupConverter.toSearchGroupDto(groupPages);
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

}
