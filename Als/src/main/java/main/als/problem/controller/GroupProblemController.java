package main.als.problem.controller;

import jakarta.validation.Valid;
import main.als.apiPayload.ApiResult;
import main.als.problem.dto.GroupProblemRequestDto;
import main.als.problem.dto.GroupProblemResponseDto;
import main.als.problem.entity.GroupProblem;
import main.als.problem.service.GroupProblemService;
import main.als.user.dto.CustomUserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/groupproblem")
public class GroupProblemController {

    private final GroupProblemService groupProblemService;

    public GroupProblemController(GroupProblemService groupProblemService) {
        this.groupProblemService = groupProblemService;
    }

    @GetMapping("/{groupId}")
    public ApiResult<List<GroupProblemResponseDto.AllGroupProblem>> getGroupProblem(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                                                    @PathVariable Long groupId) {
        String username = userDetails.getUsername();
        return ApiResult.onSuccess(groupProblemService.getGroupProblems(groupId,username));
    }

    @GetMapping("/detail/{groupProblemId}")
    public ApiResult<GroupProblemResponseDto.DetailGroupProblem> getDetailGroupProblem(@PathVariable Long groupProblemId,
                                                                                       @AuthenticationPrincipal CustomUserDetails UserDetails) {
        String username =UserDetails.getUsername();
        return ApiResult.onSuccess(groupProblemService.getDetailGroupProblem(groupProblemId,username));
    }


    @PostMapping
    public ApiResult<?> createGroupProblem(@AuthenticationPrincipal CustomUserDetails customUserDetails, @RequestBody @Valid GroupProblemRequestDto.GroupProblemDto groupProblemDto) {

        String username = customUserDetails.getUsername();
        groupProblemService.createGroupProblem(groupProblemDto, username);
        return ApiResult.onSuccess();
    }


}
