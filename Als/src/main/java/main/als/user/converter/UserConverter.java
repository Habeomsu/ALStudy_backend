package main.als.user.converter;

import main.als.user.dto.UserDto;
import main.als.user.entity.User;

import java.util.List;
import java.util.stream.Collectors;

public class UserConverter {

    public static UserDto.UsernameDto toUsernameDto(User user) {
        return UserDto.UsernameDto.builder()
                .username(user.getUsername())
                .build();

    }

    public static List<UserDto.UsernameDto> toUsernameDto(List<User> users) {
        return users.stream()
                .map(UserConverter::toUsernameDto)
                .collect(Collectors.toList());
    }
}
