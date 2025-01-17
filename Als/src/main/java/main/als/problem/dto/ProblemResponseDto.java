package main.als.problem.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import main.als.group.dto.GroupResponseDto;
import main.als.problem.entity.ProblemType;

import java.time.LocalDateTime;
import java.util.List;

public class ProblemResponseDto {

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ProblemDto{

        private Long id;
        private String title;
        private String difficultyLevel;
        private ProblemType problemType;
        private LocalDateTime createdAt;
        private String description;
        private String inputDescription;
        private String outputDescription;
        private String exampleInput;
        private String exampleOutput;

    }

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AllProblemDto{

        private Long id;
        private String title;
        private String difficultyLevel;
        private ProblemType problemType;
        private LocalDateTime createdAt;

    }

    @Builder
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SearchProblems {
        List<ProblemResponseDto.AllProblemDto> problemResDtos;
        boolean isFirst;
        boolean isLast;
        int listSize;
        long totalElements;
    }



}
