package main.als.problem.service;

import main.als.problem.dto.SubmissionRequestDto;
import org.springframework.web.multipart.MultipartFile;

public interface SubmissionService {

    void submit(MultipartFile file,String language, Long groupProblemId, String username);
}
