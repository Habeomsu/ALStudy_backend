package main.als.group.service;

import main.als.group.dto.UserGroupResponseDto;
import main.als.group.entity.Group;
import main.als.page.PostPagingDto;
import main.als.user.dto.UserDto;
import main.als.user.entity.User;


import java.util.List;

public interface UserGroupService {
    public void joinUserGroup(Long groupId,String password,String username);
    public UserDto.SearchUsers getUsersByGroupId(Long groupId, PostPagingDto.PagingDto pagingDto);
    public void resignGroup(Long groupId,String username);
    void checkCharged();
}
