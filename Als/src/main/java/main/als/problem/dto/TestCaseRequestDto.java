package main.als.problem.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class TestCaseRequestDto {


    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TestCaseDto{


        @NotBlank(message = "입력 값은 필수입니다.")
        private String input;

        @NotBlank(message = "출력 값은 필수입니다.")
        private String expectedOutput;

    }

}
