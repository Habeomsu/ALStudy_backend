package main.als.problem.service;

import main.als.page.PostPagingDto;
import main.als.problem.dto.SubmissionRequestDto;
import main.als.problem.dto.SubmissionResponseDto;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface SubmissionService {

    void submit(MultipartFile file,String language, Long groupProblemId, String username);

    SubmissionResponseDto.SearchSubmissionDto getAll(Long groupProblemId, String username, PostPagingDto.PagingDto pagingDto);

    SubmissionResponseDto.SubmissionDto getSubmission(Long groupProblemId,Long submissionId,String username);

    SubmissionResponseDto.SearchOtherSubmissionDto getOtherAll(Long groupProblemId,String username,PostPagingDto.PagingDto pagingDto);

    SubmissionResponseDto.OtherSubmissionDto getOtherSubmission(Long groupProblemId,Long submissionId,String username);



}
