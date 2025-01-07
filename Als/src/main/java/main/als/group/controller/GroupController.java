package main.als.group.controller;

import jakarta.validation.Valid;
import main.als.apiPayload.ApiResult;
import main.als.group.dto.GroupRequestDto;
import main.als.group.dto.GroupResponseDto;
import main.als.group.entity.Group;
import main.als.group.service.GroupService;
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
    public ApiResult<?> create(@Valid @RequestBody GroupRequestDto.CreateGroupDto groupRequestDto) {
        Group group = groupService.createGroup(groupRequestDto);
        return ApiResult.onSuccess();
    }


}
