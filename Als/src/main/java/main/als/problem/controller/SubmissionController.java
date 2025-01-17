package main.als.problem.controller;


import jakarta.validation.Valid;
import jakarta.ws.rs.Path;
import main.als.apiPayload.ApiResult;
import main.als.page.PagingConverter;
import main.als.problem.dto.SubmissionRequestDto;

import main.als.problem.dto.SubmissionResponseDto;
import main.als.problem.service.SubmissionService;
import main.als.user.dto.CustomUserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/submission")
public class SubmissionController {

    private final SubmissionService submissionService;

    public SubmissionController(SubmissionService submissionService) {
        this.submissionService = submissionService;
    }

    @GetMapping("/{groupProblemId}")
    public ApiResult<SubmissionResponseDto.SearchSubmissionDto> getAllSubmission(@PathVariable Long groupProblemId,
                                                                                    @AuthenticationPrincipal CustomUserDetails UserDetails,
                                                                                    @RequestParam(defaultValue = "0") int page,
                                                                                    @RequestParam(defaultValue = "10") int size,
                                                                                    @RequestParam(defaultValue = "desc") String sort) {

        String username = UserDetails.getUsername();

        return ApiResult.onSuccess(submissionService.getAll(groupProblemId,username, PagingConverter.toPagingDto(page,size,sort)));
    }

    @GetMapping("/{groupProblemId}/{submissionId}")
    public ApiResult<SubmissionResponseDto.SubmissionDto> getDetailSubmission(@PathVariable Long groupProblemId,
                                            @PathVariable Long submissionId,
                                            @AuthenticationPrincipal CustomUserDetails UserDetails) {
        String username = UserDetails.getUsername();

        return ApiResult.onSuccess(submissionService.getSubmission(groupProblemId,submissionId,username));
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

    @GetMapping("/others/{groupProblemId}")
    public ApiResult<SubmissionResponseDto.SearchOtherSubmissionDto> getOtherAllSubmission(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                                                              @PathVariable Long groupProblemId,
                                                                                              @RequestParam(defaultValue = "0") int page,
                                                                                              @RequestParam(defaultValue = "10") int size,
                                                                                              @RequestParam(defaultValue = "desc") String sort) {

        String username = userDetails.getUsername();
        return ApiResult.onSuccess(submissionService.getOtherAll(groupProblemId,username,PagingConverter.toPagingDto(page,size,sort)));

    }

    @GetMapping("/others/{groupProblemId}/{submissionId}")
    public ApiResult<SubmissionResponseDto.OtherSubmissionDto> getOtherSubmission(@AuthenticationPrincipal CustomUserDetails userDetails,
                                           @PathVariable Long groupProblemId,
                                           @PathVariable Long submissionId){
        String username = userDetails.getUsername();
        return ApiResult.onSuccess(submissionService.getOtherSubmission(groupProblemId,submissionId,username));
    }


}
