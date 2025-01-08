package main.als.group.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import main.als.group.entity.UserGroup;

import java.math.BigDecimal;
import java.util.List;

public class UserGroupResponseDto {

    @Getter
    @Builder
    @AllArgsConstructor
    public static class UserGroupsDto{
        private Long id;
        private String username;
        private Long groupId;
        private String groupName;
        private BigDecimal userDepositAmount;

    }
}
