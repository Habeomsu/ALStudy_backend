package main.als.problem.controller;

import jakarta.validation.Valid;
import main.als.apiPayload.ApiResult;
import main.als.page.PagingConverter;
import main.als.problem.dto.ProblemRequestDto;
import main.als.problem.dto.ProblemResponseDto;
import main.als.problem.service.ProblemService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/problems")
public class ProblemController {

    private final ProblemService problemService;

    public ProblemController(ProblemService problemService) {
        this.problemService = problemService;
    }

    @GetMapping
    public ApiResult<ProblemResponseDto.SearchProblems> allProblems(@RequestParam(defaultValue = "0") int page,
                                                                         @RequestParam(defaultValue = "10") int size,
                                                                         @RequestParam(defaultValue = "desc") String sort,
                                                                         @RequestParam(required = false) String problemType,
                                                                    @RequestParam(required = false) String search) {
        return ApiResult.onSuccess(problemService.getAllProblems(PagingConverter.toPagingDto(page, size, sort),problemType,search));
    }

    @GetMapping("/{problemId}")
    public ApiResult<ProblemResponseDto.ProblemDto> problemById(@PathVariable Long problemId){

        return ApiResult.onSuccess(problemService.getProblemById(problemId));
    }


    @PostMapping
    public ApiResult<?> createProblem(@Valid @RequestBody ProblemRequestDto.createProblemDto createProblemDto) {
        problemService.createProblem(createProblemDto);
        return ApiResult.onSuccess();
    }

    @PutMapping("/{problemId}")
    public ApiResult<?> updateProblem(@Valid @RequestBody ProblemRequestDto.createProblemDto createProblemDto
    , @PathVariable Long problemId) {
        problemService.updateProblem(createProblemDto,problemId);
        return ApiResult.onSuccess();
    }

    @DeleteMapping("/{problemId}")
    public ApiResult<Void> deleteProblem(@PathVariable Long problemId) {
        problemService.deleteProblem(problemId);
        return ApiResult.onSuccess();
    }

}
