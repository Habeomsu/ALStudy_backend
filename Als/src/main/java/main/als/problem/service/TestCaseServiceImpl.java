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
    public void createTestCase(TestCaseRequestDto.TestCaseDto testCaseDto,Long problemId) {
        Problem problem = problemRepository.findById(problemId)
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
    public TestCaseResponseDto.TestCaseDto getTestCaseById(Long testcaseId) {

        TestCase testcase = testCaseRepository.findById(testcaseId)
                .orElseThrow(()-> new GeneralException(ErrorStatus._NOT_FOUND_TESTCASE));

        return TestCaseConverter.toTestCase(testcase);

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
    public void updateTestCase(TestCaseRequestDto.TestCaseDto testCaseDto, Long testcaseId) {
        TestCase testCase = testCaseRepository.findById(testcaseId)
                .orElseThrow(()->new GeneralException(ErrorStatus._NOT_FOUND_TESTCASE));

        testCase.setInput(testCaseDto.getInput());
        testCase.setExpectedOutput(testCaseDto.getExpectedOutput());

        testCaseRepository.save(testCase);

    }


    @Override
    @Transactional
    public void deleteTestCase(Long testcaseId) {
        TestCase testCase = testCaseRepository.findById(testcaseId)
                .orElseThrow(()->new GeneralException(ErrorStatus._NOT_FOUND_TESTCASE));

        testCaseRepository.delete(testCase);

    }


}
