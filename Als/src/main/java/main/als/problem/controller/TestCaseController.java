package main.als.problem.controller;

import io.swagger.v3.core.jackson.ApiResponsesSerializer;
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

    @GetMapping("/{problemId}/{testcaseId}")
    public ApiResult<TestCaseResponseDto.TestCaseDto> getTestCase(@PathVariable Long problemId, @PathVariable Long testcaseId) {

        return ApiResult.onSuccess(testCaseService.getTestCaseById(testcaseId));
    }


    @PostMapping("/{problemId}")
    public ApiResult<?> createTestCase(@RequestBody @Valid TestCaseRequestDto.TestCaseDto testCaseDto,
                                       @PathVariable Long problemId) {
        testCaseService.createTestCase(testCaseDto,problemId);
        return ApiResult.onSuccess();
    }

    @PutMapping("/{problemId}/{testcaseId}")
    public ApiResult<?> updateTestCase(@RequestBody @Valid TestCaseRequestDto.TestCaseDto testCaseDto,
                                       @PathVariable Long problemId, @PathVariable Long testcaseId) {

        testCaseService.updateTestCase(testCaseDto,testcaseId);
        return ApiResult.onSuccess();
    }

    @DeleteMapping("/{problemId}/{testcaseId}")
    public ApiResult<?> deleteTestCase(@PathVariable Long testcaseId,@PathVariable Long problemId) {
        testCaseService.deleteTestCase(testcaseId);
        return ApiResult.onSuccess();
    }






}
