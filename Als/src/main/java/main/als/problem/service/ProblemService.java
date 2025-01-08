package main.als.problem.service;


import main.als.problem.dto.ProblemRequestDto;
import main.als.problem.entity.Problem;

public interface ProblemService {
    void createProblem(ProblemRequestDto.createProblemDto requestDto);
}
