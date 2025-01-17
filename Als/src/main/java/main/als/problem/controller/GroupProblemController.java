package main.als.problem.controller;

import jakarta.validation.Valid;
import main.als.apiPayload.ApiResult;
import main.als.page.PagingConverter;
import main.als.problem.dto.GroupProblemRequestDto;
import main.als.problem.dto.GroupProblemResponseDto;
import main.als.problem.service.GroupProblemService;
import main.als.user.dto.CustomUserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
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
    public ApiResult<GroupProblemResponseDto.SearchGroupProblem> getGroupProblem(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                                                    @PathVariable Long groupId,
                                                                                    @RequestParam(defaultValue = "0") int page,
                                                                                    @RequestParam(defaultValue = "10") int size,
                                                                                    @RequestParam(defaultValue = "desc") String sort
                                                                                    ) {
        String username = userDetails.getUsername();
        return ApiResult.onSuccess(groupProblemService.getGroupProblems(groupId,username, PagingConverter.toPagingDto(page,size,sort)));
    }

    @GetMapping("/{groupId}/{groupProblemId}")
    public ApiResult<GroupProblemResponseDto.DetailGroupProblem> getDetailGroupProblem(@PathVariable Long groupId,
                                                                                       @PathVariable Long groupProblemId,
                                                                                       @AuthenticationPrincipal CustomUserDetails UserDetails) {
        String username =UserDetails.getUsername();
        return ApiResult.onSuccess(groupProblemService.getDetailGroupProblem(groupId,groupProblemId,username));
    }


    @PostMapping
    public ApiResult<?> createGroupProblem(@AuthenticationPrincipal CustomUserDetails customUserDetails, @RequestBody @Valid GroupProblemRequestDto.GroupProblemDto groupProblemDto) {

        String username = customUserDetails.getUsername();
        groupProblemService.createGroupProblem(groupProblemDto, username);
        return ApiResult.onSuccess();
    }


}
