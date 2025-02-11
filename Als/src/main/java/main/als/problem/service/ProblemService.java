package main.als.problem.service;


import main.als.page.PostPagingDto;
import main.als.problem.dto.ProblemRequestDto;
import main.als.problem.dto.ProblemResponseDto;
import main.als.problem.entity.Problem;

import java.util.List;

public interface ProblemService {
    void createProblem(ProblemRequestDto.createProblemDto requestDto);
    ProblemResponseDto.SearchProblems getAllProblems(PostPagingDto.PagingDto pagingDto,String problemType,String search);
    ProblemResponseDto.ProblemDto getProblemById(Long id);
    void deleteProblem(Long id);
    void updateProblem(ProblemRequestDto.createProblemDto requestDto,Long problemId);
}
