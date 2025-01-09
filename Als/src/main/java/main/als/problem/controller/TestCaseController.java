package main.als.problem.controller;

import jakarta.validation.Valid;
import main.als.apiPayload.ApiResult;
import main.als.problem.dto.TestCaseRequestDto;
import main.als.problem.service.TestCaseService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/testcase")
public class TestCaseController {

    private final TestCaseService testCaseService;

    public TestCaseController(TestCaseService testCaseService) {
        this.testCaseService = testCaseService;
    }

    @PostMapping
    public ApiResult<?> createTestCase(@RequestBody @Valid TestCaseRequestDto.TestCaseDto testCaseDto) {
        testCaseService.createTestCase(testCaseDto);
        return ApiResult.onSuccess();
    }

}
