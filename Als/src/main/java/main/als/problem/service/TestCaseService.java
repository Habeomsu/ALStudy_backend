package main.als.problemTest.service;

import main.als.problemTest.dto.TestCaseRequestDto;
import main.als.problemTest.dto.TestCaseResponseDto;

import java.util.List;

public interface TestCaseService {
    void createTestCase(TestCaseRequestDto.TestCaseDto testCaseDto,Long problemId);
    List<TestCaseResponseDto.TestCaseDto> getTestCasesByProblemId(Long problemId);
    void deleteTestCase(Long id);
    void updateTestCase(TestCaseRequestDto.TestCaseDto testCaseDto,Long testcaseId);
    TestCaseResponseDto.TestCaseDto getTestCaseById(Long testcaseId);
}
