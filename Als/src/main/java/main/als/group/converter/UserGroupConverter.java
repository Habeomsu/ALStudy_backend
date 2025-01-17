package main.als.group.converter;

import main.als.group.dto.UserGroupRequestDto;
import main.als.group.dto.UserGroupResponseDto;
import main.als.group.entity.UserGroup;
import main.als.user.dto.UserDto;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.stream.Collectors;

public class UserGroupConverter {


    public static UserGroupResponseDto.UserGroupsDto toUserGroupDto(UserGroup userGroup) {
        return UserGroupResponseDto.UserGroupsDto.builder()
                .id(userGroup.getId())
                .username(userGroup.getUser().getUsername())
                .groupId(userGroup.getGroup().getId())
                .groupName(userGroup.getGroup().getName())
                .userDepositAmount(userGroup.getUserDepositAmount())
                .build();
    }

    public static List<UserGroupResponseDto.UserGroupsDto> toUserGroupsDto(List<UserGroup> userGroups) {
        return userGroups.stream()
                .map(UserGroupConverter::toUserGroupDto)
                .collect(Collectors.toList());
    }

    public static UserGroupResponseDto.SearchUserGroups toSearchUserGroups(Page<UserGroup> userGroups) {
        return UserGroupResponseDto.SearchUserGroups.builder()
                .userGroupsResDtos(toUserGroupsDto(userGroups.getContent()))
                .isFirst(userGroups.isFirst())
                .isLast(userGroups.isLast())
                .listSize(userGroups.getTotalPages())
                .totalElements(userGroups.getTotalElements())
                .build();
    }
}
