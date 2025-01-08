package main.als.user.converter;

import main.als.user.dto.UserDto;
import main.als.user.entity.User;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

public class UserConverter {

    public static UserDto.UsernameDto toUsernameDto(User user,BigDecimal deposit) {
        return UserDto.UsernameDto.builder()
                .username(user.getUsername())
                .depositAmount(deposit)
                .build();

    }

    public static List<UserDto.UsernameDto> toUsernameDto(List<User> users, List<BigDecimal> deposits) {
        return users.stream()
                .map(user -> toUsernameDto(user, deposits.get(users.indexOf(user))))
                .collect(Collectors.toList());
    }
}
