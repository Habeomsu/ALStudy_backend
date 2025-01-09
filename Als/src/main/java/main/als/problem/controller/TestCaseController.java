package main.als.problem.controller;

import jakarta.validation.Valid;
import main.als.apiPayload.ApiResult;
import main.als.problem.dto.TestCaseRequestDto;
import main.als.problem.dto.TestCaseResponseDto;
import main.als.problem.service.TestCaseService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/testcase")
public class TestCaseController {

    private final TestCaseService testCaseService;

    public TestCaseController(TestCaseService testCaseService) {
        this.testCaseService = testCaseService;
    }

    @GetMapping("/{problemId}")
    public ApiResult<List<TestCaseResponseDto.TestCaseDto>> getTestCases(@PathVariable Long problemId) {

        return ApiResult.onSuccess(testCaseService.getTestCasesByProblemId(problemId));
    }

    @PostMapping
    public ApiResult<?> createTestCase(@RequestBody @Valid TestCaseRequestDto.TestCaseDto testCaseDto) {
        testCaseService.createTestCase(testCaseDto);
        return ApiResult.onSuccess();
    }

    @DeleteMapping("/{testcaseId}")
    public ApiResult<?> deleteTestCase(@PathVariable Long testcaseId) {
        testCaseService.deleteTestCase(testcaseId);
        return ApiResult.onSuccess();
    }






}
