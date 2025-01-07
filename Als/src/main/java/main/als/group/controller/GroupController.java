package main.als.group.controller;

import jakarta.validation.Valid;
import main.als.apiPayload.ApiResult;
import main.als.apiPayload.code.status.ErrorStatus;
import main.als.group.dto.GroupRequestDto;
import main.als.group.dto.GroupResponseDto;
import main.als.group.entity.Group;
import main.als.group.service.GroupService;
import main.als.user.dto.CustomUserDetails;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/groups")
public class GroupController {

    private final GroupService groupService;

    public GroupController(GroupService groupService) {
        this.groupService = groupService;
    }

    @GetMapping
    public ApiResult<List<GroupResponseDto.AllGroupDto>> getAll(){
        return ApiResult.onSuccess(groupService.getAllGroups());
    }

    @PostMapping
    public ApiResult<?> create(@Valid @RequestBody GroupRequestDto.CreateGroupDto groupRequestDto, @AuthenticationPrincipal CustomUserDetails userDetails) {
        String username = userDetails.getUsername();
        Group group = groupService.createGroup(groupRequestDto,username);
        return ApiResult.onSuccess();
    }

    @PostMapping("/valid")
    public ApiResult<?> validatePassword(@Valid @RequestBody GroupRequestDto.ValidPasswordDto validPasswordDto) {
        return ApiResult.onSuccess(groupService.validateGroupPassword(validPasswordDto));
    }

    @DeleteMapping("/{groupId}")
    public ApiResult<?> deleteGroup(@PathVariable Long groupId,
                                    @RequestParam String password,
                                    @AuthenticationPrincipal CustomUserDetails userDetails) {
        String username = userDetails.getUsername();
        groupService.deleteGroup(groupId,username,password);
        return ApiResult.onSuccess();
    }

}
