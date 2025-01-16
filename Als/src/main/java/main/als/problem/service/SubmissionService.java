package main.als.problem.service;

import main.als.problem.dto.SubmissionRequestDto;
import main.als.problem.dto.SubmissionResponseDto;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface SubmissionService {

    void submit(MultipartFile file,String language, Long groupProblemId, String username);

    List<SubmissionResponseDto.AllSubmissionDto> getAll(Long groupProblemId, String username);

    SubmissionResponseDto.SubmissionDto getSubmission(Long groupProblemId,Long submissionId,String username);

    List<SubmissionResponseDto.OtherAllSubmissionDto> getOtherAll(Long groupProblemId,String username);

    SubmissionResponseDto.OtherSubmissionDto getOtherSubmission(Long groupProblemId,Long submissionId,String username);

}
