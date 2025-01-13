package main.als.problem.service;

import main.als.problem.dto.SubmissionRequestDto;
import main.als.problem.dto.SubmissionResponseDto;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface SubmissionService {

    void submit(MultipartFile file,String language, Long groupProblemId, String username);

    List<SubmissionResponseDto.AllSubmissionDto> getAll(Long groupId, String username);

    SubmissionResponseDto.SubmissionDto getSubmission(Long groupId,Long submissionId,String username);


}
