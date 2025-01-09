package main.als.problem.service;

import jakarta.transaction.Transactional;
import main.als.apiPayload.code.status.ErrorStatus;
import main.als.apiPayload.exception.GeneralException;
import main.als.problem.dto.TestCaseRequestDto;
import main.als.problem.entity.Problem;
import main.als.problem.entity.TestCase;
import main.als.problem.repository.ProblemRepository;
import main.als.problem.repository.TestCaseRepository;
import org.springframework.stereotype.Service;

@Service
public class TestCaseServiceImpl implements TestCaseService {

    private final ProblemRepository problemRepository;
    private final TestCaseRepository testCaseRepository;

    public TestCaseServiceImpl(ProblemRepository problemRepository, TestCaseRepository testCaseRepository) {
        this.problemRepository = problemRepository;
        this.testCaseRepository = testCaseRepository;
    }

    @Override
    @Transactional
    public void createTestCase(TestCaseRequestDto.TestCaseDto testCaseDto) {
        Problem problem = problemRepository.findById(testCaseDto.getProblemId())
                .orElseThrow(()-> new GeneralException(ErrorStatus._NOT_FOUND_PROBLEM));
        TestCase testCase = TestCase.builder()
                .problem(problem)
                .input(testCaseDto.getInput())
                .expectedOutput(testCaseDto.getExpectedOutput())
                .build();

        problem.getTestCases().add(testCase);
        testCaseRepository.save(testCase);
    }
}
