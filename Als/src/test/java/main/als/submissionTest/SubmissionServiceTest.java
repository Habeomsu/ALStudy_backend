package main.als.submissionTest;

import main.als.apiPayload.code.status.ErrorStatus;
import main.als.apiPayload.exception.GeneralException;
import main.als.aws.s3.AmazonS3Manager;
import main.als.group.entity.Group;
import main.als.group.repository.UserGroupRepository;
import main.als.page.PostPagingDto;
import main.als.problem.converter.SubmissionConverter;
import main.als.problem.dto.SubmissionResponseDto;
import main.als.problem.entity.*;
import main.als.problem.repository.GroupProblemRepository;
import main.als.problem.repository.SubmissionRepository;
import main.als.problem.service.SubmissionService;
import main.als.problem.service.SubmissionServiceImpl;
import main.als.problem.util.FlaskCommunicationUtil;
import main.als.user.entity.User;
import main.als.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class SubmissionServiceTest {

    @InjectMocks
    private SubmissionServiceImpl submissionService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private GroupProblemRepository groupProblemRepository;

    @Mock
    private SubmissionRepository submissionRepository;

    @Mock
    private UserGroupRepository userGroupRepository;

    @Mock
    private AmazonS3Manager amazonS3Manager;

    @Mock
    private MultipartFile multipartFile;

    LocalDateTime now = LocalDateTime.now();

    @Test
    @DisplayName("submit 테스트 - (성공)")
    public void submitSuccessTest(){

        Long groupProblemId = 1L;
        String language = "python";
        String username = "test";
        String codeUrl = "testUrl";

        Group group = Group.builder()
                .id(1L)
                .build();

        User user = User.builder()
                .id(1L)
                .username(username)
                .build();

        TestCase testCase1 = TestCase.builder()
                .id(1L)
                .build();

        TestCase testCase2 = TestCase.builder()
                .id(2L)
                .build();

        Problem problem = Problem.builder()
                .id(1L)
                .testCases(List.of(testCase1,testCase2))
                .build();

        GroupProblem groupProblem = GroupProblem.builder()
                .id(groupProblemId)
                .group(group)
                .deadline(now.plusDays(1))
                .problem(problem)
                .build();

        Map<String, Object> responseBody = Map.of("success", true);
        ResponseEntity<Map> flaskResponse = new ResponseEntity<>(responseBody, HttpStatus.OK);

        when(userRepository.findByUsername(username)).thenReturn(user);
        when(groupProblemRepository.findById(groupProblemId)).thenReturn(Optional.of(groupProblem));
        when(userGroupRepository.existsByGroupIdAndUserUsername(group.getId(), username)).thenReturn(true);
        when(multipartFile.isEmpty()).thenReturn(false);
        when(amazonS3Manager.uploadFile(anyString(),eq(multipartFile))).thenReturn(codeUrl);

        try(MockedStatic<FlaskCommunicationUtil> mockedStatic = mockStatic(FlaskCommunicationUtil.class)) {
            mockedStatic.when(()->FlaskCommunicationUtil.submitToFlask(multipartFile, problem.getTestCases()))
                    .thenReturn(flaskResponse);

            submissionService.submit(multipartFile, language, groupProblemId, username);

            verify(userRepository).findByUsername(username);
            verify(groupProblemRepository).findById(groupProblemId);
            verify(userGroupRepository).existsByGroupIdAndUserUsername(group.getId(), username);
            verify(amazonS3Manager).uploadFile(anyString(), eq(multipartFile));
            verify(submissionRepository).save(any(Submission.class));

            assertEquals(1, user.getSubmissions().size());
            Submission saved = user.getSubmissions().get(0);
            assertEquals(SubmissionStatus.SUCCEEDED, saved.getStatus());
            assertEquals(codeUrl, saved.getCode());

        }

    }

    @Test
    @DisplayName("submit 테스트 - (실패 - 사용자 없음)")
    public void submitFailTest1(){

        Long groupProblemId = 1L;
        String language = "python";
        String username = "test";

        when(userRepository.findByUsername(username)).thenReturn(null);

        GeneralException exception = assertThrows(GeneralException.class, () -> submissionService.submit(multipartFile,language,groupProblemId,username));

        assertFalse(exception.getErrorReasonHttpStatus().getIsSuccess());
        assertEquals(ErrorStatus._USERNAME_NOT_FOUND.getCode(), exception.getErrorReasonHttpStatus().getCode());
        assertEquals(ErrorStatus._USERNAME_NOT_FOUND.getMessage(), exception.getErrorReasonHttpStatus().getMessage());
        assertEquals(ErrorStatus._USERNAME_NOT_FOUND.getHttpStatus(), exception.getErrorReasonHttpStatus().getHttpStatus());

    }

    @Test
    @DisplayName("submit 테스트 - (실패 - 그룹 문제 없음)")
    public void submitFailTest2(){

        Long groupProblemId = 1L;
        String language = "python";
        String username = "test";

        User user = User.builder()
                .id(1L)
                .username(username)
                .build();

        when(userRepository.findByUsername(username)).thenReturn(user);
        when(groupProblemRepository.findById(groupProblemId)).thenReturn(Optional.empty());

        GeneralException exception = assertThrows(GeneralException.class, () -> submissionService.submit(multipartFile,language,groupProblemId,username));

        assertFalse(exception.getErrorReasonHttpStatus().getIsSuccess());
        assertEquals(ErrorStatus._NOT_FOUND_GROUPPROBLEM.getCode(), exception.getErrorReasonHttpStatus().getCode());
        assertEquals(ErrorStatus._NOT_FOUND_GROUPPROBLEM.getMessage(), exception.getErrorReasonHttpStatus().getMessage());
        assertEquals(ErrorStatus._NOT_FOUND_GROUPPROBLEM.getHttpStatus(), exception.getErrorReasonHttpStatus().getHttpStatus());

    }

    @Test
    @DisplayName("submit 테스트 - (실패 - 유저그룹 미포함)")
    public void submitFailTest3(){

        Long groupProblemId = 1L;
        String language = "python";
        String username = "test";

        Group group = Group.builder()
                .id(1L)
                .build();

        User user = User.builder()
                .id(1L)
                .username(username)
                .build();

        TestCase testCase1 = TestCase.builder()
                .id(1L)
                .build();

        TestCase testCase2 = TestCase.builder()
                .id(2L)
                .build();

        Problem problem = Problem.builder()
                .id(1L)
                .testCases(List.of(testCase1,testCase2))
                .build();

        GroupProblem groupProblem = GroupProblem.builder()
                .id(groupProblemId)
                .group(group)
                .deadline(now.plusDays(1))
                .problem(problem)
                .build();

        when(userRepository.findByUsername(username)).thenReturn(user);
        when(groupProblemRepository.findById(groupProblemId)).thenReturn(Optional.ofNullable(groupProblem));
        when(userGroupRepository.existsByGroupIdAndUserUsername(group.getId(), username)).thenReturn(false);

        GeneralException exception = assertThrows(GeneralException.class, () -> submissionService.submit(multipartFile,language,groupProblemId,username));

        assertFalse(exception.getErrorReasonHttpStatus().getIsSuccess());
        assertEquals(ErrorStatus._NOT_IN_USERGROUP.getCode(), exception.getErrorReasonHttpStatus().getCode());
        assertEquals(ErrorStatus._NOT_IN_USERGROUP.getMessage(), exception.getErrorReasonHttpStatus().getMessage());
        assertEquals(ErrorStatus._NOT_IN_USERGROUP.getHttpStatus(), exception.getErrorReasonHttpStatus().getHttpStatus());

    }

    @Test
    @DisplayName("submit 테스트 - (실패 - 제출 기한 지남)")
    public void submitFailTest4(){

        Long groupProblemId = 1L;
        String language = "python";
        String username = "test";

        Group group = Group.builder()
                .id(1L)
                .build();

        User user = User.builder()
                .id(1L)
                .username(username)
                .build();

        TestCase testCase1 = TestCase.builder()
                .id(1L)
                .build();

        TestCase testCase2 = TestCase.builder()
                .id(2L)
                .build();

        Problem problem = Problem.builder()
                .id(1L)
                .testCases(List.of(testCase1,testCase2))
                .build();

        GroupProblem groupProblem = GroupProblem.builder()
                .id(groupProblemId)
                .group(group)
                .deadline(now.minusDays(1))
                .problem(problem)
                .build();

        when(userRepository.findByUsername(username)).thenReturn(user);
        when(groupProblemRepository.findById(groupProblemId)).thenReturn(Optional.ofNullable(groupProblem));
        when(userGroupRepository.existsByGroupIdAndUserUsername(group.getId(), username)).thenReturn(true);

        GeneralException exception = assertThrows(GeneralException.class, () -> submissionService.submit(multipartFile,language,groupProblemId,username));

        assertFalse(exception.getErrorReasonHttpStatus().getIsSuccess());
        assertEquals(ErrorStatus._SUBMISSION_DEADLINE_EXCEEDED.getCode(), exception.getErrorReasonHttpStatus().getCode());
        assertEquals(ErrorStatus._SUBMISSION_DEADLINE_EXCEEDED.getMessage(), exception.getErrorReasonHttpStatus().getMessage());
        assertEquals(ErrorStatus._SUBMISSION_DEADLINE_EXCEEDED.getHttpStatus(), exception.getErrorReasonHttpStatus().getHttpStatus());

    }

    @Test
    @DisplayName("submit 테스트 - (실패 - 파일없음)")
    public void submitFailTest5(){

        Long groupProblemId = 1L;
        String language = "python";
        String username = "test";

        Group group = Group.builder()
                .id(1L)
                .build();

        User user = User.builder()
                .id(1L)
                .username(username)
                .build();

        TestCase testCase1 = TestCase.builder()
                .id(1L)
                .build();

        TestCase testCase2 = TestCase.builder()
                .id(2L)
                .build();

        Problem problem = Problem.builder()
                .id(1L)
                .testCases(List.of(testCase1,testCase2))
                .build();

        GroupProblem groupProblem = GroupProblem.builder()
                .id(groupProblemId)
                .group(group)
                .deadline(now.plusDays(1))
                .problem(problem)
                .build();

        when(userRepository.findByUsername(username)).thenReturn(user);
        when(groupProblemRepository.findById(groupProblemId)).thenReturn(Optional.ofNullable(groupProblem));
        when(userGroupRepository.existsByGroupIdAndUserUsername(group.getId(), username)).thenReturn(true);

        GeneralException exception = assertThrows(GeneralException.class, () -> submissionService.submit(null,language,groupProblemId,username));

        assertFalse(exception.getErrorReasonHttpStatus().getIsSuccess());
        assertEquals(ErrorStatus._FILE_NOT_FOUND.getCode(), exception.getErrorReasonHttpStatus().getCode());
        assertEquals(ErrorStatus._FILE_NOT_FOUND.getMessage(), exception.getErrorReasonHttpStatus().getMessage());
        assertEquals(ErrorStatus._FILE_NOT_FOUND.getHttpStatus(), exception.getErrorReasonHttpStatus().getHttpStatus());

    }

    @Test
    @DisplayName("submit 테스트 - (실패 - 플라스크 통신 실패)")
    public void submitFailTest6() {

        Long groupProblemId = 1L;
        String language = "python";
        String username = "test";
        String codeUrl = "testUrl";

        Group group = Group.builder()
                .id(1L)
                .build();

        User user = User.builder()
                .id(1L)
                .username(username)
                .build();

        TestCase testCase1 = TestCase.builder()
                .id(1L)
                .build();

        TestCase testCase2 = TestCase.builder()
                .id(2L)
                .build();

        Problem problem = Problem.builder()
                .id(1L)
                .testCases(List.of(testCase1, testCase2))
                .build();

        GroupProblem groupProblem = GroupProblem.builder()
                .id(groupProblemId)
                .group(group)
                .deadline(now.plusDays(1))
                .problem(problem)
                .build();

        when(userRepository.findByUsername(username)).thenReturn(user);
        when(groupProblemRepository.findById(groupProblemId)).thenReturn(Optional.ofNullable(groupProblem));
        when(userGroupRepository.existsByGroupIdAndUserUsername(group.getId(), username)).thenReturn(true);
        when(multipartFile.isEmpty()).thenReturn(false);
        when(amazonS3Manager.uploadFile(anyString(), eq(multipartFile))).thenReturn(codeUrl);

        try (MockedStatic<FlaskCommunicationUtil> mockedStatic = mockStatic(FlaskCommunicationUtil.class)) {
            mockedStatic.when(() -> FlaskCommunicationUtil.submitToFlask(multipartFile, problem.getTestCases()))
                    .thenThrow(new IOException("Flask 서버 에러"));

            GeneralException exception = assertThrows(GeneralException.class, () -> submissionService.submit(multipartFile, language, groupProblemId, username));

            assertFalse(exception.getErrorReasonHttpStatus().getIsSuccess());
            assertEquals(ErrorStatus._FLASK_SERVER_ERROR.getCode(), exception.getErrorReasonHttpStatus().getCode());
            assertEquals(ErrorStatus._FLASK_SERVER_ERROR.getMessage(), exception.getErrorReasonHttpStatus().getMessage());
            assertEquals(ErrorStatus._FLASK_SERVER_ERROR.getHttpStatus(), exception.getErrorReasonHttpStatus().getHttpStatus());

        }

    }

    @Test
    @DisplayName("submit 테스트 - (실패 - 플라스크 응답 오류)")
    public void submitFailTest7() {

        Long groupProblemId = 1L;
        String language = "python";
        String username = "test";
        String codeUrl = "testUrl";

        Group group = Group.builder()
                .id(1L)
                .build();

        User user = User.builder()
                .id(1L)
                .username(username)
                .build();

        TestCase testCase1 = TestCase.builder()
                .id(1L)
                .build();

        TestCase testCase2 = TestCase.builder()
                .id(2L)
                .build();

        Problem problem = Problem.builder()
                .id(1L)
                .testCases(List.of(testCase1, testCase2))
                .build();

        GroupProblem groupProblem = GroupProblem.builder()
                .id(groupProblemId)
                .group(group)
                .deadline(now.plusDays(1))
                .problem(problem)
                .build();

        Map<String, Object> responseBody = Map.of();
        ResponseEntity<Map> flaskResponse = new ResponseEntity<>(responseBody, HttpStatus.OK);


        when(userRepository.findByUsername(username)).thenReturn(user);
        when(groupProblemRepository.findById(groupProblemId)).thenReturn(Optional.ofNullable(groupProblem));
        when(userGroupRepository.existsByGroupIdAndUserUsername(group.getId(), username)).thenReturn(true);
        when(multipartFile.isEmpty()).thenReturn(false);
        when(amazonS3Manager.uploadFile(anyString(), eq(multipartFile))).thenReturn(codeUrl);

        try (MockedStatic<FlaskCommunicationUtil> mockedStatic = mockStatic(FlaskCommunicationUtil.class)) {
            mockedStatic.when(() -> FlaskCommunicationUtil.submitToFlask(multipartFile, problem.getTestCases()))
                    .thenReturn(flaskResponse);

            GeneralException exception = assertThrows(GeneralException.class, () -> submissionService.submit(multipartFile, language, groupProblemId, username));

            assertFalse(exception.getErrorReasonHttpStatus().getIsSuccess());
            assertEquals(ErrorStatus._FLASK_SERVER_ERROR.getCode(), exception.getErrorReasonHttpStatus().getCode());
            assertEquals(ErrorStatus._FLASK_SERVER_ERROR.getMessage(), exception.getErrorReasonHttpStatus().getMessage());
            assertEquals(ErrorStatus._FLASK_SERVER_ERROR.getHttpStatus(), exception.getErrorReasonHttpStatus().getHttpStatus());

        }
    }

    @Test
    @DisplayName("getAll 테스트 - (성공)")
    public void getAllSuccessTest(){

        String username = "test";
        Long groupProblemId = 1L;

        Group group = Group.builder()
                .id(1L)
                .build();

        User user = User.builder()
                .id(1L)
                .username(username)
                .build();

        TestCase testCase1 = TestCase.builder()
                .id(1L)
                .build();

        TestCase testCase2 = TestCase.builder()
                .id(2L)
                .build();

        Problem problem = Problem.builder()
                .id(1L)
                .testCases(List.of(testCase1, testCase2))
                .build();

        GroupProblem groupProblem = GroupProblem.builder()
                .id(groupProblemId)
                .group(group)
                .deadline(now.plusDays(1))
                .problem(problem)
                .build();

        Submission submission1 = Submission.builder()
                .id(1L)
                .user(user)
                .groupProblem(groupProblem)
                .language("python")
                .status(SubmissionStatus.FAILED)
                .submissionTime(LocalDateTime.now())
                .build();

        Submission submission2 = Submission.builder()
                .id(2L)
                .user(user)
                .groupProblem(groupProblem)
                .language("python")
                .status(SubmissionStatus.SUCCEEDED)
                .submissionTime(LocalDateTime.now())
                .build();

        PostPagingDto.PagingDto pagingDto = PostPagingDto.PagingDto.builder()
                .page(0)
                .size(10)
                .sort("DESC")
                .build();

        Page<Submission> page = new PageImpl<>(List.of(submission1, submission2));

        when(userRepository.findByUsername(username)).thenReturn(user);
        when(groupProblemRepository.findById(groupProblemId)).thenReturn(Optional.ofNullable(groupProblem));
        when(userGroupRepository.existsByGroupIdAndUserUsername(group.getId(), username)).thenReturn(true);
        when(submissionRepository.findByUserAndGroupProblem(eq(user),eq(groupProblem),any(Pageable.class))).thenReturn(page);

        SubmissionResponseDto.SearchSubmissionDto result =
                submissionService.getAll(groupProblemId, username, pagingDto);

        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
        assertEquals("python", result.getSubmissionResDtos().get(0).getLanguage());

        verify(submissionRepository).findByUserAndGroupProblem(eq(user), eq(groupProblem), any(Pageable.class));

    }

    @Test
    @DisplayName("getAll 테스트 - (실패 - 사용자 없음)")
    public void getAllFailTest1(){

        String username = "test";
        Long groupProblemId = 1L;

        PostPagingDto.PagingDto pagingDto = PostPagingDto.PagingDto.builder()
                .page(0)
                .size(10)
                .sort("DESC")
                .build();

        when(userRepository.findByUsername(username)).thenReturn(null);

        GeneralException exception = assertThrows(GeneralException.class, () -> submissionService.getAll(groupProblemId,username, pagingDto));

        assertFalse(exception.getErrorReasonHttpStatus().getIsSuccess());
        assertEquals(ErrorStatus._USERNAME_NOT_FOUND.getCode(), exception.getErrorReasonHttpStatus().getCode());
        assertEquals(ErrorStatus._USERNAME_NOT_FOUND.getMessage(), exception.getErrorReasonHttpStatus().getMessage());
        assertEquals(ErrorStatus._USERNAME_NOT_FOUND.getHttpStatus(), exception.getErrorReasonHttpStatus().getHttpStatus());

    }

    @Test
    @DisplayName("getAll 테스트 - (실패 - 그룹문제 없음)")
    public void getAllFailTest2(){

        String username = "test";
        Long groupProblemId = 1L;

        Group group = Group.builder()
                .id(1L)
                .build();

        User user = User.builder()
                .id(1L)
                .username(username)
                .build();

        TestCase testCase1 = TestCase.builder()
                .id(1L)
                .build();

        TestCase testCase2 = TestCase.builder()
                .id(2L)
                .build();

        Problem problem = Problem.builder()
                .id(1L)
                .testCases(List.of(testCase1, testCase2))
                .build();

        GroupProblem groupProblem = GroupProblem.builder()
                .id(groupProblemId)
                .group(group)
                .deadline(now.plusDays(1))
                .problem(problem)
                .build();

        Submission submission1 = Submission.builder()
                .id(1L)
                .user(user)
                .groupProblem(groupProblem)
                .language("python")
                .status(SubmissionStatus.FAILED)
                .submissionTime(LocalDateTime.now())
                .build();

        Submission submission2 = Submission.builder()
                .id(2L)
                .user(user)
                .groupProblem(groupProblem)
                .language("python")
                .status(SubmissionStatus.SUCCEEDED)
                .submissionTime(LocalDateTime.now())
                .build();

        PostPagingDto.PagingDto pagingDto = PostPagingDto.PagingDto.builder()
                .page(0)
                .size(10)
                .sort("DESC")
                .build();

        when(userRepository.findByUsername(username)).thenReturn(user);
        when(groupProblemRepository.findById(groupProblemId)).thenReturn(Optional.empty());

        GeneralException exception = assertThrows(GeneralException.class, () -> submissionService.getAll(groupProblemId,username, pagingDto));

        assertFalse(exception.getErrorReasonHttpStatus().getIsSuccess());
        assertEquals(ErrorStatus._NOT_FOUND_GROUPPROBLEM.getCode(), exception.getErrorReasonHttpStatus().getCode());
        assertEquals(ErrorStatus._NOT_FOUND_GROUPPROBLEM.getMessage(), exception.getErrorReasonHttpStatus().getMessage());
        assertEquals(ErrorStatus._NOT_FOUND_GROUPPROBLEM.getHttpStatus(), exception.getErrorReasonHttpStatus().getHttpStatus());

    }

    @Test
    @DisplayName("getAll 테스트 - (실패 - 유저그룹이 아님)")
    public void getAllFailTest3(){

        String username = "test";
        Long groupProblemId = 1L;

        Group group = Group.builder()
                .id(1L)
                .build();

        User user = User.builder()
                .id(1L)
                .username(username)
                .build();

        TestCase testCase1 = TestCase.builder()
                .id(1L)
                .build();

        TestCase testCase2 = TestCase.builder()
                .id(2L)
                .build();

        Problem problem = Problem.builder()
                .id(1L)
                .testCases(List.of(testCase1, testCase2))
                .build();

        GroupProblem groupProblem = GroupProblem.builder()
                .id(groupProblemId)
                .group(group)
                .deadline(now.plusDays(1))
                .problem(problem)
                .build();

        Submission submission1 = Submission.builder()
                .id(1L)
                .user(user)
                .groupProblem(groupProblem)
                .language("python")
                .status(SubmissionStatus.FAILED)
                .submissionTime(LocalDateTime.now())
                .build();

        Submission submission2 = Submission.builder()
                .id(2L)
                .user(user)
                .groupProblem(groupProblem)
                .language("python")
                .status(SubmissionStatus.SUCCEEDED)
                .submissionTime(LocalDateTime.now())
                .build();

        PostPagingDto.PagingDto pagingDto = PostPagingDto.PagingDto.builder()
                .page(0)
                .size(10)
                .sort("DESC")
                .build();

        when(userRepository.findByUsername(username)).thenReturn(user);
        when(groupProblemRepository.findById(groupProblemId)).thenReturn(Optional.of(groupProblem));
        when(userGroupRepository.existsByGroupIdAndUserUsername(group.getId(), username)).thenReturn(false);

        GeneralException exception = assertThrows(GeneralException.class, () -> submissionService.getAll(groupProblemId,username, pagingDto));

        assertFalse(exception.getErrorReasonHttpStatus().getIsSuccess());
        assertEquals(ErrorStatus._NOT_IN_USERGROUP.getCode(), exception.getErrorReasonHttpStatus().getCode());
        assertEquals(ErrorStatus._NOT_IN_USERGROUP.getMessage(), exception.getErrorReasonHttpStatus().getMessage());
        assertEquals(ErrorStatus._NOT_IN_USERGROUP.getHttpStatus(), exception.getErrorReasonHttpStatus().getHttpStatus());

    }

    @Test
    @DisplayName("getSubmission 테스트 - (성공)")
    public void getSubmissionSuccessTest(){

        String username = "test";
        Long groupProblemId = 1L;
        Long submissionId = 1L;

        Group group = Group.builder()
                .id(1L)
                .build();

        User user = User.builder()
                .id(1L)
                .username(username)
                .build();

        TestCase testCase1 = TestCase.builder()
                .id(1L)
                .build();

        TestCase testCase2 = TestCase.builder()
                .id(2L)
                .build();

        Problem problem = Problem.builder()
                .id(1L)
                .testCases(List.of(testCase1, testCase2))
                .build();

        GroupProblem groupProblem = GroupProblem.builder()
                .id(groupProblemId)
                .group(group)
                .deadline(now.plusDays(1))
                .problem(problem)
                .build();

        Submission submission1 = Submission.builder()
                .id(submissionId)
                .user(user)
                .groupProblem(groupProblem)
                .language("python")
                .status(SubmissionStatus.FAILED)
                .submissionTime(LocalDateTime.now())
                .build();

        when(userRepository.findByUsername(username)).thenReturn(user);
        when(groupProblemRepository.findById(groupProblemId)).thenReturn(Optional.of(groupProblem));
        when(userGroupRepository.existsByGroupIdAndUserUsername(group.getId(), username)).thenReturn(true);
        when(submissionRepository.findById(submissionId)).thenReturn(Optional.of(submission1));

        SubmissionResponseDto.SubmissionDto mockDto = SubmissionResponseDto.SubmissionDto.builder()
                .id(submissionId)
                .groupProblemId(groupProblemId)
                .language("python")
                .status(submission1.getStatus())
                .username(user.getUsername())
                .build();

        try (MockedStatic<SubmissionConverter> mocked = mockStatic(SubmissionConverter.class)) {
            mocked.when(() -> SubmissionConverter.toSubmission(any()))
                    .thenReturn(mockDto);

            SubmissionResponseDto.SubmissionDto result =
                    submissionService.getSubmission(groupProblemId, submissionId, username);

            assertNotNull(result);
            assertEquals(submission1.getId(), result.getId());
            assertEquals(groupProblemId, result.getGroupProblemId());
        }

    }

    @Test
    @DisplayName("getSubmission 테스트 - (실패 - 사용자없음)")
    public void getSubmissionFailTest1(){

        String username = "test";
        Long groupProblemId = 1L;
        Long submissionId = 1L;

        when(userRepository.findByUsername(username)).thenReturn(null);

        GeneralException exception = assertThrows(GeneralException.class, () -> submissionService.getSubmission(groupProblemId, submissionId, username));

        assertFalse(exception.getErrorReasonHttpStatus().getIsSuccess());
        assertEquals(ErrorStatus._USERNAME_NOT_FOUND.getCode(), exception.getErrorReasonHttpStatus().getCode());
        assertEquals(ErrorStatus._USERNAME_NOT_FOUND.getMessage(), exception.getErrorReasonHttpStatus().getMessage());
        assertEquals(ErrorStatus._USERNAME_NOT_FOUND.getHttpStatus(), exception.getErrorReasonHttpStatus().getHttpStatus());

    }

    @Test
    @DisplayName("getSubmission 테스트 - (실패 - 그룹문제 없음)")
    public void getSubmissionFailTest2(){

        String username = "test";
        Long groupProblemId = 1L;
        Long submissionId = 1L;

        User user = User.builder()
                .id(1L)
                .username(username)
                .build();

        when(userRepository.findByUsername(username)).thenReturn(user);
        when(groupProblemRepository.findById(groupProblemId)).thenReturn(Optional.empty());

        GeneralException exception = assertThrows(GeneralException.class, () -> submissionService.getSubmission(groupProblemId, submissionId, username));

        assertFalse(exception.getErrorReasonHttpStatus().getIsSuccess());
        assertEquals(ErrorStatus._NOT_FOUND_GROUPPROBLEM.getCode(), exception.getErrorReasonHttpStatus().getCode());
        assertEquals(ErrorStatus._NOT_FOUND_GROUPPROBLEM.getMessage(), exception.getErrorReasonHttpStatus().getMessage());
        assertEquals(ErrorStatus._NOT_FOUND_GROUPPROBLEM.getHttpStatus(), exception.getErrorReasonHttpStatus().getHttpStatus());

    }

    @Test
    @DisplayName("getSubmission 테스트 - (실패 - 그룹 유저가 아님)")
    public void getSubmissionFailTest3(){

        String username = "test";
        Long groupProblemId = 1L;
        Long submissionId = 1L;

        Group group = Group.builder()
                .id(1L)
                .build();

        User user = User.builder()
                .id(1L)
                .username(username)
                .build();

        TestCase testCase1 = TestCase.builder()
                .id(1L)
                .build();

        TestCase testCase2 = TestCase.builder()
                .id(2L)
                .build();

        Problem problem = Problem.builder()
                .id(1L)
                .testCases(List.of(testCase1, testCase2))
                .build();

        GroupProblem groupProblem = GroupProblem.builder()
                .id(groupProblemId)
                .group(group)
                .deadline(now.plusDays(1))
                .problem(problem)
                .build();

        Submission submission1 = Submission.builder()
                .id(submissionId)
                .user(user)
                .groupProblem(groupProblem)
                .language("python")
                .status(SubmissionStatus.FAILED)
                .submissionTime(LocalDateTime.now())
                .build();

        when(userRepository.findByUsername(username)).thenReturn(user);
        when(groupProblemRepository.findById(groupProblemId)).thenReturn(Optional.of(groupProblem));
        when(userGroupRepository.existsByGroupIdAndUserUsername(group.getId(), username)).thenReturn(false);

        GeneralException exception = assertThrows(GeneralException.class, () -> submissionService.getSubmission(groupProblemId, submissionId, username));

        assertFalse(exception.getErrorReasonHttpStatus().getIsSuccess());
        assertEquals(ErrorStatus._NOT_IN_USERGROUP.getCode(), exception.getErrorReasonHttpStatus().getCode());
        assertEquals(ErrorStatus._NOT_IN_USERGROUP.getMessage(), exception.getErrorReasonHttpStatus().getMessage());
        assertEquals(ErrorStatus._NOT_IN_USERGROUP.getHttpStatus(), exception.getErrorReasonHttpStatus().getHttpStatus());

    }

    @Test
    @DisplayName("getSubmission 테스트 - (실패 - 제출 없음)")
    public void getSubmissionFailTest4(){

        String username = "test";
        Long groupProblemId = 1L;
        Long submissionId = 1L;

        Group group = Group.builder()
                .id(1L)
                .build();

        User user = User.builder()
                .id(1L)
                .username(username)
                .build();

        TestCase testCase1 = TestCase.builder()
                .id(1L)
                .build();

        TestCase testCase2 = TestCase.builder()
                .id(2L)
                .build();

        Problem problem = Problem.builder()
                .id(1L)
                .testCases(List.of(testCase1, testCase2))
                .build();

        GroupProblem groupProblem = GroupProblem.builder()
                .id(groupProblemId)
                .group(group)
                .deadline(now.plusDays(1))
                .problem(problem)
                .build();

        Submission submission1 = Submission.builder()
                .id(submissionId)
                .user(user)
                .groupProblem(groupProblem)
                .language("python")
                .status(SubmissionStatus.FAILED)
                .submissionTime(LocalDateTime.now())
                .build();

        when(userRepository.findByUsername(username)).thenReturn(user);
        when(groupProblemRepository.findById(groupProblemId)).thenReturn(Optional.of(groupProblem));
        when(userGroupRepository.existsByGroupIdAndUserUsername(group.getId(), username)).thenReturn(true);
        when(submissionRepository.findById(submissionId)).thenReturn(Optional.empty());

        GeneralException exception = assertThrows(GeneralException.class, () -> submissionService.getSubmission(groupProblemId, submissionId, username));

        assertFalse(exception.getErrorReasonHttpStatus().getIsSuccess());
        assertEquals(ErrorStatus._NOT_FOUND_SUBMISSION.getCode(), exception.getErrorReasonHttpStatus().getCode());
        assertEquals(ErrorStatus._NOT_FOUND_SUBMISSION.getMessage(), exception.getErrorReasonHttpStatus().getMessage());
        assertEquals(ErrorStatus._NOT_FOUND_SUBMISSION.getHttpStatus(), exception.getErrorReasonHttpStatus().getHttpStatus());

    }

    @Test
    @DisplayName("getOtherAll 테스트 - (성공)")
    public void getOtherAllSuccessTest(){

        String username = "test";
        Long groupProblemId = 1L;

        Group group = Group.builder()
                .id(1L)
                .build();

        User user = User.builder()
                .id(1L)
                .username(username)
                .build();

        TestCase testCase1 = TestCase.builder()
                .id(1L)
                .build();

        TestCase testCase2 = TestCase.builder()
                .id(2L)
                .build();

        Problem problem = Problem.builder()
                .id(1L)
                .testCases(List.of(testCase1, testCase2))
                .build();

        GroupProblem groupProblem = GroupProblem.builder()
                .id(groupProblemId)
                .group(group)
                .deadline(now.plusDays(1))
                .problem(problem)
                .build();

        Submission submission1 = Submission.builder()
                .id(1L)
                .user(user)
                .groupProblem(groupProblem)
                .language("python")
                .status(SubmissionStatus.FAILED)
                .submissionTime(LocalDateTime.now())
                .build();

        Submission submission2 = Submission.builder()
                .id(2L)
                .user(user)
                .groupProblem(groupProblem)
                .language("python")
                .status(SubmissionStatus.SUCCEEDED)
                .submissionTime(LocalDateTime.now())
                .build();

        PostPagingDto.PagingDto pagingDto = PostPagingDto.PagingDto.builder()
                .page(0)
                .size(10)
                .sort("DESC")
                .build();

        Sort sort = Sort.by(Sort.Direction.fromString(pagingDto.getSort()),"submissionTime" );
        Pageable pageable = PageRequest.of(pagingDto.getPage(), pagingDto.getSize(), sort);

        Page<Submission> page = new PageImpl<>(List.of(submission2));

        SubmissionResponseDto.OtherAllSubmissionDto dto = SubmissionResponseDto.OtherAllSubmissionDto.builder()
                .id(submission1.getId())
                .build();

        SubmissionResponseDto.SearchOtherSubmissionDto expectedDto =
                SubmissionResponseDto.SearchOtherSubmissionDto.builder()
                        .totalElements(1)
                        .otherSubmissionResDtos(List.of(dto)) // 필요한 구조에 맞게 수정
                        .build();

        when(userRepository.findByUsername(username)).thenReturn(user);
        when(groupProblemRepository.findById(groupProblemId)).thenReturn(Optional.of(groupProblem));
        when(userGroupRepository.existsByGroupIdAndUserUsername(group.getId(), username)).thenReturn(true);
        when(submissionRepository.findByGroupProblemIdAndStatus(groupProblemId, SubmissionStatus.SUCCEEDED,pageable)).thenReturn(page);

        try (MockedStatic<SubmissionConverter> mocked = mockStatic(SubmissionConverter.class)) {
            mocked.when(() -> SubmissionConverter.toSearchOtherSubmission(page)).thenReturn(expectedDto);

            // then
            SubmissionResponseDto.SearchOtherSubmissionDto result =
                    submissionService.getOtherAll(groupProblemId, username, pagingDto);

            assertNotNull(result);
            assertEquals(1, result.getTotalElements());
            assertEquals(expectedDto, result);
        }

    }

    @Test
    @DisplayName("getOtherAll 테스트 - (실패 - 사용자 없음)")
    public void getOtherAllFailTest1(){

        String username = "test";
        Long groupProblemId = 1L;

        PostPagingDto.PagingDto pagingDto = PostPagingDto.PagingDto.builder()
                .page(0)
                .size(10)
                .sort("DESC")
                .build();

        Sort sort = Sort.by(Sort.Direction.fromString(pagingDto.getSort()),"submissionTime" );
        Pageable pageable = PageRequest.of(pagingDto.getPage(), pagingDto.getSize(), sort);

        when(userRepository.findByUsername(username)).thenReturn(null);

        GeneralException exception = assertThrows(GeneralException.class, () -> submissionService.getOtherAll(groupProblemId, username, pagingDto));

        assertFalse(exception.getErrorReasonHttpStatus().getIsSuccess());
        assertEquals(ErrorStatus._USERNAME_NOT_FOUND.getCode(), exception.getErrorReasonHttpStatus().getCode());
        assertEquals(ErrorStatus._USERNAME_NOT_FOUND.getMessage(), exception.getErrorReasonHttpStatus().getMessage());
        assertEquals(ErrorStatus._USERNAME_NOT_FOUND.getHttpStatus(), exception.getErrorReasonHttpStatus().getHttpStatus());

    }

    @Test
    @DisplayName("getOtherAll 테스트 - (실패 - 그룹 문제 없음)")
    public void getOtherAllFailTest2(){

        String username = "test";
        Long groupProblemId = 1L;

        User user = User.builder()
                .id(1L)
                .username(username)
                .build();

        PostPagingDto.PagingDto pagingDto = PostPagingDto.PagingDto.builder()
                .page(0)
                .size(10)
                .sort("DESC")
                .build();

        Sort sort = Sort.by(Sort.Direction.fromString(pagingDto.getSort()),"submissionTime" );
        Pageable pageable = PageRequest.of(pagingDto.getPage(), pagingDto.getSize(), sort);

        when(userRepository.findByUsername(username)).thenReturn(user);
        when(groupProblemRepository.findById(groupProblemId)).thenReturn(Optional.empty());

        GeneralException exception = assertThrows(GeneralException.class, () -> submissionService.getOtherAll(groupProblemId, username, pagingDto));

        assertFalse(exception.getErrorReasonHttpStatus().getIsSuccess());
        assertEquals(ErrorStatus._NOT_FOUND_GROUPPROBLEM.getCode(), exception.getErrorReasonHttpStatus().getCode());
        assertEquals(ErrorStatus._NOT_FOUND_GROUPPROBLEM.getMessage(), exception.getErrorReasonHttpStatus().getMessage());
        assertEquals(ErrorStatus._NOT_FOUND_GROUPPROBLEM.getHttpStatus(), exception.getErrorReasonHttpStatus().getHttpStatus());

    }

    @Test
    @DisplayName("getOtherAll 테스트 - (실패 - 그룹 사용자 아님)")
    public void getOtherAllFailTest3(){

        String username = "test";
        Long groupProblemId = 1L;

        User user = User.builder()
                .id(1L)
                .username(username)
                .build();

        Group group = Group.builder()
                .id(1L)
                .build();

        TestCase testCase1 = TestCase.builder()
                .id(1L)
                .build();

        TestCase testCase2 = TestCase.builder()
                .id(2L)
                .build();

        Problem problem = Problem.builder()
                .id(1L)
                .testCases(List.of(testCase1, testCase2))
                .build();

        GroupProblem groupProblem = GroupProblem.builder()
                .id(groupProblemId)
                .group(group)
                .deadline(now.plusDays(1))
                .problem(problem)
                .build();

        PostPagingDto.PagingDto pagingDto = PostPagingDto.PagingDto.builder()
                .page(0)
                .size(10)
                .sort("DESC")
                .build();

        Sort sort = Sort.by(Sort.Direction.fromString(pagingDto.getSort()),"submissionTime" );
        Pageable pageable = PageRequest.of(pagingDto.getPage(), pagingDto.getSize(), sort);

        when(userRepository.findByUsername(username)).thenReturn(user);
        when(groupProblemRepository.findById(groupProblemId)).thenReturn(Optional.of(groupProblem));
        when(userGroupRepository.existsByGroupIdAndUserUsername(group.getId(), username)).thenReturn(false);

        GeneralException exception = assertThrows(GeneralException.class, () -> submissionService.getOtherAll(groupProblemId, username, pagingDto));

        assertFalse(exception.getErrorReasonHttpStatus().getIsSuccess());
        assertEquals(ErrorStatus._NOT_IN_USERGROUP.getCode(), exception.getErrorReasonHttpStatus().getCode());
        assertEquals(ErrorStatus._NOT_IN_USERGROUP.getMessage(), exception.getErrorReasonHttpStatus().getMessage());
        assertEquals(ErrorStatus._NOT_IN_USERGROUP.getHttpStatus(), exception.getErrorReasonHttpStatus().getHttpStatus());

    }

    @Test
    @DisplayName("getOtherSubmission 테스트 - (성공)")
    public void getOtherSubmissionSuccessTest(){

        String username = "test";
        Long groupProblemId = 1L;
        Long submissionId = 1L;

        Group group = Group.builder()
                .id(1L)
                .build();

        User user = User.builder()
                .id(1L)
                .username(username)
                .build();

        TestCase testCase1 = TestCase.builder()
                .id(1L)
                .build();

        TestCase testCase2 = TestCase.builder()
                .id(2L)
                .build();

        Problem problem = Problem.builder()
                .id(1L)
                .testCases(List.of(testCase1, testCase2))
                .build();

        GroupProblem groupProblem = GroupProblem.builder()
                .id(groupProblemId)
                .group(group)
                .deadline(now.plusDays(1))
                .problem(problem)
                .build();

        Submission submission1 = Submission.builder()
                .id(submissionId)
                .user(user)
                .groupProblem(groupProblem)
                .language("python")
                .status(SubmissionStatus.SUCCEEDED)
                .submissionTime(LocalDateTime.now())
                .build();

        when(userRepository.findByUsername(username)).thenReturn(user);
        when(groupProblemRepository.findById(groupProblemId)).thenReturn(Optional.of(groupProblem));
        when(userGroupRepository.existsByGroupIdAndUserUsername(group.getId(), username)).thenReturn(true);
        when(submissionRepository.existsByUserAndGroupProblemAndStatus(user, groupProblem, SubmissionStatus.SUCCEEDED)).thenReturn(true);
        when(submissionRepository.findById(submissionId)).thenReturn(Optional.of(submission1));

        SubmissionResponseDto.OtherSubmissionDto dto = SubmissionResponseDto.OtherSubmissionDto.builder()
                .id(submissionId)
                .build();

        try(MockedStatic<SubmissionConverter> mockedStatic = Mockito.mockStatic(SubmissionConverter.class)) {
            mockedStatic.when(()->SubmissionConverter.toOtherSubmission(submission1)).thenReturn(dto);

            SubmissionResponseDto.OtherSubmissionDto result  = submissionService.getOtherSubmission(groupProblemId, submissionId, username);

            assertEquals(dto, result);

        }
    }

    @Test
    @DisplayName("getOtherSubmission 테스트 - (실패 - 사용자없음)")
    public void getOtherSubmissionFailTest1(){

        String username = "test";
        Long groupProblemId = 1L;
        Long submissionId = 1L;

        when(userRepository.findByUsername(username)).thenReturn(null);

        GeneralException exception = assertThrows(GeneralException.class, () -> submissionService.getOtherSubmission(groupProblemId, submissionId, username));

        assertFalse(exception.getErrorReasonHttpStatus().getIsSuccess());
        assertEquals(ErrorStatus._USERNAME_NOT_FOUND.getCode(), exception.getErrorReasonHttpStatus().getCode());
        assertEquals(ErrorStatus._USERNAME_NOT_FOUND.getMessage(), exception.getErrorReasonHttpStatus().getMessage());
        assertEquals(ErrorStatus._USERNAME_NOT_FOUND.getHttpStatus(), exception.getErrorReasonHttpStatus().getHttpStatus());

    }

    @Test
    @DisplayName("getOtherSubmission 테스트 - (실패 - 그룹 문제 없음)")
    public void getOtherSubmissionFailTest2(){

        String username = "test";
        Long groupProblemId = 1L;
        Long submissionId = 1L;

        User user = User.builder()
                .id(1L)
                .username(username)
                .build();

        when(userRepository.findByUsername(username)).thenReturn(user);
        when(groupProblemRepository.findById(groupProblemId)).thenReturn(Optional.empty());

        GeneralException exception = assertThrows(GeneralException.class, () -> submissionService.getOtherSubmission(groupProblemId, submissionId, username));

        assertFalse(exception.getErrorReasonHttpStatus().getIsSuccess());
        assertEquals(ErrorStatus._NOT_FOUND_GROUPPROBLEM.getCode(), exception.getErrorReasonHttpStatus().getCode());
        assertEquals(ErrorStatus._NOT_FOUND_GROUPPROBLEM.getMessage(), exception.getErrorReasonHttpStatus().getMessage());
        assertEquals(ErrorStatus._NOT_FOUND_GROUPPROBLEM.getHttpStatus(), exception.getErrorReasonHttpStatus().getHttpStatus());

    }

    @Test
    @DisplayName("getOtherSubmission 테스트 - (실패 - 그룹 사용자 아님)")
    public void getOtherSubmissionFailTest3(){

        String username = "test";
        Long groupProblemId = 1L;
        Long submissionId = 1L;

        Group group = Group.builder()
                .id(1L)
                .build();

        User user = User.builder()
                .id(1L)
                .username(username)
                .build();

        TestCase testCase1 = TestCase.builder()
                .id(1L)
                .build();

        TestCase testCase2 = TestCase.builder()
                .id(2L)
                .build();

        Problem problem = Problem.builder()
                .id(1L)
                .testCases(List.of(testCase1, testCase2))
                .build();

        GroupProblem groupProblem = GroupProblem.builder()
                .id(groupProblemId)
                .group(group)
                .deadline(now.plusDays(1))
                .problem(problem)
                .build();

        Submission submission1 = Submission.builder()
                .id(submissionId)
                .user(user)
                .groupProblem(groupProblem)
                .language("python")
                .status(SubmissionStatus.SUCCEEDED)
                .submissionTime(LocalDateTime.now())
                .build();

        when(userRepository.findByUsername(username)).thenReturn(user);
        when(groupProblemRepository.findById(groupProblemId)).thenReturn(Optional.of(groupProblem));
        when(userGroupRepository.existsByGroupIdAndUserUsername(group.getId(), username)).thenReturn(false);

        GeneralException exception = assertThrows(GeneralException.class, () -> submissionService.getOtherSubmission(groupProblemId, submissionId, username));

        assertFalse(exception.getErrorReasonHttpStatus().getIsSuccess());
        assertEquals(ErrorStatus._NOT_IN_USERGROUP.getCode(), exception.getErrorReasonHttpStatus().getCode());
        assertEquals(ErrorStatus._NOT_IN_USERGROUP.getMessage(), exception.getErrorReasonHttpStatus().getMessage());
        assertEquals(ErrorStatus._NOT_IN_USERGROUP.getHttpStatus(), exception.getErrorReasonHttpStatus().getHttpStatus());

    }

    @Test
    @DisplayName("getOtherSubmission 테스트 - (실패 - 성공 제출이 없음)")
    public void getOtherSubmissionFailTest4(){

        String username = "test";
        Long groupProblemId = 1L;
        Long submissionId = 1L;

        Group group = Group.builder()
                .id(1L)
                .build();

        User user = User.builder()
                .id(1L)
                .username(username)
                .build();

        TestCase testCase1 = TestCase.builder()
                .id(1L)
                .build();

        TestCase testCase2 = TestCase.builder()
                .id(2L)
                .build();

        Problem problem = Problem.builder()
                .id(1L)
                .testCases(List.of(testCase1, testCase2))
                .build();

        GroupProblem groupProblem = GroupProblem.builder()
                .id(groupProblemId)
                .group(group)
                .deadline(now.plusDays(1))
                .problem(problem)
                .build();

        Submission submission1 = Submission.builder()
                .id(submissionId)
                .user(user)
                .groupProblem(groupProblem)
                .language("python")
                .status(SubmissionStatus.SUCCEEDED)
                .submissionTime(LocalDateTime.now())
                .build();

        when(userRepository.findByUsername(username)).thenReturn(user);
        when(groupProblemRepository.findById(groupProblemId)).thenReturn(Optional.of(groupProblem));
        when(userGroupRepository.existsByGroupIdAndUserUsername(group.getId(), username)).thenReturn(true);
        when(submissionRepository.existsByUserAndGroupProblemAndStatus(user, groupProblem, SubmissionStatus.SUCCEEDED)).thenReturn(false);

        GeneralException exception = assertThrows(GeneralException.class, () -> submissionService.getOtherSubmission(groupProblemId, submissionId, username));

        assertFalse(exception.getErrorReasonHttpStatus().getIsSuccess());
        assertEquals(ErrorStatus._NO_SUCCEEDED_SUBMISSION.getCode(), exception.getErrorReasonHttpStatus().getCode());
        assertEquals(ErrorStatus._NO_SUCCEEDED_SUBMISSION.getMessage(), exception.getErrorReasonHttpStatus().getMessage());
        assertEquals(ErrorStatus._NO_SUCCEEDED_SUBMISSION.getHttpStatus(), exception.getErrorReasonHttpStatus().getHttpStatus());

    }

    @Test
    @DisplayName("getOtherSubmission 테스트 - (실패 - 제출 없음)")
    public void getOtherSubmissionFailTest5(){

        String username = "test";
        Long groupProblemId = 1L;
        Long submissionId = 1L;

        Group group = Group.builder()
                .id(1L)
                .build();

        User user = User.builder()
                .id(1L)
                .username(username)
                .build();

        TestCase testCase1 = TestCase.builder()
                .id(1L)
                .build();

        TestCase testCase2 = TestCase.builder()
                .id(2L)
                .build();

        Problem problem = Problem.builder()
                .id(1L)
                .testCases(List.of(testCase1, testCase2))
                .build();

        GroupProblem groupProblem = GroupProblem.builder()
                .id(groupProblemId)
                .group(group)
                .deadline(now.plusDays(1))
                .problem(problem)
                .build();

        Submission submission1 = Submission.builder()
                .id(submissionId)
                .user(user)
                .groupProblem(groupProblem)
                .language("python")
                .status(SubmissionStatus.SUCCEEDED)
                .submissionTime(LocalDateTime.now())
                .build();

        when(userRepository.findByUsername(username)).thenReturn(user);
        when(groupProblemRepository.findById(groupProblemId)).thenReturn(Optional.of(groupProblem));
        when(userGroupRepository.existsByGroupIdAndUserUsername(group.getId(), username)).thenReturn(true);
        when(submissionRepository.existsByUserAndGroupProblemAndStatus(user, groupProblem, SubmissionStatus.SUCCEEDED)).thenReturn(true);
        when(submissionRepository.findById(submissionId)).thenReturn(Optional.empty());

        GeneralException exception = assertThrows(GeneralException.class, () -> submissionService.getOtherSubmission(groupProblemId, submissionId, username));

        assertFalse(exception.getErrorReasonHttpStatus().getIsSuccess());
        assertEquals(ErrorStatus._NOT_FOUND_SUBMISSION.getCode(), exception.getErrorReasonHttpStatus().getCode());
        assertEquals(ErrorStatus._NOT_FOUND_SUBMISSION.getMessage(), exception.getErrorReasonHttpStatus().getMessage());
        assertEquals(ErrorStatus._NOT_FOUND_SUBMISSION.getHttpStatus(), exception.getErrorReasonHttpStatus().getHttpStatus());

    }


}
