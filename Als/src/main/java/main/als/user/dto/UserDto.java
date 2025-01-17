package main.als.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import main.als.group.dto.UserGroupResponseDto;

import java.math.BigDecimal;
import java.util.List;

public class UserDto {

    @Getter
    @Builder
    @AllArgsConstructor
    public static class UsernameDto{

        private String username;
        private BigDecimal depositAmount;

    }

    @Builder
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SearchUsers {
        List<UsernameDto> usernameDtos;
        boolean isFirst;
        boolean isLast;
        int listSize;
        long totalElements;
    }
}
