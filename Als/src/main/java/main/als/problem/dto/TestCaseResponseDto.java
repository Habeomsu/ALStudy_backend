package main.als.problem.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class TestCaseResponseDto {

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TestCaseDto{

        private Long id;

        private Long problemId;

        private String input;

        private String expectedOutput;

    }

}
