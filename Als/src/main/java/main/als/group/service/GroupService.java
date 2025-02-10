package main.als.group.service;

import main.als.group.dto.GroupRequestDto;
import main.als.group.dto.GroupResponseDto;
import main.als.group.entity.Group;
import main.als.page.PostPagingDto;

import java.awt.print.Pageable;
import java.util.List;

public interface GroupService {

    public Group createGroup(GroupRequestDto.CreateGroupDto GroupRequestDto,String username);
    public GroupResponseDto.SearchGroups getAllGroups(PostPagingDto.PagingDto pagingDto,String search);
    public void deleteExpiredGroups();
    public boolean validateGroupPassword(GroupRequestDto.ValidPasswordDto validPasswordDto);
    public void deleteGroup(Long id,String username,String password);
    public GroupResponseDto.AllGroupDto getGroup(Long GorupId);
    public void createGroupWithPayment(GroupRequestDto.CreateWithPaymentDto createWithPaymentDto,String username);

}
