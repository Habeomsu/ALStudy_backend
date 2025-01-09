package main.als.problem.service;

import jakarta.transaction.Transactional;
import main.als.apiPayload.code.status.ErrorStatus;
import main.als.apiPayload.exception.GeneralException;
import main.als.group.entity.Group;
import main.als.group.repository.GroupRepository;
import main.als.problem.dto.GroupProblemRequestDto;
import main.als.problem.entity.GroupProblem;
import main.als.problem.entity.Problem;
import main.als.problem.repository.GroupProblemRepository;
import main.als.problem.repository.ProblemRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class GroupProblemServiceImpl implements GroupProblemService {

    private final GroupProblemRepository groupProblemRepository;
    private final ProblemRepository problemRepository;
    private final GroupRepository groupRepository;

    public GroupProblemServiceImpl(GroupProblemRepository groupProblemRepository,ProblemRepository problemRepository,
                                   GroupRepository groupRepository) {
        this.groupProblemRepository = groupProblemRepository;
        this.problemRepository = problemRepository;
        this.groupRepository = groupRepository;
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
}
