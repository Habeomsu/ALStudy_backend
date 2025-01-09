package main.als.problem.service;


import jakarta.transaction.Transactional;
import main.als.apiPayload.code.status.ErrorStatus;
import main.als.apiPayload.exception.GeneralException;
import main.als.problem.converter.ProblemConverter;
import main.als.problem.dto.ProblemRequestDto;
import main.als.problem.dto.ProblemResponseDto;
import main.als.problem.entity.Problem;
import main.als.problem.entity.TestCase;
import main.als.problem.repository.ProblemRepository;
import main.als.problem.repository.TestCaseRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProblemServiceImpl implements ProblemService {
    private final ProblemRepository problemRepository;
    private final TestCaseRepository testCaseRepository;

    public ProblemServiceImpl(ProblemRepository problemRepository, TestCaseRepository testCaseRepository) {
        this.problemRepository = problemRepository;
        this.testCaseRepository = testCaseRepository;
    }

    @Override
    @Transactional
    public void createProblem(ProblemRequestDto.createProblemDto requestDto) {
        try {
            Problem problem = ProblemConverter.toProblem(requestDto);
            TestCase testCase =TestCase.builder()
                    .problem(problem)
                    .input(requestDto.getExampleInput())
                    .expectedOutput(requestDto.getExampleOutput())
                    .build();

            problem.getTestCases().add(testCase);
            problemRepository.save(problem);
        } catch (Exception e) {
            throw new GeneralException(ErrorStatus._NOT_CREATED_PROBLEM);
        }
    }

    @Override
    public List<ProblemResponseDto.AllProblemDto> getAllProblems() {

        List<Problem> problems = problemRepository.findAll();

        return ProblemConverter.toAllProblemDto(problems);
    }

    @Override
    public ProblemResponseDto.ProblemDto getProblemById(Long id) {
        Problem problem = problemRepository.findById(id)
                .orElseThrow(()->new GeneralException(ErrorStatus._NOT_FOUND_PROBLEM));
        return ProblemConverter.toProblemDto(problem);
    }

    @Override
    public void deleteProblem(Long id) {
        Problem problem = problemRepository.findById(id)
                .orElseThrow(()->new GeneralException(ErrorStatus._NOT_FOUND_PROBLEM));
        problemRepository.deleteById(id);
    }


}
