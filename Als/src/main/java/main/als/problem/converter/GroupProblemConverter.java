package main.als.problem.converter;

import main.als.problem.dto.GroupProblemRequestDto;
import main.als.problem.dto.GroupProblemResponseDto;
import main.als.problem.entity.GroupProblem;
import main.als.problem.entity.SubmissionStatus;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class GroupProblemConverter {

    public static GroupProblemResponseDto.AllGroupProblem toGroupProblemDto (GroupProblem groupProblem,SubmissionStatus status) {
        return GroupProblemResponseDto.AllGroupProblem.builder()
                .groupProblemId(groupProblem.getId())
                .title(groupProblem.getProblem().getTitle())
                .difficultyLevel(groupProblem.getProblem().getDifficultyLevel())
                .createdAt(groupProblem.getCreatedAt())
                .deadline(groupProblem.getDeadline())
                .deductionAmount(groupProblem.getDeductionAmount())
                .status(status)
                .build();

    }

    public static List<GroupProblemResponseDto.AllGroupProblem> toGroupProblemDto (List<GroupProblem> groupProblems, Map<Long, SubmissionStatus> submissionStatusMap) {
        return groupProblems.stream()
                .map(groupProblem -> {
                    // 해당 그룹 문제 ID에 대한 제출 상태를 가져옵니다.
                    SubmissionStatus status = submissionStatusMap.getOrDefault(groupProblem.getId(), SubmissionStatus.PENDING);
                    return toGroupProblemDto(groupProblem, status);
                })
                .collect(Collectors.toList());
    }

    public static GroupProblemResponseDto.SearchGroupProblem toSearchGroupProblemDto (Page<GroupProblem> groupProblems,Map<Long, SubmissionStatus> submissionStatusMap) {
        return GroupProblemResponseDto.SearchGroupProblem.builder()
                .groupProblemResDtos(toGroupProblemDto(groupProblems.getContent(), submissionStatusMap))
                .isFirst(groupProblems.isFirst())
                .isLast(groupProblems.isLast())
                .listSize(groupProblems.getTotalPages())
                .totalElements(groupProblems.getTotalElements())
                .build();
    }


    public static GroupProblemResponseDto.DetailGroupProblem toDetailGroupProblem (GroupProblem groupProblem, SubmissionStatus status) {
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
                .status(status)
                .deadline(groupProblem.getDeadline())
                .build();
    }

}
