package main.als.group.controller;

import jakarta.validation.Valid;
import main.als.apiPayload.ApiResult;
import main.als.apiPayload.code.status.ErrorStatus;
import main.als.group.dto.GroupRequestDto;
import main.als.group.dto.GroupResponseDto;
import main.als.group.entity.Group;
import main.als.group.service.GroupService;
import main.als.page.PagingConverter;
import main.als.page.PostPagingDto;
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
    public ApiResult<GroupResponseDto.SearchGroups> getAll(@RequestParam(defaultValue = "0") int page,
                                                           @RequestParam(defaultValue = "10") int size,
                                                           @RequestParam(defaultValue = "desc") String sort,
                                                           @RequestParam(required = false) String search) {
        return ApiResult.onSuccess(groupService.getAllGroups(PagingConverter.toPagingDto(page, size, sort),search));
    }

    @GetMapping("/{groupId}")
    public ApiResult<GroupResponseDto.AllGroupDto> getGroup(@PathVariable Long groupId) {
        return ApiResult.onSuccess(groupService.getGroup(groupId));
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

    @PostMapping("/payment")
    public ApiResult<?> createWithPayment(@RequestBody GroupRequestDto.CreateWithPaymentDto createWithPaymentDto,
                                          @AuthenticationPrincipal CustomUserDetails userDetails) {
        String username = userDetails.getUsername();
        groupService.createGroupWithPayment(createWithPaymentDto,username);
        return ApiResult.onSuccess();
    }

}
