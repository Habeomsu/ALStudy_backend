package main.als.problem.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import main.als.problem.entity.ProblemType;

public class ProblemRequestDto {


    @Getter
    @Builder
    @AllArgsConstructor
    public static class createProblemDto{

        @NotBlank(message = "문제 제목은 필수입니다.")
        private String title;

        @NotBlank(message = "문제 설명은 필수입니다.")
        private String description;

        @NotBlank(message = "문제 난이도는 필수입니다.")
        private String difficultyLevel;

        @NotBlank(message = "입력 설명은 필수입니다.")
        private String inputDescription;

        @NotBlank(message = "출력 설명은 필수입니다.")
        private String outputDescription;


        @NotNull(message = "문제 유형은 필수입니다.") // 나중에 enum만 들어가게 valid추가
        private ProblemType problemType;

        @NotBlank(message = "예시 입력은 필수입니다.")
        private String exampleInput;

        @NotBlank(message = "예시 출력은 필수입니다.")
        private String exampleOutput;

    }
}
