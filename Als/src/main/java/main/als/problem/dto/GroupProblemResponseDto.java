package main.als.problem.dto;

import lombok.*;
import main.als.problem.entity.ProblemType;
import main.als.problem.entity.SubmissionStatus;

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
        private SubmissionStatus status;
    }


    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DetailGroupProblem{
        private Long groupProblemId;
        private Long problemId;
        private String title;
        private String difficultyLevel;
        private ProblemType problemType;
        private String description;
        private String inputDescription;
        private String outputDescription;
        private String exampleInput;
        private String exampleOutput;
        private BigDecimal deductionAmount;
        private SubmissionStatus status;
    }


}
