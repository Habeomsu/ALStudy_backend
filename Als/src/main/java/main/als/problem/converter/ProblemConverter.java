package main.als.problem.converter;

import main.als.problem.dto.ProblemRequestDto;
import main.als.problem.entity.Problem;

import java.time.LocalDateTime;

public class ProblemConverter {

    public static Problem toProblem(ProblemRequestDto.createProblemDto requestDto){
        return Problem.builder()
                .title(requestDto.getTitle())
                .description(requestDto.getDescription())
                .difficultyLevel(requestDto.getDifficultyLevel())
                .inputDescription(requestDto.getInputDescription())
                .outputDescription(requestDto.getOutputDescription())
                .problemType(requestDto.getProblemType())
                .createdAt(LocalDateTime.now())
                .exampleInput(requestDto.getExampleInput())
                .exampleOutput(requestDto.getExampleOutput())
                .build();

    }
}
