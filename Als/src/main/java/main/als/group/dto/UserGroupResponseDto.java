package main.als.group.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import main.als.group.entity.UserGroup;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class UserGroupResponseDto {

    @Getter
    @Builder
    @AllArgsConstructor
    public static class UserGroupsDto{
        private Long id;
        private String username;
        private Long groupId;
        private LocalDateTime studyEndTime;
        private String groupName;
        private BigDecimal userDepositAmount;
        private boolean refunded;
        private boolean charged;
        private BigDecimal groupDepositAmount;
    }

    @Builder
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SearchUserGroups {
        List<UserGroupsDto> userGroupsResDtos;
        boolean isFirst;
        boolean isLast;
        int listSize;
        long totalElements;
    }


}
