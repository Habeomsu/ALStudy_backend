package main.als.problem.controller;


import jakarta.validation.Valid;
import main.als.apiPayload.ApiResult;
import main.als.problem.dto.SubmissionRequestDto;

import main.als.problem.service.SubmissionService;
import main.als.user.dto.CustomUserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/submission")
public class SubmissionController {

    private final SubmissionService submissionService;

    public SubmissionController(SubmissionService submissionService) {
        this.submissionService = submissionService;
    }

    @PostMapping(value = "/{groupProblemId}",consumes = "multipart/form-data")
    public ApiResult<?> submit(@AuthenticationPrincipal CustomUserDetails UserDetails,
                               @RequestPart(value = "language" ) String language ,
                               @RequestPart(value = "file") MultipartFile file,
                               @PathVariable Long groupProblemId) {

        String username = UserDetails.getUsername();
        submissionService.submit(file,language, groupProblemId, username);
        return ApiResult.onSuccess();

    }

}
