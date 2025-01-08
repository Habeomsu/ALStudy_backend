package main.als.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

public class UserDto {

    @Getter
    @Builder
    @AllArgsConstructor
    public static class UsernameDto{

        private String username;

    }
}
