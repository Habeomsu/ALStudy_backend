package main.als.problem.service;

import main.als.apiPayload.code.status.ErrorStatus;
import main.als.apiPayload.exception.GeneralException;
import main.als.aws.s3.AmazonS3Manager;
import main.als.group.repository.UserGroupRepository;
import main.als.problem.converter.SubmissionConverter;
import main.als.problem.dto.SubmissionRequestDto;
import main.als.problem.dto.SubmissionResponseDto;
import main.als.problem.entity.GroupProblem;
import main.als.problem.entity.Submission;
import main.als.problem.entity.SubmissionStatus;
import main.als.problem.repository.GroupProblemRepository;
import main.als.problem.repository.SubmissionRepository;
import main.als.user.entity.User;
import main.als.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class SubmissionServiceImpl implements SubmissionService {

    private final UserRepository userRepository;
    private final GroupProblemRepository groupProblemRepository;
    private final SubmissionRepository submissionRepository;
    private final UserGroupRepository userGroupRepository;
    private final AmazonS3Manager amazonS3Manager;

    public SubmissionServiceImpl(UserRepository userRepository,
                                 GroupProblemRepository groupProblemRepository,
                                 SubmissionRepository submissionRepository,
                                 UserGroupRepository userGroupRepository,
                                 AmazonS3Manager amazonS3Manager) {;
        this.userRepository = userRepository;
        this.groupProblemRepository = groupProblemRepository;
        this.submissionRepository = submissionRepository;
        this.userGroupRepository = userGroupRepository;
        this.amazonS3Manager = amazonS3Manager;
    }

    @Override
    public void submit(MultipartFile file,String language, Long groupProblemId, String username) {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new GeneralException(ErrorStatus._USERNAME_NOT_FOUND);
        }
        GroupProblem groupProblem = groupProblemRepository.findById(groupProblemId)
                .orElseThrow(()->new GeneralException(ErrorStatus._NOT_FOUND_GROUPPROBLEM));

        boolean isMember = userGroupRepository.existsByGroupIdAndUserUsername(groupProblem.getGroup().getId(), user.getUsername());

        if (!isMember) {
            throw new GeneralException(ErrorStatus._NOT_IN_USERGROUP); // 권한이 없는 경우 예외 처리
        }

        // 파일 검증
        if (file == null || file.isEmpty()) {
            throw new GeneralException(ErrorStatus._FILE_NOT_FOUND); // 파일이 없을 경우 예외 처리
        }

        String uuid = UUID.randomUUID().toString();

        String extension = "";
        switch (language.toLowerCase()) {
            case "java":
                extension = ".java";
                break;
            case "python":
                extension = ".py";
                break;
            default:
                extension = ".txt";
                break;
        }

        String fileName = "submissions/" + uuid + extension;

        String codeUrl = amazonS3Manager.uploadFile(fileName,file);


        Submission submission = Submission.builder()
                .groupProblem(groupProblem)
                .user(user)
                .language(language)
                .code(codeUrl)
                .status(SubmissionStatus.FAILED)
                .submissionTime(LocalDateTime.now())
                .build();

        user.getSubmissions().add(submission);
        groupProblem.getSubmissions().add(submission);

        submissionRepository.save(submission);

    }

    @Override
    public List<SubmissionResponseDto.AllSubmissionDto> getAll(Long groupProblemId, String username) {

        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new GeneralException(ErrorStatus._USERNAME_NOT_FOUND);
        }

        GroupProblem groupProblem = groupProblemRepository.findById(groupProblemId)
                .orElseThrow(()->new GeneralException(ErrorStatus._NOT_FOUND_GROUPPROBLEM));

        boolean isMember = userGroupRepository.existsByGroupIdAndUserUsername(groupProblem.getGroup().getId(), user.getUsername());

        if (!isMember) {
            throw new GeneralException(ErrorStatus._NOT_IN_USERGROUP); // 권한이 없는 경우 예외 처리
        }

        List<Submission> submissions = submissionRepository.findByUserAndGroupProblem(user, groupProblem);

        return SubmissionConverter.toAllSubmission(submissions);
    }

    @Override
    public SubmissionResponseDto.SubmissionDto getSubmission(Long groupProblemId, Long submissionId, String username) {

        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new GeneralException(ErrorStatus._USERNAME_NOT_FOUND);
        }

        GroupProblem groupProblem = groupProblemRepository.findById(groupProblemId)
                .orElseThrow(()->new GeneralException(ErrorStatus._NOT_FOUND_GROUPPROBLEM));

        boolean isMember = userGroupRepository.existsByGroupIdAndUserUsername(groupProblem.getGroup().getId(), user.getUsername());

        if (!isMember) {
            throw new GeneralException(ErrorStatus._NOT_IN_USERGROUP); // 권한이 없는 경우 예외 처리
        }

        Submission submission = submissionRepository.findById(submissionId)
                .orElseThrow(()->new GeneralException(ErrorStatus._NOT_FOUND_SUBMISSION));

        return SubmissionConverter.toSubmission(submission);
    }


}
