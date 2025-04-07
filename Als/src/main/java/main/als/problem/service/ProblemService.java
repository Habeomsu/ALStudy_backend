package main.als.problemTest.service;


import main.als.page.PostPagingDto;
import main.als.problemTest.dto.ProblemRequestDto;
import main.als.problemTest.dto.ProblemResponseDto;

public interface ProblemService {
    void createProblem(ProblemRequestDto.createProblemDto requestDto);
    ProblemResponseDto.SearchProblems getAllProblems(PostPagingDto.PagingDto pagingDto,String problemType,String search);
    ProblemResponseDto.ProblemDto getProblemById(Long id);
    void deleteProblem(Long id);
    void updateProblem(ProblemRequestDto.createProblemDto requestDto,Long problemId);
}
