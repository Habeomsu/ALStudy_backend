package main.als.problem.converter;

import main.als.problem.dto.ProblemRequestDto;
import main.als.problem.dto.ProblemResponseDto;
import main.als.problem.entity.Problem;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

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

    public static ProblemResponseDto.ProblemDto toProblemDto(Problem problem){
        return ProblemResponseDto.ProblemDto.builder()
                .id(problem.getId())
                .title(problem.getTitle())
                .description(problem.getDescription())
                .difficultyLevel(problem.getDifficultyLevel())
                .inputDescription(problem.getInputDescription())
                .outputDescription(problem.getOutputDescription())
                .problemType(problem.getProblemType())
                .createdAt(problem.getCreatedAt())
                .exampleInput(problem.getExampleInput())
                .exampleOutput(problem.getExampleOutput())
                .build();
    }

    public static ProblemResponseDto.AllProblemDto toAllProblemDto(Problem problem){
        return ProblemResponseDto.AllProblemDto.builder()
                .id(problem.getId())
                .title(problem.getTitle())
                .difficultyLevel(problem.getDifficultyLevel())
                .problemType(problem.getProblemType())
                .createdAt(problem.getCreatedAt())
                .build();
    }

    public static List<ProblemResponseDto.AllProblemDto> toAllProblemDto(List<Problem> problems){
        return problems.stream()
                .map(ProblemConverter::toAllProblemDto)
                .collect(Collectors.toList());

    }

    public static ProblemResponseDto.SearchProblems toSearchProblemDto(Page<Problem> problems){
        return ProblemResponseDto.SearchProblems.builder()
                .problemResDtos(toAllProblemDto(problems.getContent()))
                .isFirst(problems.isFirst())
                .isLast(problems.isLast())
                .listSize(problems.getTotalPages())
                .totalElements(problems.getTotalElements())
                .build();
    }

}
