package main.als.group.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.cglib.core.Local;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class GroupResponseDto {

    @Builder
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AllGroupDto{
        Long id;
        String groupname;
        String username;
        BigDecimal depositAmount;
        LocalDateTime createdAt;
        LocalDateTime deadline;
        LocalDateTime stutyEndDate;
    }

    @Builder
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SearchGroups {
        List<AllGroupDto> groupResDtos;
        boolean isFirst;
        boolean isLast;
        int listSize;
        long totalElements;
    }



}
