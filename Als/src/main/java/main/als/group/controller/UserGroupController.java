package main.als.group.controller;

import main.als.apiPayload.ApiResult;
import main.als.group.converter.UserGroupConverter;
import main.als.group.dto.UserGroupRequestDto;
import main.als.group.dto.UserGroupResponseDto;
import main.als.group.entity.UserGroup;
import main.als.group.service.UserGroupService;
import main.als.user.converter.UserConverter;
import main.als.user.dto.CustomUserDetails;
import main.als.user.dto.UserDto;
import main.als.user.entity.User;
import main.als.user.service.FindUserGroupService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/usergroups")
public class UserGroupController {

    private final UserGroupService userGroupService;
    private final FindUserGroupService findUserGroupService;

    public UserGroupController(UserGroupService userGroupService, FindUserGroupService findUserGroupService) {
        this.userGroupService = userGroupService;
        this.findUserGroupService = findUserGroupService;
    }

    @GetMapping
    public ApiResult<List<UserGroupResponseDto.UserGroupsDto>> usergroups(@AuthenticationPrincipal CustomUserDetails UserDetails) {
        String username = UserDetails.getUsername();
        List<UserGroup> userGroups = findUserGroupService.userGroups(username);
        return ApiResult.onSuccess(UserGroupConverter.toUserGroupsDto(userGroups));

    }


    @PostMapping("/{groupId}")
    public ApiResult<?> joinUserGroup(@PathVariable("groupId") Long groupId,
                                      @AuthenticationPrincipal CustomUserDetails userDetails,
                                      @RequestBody UserGroupRequestDto.joinGroupDto joinGroupDto) {

        String username = userDetails.getUsername();
        String password = joinGroupDto.getPassword();
        userGroupService.joinUserGroup(groupId,password,username);
        return ApiResult.onSuccess();
    }

    @GetMapping("/{groupId}/users")
    public ApiResult<List<UserDto.UsernameDto>> getUsers(@PathVariable("groupId") Long groupId){
        List<User> users = userGroupService.getUsersByGroupId(groupId);
        return ApiResult.onSuccess(UserConverter.toUsernameDto(users));

    }



}
