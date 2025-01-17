package main.als.problem.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import main.als.problem.entity.Submission;
import main.als.problem.entity.SubmissionStatus;

import java.time.LocalDateTime;
import java.util.List;

public class SubmissionResponseDto {

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SubmissionDto{

        private Long id;

        private Long groupProblemId;

        private String title;

        private String username;

        private String language;

        private String code;

        private SubmissionStatus status;

        private LocalDateTime submissionTime;

    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AllSubmissionDto{

        private Long id;

        private Long groupProblemId;

        private String title;

        private String username;

        private String language;

        private SubmissionStatus status;

        private LocalDateTime submissionTime;

    }

    @Builder
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SearchSubmissionDto {
        List<SubmissionResponseDto.AllSubmissionDto> submissionResDtos;
        boolean isFirst;
        boolean isLast;
        int listSize;
        long totalElements;
    }



    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OtherSubmissionDto{

        private Long id;

        private Long groupProblemId;

        private String title;

        private String username;

        private String language;

        private String code;

        private LocalDateTime submissionTime;

    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OtherAllSubmissionDto{

        private Long id;

        private Long groupProblemId;

        private String title;

        private String username;

        private String language;

        private LocalDateTime submissionTime;

    }

    @Builder
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SearchOtherSubmissionDto {
        List<SubmissionResponseDto.OtherAllSubmissionDto> otherSubmissionResDtos;
        boolean isFirst;
        boolean isLast;
        int listSize;
        long totalElements;
    }

}
