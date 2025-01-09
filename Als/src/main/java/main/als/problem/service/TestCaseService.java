package main.als.problem.service;

import main.als.problem.dto.TestCaseRequestDto;

public interface TestCaseService {
    void createTestCase(TestCaseRequestDto.TestCaseDto testCaseDto);
}
