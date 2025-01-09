package main.als.problem.service;

import main.als.problem.dto.GroupProblemRequestDto;

public interface GroupProblemService {
    void createGroupProblem(GroupProblemRequestDto.GroupProblemDto groupProblemDto,String username);
}
