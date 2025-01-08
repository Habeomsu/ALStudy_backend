package main.als.problem.controller;

import jakarta.validation.Valid;
import main.als.apiPayload.ApiResult;
import main.als.problem.dto.ProblemRequestDto;
import main.als.problem.service.ProblemService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/problems")
public class ProblemController {

    private final ProblemService problemService;

    public ProblemController(ProblemService problemService) {
        this.problemService = problemService;
    }

    @PostMapping
    public ApiResult<?> createProblem(@Valid @RequestBody ProblemRequestDto.createProblemDto createProblemDto) {
        problemService.createProblem(createProblemDto);
        return ApiResult.onSuccess();
    }

}
