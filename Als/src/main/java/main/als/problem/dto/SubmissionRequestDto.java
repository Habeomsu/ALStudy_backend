package main.als.problem.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class SubmissionRequestDto {


    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SubmissionDto{


        @NotNull(message = "언어 설정은 필수입니다.")
        private String language;

        @NotNull(message = "코드 내용은 필수입니다")
        @Size(max = 1000000)
        private String code;


    }
}
