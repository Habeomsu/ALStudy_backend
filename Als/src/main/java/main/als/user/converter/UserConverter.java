package main.als.user.converter;

import main.als.group.entity.UserGroup;
import main.als.user.dto.UserDto;
import main.als.user.entity.User;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

public class UserConverter {

    public static UserDto.UsernameDto toUsernameDto(UserGroup userGroup) {
        return UserDto.UsernameDto.builder()
                .username(userGroup.getUser().getUsername())
                .depositAmount(userGroup.getUserDepositAmount())
                .build();

    }

    public static List<UserDto.UsernameDto> toUsernameDto(List<UserGroup> userGroups) {
        return userGroups.stream()
                .map(UserConverter::toUsernameDto)
                .collect(Collectors.toList());
    }
}
