package main.als.problem.service;

import main.als.problem.dto.SubmissionRequestDto;

public interface SubmissionService {

    void submit(SubmissionRequestDto.SubmissionDto submissionDto,Long groupProblemId,String username);
}
