package main.als.problem.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class GroupProblemResponseDto {

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class GroupProblem{

        private Long groupProblemId;
        private Long problemId;
        private Long groupId;

        private LocalDateTime createdAt;
        private LocalDateTime deadline;

        private BigDecimal deductionAmount;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AllGroupProblem{

        private Long groupProblemId;
        private String title;
        private String difficultyLevel;
        private LocalDateTime createdAt;
        private LocalDateTime deadline;
        private BigDecimal deductionAmount;
    }




}
