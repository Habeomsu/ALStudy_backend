package main.als.group.service;

import main.als.group.dto.GroupRequestDto;
import main.als.group.dto.GroupResponseDto;
import main.als.group.entity.Group;

import java.util.List;

public interface GroupService {

    public Group createGroup(GroupRequestDto.CreateGroupDto GroupRequestDto,String username);
    public List<GroupResponseDto.AllGroupDto> getAllGroups();
    public void deleteExpiredGroups();
    public boolean validateGroupPassword(GroupRequestDto.ValidPasswordDto validPasswordDto);
    public void deleteGroup(Long id,String username,String password);
}
