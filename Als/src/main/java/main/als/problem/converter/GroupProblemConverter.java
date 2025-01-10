package main.als.problem.converter;

import main.als.problem.dto.GroupProblemRequestDto;
import main.als.problem.dto.GroupProblemResponseDto;
import main.als.problem.entity.GroupProblem;

import java.util.List;
import java.util.stream.Collectors;

public class GroupProblemConverter {

    public static GroupProblemResponseDto.AllGroupProblem toGroupProblemDto (GroupProblem groupProblem) {
        return GroupProblemResponseDto.AllGroupProblem.builder()
                .groupProblemId(groupProblem.getId())
                .title(groupProblem.getProblem().getTitle())
                .difficultyLevel(groupProblem.getProblem().getDifficultyLevel())
                .createdAt(groupProblem.getProblem().getCreatedAt())
                .deadline(groupProblem.getDeadline())
                .deductionAmount(groupProblem.getDeductionAmount())
                .build();

    }

    public static List<GroupProblemResponseDto.AllGroupProblem> toGroupProblemDto (List<GroupProblem> groupProblems) {
        return groupProblems
                .stream()
                .map(GroupProblemConverter::toGroupProblemDto)
                .collect(Collectors.toList());
    }

    public static GroupProblemResponseDto.DetailGroupProblem toDetailGroupProblem (GroupProblem groupProblem) {
        return GroupProblemResponseDto.DetailGroupProblem.builder()
                .groupProblemId(groupProblem.getId())
                .problemId(groupProblem.getProblem().getId())
                .title(groupProblem.getProblem().getTitle())
                .difficultyLevel(groupProblem.getProblem().getDifficultyLevel())
                .problemType(groupProblem.getProblem().getProblemType())
                .description(groupProblem.getProblem().getDescription())
                .inputDescription(groupProblem.getProblem().getInputDescription())
                .outputDescription(groupProblem.getProblem().getOutputDescription())
                .exampleInput(groupProblem.getProblem().getExampleInput())
                .exampleOutput(groupProblem.getProblem().getExampleOutput())
                .deductionAmount(groupProblem.getDeductionAmount())
                .build();
    }

}
