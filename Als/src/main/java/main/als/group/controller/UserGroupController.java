package main.als.group.controller;

import main.als.apiPayload.ApiResult;
import main.als.group.converter.UserGroupConverter;
import main.als.group.dto.UserGroupRequestDto;
import main.als.group.dto.UserGroupResponseDto;
import main.als.group.entity.UserGroup;
import main.als.group.service.UserGroupService;
import main.als.page.PagingConverter;
import main.als.page.PostPagingDto;
import main.als.user.converter.UserConverter;
import main.als.user.dto.CustomUserDetails;
import main.als.user.dto.UserDto;
import main.als.user.entity.User;
import main.als.user.service.FindUserGroupService;
import org.springframework.data.domain.Page;
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
    public ApiResult<UserGroupResponseDto.SearchUserGroups> usergroups(@AuthenticationPrincipal CustomUserDetails UserDetails,
                                                                          @RequestParam(defaultValue = "0") int page,
                                                                          @RequestParam(defaultValue = "10") int size,
                                                                          @RequestParam(defaultValue = "asc") String sort) {
        String username = UserDetails.getUsername();
        Page<UserGroup> userGroups = findUserGroupService.userGroups(username, PagingConverter.toPagingDto(page,size,sort));
        return ApiResult.onSuccess(UserGroupConverter.toSearchUserGroups(userGroups));

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
    public ApiResult<UserDto.SearchUsers> getUsers(@PathVariable("groupId") Long groupId,
                                                         @RequestParam(defaultValue = "0") int page,
                                                         @RequestParam(defaultValue = "10") int size,
                                                         @RequestParam(defaultValue = "asc") String sort){
        userGroupService.getUsersByGroupId(groupId, PagingConverter.toPagingDto(page,size,sort));
        return ApiResult.onSuccess(userGroupService.getUsersByGroupId(groupId, PagingConverter.toPagingDto(page,size,sort)));

    }

    @DeleteMapping("/{groupId}")
    public ApiResult<?> resignGroup(@PathVariable("groupId") Long groupId,
                                    @AuthenticationPrincipal CustomUserDetails userDetails){

        String username = userDetails.getUsername();
        userGroupService.resignGroup(groupId,username);
        return ApiResult.onSuccess();

    }



}
