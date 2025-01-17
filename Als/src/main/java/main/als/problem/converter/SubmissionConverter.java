package main.als.problem.converter;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import main.als.problem.dto.SubmissionRequestDto;
import main.als.problem.dto.SubmissionResponseDto;
import main.als.problem.entity.Submission;
import org.springframework.data.domain.Page;

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

    public static SubmissionResponseDto.SearchSubmissionDto toSearchSubmission(Page<Submission> submissions) {
        return SubmissionResponseDto.SearchSubmissionDto.builder()
                .submissionResDtos(toAllSubmission(submissions.getContent()))
                .isFirst(submissions.isFirst())
                .isLast(submissions.isLast())
                .listSize(submissions.getTotalPages())
                .totalElements(submissions.getTotalElements())
                .build();
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

    public static SubmissionResponseDto.OtherAllSubmissionDto toOtherAllSubmission(Submission submission) {
        return SubmissionResponseDto.OtherAllSubmissionDto.builder()
                .id(submission.getId())
                .groupProblemId(submission.getGroupProblem().getId())
                .title(submission.getGroupProblem().getProblem().getTitle())
                .username(submission.getUser().getUsername())
                .language(submission.getLanguage())
                .submissionTime(submission.getSubmissionTime())
                .build();
    }

    public static List<SubmissionResponseDto.OtherAllSubmissionDto> toOtherAllSubmission(List<Submission> submissions) {
        return submissions.stream()
                .map(SubmissionConverter::toOtherAllSubmission)
                .collect(Collectors.toList());
    }

    public static SubmissionResponseDto.SearchOtherSubmissionDto toSearchOtherSubmission(Page<Submission> otherSubmissions) {
        return SubmissionResponseDto.SearchOtherSubmissionDto.builder()
                .otherSubmissionResDtos(toOtherAllSubmission(otherSubmissions.getContent()))
                .isFirst(otherSubmissions.isFirst())
                .isLast(otherSubmissions.isLast())
                .listSize(otherSubmissions.getTotalPages())
                .totalElements(otherSubmissions.getTotalElements())
                .build();
    }

    public static SubmissionResponseDto.OtherSubmissionDto toOtherSubmission(Submission submission) {
        return SubmissionResponseDto.OtherSubmissionDto.builder()
                .id(submission.getId())
                .groupProblemId(submission.getGroupProblem().getId())
                .title(submission.getGroupProblem().getProblem().getTitle())
                .username(submission.getUser().getUsername())
                .language(submission.getLanguage())
                .code(submission.getCode())
                .submissionTime(submission.getSubmissionTime())
                .build();
    }


}
