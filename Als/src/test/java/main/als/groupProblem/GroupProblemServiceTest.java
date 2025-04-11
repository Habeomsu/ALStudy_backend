package main.als.groupProblem;

import com.mysql.cj.log.Log;
import main.als.apiPayload.code.status.ErrorStatus;
import main.als.apiPayload.exception.GeneralException;
import main.als.group.entity.Group;
import main.als.group.entity.UserGroup;
import main.als.group.repository.GroupRepository;
import main.als.group.repository.UserGroupRepository;
import main.als.page.PostPagingDto;
import main.als.problem.converter.GroupProblemConverter;
import main.als.problem.dto.GroupProblemRequestDto;
import main.als.problem.dto.GroupProblemResponseDto;
import main.als.problem.entity.*;
import main.als.problem.repository.GroupProblemRepository;
import main.als.problem.repository.ProblemRepository;
import main.als.problem.repository.SubmissionRepository;
import main.als.problem.service.GroupProblemService;
import main.als.problem.service.GroupProblemServiceImpl;
import main.als.problem.util.SubmissionStatusDeterminer;
import main.als.user.entity.User;
import main.als.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.convert.DataSizeUnit;
import org.springframework.data.domain.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class GroupProblemServiceTest {

    @InjectMocks
    private GroupProblemServiceImpl groupProblemService;

    @Mock
    private GroupProblemRepository groupProblemRepository;

    @Mock
    private ProblemRepository problemRepository;

    @Mock
    private GroupRepository groupRepository;

    @Mock
    private UserGroupRepository userGroupRepository;

    @Mock
    private SubmissionRepository submissionRepository;

    @Mock
    private UserRepository userRepository;

    @Captor
    private ArgumentCaptor<GroupProblem> groupProblemCaptor;

    LocalDateTime now = LocalDateTime.now();


    @Test
    @DisplayName("createGroupProblem 테스트 - (성공)")
    public void createGroupProblemSuccessTest(){

        String username = "test";
        Long groupId = 1L;

        GroupProblemRequestDto.GroupProblemDto groupProblemDto = GroupProblemRequestDto.GroupProblemDto.builder()
                .problem_id(1L)
                .deadline(now.plusDays(1))
                .deductionAmount(BigDecimal.valueOf(1000))
                .build();

        Problem problem = Problem.builder()
                .id(1L)
                .title("test")
                .build();

        Group group = Group.builder()
                .id(groupId)
                .name("test")
                .leader(username)
                .groupProblems(new ArrayList<>())
                .build();

        when(problemRepository.findById(any())).thenReturn(Optional.of(problem));
        when(groupRepository.findById(any())).thenReturn(Optional.of(group));

        groupProblemService.createGroupProblem(groupProblemDto,username,groupId);

        verify(groupProblemRepository,times(1)).save(any(GroupProblem.class));

        assertEquals(1, group.getGroupProblems().size());
        assertEquals(problem.getId(), group.getGroupProblems().get(0).getProblem().getId());

    }

    @Test
    @DisplayName("createGroupProblem 테스트 - (실패 문제번호 못 찾음)")
    public void createGroupProblemFailTest1(){

        GroupProblemRequestDto.GroupProblemDto groupProblemDto = GroupProblemRequestDto.GroupProblemDto.builder()
                .problem_id(1L)
                .deadline(now.plusDays(1))
                .deductionAmount(BigDecimal.valueOf(1000))
                .build();

        String username = "test";
        Long groupId = 1L;

        when(problemRepository.findById(any())).thenReturn(Optional.empty());

        GeneralException exception = assertThrows(GeneralException.class, () -> groupProblemService.createGroupProblem(groupProblemDto,username,groupId));

        assertFalse(exception.getErrorReasonHttpStatus().getIsSuccess());
        assertEquals(ErrorStatus._NOT_FOUND_PROBLEM.getCode(), exception.getErrorReasonHttpStatus().getCode());
        assertEquals(ErrorStatus._NOT_FOUND_PROBLEM.getMessage(), exception.getErrorReasonHttpStatus().getMessage());
        assertEquals(ErrorStatus._NOT_FOUND_PROBLEM.getHttpStatus(), exception.getErrorReasonHttpStatus().getHttpStatus());

    }

    @Test
    @DisplayName("createGroupProblem 테스트 - (실패 - 그룹 못 찾음)")
    public void createGroupProblemFailTest2(){

        GroupProblemRequestDto.GroupProblemDto groupProblemDto = GroupProblemRequestDto.GroupProblemDto.builder()
                .problem_id(1L)
                .deadline(now.plusDays(1))
                .deductionAmount(BigDecimal.valueOf(1000))
                .build();

        Problem problem = Problem.builder()
                .id(1L)
                .title("test")
                .build();

        String username = "test";
        Long groupId = 1L;

        when(problemRepository.findById(any())).thenReturn(Optional.of(problem));
        when(groupRepository.findById(any())).thenReturn(Optional.empty());

        GeneralException exception = assertThrows(GeneralException.class, () -> groupProblemService.createGroupProblem(groupProblemDto,username,groupId));

        assertFalse(exception.getErrorReasonHttpStatus().getIsSuccess());
        assertEquals(ErrorStatus._NOT_FOUND_GROUP.getCode(), exception.getErrorReasonHttpStatus().getCode());
        assertEquals(ErrorStatus._NOT_FOUND_GROUP.getMessage(), exception.getErrorReasonHttpStatus().getMessage());
        assertEquals(ErrorStatus._NOT_FOUND_GROUP.getHttpStatus(), exception.getErrorReasonHttpStatus().getHttpStatus());

    }

    @Test
    @DisplayName("createGroupProblem 테스트 - (실패 - 리더 불 일치)")
    public void createGroupProblemFailTest3(){

        String username = "test";
        Long groupId = 1L;

        GroupProblemRequestDto.GroupProblemDto groupProblemDto = GroupProblemRequestDto.GroupProblemDto.builder()
                .problem_id(1L)
                .deadline(now.plusDays(1))
                .deductionAmount(BigDecimal.valueOf(1000))
                .build();

        Problem problem = Problem.builder()
                .id(1L)
                .title("test")
                .build();

        Group group = Group.builder()
                .id(groupId)
                .name("test")
                .leader("username")
                .groupProblems(new ArrayList<>())
                .build();

        when(problemRepository.findById(any())).thenReturn(Optional.of(problem));
        when(groupRepository.findById(any())).thenReturn(Optional.of(group));

        GeneralException exception = assertThrows(GeneralException.class, () -> groupProblemService.createGroupProblem(groupProblemDto,username,groupId));

        assertFalse(exception.getErrorReasonHttpStatus().getIsSuccess());
        assertEquals(ErrorStatus._NOT_MATCH_LEADER.getCode(), exception.getErrorReasonHttpStatus().getCode());
        assertEquals(ErrorStatus._NOT_MATCH_LEADER.getMessage(), exception.getErrorReasonHttpStatus().getMessage());
        assertEquals(ErrorStatus._NOT_MATCH_LEADER.getHttpStatus(), exception.getErrorReasonHttpStatus().getHttpStatus());

    }

    @Test
    @DisplayName("createGroupProblem 테스트 - (실패 - 그룹 문제 중복)")
    public void createGroupProblemFailTest4(){

        String username = "test";
        Long groupId = 1L;

        GroupProblemRequestDto.GroupProblemDto groupProblemDto = GroupProblemRequestDto.GroupProblemDto.builder()
                .problem_id(1L)
                .deadline(now.plusDays(1))
                .deductionAmount(BigDecimal.valueOf(1000))
                .build();

        Problem problem = Problem.builder()
                .id(1L)
                .title("test")
                .build();

        GroupProblem groupProblem = GroupProblem.builder()
                .problem(problem)
                .build();

        Group group = Group.builder()
                .id(groupId)
                .name("test")
                .leader("test")
                .groupProblems(new ArrayList<>())
                .build();

        group.getGroupProblems().add(groupProblem);

        when(problemRepository.findById(any())).thenReturn(Optional.of(problem));
        when(groupRepository.findById(any())).thenReturn(Optional.of(group));

        GeneralException exception = assertThrows(GeneralException.class, () -> groupProblemService.createGroupProblem(groupProblemDto,username,groupId));

        assertFalse(exception.getErrorReasonHttpStatus().getIsSuccess());
        assertEquals(ErrorStatus._DUPLICATE_GROUP_PROBLEM.getCode(), exception.getErrorReasonHttpStatus().getCode());
        assertEquals(ErrorStatus._DUPLICATE_GROUP_PROBLEM.getMessage(), exception.getErrorReasonHttpStatus().getMessage());
        assertEquals(ErrorStatus._DUPLICATE_GROUP_PROBLEM.getHttpStatus(), exception.getErrorReasonHttpStatus().getHttpStatus());

    }

    @Test
    @DisplayName("getGroupProblems 테스트 - (성공)")
    public void getGroupProblemsSuccessTest(){

        Long groupId = 1L;
        String username = "test";

        User user = User.builder()
                .id(1L)
                .username(username)
                .build();

        PostPagingDto.PagingDto pagingDto = PostPagingDto.PagingDto.builder()
                .page(1)
                .size(10)
                .sort("DESC")
                .build();

        Sort sort = Sort.by(Sort.Direction.fromString(pagingDto.getSort()),"createdAt");
        Pageable pageable = PageRequest.of(pagingDto.getPage(), pagingDto.getSize(), sort);

        GroupProblem groupProblem1 = GroupProblem.builder()
                .id(1L)
                .problem(new Problem())
                .build();

        Page<GroupProblem> groupProblems = new PageImpl<>(List.of(groupProblem1));

        Submission submission1 = Submission.builder()
                .id(1L)
                .groupProblem(groupProblem1)
                .user(user)
                .status(SubmissionStatus.FAILED)
                .build();

        Submission submission2 = Submission.builder()
                .id(2L)
                .groupProblem(groupProblem1)
                .user(user)
                .status(SubmissionStatus.SUCCEEDED)
                .build();

        GroupProblemResponseDto.AllGroupProblem allGroupProblem = GroupProblemResponseDto.AllGroupProblem.builder()
                .groupProblemId(groupProblem1.getId())
                .title("test")
                .difficultyLevel(null)
                .createdAt(null)
                .deadline(null)
                .deductionAmount(null)
                .status(SubmissionStatus.SUCCEEDED)
                .build();

        GroupProblemResponseDto.SearchGroupProblem mockedDto = GroupProblemResponseDto.SearchGroupProblem.builder()
                .groupProblemResDtos(new ArrayList<>())
                .isFirst(true)
                .isLast(false)
                .listSize(1)
                .totalElements(1)
                .build();
        mockedDto.getGroupProblemResDtos().add(allGroupProblem);

        List<Submission> submissions = List.of(submission1, submission2);

        Map<Long, SubmissionStatus> submissionStatusMap = new HashMap<>();
        submissionStatusMap.put(1L, SubmissionStatus.SUCCEEDED);


        when(groupRepository.existsById(eq(groupId))).thenReturn(true);
        when(groupProblemRepository.findByGroupId(eq(groupId),any(Pageable.class))).thenReturn(groupProblems);
        when(submissionRepository.findByUserUsername(username)).thenReturn(submissions);

        try(MockedStatic<GroupProblemConverter> mockedStatic = Mockito.mockStatic(GroupProblemConverter.class)){
            mockedStatic.when(()->GroupProblemConverter.toSearchGroupProblemDto(groupProblems,submissionStatusMap)).thenReturn(mockedDto);

            GroupProblemResponseDto.SearchGroupProblem result = groupProblemService.getGroupProblems(groupId,username,pagingDto);

            assertEquals(1, result.getGroupProblemResDtos().size());
            assertEquals(mockedDto, result);

        };

    }

    @Test
    @DisplayName("getGroupProblems 테스트 - (실패 - 그룹 없음)")
    public void getGroupProblemsFailTest1(){

        Long groupId = 1L;
        String username = "test";

        PostPagingDto.PagingDto pagingDto = PostPagingDto.PagingDto.builder()
                .page(1)
                .size(10)
                .sort("DESC")
                .build();

        when(groupRepository.existsById(groupId)).thenReturn(false);

        GeneralException exception = assertThrows(GeneralException.class, () -> groupProblemService.getGroupProblems(groupId, username, pagingDto));

        assertFalse(exception.getErrorReasonHttpStatus().getIsSuccess());
        assertEquals(ErrorStatus._NOT_FOUND_GROUP.getCode(), exception.getErrorReasonHttpStatus().getCode());
        assertEquals(ErrorStatus._NOT_FOUND_GROUP.getMessage(), exception.getErrorReasonHttpStatus().getMessage());
        assertEquals(ErrorStatus._NOT_FOUND_GROUP.getHttpStatus(), exception.getErrorReasonHttpStatus().getHttpStatus());

    }

    @Test
    @DisplayName("getTodayGroupProblems 테스트 - (성공)")
    public void getTodayGroupProblemsSuccessTest(){

        Long groupId = 1L;
        String username = "test";

        PostPagingDto.PagingDto pagingDto = PostPagingDto.PagingDto.builder()
                .page(1)
                .size(10)
                .sort("DESC")
                .build();

        User user = User.builder()
                .id(1L)
                .username(username)
                .build();

        LocalDateTime now = LocalDateTime.now();

        GroupProblem groupProblem1 = GroupProblem.builder()
                .id(1L)
                .problem(new Problem())
                .build();

        Page<GroupProblem> groupProblems = new PageImpl<>(List.of(groupProblem1));

        Submission submission1 = Submission.builder()
                .id(1L)
                .groupProblem(groupProblem1)
                .user(user)
//                .status(SubmissionStatus.FAILED)
                .build();

        Submission submission2 = Submission.builder()
                .id(2L)
                .groupProblem(groupProblem1)
                .user(user)
//                .status(SubmissionStatus.SUCCEEDED)
                .build();

        List<Submission> submissions = List.of(submission1, submission2);

        Map<Long, SubmissionStatus> submissionStatusMap = new HashMap<>();
        submissionStatusMap.put(1L, SubmissionStatus.PENDING);

        GroupProblemResponseDto.AllGroupProblem allGroupProblem = GroupProblemResponseDto.AllGroupProblem.builder()
                .groupProblemId(groupProblem1.getId())
                .title("test")
                .difficultyLevel(null)
                .createdAt(null)
                .deadline(null)
                .deductionAmount(null)
                .status(SubmissionStatus.PENDING)
                .build();

        GroupProblemResponseDto.SearchGroupProblem mockedDto = GroupProblemResponseDto.SearchGroupProblem.builder()
                .groupProblemResDtos(new ArrayList<>())
                .isFirst(true)
                .isLast(false)
                .listSize(1)
                .totalElements(1)
                .build();

        when(groupRepository.existsById(groupId)).thenReturn(true);
        when(groupProblemRepository.findByGroupIdAndDeadlineGreaterThanEqual(eq(groupId),any(LocalDateTime.class),any(Pageable.class))).thenReturn(groupProblems);
        when(submissionRepository.findByUserUsername(username)).thenReturn(submissions);

        try(MockedStatic<GroupProblemConverter> mockedStatic = Mockito.mockStatic(GroupProblemConverter.class)){
            mockedStatic.when(()->GroupProblemConverter.toSearchGroupProblemDto(groupProblems,submissionStatusMap)).thenReturn(mockedDto);

            GroupProblemResponseDto.SearchGroupProblem result = groupProblemService.getTodayGroupProblems(groupId,username,pagingDto);


            assertEquals(mockedDto, result);

        };

    }

    @Test
    @DisplayName("getTodayGroupProblems 테스트 - (실패 - 그룹 없음)")
    public void getTodayGroupProblemsFailTest1(){

        Long groupId = 1L;
        String username = "test";

        PostPagingDto.PagingDto pagingDto = PostPagingDto.PagingDto.builder()
                .page(1)
                .size(10)
                .sort("DESC")
                .build();

        when(groupRepository.existsById(groupId)).thenReturn(false);

        GeneralException exception = assertThrows(GeneralException.class, () -> groupProblemService.getGroupProblems(groupId, username, pagingDto));

        assertFalse(exception.getErrorReasonHttpStatus().getIsSuccess());
        assertEquals(ErrorStatus._NOT_FOUND_GROUP.getCode(), exception.getErrorReasonHttpStatus().getCode());
        assertEquals(ErrorStatus._NOT_FOUND_GROUP.getMessage(), exception.getErrorReasonHttpStatus().getMessage());
        assertEquals(ErrorStatus._NOT_FOUND_GROUP.getHttpStatus(), exception.getErrorReasonHttpStatus().getHttpStatus());

    }

    @Test
    @DisplayName("getDetailGroupProblem 테스트 - (성공)")
    public void getDetailGroupProblemSuccessTest(){

        Long groupId = 1L;
        Long groupProblemId = 1L;
        String username = "test";

        Group group = Group.builder()
                .id(groupId)
                .build();

        GroupProblem groupProblem = GroupProblem.builder()
                .id(groupProblemId)
                .problem(new Problem())
                .group(group)
                .build();

        User user = User.builder()
                .id(1L)
                .username(username)
                .build();

        Submission submission1 = Submission.builder()
                .id(1L)
                .groupProblem(groupProblem)
                .build();

        Submission submission2 = Submission.builder()
                .id(1L)
                .groupProblem(groupProblem)
                .build();

        List<Submission> submissions = List.of(submission1, submission2);

        SubmissionStatus finalStatus = SubmissionStatus.PENDING;

        GroupProblemResponseDto.DetailGroupProblem mockedDto = GroupProblemResponseDto.DetailGroupProblem.builder()
                .groupProblemId(groupProblemId)
                .problemId(1L)
                .status(finalStatus)
                .build();

        when(groupProblemRepository.findById(groupProblemId)).thenReturn(Optional.of(groupProblem));
        when(userGroupRepository.existsByGroupIdAndUserUsername(groupId,username)).thenReturn(true);
        when(userRepository.findByUsername(username)).thenReturn(user);
        when(submissionRepository.findByUserAndGroupProblem(user,groupProblem)).thenReturn(submissions);

        try (MockedStatic<GroupProblemConverter> converterMockedStatic = Mockito.mockStatic(GroupProblemConverter.class);
             MockedStatic<SubmissionStatusDeterminer> statusMockedStatic = Mockito.mockStatic(SubmissionStatusDeterminer.class)) {

            statusMockedStatic.when(() ->
                    SubmissionStatusDeterminer.determineFinalSubmissionStatus(any())
            ).thenReturn(finalStatus);

            converterMockedStatic.when(() ->
                    GroupProblemConverter.toDetailGroupProblem(any(), any())
            ).thenReturn(mockedDto);

            GroupProblemResponseDto.DetailGroupProblem result =
                    groupProblemService.getDetailGroupProblem(groupId, groupProblemId, username);

            assertEquals(mockedDto, result);
        }

    }

    @Test
    @DisplayName("getDetailGroupProblem 테스트 - (실패 - 그룹문제 못찾음)")
    public void getDetailGroupProblemFailTest1(){

        Long groupId = 1L;
        Long groupProblemId = 1L;
        String username = "test";

        when(groupProblemRepository.findById(groupProblemId)).thenReturn(Optional.empty());

        GeneralException exception = assertThrows(GeneralException.class, () -> groupProblemService.getDetailGroupProblem(groupId, groupProblemId, username));

        assertFalse(exception.getErrorReasonHttpStatus().getIsSuccess());
        assertEquals(ErrorStatus._NOT_FOUND_GROUPPROBLEM.getCode(), exception.getErrorReasonHttpStatus().getCode());
        assertEquals(ErrorStatus._NOT_FOUND_GROUPPROBLEM.getMessage(), exception.getErrorReasonHttpStatus().getMessage());
        assertEquals(ErrorStatus._NOT_FOUND_GROUPPROBLEM.getHttpStatus(), exception.getErrorReasonHttpStatus().getHttpStatus());

    }

    @Test
    @DisplayName("getDetailGroupProblem 테스트 - (실패 - 그룹아이디 불일치)")
    public void getDetailGroupProblemFailTest2(){

        Long groupId = 1L;
        Long groupProblemId = 1L;
        String username = "test";

        Group group = Group.builder()
                .id(2L)
                .build();

        GroupProblem groupProblem = GroupProblem.builder()
                .id(groupProblemId)
                .problem(new Problem())
                .group(group)
                .build();

        when(groupProblemRepository.findById(groupProblemId)).thenReturn(Optional.of(groupProblem));

        GeneralException exception = assertThrows(GeneralException.class, () -> groupProblemService.getDetailGroupProblem(groupId, groupProblemId, username));

        assertFalse(exception.getErrorReasonHttpStatus().getIsSuccess());
        assertEquals(ErrorStatus._NOT_FOUND_GROUP.getCode(), exception.getErrorReasonHttpStatus().getCode());
        assertEquals(ErrorStatus._NOT_FOUND_GROUP.getMessage(), exception.getErrorReasonHttpStatus().getMessage());
        assertEquals(ErrorStatus._NOT_FOUND_GROUP.getHttpStatus(), exception.getErrorReasonHttpStatus().getHttpStatus());

    }

    @Test
    @DisplayName("getDetailGroupProblem 테스트 - (실패 - 그룹에 없는 사용자)")
    public void getDetailGroupProblemFailTest3(){

        Long groupId = 1L;
        Long groupProblemId = 1L;
        String username = "test";

        Group group = Group.builder()
                .id(groupId)
                .build();

        GroupProblem groupProblem = GroupProblem.builder()
                .id(groupProblemId)
                .problem(new Problem())
                .group(group)
                .build();

        when(groupProblemRepository.findById(groupProblemId)).thenReturn(Optional.of(groupProblem));
        when(userGroupRepository.existsByGroupIdAndUserUsername(groupId,username)).thenReturn(false);

        GeneralException exception = assertThrows(GeneralException.class, () -> groupProblemService.getDetailGroupProblem(groupId, groupProblemId, username));

        assertFalse(exception.getErrorReasonHttpStatus().getIsSuccess());
        assertEquals(ErrorStatus._NOT_IN_USERGROUP.getCode(), exception.getErrorReasonHttpStatus().getCode());
        assertEquals(ErrorStatus._NOT_IN_USERGROUP.getMessage(), exception.getErrorReasonHttpStatus().getMessage());
        assertEquals(ErrorStatus._NOT_IN_USERGROUP.getHttpStatus(), exception.getErrorReasonHttpStatus().getHttpStatus());

    }

    @Test
    @DisplayName("getDetailGroupProblem 테스트 - (실패 - 사용자 없음)")
    public void getDetailGroupProblemFailTest4(){

        Long groupId = 1L;
        Long groupProblemId = 1L;
        String username = "test";

        Group group = Group.builder()
                .id(groupId)
                .build();

        GroupProblem groupProblem = GroupProblem.builder()
                .id(groupProblemId)
                .problem(new Problem())
                .group(group)
                .build();

        when(groupProblemRepository.findById(groupProblemId)).thenReturn(Optional.of(groupProblem));
        when(userGroupRepository.existsByGroupIdAndUserUsername(groupId,username)).thenReturn(true);
        when(userRepository.findByUsername(username)).thenReturn(null);


        GeneralException exception = assertThrows(GeneralException.class, () -> groupProblemService.getDetailGroupProblem(groupId, groupProblemId, username));

        assertFalse(exception.getErrorReasonHttpStatus().getIsSuccess());
        assertEquals(ErrorStatus._USERNAME_NOT_FOUND.getCode(), exception.getErrorReasonHttpStatus().getCode());
        assertEquals(ErrorStatus._USERNAME_NOT_FOUND.getMessage(), exception.getErrorReasonHttpStatus().getMessage());
        assertEquals(ErrorStatus._USERNAME_NOT_FOUND.getHttpStatus(), exception.getErrorReasonHttpStatus().getHttpStatus());

    }

    @Test
    @DisplayName("checkDeadlines 테스트 - (성공 - 마감일 초과, 제출 x)")
    public void checkDeadlinesSuccessTest1(){

        LocalDateTime now = LocalDateTime.now();

        User user = User.builder()
                .id(1L)
                .build();

        Submission submission1 = Submission.builder()
                .user(user)
                .build();

        Group group = Group.builder()
                .id(1L)
                .build();

        GroupProblem groupProblem = GroupProblem.builder()
                .id(1L)
                .deadline(now.minusDays(1))
                .deduct(Deduct.FALSE)
//                .submissions(List.of(submission1))
                .group(group)
                .deductionAmount(BigDecimal.valueOf(1000))
                .build();

        UserGroup userGroup = UserGroup.builder()
                .id(1L)
                .group(group)
                .user(user)
                .userDepositAmount(BigDecimal.valueOf(1500))
                .build();

        when(groupProblemRepository.findAll()).thenReturn(List.of(groupProblem));
        when(userGroupRepository.findByGroupId(any())).thenReturn(List.of(userGroup));

        groupProblemService.checkDeadlines();

        verify(userGroupRepository,times(1)).save(userGroup);
        verify(groupProblemRepository,times(1)).save(groupProblem);

        assertEquals(Deduct.TRUE, groupProblem.getDeduct());
        assertEquals(BigDecimal.valueOf(500), userGroup.getUserDepositAmount());

    }
    @Test
    @DisplayName("checkDeadlines 테스트 - (성공 - 마감일 안지난 경우)")
    public void checkDeadlinesSuccessTest2(){

        LocalDateTime now = LocalDateTime.now();

        User user = User.builder()
                .id(1L)
                .build();

        Submission submission1 = Submission.builder()
                .user(user)
                .build();

        Group group = Group.builder()
                .id(1L)
                .build();

        GroupProblem groupProblem = GroupProblem.builder()
                .id(1L)
                .deadline(now.plusDays(2))
                .deduct(Deduct.FALSE)
//                .submissions(List.of(submission1))
                .group(group)
                .deductionAmount(BigDecimal.valueOf(1000))
                .build();

        UserGroup userGroup = UserGroup.builder()
                .id(1L)
                .group(group)
                .user(user)
                .userDepositAmount(BigDecimal.valueOf(1500))
                .build();

        when(groupProblemRepository.findAll()).thenReturn(List.of(groupProblem));

        groupProblemService.checkDeadlines();

        assertEquals(Deduct.FALSE, groupProblem.getDeduct());
        assertEquals(BigDecimal.valueOf(1500), userGroup.getUserDepositAmount());

    }

    @Test
    @DisplayName("checkDeadlines 테스트 - (성공 - 마감일 초과, 차감 O)")
    public void checkDeadlinesSuccessTest3(){

        LocalDateTime now = LocalDateTime.now();

        User user = User.builder()
                .id(1L)
                .build();

        Submission submission1 = Submission.builder()
                .user(user)
                .build();

        Group group = Group.builder()
                .id(1L)
                .build();

        GroupProblem groupProblem = GroupProblem.builder()
                .id(1L)
                .deadline(now.minusDays(1))
                .deduct(Deduct.TRUE)
//                .submissions(List.of(submission1))
                .group(group)
                .deductionAmount(BigDecimal.valueOf(1000))
                .build();

        UserGroup userGroup = UserGroup.builder()
                .id(1L)
                .group(group)
                .user(user)
                .userDepositAmount(BigDecimal.valueOf(1500))
                .build();

        when(groupProblemRepository.findAll()).thenReturn(List.of(groupProblem));

        groupProblemService.checkDeadlines();

        assertEquals(Deduct.TRUE, groupProblem.getDeduct());
        assertEquals(BigDecimal.valueOf(1500), userGroup.getUserDepositAmount());

    }

    @Test
    @DisplayName("checkDeadlines 테스트 - (성공 - 마감일 초과, 실패)")
    public void checkDeadlinesSuccessTest4(){

        LocalDateTime now = LocalDateTime.now();

        User user = User.builder()
                .id(1L)
                .build();

        Submission submission1 = Submission.builder()
                .user(user)
                .status(SubmissionStatus.FAILED)
                .build();

        Group group = Group.builder()
                .id(1L)
                .build();

        GroupProblem groupProblem = GroupProblem.builder()
                .id(1L)
                .deadline(now.minusDays(1))
                .deduct(Deduct.FALSE)
                .submissions(List.of(submission1))
                .group(group)
                .deductionAmount(BigDecimal.valueOf(1000))
                .build();

        UserGroup userGroup = UserGroup.builder()
                .id(1L)
                .group(group)
                .user(user)
                .userDepositAmount(BigDecimal.valueOf(1500))
                .build();

        when(groupProblemRepository.findAll()).thenReturn(List.of(groupProblem));
        when(userGroupRepository.findByGroupId(any())).thenReturn(List.of(userGroup));

        groupProblemService.checkDeadlines();

        verify(userGroupRepository,times(1)).save(userGroup);
        verify(groupProblemRepository,times(1)).save(groupProblem);

        assertEquals(Deduct.TRUE, groupProblem.getDeduct());
        assertEquals(BigDecimal.valueOf(500), userGroup.getUserDepositAmount());

    }

    @Test
    @DisplayName("checkDeadlines 테스트 - (성공 - 마감일 초과, 성공)")
    public void checkDeadlinesSuccessTest5(){

        LocalDateTime now = LocalDateTime.now();

        User user = User.builder()
                .id(1L)
                .build();

        Submission submission1 = Submission.builder()
                .user(user)
                .status(SubmissionStatus.SUCCEEDED)
                .build();

        Group group = Group.builder()
                .id(1L)
                .build();

        GroupProblem groupProblem = GroupProblem.builder()
                .id(1L)
                .deadline(now.minusDays(1))
                .deduct(Deduct.FALSE)
                .submissions(List.of(submission1))
                .group(group)
                .deductionAmount(BigDecimal.valueOf(1000))
                .build();

        UserGroup userGroup = UserGroup.builder()
                .id(1L)
                .group(group)
                .user(user)
                .userDepositAmount(BigDecimal.valueOf(1500))
                .build();

        when(groupProblemRepository.findAll()).thenReturn(List.of(groupProblem));
        when(userGroupRepository.findByGroupId(any())).thenReturn(List.of(userGroup));

        groupProblemService.checkDeadlines();

        verify(groupProblemRepository,times(1)).save(groupProblem);

        assertEquals(Deduct.TRUE, groupProblem.getDeduct());
        assertEquals(BigDecimal.valueOf(1500), userGroup.getUserDepositAmount());

    }

    @Test
    @DisplayName("checkDeadlines 테스트 - (성공 - 마감일 초과, 남은 금액이 차감액 보다 적은 경우)")
    public void checkDeadlinesSuccessTest6(){

        LocalDateTime now = LocalDateTime.now();

        User user = User.builder()
                .id(1L)
                .build();

        Submission submission1 = Submission.builder()
                .user(user)
                .build();

        Group group = Group.builder()
                .id(1L)
                .build();

        GroupProblem groupProblem = GroupProblem.builder()
                .id(1L)
                .deadline(now.minusDays(1))
                .deduct(Deduct.FALSE)
//                .submissions(List.of(submission1))
                .group(group)
                .deductionAmount(BigDecimal.valueOf(1000))
                .build();

        UserGroup userGroup = UserGroup.builder()
                .id(1L)
                .group(group)
                .user(user)
                .userDepositAmount(BigDecimal.valueOf(500))
                .build();

        when(groupProblemRepository.findAll()).thenReturn(List.of(groupProblem));
        when(userGroupRepository.findByGroupId(any())).thenReturn(List.of(userGroup));

        groupProblemService.checkDeadlines();

        verify(userGroupRepository,times(1)).save(userGroup);
        verify(groupProblemRepository,times(1)).save(groupProblem);

        assertEquals(Deduct.TRUE, groupProblem.getDeduct());
        assertEquals(BigDecimal.valueOf(0), userGroup.getUserDepositAmount());

    }


    @Test
    @DisplayName("deleteGroupProblem 테스트 - (성공)")
    public void deleteGroupProblemSuccessTest(){

        Long groupId = 1L;
        Long groupProblemId = 1L;
        String username = "test";

        Group group = Group.builder()
                .id(groupId)
                .leader(username)
                .build();

        GroupProblem groupProblem = GroupProblem.builder()
                .id(groupProblemId)
                .problem(new Problem())
                .group(group)
                .build();

        when(groupProblemRepository.findById(groupProblemId)).thenReturn(Optional.of(groupProblem));

        groupProblemService.deleteGroupProblem(groupId, groupProblemId, username);

        verify(groupProblemRepository, times(1)).delete(groupProblem);

    }

    @Test
    @DisplayName("deleteGroupProblem 테스트 - (실패 - 그룹 문제 못 찾음)")
    public void deleteGroupProblemFailTest1(){

        Long groupId = 1L;
        Long groupProblemId = 1L;
        String username = "test";

        when(groupProblemRepository.findById(groupProblemId)).thenReturn(Optional.empty());

        GeneralException exception = assertThrows(GeneralException.class, () -> groupProblemService.deleteGroupProblem(groupId, groupProblemId, username));

        assertFalse(exception.getErrorReasonHttpStatus().getIsSuccess());
        assertEquals(ErrorStatus._NOT_FOUND_GROUPPROBLEM.getCode(), exception.getErrorReasonHttpStatus().getCode());
        assertEquals(ErrorStatus._NOT_FOUND_GROUPPROBLEM.getMessage(), exception.getErrorReasonHttpStatus().getMessage());
        assertEquals(ErrorStatus._NOT_FOUND_GROUPPROBLEM.getHttpStatus(), exception.getErrorReasonHttpStatus().getHttpStatus());

    }

    @Test
    @DisplayName("deleteGroupProblem 테스트 - (실패 - 그룹 불 일치)")
    public void deleteGroupProblemFailTest2(){

        Long groupId = 1L;
        Long groupProblemId = 1L;
        String username = "test";

        Group group = Group.builder()
                .id(2L)
                .leader(username)
                .build();

        GroupProblem groupProblem = GroupProblem.builder()
                .id(groupProblemId)
                .problem(new Problem())
                .group(group)
                .build();

        when(groupProblemRepository.findById(groupProblemId)).thenReturn(Optional.of(groupProblem));

        GeneralException exception = assertThrows(GeneralException.class, () -> groupProblemService.deleteGroupProblem(groupId, groupProblemId, username));

        assertFalse(exception.getErrorReasonHttpStatus().getIsSuccess());
        assertEquals(ErrorStatus._NOT_MATCH_GROUP.getCode(), exception.getErrorReasonHttpStatus().getCode());
        assertEquals(ErrorStatus._NOT_MATCH_GROUP.getMessage(), exception.getErrorReasonHttpStatus().getMessage());
        assertEquals(ErrorStatus._NOT_MATCH_GROUP.getHttpStatus(), exception.getErrorReasonHttpStatus().getHttpStatus());

    }

    @Test
    @DisplayName("deleteGroupProblem 테스트 - (실패 - 리더 불 일치)")
    public void deleteGroupProblemFailTest3(){

        Long groupId = 1L;
        Long groupProblemId = 1L;
        String username = "test";

        Group group = Group.builder()
                .id(1L)
                .leader("notTest")
                .build();

        GroupProblem groupProblem = GroupProblem.builder()
                .id(groupProblemId)
                .problem(new Problem())
                .group(group)
                .build();

        when(groupProblemRepository.findById(groupProblemId)).thenReturn(Optional.of(groupProblem));

        GeneralException exception = assertThrows(GeneralException.class, () -> groupProblemService.deleteGroupProblem(groupId, groupProblemId, username));

        assertFalse(exception.getErrorReasonHttpStatus().getIsSuccess());
        assertEquals(ErrorStatus._NOT_MATCH_LEADER.getCode(), exception.getErrorReasonHttpStatus().getCode());
        assertEquals(ErrorStatus._NOT_MATCH_LEADER.getMessage(), exception.getErrorReasonHttpStatus().getMessage());
        assertEquals(ErrorStatus._NOT_MATCH_LEADER.getHttpStatus(), exception.getErrorReasonHttpStatus().getHttpStatus());

    }

    @Test
    @DisplayName("updateGroupProblem 테스트 - (성공) ")
    public void updateGroupProblemSuccessTest(){
        Long groupId = 1L;
        Long groupProblemId = 1L;
        String username = "test";
        LocalDateTime now = LocalDateTime.now();

        GroupProblemRequestDto.UpdateGroupProblemDto updateGroupProblemDto = GroupProblemRequestDto.UpdateGroupProblemDto.builder()
                .deadline(now.plusDays(1))
                .deductionAmount(BigDecimal.valueOf(1000))
                .build();

        Group group = Group.builder()
                .id(groupId)
                .leader(username)
                .build();

        GroupProblem groupProblem = GroupProblem.builder()
                .id(groupProblemId)
                .problem(new Problem())
                .group(group)
                .deadline(now.plusDays(2))
                .build();

        when(groupProblemRepository.findById(groupProblemId)).thenReturn(Optional.of(groupProblem));

        groupProblemService.updateGroupProblem(updateGroupProblemDto, groupId, groupProblemId, username);

        verify(groupProblemRepository, times(1)).save(groupProblem);

        assertEquals(now.plusDays(1), groupProblem.getDeadline());
        assertEquals(BigDecimal.valueOf(1000), groupProblem.getDeductionAmount());

    }

    @Test
    @DisplayName("updateGroupProblem 테스트 - (실패 - 그룹 문제 못 찾음) ")
    public void updateGroupProblemFailTest1(){

        Long groupId = 1L;
        Long groupProblemId = 1L;
        String username = "test";
        LocalDateTime now = LocalDateTime.now();

        GroupProblemRequestDto.UpdateGroupProblemDto updateGroupProblemDto = GroupProblemRequestDto.UpdateGroupProblemDto.builder()
                .deadline(now.plusDays(1))
                .deductionAmount(BigDecimal.valueOf(1000))
                .build();

        when(groupProblemRepository.findById(groupProblemId)).thenReturn(Optional.empty());

        GeneralException exception = assertThrows(GeneralException.class,()-> groupProblemService.updateGroupProblem(updateGroupProblemDto, groupId, groupProblemId, username));

        assertFalse(exception.getErrorReasonHttpStatus().getIsSuccess());
        assertEquals(ErrorStatus._NOT_FOUND_GROUPPROBLEM.getCode(), exception.getErrorReasonHttpStatus().getCode());
        assertEquals(ErrorStatus._NOT_FOUND_GROUPPROBLEM.getMessage(), exception.getErrorReasonHttpStatus().getMessage());
        assertEquals(ErrorStatus._NOT_FOUND_GROUPPROBLEM.getHttpStatus(), exception.getErrorReasonHttpStatus().getHttpStatus());

    }

    @Test
    @DisplayName("updateGroupProblem 테스트 - (실패 - 그룹 불 일치) ")
    public void updateGroupProblemFailTest2(){

        Long groupId = 1L;
        Long groupProblemId = 1L;
        String username = "test";
        LocalDateTime now = LocalDateTime.now();

        GroupProblemRequestDto.UpdateGroupProblemDto updateGroupProblemDto = GroupProblemRequestDto.UpdateGroupProblemDto.builder()
                .deadline(now.plusDays(1))
                .deductionAmount(BigDecimal.valueOf(1000))
                .build();

        Group group = Group.builder()
                .id(2L)
                .leader(username)
                .build();

        GroupProblem groupProblem = GroupProblem.builder()
                .id(groupProblemId)
                .problem(new Problem())
                .group(group)
                .deadline(now.plusDays(2))
                .build();

        when(groupProblemRepository.findById(groupProblemId)).thenReturn(Optional.of(groupProblem));

        GeneralException exception = assertThrows(GeneralException.class,()-> groupProblemService.updateGroupProblem(updateGroupProblemDto, groupId, groupProblemId, username));

        assertFalse(exception.getErrorReasonHttpStatus().getIsSuccess());
        assertEquals(ErrorStatus._NOT_MATCH_GROUP.getCode(), exception.getErrorReasonHttpStatus().getCode());
        assertEquals(ErrorStatus._NOT_MATCH_GROUP.getMessage(), exception.getErrorReasonHttpStatus().getMessage());
        assertEquals(ErrorStatus._NOT_MATCH_GROUP.getHttpStatus(), exception.getErrorReasonHttpStatus().getHttpStatus());

    }

    @Test
    @DisplayName("updateGroupProblem 테스트 - (실패 - 리더 불 일치) ")
    public void updateGroupProblemFailTest3(){

        Long groupId = 1L;
        Long groupProblemId = 1L;
        String username = "test";
        LocalDateTime now = LocalDateTime.now();

        GroupProblemRequestDto.UpdateGroupProblemDto updateGroupProblemDto = GroupProblemRequestDto.UpdateGroupProblemDto.builder()
                .deadline(now.plusDays(1))
                .deductionAmount(BigDecimal.valueOf(1000))
                .build();

        Group group = Group.builder()
                .id(groupId)
                .leader("notTest")
                .build();

        GroupProblem groupProblem = GroupProblem.builder()
                .id(groupProblemId)
                .problem(new Problem())
                .group(group)
                .deadline(now.plusDays(2))
                .build();

        when(groupProblemRepository.findById(groupProblemId)).thenReturn(Optional.of(groupProblem));

        GeneralException exception = assertThrows(GeneralException.class,()-> groupProblemService.updateGroupProblem(updateGroupProblemDto, groupId, groupProblemId, username));

        assertFalse(exception.getErrorReasonHttpStatus().getIsSuccess());
        assertEquals(ErrorStatus._NOT_MATCH_LEADER.getCode(), exception.getErrorReasonHttpStatus().getCode());
        assertEquals(ErrorStatus._NOT_MATCH_LEADER.getMessage(), exception.getErrorReasonHttpStatus().getMessage());
        assertEquals(ErrorStatus._NOT_MATCH_LEADER.getHttpStatus(), exception.getErrorReasonHttpStatus().getHttpStatus());

    }

    @Test
    @DisplayName("updateGroupProblem 테스트 - (실패 - 마감일 지남) ")
    public void updateGroupProblemFailTest4(){

        Long groupId = 1L;
        Long groupProblemId = 1L;
        String username = "test";
        LocalDateTime now = LocalDateTime.now();

        GroupProblemRequestDto.UpdateGroupProblemDto updateGroupProblemDto = GroupProblemRequestDto.UpdateGroupProblemDto.builder()
                .deadline(now.plusDays(1))
                .deductionAmount(BigDecimal.valueOf(1000))
                .build();

        Group group = Group.builder()
                .id(groupId)
                .leader(username)
                .build();

        GroupProblem groupProblem = GroupProblem.builder()
                .id(groupProblemId)
                .problem(new Problem())
                .group(group)
                .deadline(now.minusDays(2))
                .build();

        when(groupProblemRepository.findById(groupProblemId)).thenReturn(Optional.of(groupProblem));

        GeneralException exception = assertThrows(GeneralException.class,()-> groupProblemService.updateGroupProblem(updateGroupProblemDto, groupId, groupProblemId, username));

        assertFalse(exception.getErrorReasonHttpStatus().getIsSuccess());
        assertEquals(ErrorStatus._DEADLINE_EXPIRED.getCode(), exception.getErrorReasonHttpStatus().getCode());
        assertEquals(ErrorStatus._DEADLINE_EXPIRED.getMessage(), exception.getErrorReasonHttpStatus().getMessage());
        assertEquals(ErrorStatus._DEADLINE_EXPIRED.getHttpStatus(), exception.getErrorReasonHttpStatus().getHttpStatus());

    }

}
