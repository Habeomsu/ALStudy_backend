package main.als.problem.service;


import main.als.problem.dto.ProblemRequestDto;
import main.als.problem.dto.ProblemResponseDto;
import main.als.problem.entity.Problem;

import java.util.List;

public interface ProblemService {
    void createProblem(ProblemRequestDto.createProblemDto requestDto);
    List<ProblemResponseDto.AllProblemDto> getAllProblems();
    ProblemResponseDto.ProblemDto getProblemById(Long id);
}
