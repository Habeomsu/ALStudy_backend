package main.als.problem.controller;


import jakarta.validation.Valid;
import main.als.apiPayload.ApiResult;
import main.als.problem.dto.SubmissionRequestDto;

import main.als.problem.service.SubmissionService;
import main.als.user.dto.CustomUserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/submission")
public class SubmissionController {

    private final SubmissionService submissionService;

    public SubmissionController(SubmissionService submissionService) {
        this.submissionService = submissionService;
    }

    @PostMapping("/{groupProblemId}")
    public ApiResult<?> submit(@AuthenticationPrincipal CustomUserDetails UserDetails,
                               @RequestBody @Valid SubmissionRequestDto.SubmissionDto submissionDto,
                               @PathVariable Long groupProblemId) {

        String username = UserDetails.getUsername();
        submissionService.submit(submissionDto, groupProblemId, username);
        return ApiResult.onSuccess();

    }

}
