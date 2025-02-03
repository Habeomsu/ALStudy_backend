package main.als.problem.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
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

    // 오늘의 문제를 가지고 오는 컨트롤러
    @GetMapping("/{groupId}/todayProblem")
    public ApiResult<GroupProblemResponseDto.SearchGroupProblem> getTodayGroupProblem(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                                                 @PathVariable Long groupId,
                                                                                 @RequestParam(defaultValue = "0") int page,
                                                                                 @RequestParam(defaultValue = "10") int size,
                                                                                 @RequestParam(defaultValue = "desc") String sort
    ) {
        String username = userDetails.getUsername();
        return ApiResult.onSuccess(groupProblemService.getTodayGroupProblems(groupId,username, PagingConverter.toPagingDto(page,size,sort)));
    }

    @GetMapping("/{groupId}/{groupProblemId}")
    public ApiResult<GroupProblemResponseDto.DetailGroupProblem> getDetailGroupProblem(@PathVariable Long groupId,
                                                                                       @PathVariable Long groupProblemId,
                                                                                       @AuthenticationPrincipal CustomUserDetails UserDetails) {
        String username =UserDetails.getUsername();
        return ApiResult.onSuccess(groupProblemService.getDetailGroupProblem(groupId,groupProblemId,username));
    }

    @DeleteMapping("/{groupId}/{groupProblemId}")
    public ApiResult<?> DeleteGroupProblem(@PathVariable Long groupId,
                                              @PathVariable Long groupProblemId,
                                              @AuthenticationPrincipal CustomUserDetails UserDetails) {
        String username =UserDetails.getUsername();
        groupProblemService.deleteGroupProblem(groupId,groupProblemId,username);
        return ApiResult.onSuccess();
    }


    @PostMapping("/{groupId}")
    public ApiResult<?> createGroupProblem(@AuthenticationPrincipal CustomUserDetails customUserDetails,
                                           @RequestBody @Valid GroupProblemRequestDto.GroupProblemDto groupProblemDto,
                                           @PathVariable Long groupId) {

        String username = customUserDetails.getUsername();
        groupProblemService.createGroupProblem(groupProblemDto, username,groupId);
        return ApiResult.onSuccess();
    }

    @PutMapping("/{groupId}/{groupProblemId}")
    public ApiResult<?> UpdateGroupProblem(@PathVariable Long groupId,
                                           @PathVariable Long groupProblemId,
                                           @AuthenticationPrincipal CustomUserDetails UserDetails,
                                           @RequestBody @Valid GroupProblemRequestDto.UpdateGroupProblemDto updateGroupProblemDto){

        String username=UserDetails.getUsername();
        groupProblemService.updateGroupProblem(updateGroupProblemDto,groupId,groupProblemId,username);
        return ApiResult.onSuccess();
    }


}
