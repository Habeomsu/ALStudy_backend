package main.als.problem.converter;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import main.als.problem.dto.SubmissionRequestDto;
import main.als.problem.dto.SubmissionResponseDto;
import main.als.problem.entity.Submission;

import java.util.List;
import java.util.stream.Collectors;

public class SubmissionConverter {


    public static SubmissionResponseDto.AllSubmissionDto toAllSubmission(Submission submission) {
        return SubmissionResponseDto.AllSubmissionDto.builder()
                .id(submission.getId())
                .groupProblemId(submission.getGroupProblem().getId())
                .title(submission.getGroupProblem().getProblem().getTitle())
                .username(submission.getUser().getUsername())
                .language(submission.getLanguage())
                .status(submission.getStatus())
                .submissionTime(submission.getSubmissionTime())
                .build();
    }

    public static List<SubmissionResponseDto.AllSubmissionDto> toAllSubmission(List<Submission> submissions) {
        return submissions.stream()
                .map(SubmissionConverter::toAllSubmission)
                .collect(Collectors.toList());
    }

    public static SubmissionResponseDto.SubmissionDto toSubmission(Submission submission) {
        return SubmissionResponseDto.SubmissionDto.builder()
                .id(submission.getId())
                .groupProblemId(submission.getGroupProblem().getId())
                .title(submission.getGroupProblem().getProblem().getTitle())
                .username(submission.getUser().getUsername())
                .language(submission.getLanguage())
                .code(submission.getCode())
                .status(submission.getStatus())
                .submissionTime(submission.getSubmissionTime())
                .build();
    }


}
