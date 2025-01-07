package main.als.group.controller;

import main.als.apiPayload.ApiResult;
import main.als.group.dto.UserGroupRequestDto;
import main.als.group.entity.UserGroup;
import main.als.group.service.UserGroupService;
import main.als.user.dto.CustomUserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/usergroups")
public class UserGroupController {

    private final UserGroupService userGroupService;

    public UserGroupController(UserGroupService userGroupService) {
        this.userGroupService = userGroupService;
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
}
