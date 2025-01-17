package main.als.problem.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import main.als.apiPayload.code.status.ErrorStatus;
import main.als.apiPayload.exception.GeneralException;
import main.als.aws.s3.AmazonS3Manager;
import main.als.group.repository.UserGroupRepository;
import main.als.page.PostPagingDto;
import main.als.problem.converter.SubmissionConverter;
import main.als.problem.dto.SubmissionResponseDto;
import main.als.problem.entity.GroupProblem;
import main.als.problem.entity.Submission;
import main.als.problem.entity.SubmissionStatus;
import main.als.problem.entity.TestCase;
import main.als.problem.repository.GroupProblemRepository;
import main.als.problem.repository.SubmissionRepository;
import main.als.problem.util.FlaskCommunicationUtil;
import main.als.user.entity.User;
import main.als.user.repository.UserRepository;
import org.springframework.core.io.FileSystemResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

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
    @Transactional
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

        if (LocalDateTime.now().isAfter(groupProblem.getDeadline())) {
            throw new GeneralException(ErrorStatus._SUBMISSION_DEADLINE_EXCEEDED);// 기한 초과 예외
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

        // 테스트 케이스 가져오기
        List<TestCase> testCases = groupProblem.getProblem().getTestCases();

        // Flask 서버에 데이터 전송
        ResponseEntity<Map> response;
        try {
            response = FlaskCommunicationUtil.submitToFlask(file, testCases);
        } catch (IOException e) {
            throw new GeneralException(ErrorStatus._FLASK_SERVER_ERROR); // 통신 오류 처리
        }

        Submission submission = Submission.builder()
                .groupProblem(groupProblem)
                .user(user)
                .language(language)
                .code(codeUrl)
                .status(SubmissionStatus.FAILED)
                .submissionTime(LocalDateTime.now())
                .build();

        if (response.getBody() != null && response.getBody().containsKey("success")) {
            boolean success = (Boolean) response.getBody().get("success");
            if (success) {
                submission.setStatus(SubmissionStatus.SUCCEEDED); // 성공 시 상태 변경
            } else {
                submission.setStatus(SubmissionStatus.FAILED); // 실패 시 상태 변경

            }
        } else {
            throw new GeneralException(ErrorStatus._FLASK_SERVER_ERROR); // Flask 서버 오류 처리
        }


        user.getSubmissions().add(submission);
        groupProblem.getSubmissions().add(submission);

        submissionRepository.save(submission);

    }

    @Override
    public SubmissionResponseDto.SearchSubmissionDto getAll(Long groupProblemId, String username, PostPagingDto.PagingDto pagingDto) {

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

        Sort sort = Sort.by(Sort.Direction.fromString(pagingDto.getSort()),"submissionTime" );
        Pageable pageable = PageRequest.of(pagingDto.getPage(), pagingDto.getSize(), sort);

        Page<Submission> submissions = submissionRepository.findByUserAndGroupProblem(user, groupProblem,pageable);

        return SubmissionConverter.toSearchSubmission(submissions);
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

    @Override
    public SubmissionResponseDto.SearchOtherSubmissionDto getOtherAll(Long groupProblemId, String username,PostPagingDto.PagingDto pagingDto) {
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

        Sort sort = Sort.by(Sort.Direction.fromString(pagingDto.getSort()),"submissionTime" );
        Pageable pageable = PageRequest.of(pagingDto.getPage(), pagingDto.getSize(), sort);

        // 성공한 제출을 가져오는 로직 추가
        Page<Submission> successfulSubmissions = submissionRepository.findByGroupProblemIdAndStatus(groupProblemId, SubmissionStatus.SUCCEEDED,pageable);

        return SubmissionConverter.toSearchOtherSubmission(successfulSubmissions);
    }

    @Override
    public SubmissionResponseDto.OtherSubmissionDto getOtherSubmission(Long groupProblemId, Long submissionId, String username) {
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

        // 사용자가 해당 문제에 대해 성공적으로 제출한 기록이 있는지 확인
        boolean hasSucceededSubmission = submissionRepository.existsByUserAndGroupProblemAndStatus(user, groupProblem, SubmissionStatus.SUCCEEDED);

        if (!hasSucceededSubmission) {
            throw new GeneralException(ErrorStatus._NO_SUCCEEDED_SUBMISSION); // 성공한 제출이 없을 경우 예외 처리
        }

        // 제출 상세 조회 로직
        Submission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new GeneralException(ErrorStatus._NOT_FOUND_SUBMISSION));


        return SubmissionConverter.toOtherSubmission(submission);
    }


}
