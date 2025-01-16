package main.als.problem.service;

import main.als.problem.dto.GroupProblemRequestDto;
import main.als.problem.dto.GroupProblemResponseDto;

import java.util.List;

public interface GroupProblemService {
    void createGroupProblem(GroupProblemRequestDto.GroupProblemDto groupProblemDto,String username);
    List<GroupProblemResponseDto.AllGroupProblem> getGroupProblems(Long groupId,String username);
    GroupProblemResponseDto.DetailGroupProblem getDetailGroupProblem(Long groupId,Long groupProblemId,String username);
    void checkDeadlines();
}
