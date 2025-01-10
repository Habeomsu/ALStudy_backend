package main.als.problem.service;

import jakarta.transaction.Transactional;
import main.als.apiPayload.code.status.ErrorStatus;
import main.als.apiPayload.exception.GeneralException;
import main.als.group.entity.Group;
import main.als.group.repository.GroupRepository;
import main.als.group.repository.UserGroupRepository;
import main.als.problem.converter.GroupProblemConverter;
import main.als.problem.dto.GroupProblemRequestDto;
import main.als.problem.dto.GroupProblemResponseDto;
import main.als.problem.entity.GroupProblem;
import main.als.problem.entity.Problem;
import main.als.problem.repository.GroupProblemRepository;
import main.als.problem.repository.ProblemRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class GroupProblemServiceImpl implements GroupProblemService {

    private final GroupProblemRepository groupProblemRepository;
    private final ProblemRepository problemRepository;
    private final GroupRepository groupRepository;
    private final UserGroupRepository userGroupRepository;

    public GroupProblemServiceImpl(GroupProblemRepository groupProblemRepository,ProblemRepository problemRepository,
                                   GroupRepository groupRepository,UserGroupRepository userGroupRepository) {
        this.groupProblemRepository = groupProblemRepository;
        this.problemRepository = problemRepository;
        this.groupRepository = groupRepository;
        this.userGroupRepository = userGroupRepository;
    }

    @Override
    @Transactional
    public void createGroupProblem(GroupProblemRequestDto.GroupProblemDto groupProblemDto,String username) {

        Problem problem = problemRepository.findById(groupProblemDto.getProblem_id())
                .orElseThrow(()-> new GeneralException(ErrorStatus._NOT_FOUND_PROBLEM));

        Group group = groupRepository.findById(groupProblemDto.getGroup_id())
                .orElseThrow(()-> new GeneralException(ErrorStatus._NOT_FOUND_GROUP));

        String leader = group.getLeader();

        if (!username.equals(leader)) {
            throw new GeneralException(ErrorStatus._NOT_MATCH_LEADER);
        }

        if(group.getDeadline().isAfter(LocalDateTime.now())) {
            throw new GeneralException(ErrorStatus._DEADLINE_NOT_PASSED);
        }

        GroupProblem groupProblem = GroupProblem.builder()
                .problem(problem)
                .group(group)
                .createdAt(LocalDateTime.now())
                .deadline(groupProblemDto.getDeadline())
                .deductionAmount(groupProblemDto.getDeductionAmount())
                .build();

        group.getGroupProblems().add(groupProblem);

        groupProblemRepository.save(groupProblem);

    }

    @Override
    public List<GroupProblemResponseDto.AllGroupProblem> getGroupProblems(Long groupId) {

        if (!groupRepository.existsById(groupId)) {
            throw new GeneralException(ErrorStatus._NOT_FOUND_GROUP); // 적절한 예외 메시지
        }

        List<GroupProblem> groupProblems = groupProblemRepository.findByGroupId(groupId);
        return GroupProblemConverter.toGroupProblemDto(groupProblems);
    }

    @Override
    public GroupProblemResponseDto.DetailGroupProblem getDetailGroupProblem(Long groupProblemId,String username) {
        GroupProblem groupProblem = groupProblemRepository.findById(groupProblemId)
                .orElseThrow(()->new GeneralException(ErrorStatus._NOT_FOUND_GROUPPROBLEM));

        Group group = groupProblem.getGroup();

        boolean isMember = userGroupRepository.existsByGroupIdAndUserUsername(group.getId(), username);

        if (!isMember) {
            throw new GeneralException(ErrorStatus._NOT_IN_USERGROUP); // 권한이 없는 경우 예외 처리
        }

        return GroupProblemConverter.toDetailGroupProblem(groupProblem);
    }


}
