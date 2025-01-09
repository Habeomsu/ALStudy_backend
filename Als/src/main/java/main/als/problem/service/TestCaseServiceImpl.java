package main.als.problem.service;

import jakarta.transaction.Transactional;
import main.als.apiPayload.code.status.ErrorStatus;
import main.als.apiPayload.exception.GeneralException;
import main.als.problem.converter.TestCaseConverter;
import main.als.problem.dto.TestCaseRequestDto;
import main.als.problem.dto.TestCaseResponseDto;
import main.als.problem.entity.Problem;
import main.als.problem.entity.TestCase;
import main.als.problem.repository.ProblemRepository;
import main.als.problem.repository.TestCaseRepository;
import org.springframework.stereotype.Service;

import java.util.List;

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

    @Override
    public List<TestCaseResponseDto.TestCaseDto> getTestCasesByProblemId(Long problemId) {

        if (!problemRepository.existsById(problemId)) {
            throw new GeneralException(ErrorStatus._NOT_FOUND_PROBLEM); // 적절한 예외 메시지
        }

        List<TestCase> testCases = testCaseRepository.findByProblemId(problemId);
        return TestCaseConverter.toTestCase(testCases);
    }

    @Override
    @Transactional
    public void deleteTestCase(Long id) {
        TestCase testCase = testCaseRepository.findById(id)
                .orElseThrow(()->new GeneralException(ErrorStatus._NOT_FOUND_TESTCASE));

        testCaseRepository.delete(testCase);

    }
}
