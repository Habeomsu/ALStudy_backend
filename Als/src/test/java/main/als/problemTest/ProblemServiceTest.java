package main.als.problemTest;

import main.als.apiPayload.code.status.ErrorStatus;
import main.als.apiPayload.exception.GeneralException;
import main.als.page.PostPagingDto;
import main.als.problem.converter.ProblemConverter;
import main.als.problem.dto.ProblemRequestDto;
import main.als.problem.dto.ProblemResponseDto;
import main.als.problem.entity.Problem;
import main.als.problem.entity.ProblemType;
import main.als.problem.entity.TestCase;
import main.als.problem.repository.ProblemRepository;
import main.als.problem.repository.TestCaseRepository;
import main.als.problem.service.ProblemServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProblemServiceTest {

    @InjectMocks
    private ProblemServiceImpl problemService;

    @Mock
    private ProblemRepository problemRepository;

    @Mock
    private TestCaseRepository testCaseRepository;

    @Captor
    private ArgumentCaptor<Problem> problemCaptor;

    @Test
    @DisplayName("createProblem 테스트 - (성공)")
    public void createProblemSuccessTest(){

        ProblemRequestDto.createProblemDto problemDto = ProblemRequestDto.createProblemDto.builder()
                .title("title1")
                .difficultyLevel("easy")
                .problemType(ProblemType.GREEDY)
                .description("description1")
                .inputDescription("inputDescription1")
                .outputDescription("outputDescription1")
                .exampleInput("exampleInput1")
                .exampleOutput("exampleOutput1")
                .build();

        Problem problem = Problem.builder()
                .id(1L)
                .title("title1")
                .difficultyLevel("easy")
                .problemType(ProblemType.GREEDY)
                .description("description1")
                .inputDescription("inputDescription1")
                .outputDescription("outputDescription1")
                .exampleInput("exampleInput1")
                .exampleOutput("exampleOutput1")
                .build();

        try (MockedStatic<ProblemConverter> mockedStatic = mockStatic(ProblemConverter.class)){

            mockedStatic.when(()->ProblemConverter.toProblem(problemDto)).thenReturn(problem);

            problemService.createProblem(problemDto);

            // captor를 사용해서 testcase 저장 확인
            verify(problemRepository, times(1)).save(problemCaptor.capture());

            Problem saved = problemCaptor.getValue();
            assertEquals(1, saved.getTestCases().size());

        }

    }

    @Test
    @DisplayName("createProblem 테스트 - (실패 문제 생성 실패)")
    public void createProblemFailTest(){

        ProblemRequestDto.createProblemDto problemDto = ProblemRequestDto.createProblemDto.builder()
                .title("title1")
                .difficultyLevel("easy")
                .problemType(ProblemType.GREEDY)
                .description("description1")
                .inputDescription("inputDescription1")
                .outputDescription("outputDescription1")
                .exampleInput("exampleInput1")
                .exampleOutput("exampleOutput1")
                .build();

        try (MockedStatic<ProblemConverter> mockedStatic = mockStatic(ProblemConverter.class)) {
            mockedStatic.when(() -> ProblemConverter.toProblem(problemDto))
                    .thenThrow(new RuntimeException("변환 실패"));

            GeneralException exception = assertThrows(GeneralException.class, () -> {
                problemService.createProblem(problemDto);
            });

            assertEquals(false,exception.getErrorReasonHttpStatus().getIsSuccess());
            assertEquals("PROBLEM400_1",exception.getErrorReasonHttpStatus().getCode());
            assertEquals("문제 생성에 실패하였습니다.",exception.getErrorReasonHttpStatus().getMessage());
            assertEquals(HttpStatus.BAD_REQUEST,exception.getErrorReasonHttpStatus().getHttpStatus());

        }
    }

    @Test
    @DisplayName("updateProblem 테스트 - (성공 테스트케이스 존재 x)")
    public void updateProblemSuceessTest1(){

        ProblemRequestDto.createProblemDto problemDto = ProblemRequestDto.createProblemDto.builder()
                .title("title2")
                .difficultyLevel("easy")
                .problemType(ProblemType.GREEDY)
                .description("description2")
                .inputDescription("inputDescription2")
                .outputDescription("outputDescription2")
                .exampleInput("exampleInput2")
                .exampleOutput("exampleOutput2")
                .build();

        Problem problem = Problem.builder()
                .id(1L)
                .title("title1")
                .difficultyLevel("easy")
                .problemType(ProblemType.GREEDY)
                .description("description1")
                .inputDescription("inputDescription1")
                .outputDescription("outputDescription1")
                .exampleInput("exampleInput1")
                .exampleOutput("exampleOutput1")
                .testCases(List.of(TestCase.builder()
                        .input("old input")
                        .expectedOutput("old output")
                        .build()))
                .build();

        Long problemId = 1L;

        when(problemRepository.findById(problemId)).thenReturn(Optional.ofNullable(problem));

        problemService.updateProblem(problemDto, problemId);

        verify(problemRepository, times(1)).save(problemCaptor.capture());

        Problem saved = problemCaptor.getValue();
        assertEquals("title2",saved.getTitle());
        assertEquals(1, saved.getTestCases().size());
        TestCase testCase = saved.getTestCases().get(0);
        assertEquals("exampleInput2", testCase.getInput());
        assertEquals("exampleOutput2", testCase.getExpectedOutput());

    }

    @Test
    @DisplayName("updateProblem 테스트 - (성공 테스트케이스 존재 O)")
    public void updateProblemSuceessTest2(){

        ProblemRequestDto.createProblemDto problemDto = ProblemRequestDto.createProblemDto.builder()
                .title("title2")
                .difficultyLevel("easy")
                .problemType(ProblemType.GREEDY)
                .description("description2")
                .inputDescription("inputDescription2")
                .outputDescription("outputDescription2")
                .exampleInput("exampleInput2")
                .exampleOutput("exampleOutput2")
                .build();

        Problem problem = Problem.builder()
                .id(1L)
                .title("title1")
                .difficultyLevel("easy")
                .problemType(ProblemType.GREEDY)
                .description("description1")
                .inputDescription("inputDescription1")
                .outputDescription("outputDescription1")
                .exampleInput("exampleInput1")
                .exampleOutput("exampleOutput1")
                .testCases(new ArrayList<>())
                .build();

        Long problemId = 1L;

        when(problemRepository.findById(problemId)).thenReturn(Optional.ofNullable(problem));

        problemService.updateProblem(problemDto, problemId);

        verify(problemRepository, times(1)).save(problemCaptor.capture());

        Problem saved = problemCaptor.getValue();
        assertEquals("title2",saved.getTitle());
        assertEquals(1, saved.getTestCases().size());
        TestCase testCase = saved.getTestCases().get(0);
        assertEquals("exampleInput2", testCase.getInput());
        assertEquals("exampleOutput2", testCase.getExpectedOutput());

    }

    @Test
    @DisplayName("updateProblem 테스트 - (실패 문제 없음)")
    public void updateProblemFailTest1(){

        ProblemRequestDto.createProblemDto problemDto = ProblemRequestDto.createProblemDto.builder()
                .title("title1")
                .difficultyLevel("easy")
                .problemType(ProblemType.GREEDY)
                .description("description1")
                .inputDescription("inputDescription1")
                .outputDescription("outputDescription1")
                .exampleInput("exampleInput1")
                .exampleOutput("exampleOutput1")
                .build();

        Long problemId = 1L;

        when(problemRepository.findById(problemId)).thenReturn(Optional.empty());

        GeneralException exception = assertThrows(GeneralException.class,()->problemService.updateProblem(problemDto, problemId));

        assertEquals(false,exception.getErrorReasonHttpStatus().getIsSuccess());
        assertEquals("PROBLEM400_2",exception.getErrorReasonHttpStatus().getCode());
        assertEquals("문제를 찾지 못했습니다.",exception.getErrorReasonHttpStatus().getMessage());
        assertEquals(HttpStatus.NOT_FOUND,exception.getErrorReasonHttpStatus().getHttpStatus());

    }

    @Test
    @DisplayName("updateProblem 테스트 - (실패 갱신 오류)")
    public void updateProblemFailTest2(){

        ProblemRequestDto.createProblemDto problemDto = ProblemRequestDto.createProblemDto.builder()
                .title("title1")
                .difficultyLevel("easy")
                .problemType(ProblemType.GREEDY)
                .description("description1")
                .inputDescription("inputDescription1")
                .outputDescription("outputDescription1")
                .exampleInput("exampleInput1")
                .exampleOutput("exampleOutput1")
                .build();

        Problem problem = Problem.builder()
                .id(1L)
                .title("title1")
                .difficultyLevel("easy")
                .problemType(ProblemType.GREEDY)
                .description("description1")
                .inputDescription("inputDescription1")
                .outputDescription("outputDescription1")
                .exampleInput("exampleInput1")
                .exampleOutput("exampleOutput1")
                .testCases(new ArrayList<>())
                .build();

        Long problemId = 1L;

        when(problemRepository.findById(problemId)).thenReturn(Optional.ofNullable(problem));
        when(problemRepository.save(problem)).thenThrow(new RuntimeException());

        GeneralException exception = assertThrows(GeneralException.class,()->problemService.updateProblem(problemDto, problemId));

        assertEquals(false,exception.getErrorReasonHttpStatus().getIsSuccess());
        assertEquals("PROBLEM400_4",exception.getErrorReasonHttpStatus().getCode());
        assertEquals("문제 수정에 실패하였습니다.",exception.getErrorReasonHttpStatus().getMessage());
        assertEquals(HttpStatus.BAD_REQUEST,exception.getErrorReasonHttpStatus().getHttpStatus());

    }

    @Test
    @DisplayName("getAllProblems 테스트 - (성공 문제유형,검색어 존재)")
    public void getAllProblemsSuccessTest1(){

        LocalDateTime createdAt = LocalDateTime.now();

        Problem problem = Problem.builder()
                .id(1L)
                .title("title1")
                .difficultyLevel("easy")
                .problemType(ProblemType.GREEDY)
                .description("description1")
                .inputDescription("inputDescription1")
                .outputDescription("outputDescription1")
                .exampleInput("exampleInput1")
                .exampleOutput("exampleOutput1")
                .testCases(new ArrayList<>())
                .build();

        PostPagingDto.PagingDto pagingDto = PostPagingDto.PagingDto.builder()
                .page(1)
                .size(10)
                .sort("DESC")
                .build();

        ProblemResponseDto.AllProblemDto problemDto = ProblemResponseDto.AllProblemDto.builder()
                .id(1L)
                .title("title1")
                .difficultyLevel("easy")
                .problemType(ProblemType.GREEDY)
                .createdAt(createdAt)
                .build();


        ProblemResponseDto.SearchProblems searchProblems = ProblemResponseDto.SearchProblems.builder()
                .problemResDtos(List.of(problemDto))
                .isFirst(true)
                .isLast(false)
                .listSize(1)
                .totalElements(1)
                .build();

        Sort sort = Sort.by(Sort.Direction.fromString(pagingDto.getSort()),"id");
        Pageable pageable = PageRequest.of(pagingDto.getPage(), pagingDto.getSize(), sort);

        List<Problem> problems1 = List.of(problem);
        Page<Problem> problems2 = new PageImpl<>(problems1,pageable, problems1.size());

        String problemType = "GREEDY";
        String search = "title1";
        ProblemType type = ProblemType.valueOf(problemType.toUpperCase());


        when(problemRepository.findByProblemTypeAndTitleContaining(type,search,pageable)).thenReturn(problems2);

        try(MockedStatic<ProblemConverter> mockedStatic = mockStatic(ProblemConverter.class)) {

            mockedStatic.when(()->ProblemConverter.toSearchProblemDto(problems2)).thenReturn(searchProblems);

            ProblemResponseDto.SearchProblems result = problemService.getAllProblems(pagingDto,problemType,search);

            assertNotNull(result);
            assertEquals(searchProblems.getProblemResDtos().size(),result.getProblemResDtos().size());
            assertEquals(searchProblems.isFirst(),result.isFirst());
            assertEquals(searchProblems.isLast(),result.isLast());
            assertEquals(searchProblems.getListSize(),result.getListSize());
            assertEquals(searchProblems.getTotalElements(),result.getTotalElements());

        }

    }

    @Test
    @DisplayName("getAllProblems 테스트 - (성공 문제유형만 존재)")
    public void getAllProblemsSuccessTest2(){
        LocalDateTime createdAt = LocalDateTime.now();

        Problem problem = Problem.builder()
                .id(1L)
                .title("title1")
                .difficultyLevel("easy")
                .problemType(ProblemType.GREEDY)
                .description("description1")
                .inputDescription("inputDescription1")
                .outputDescription("outputDescription1")
                .exampleInput("exampleInput1")
                .exampleOutput("exampleOutput1")
                .testCases(new ArrayList<>())
                .build();

        PostPagingDto.PagingDto pagingDto = PostPagingDto.PagingDto.builder()
                .page(1)
                .size(10)
                .sort("DESC")
                .build();

        ProblemResponseDto.AllProblemDto problemDto = ProblemResponseDto.AllProblemDto.builder()
                .id(1L)
                .title("title1")
                .difficultyLevel("easy")
                .problemType(ProblemType.GREEDY)
                .createdAt(createdAt)
                .build();


        ProblemResponseDto.SearchProblems searchProblems = ProblemResponseDto.SearchProblems.builder()
                .problemResDtos(List.of(problemDto))
                .isFirst(true)
                .isLast(false)
                .listSize(1)
                .totalElements(1)
                .build();

        Sort sort = Sort.by(Sort.Direction.fromString(pagingDto.getSort()),"id");
        Pageable pageable = PageRequest.of(pagingDto.getPage(), pagingDto.getSize(), sort);

        List<Problem> problems1 = List.of(problem);
        Page<Problem> problems2 = new PageImpl<>(problems1,pageable, problems1.size());

        String problemType = "GREEDY";
        ProblemType type = ProblemType.valueOf(problemType.toUpperCase());

        when(problemRepository.findByProblemType(type,pageable)).thenReturn(problems2);

        try(MockedStatic<ProblemConverter> mockedStatic = mockStatic(ProblemConverter.class)) {

            mockedStatic.when(()->ProblemConverter.toSearchProblemDto(problems2)).thenReturn(searchProblems);

            ProblemResponseDto.SearchProblems result = problemService.getAllProblems(pagingDto,problemType,null);

            assertNotNull(result);
            assertEquals(searchProblems.getProblemResDtos().size(),result.getProblemResDtos().size());
            assertEquals(searchProblems.isFirst(),result.isFirst());
            assertEquals(searchProblems.isLast(),result.isLast());
            assertEquals(searchProblems.getListSize(),result.getListSize());
            assertEquals(searchProblems.getTotalElements(),result.getTotalElements());

        }

    }

    @Test
    @DisplayName("getAllProblems 테스트 - (성공 검색어만 존재)")
    public void getAllProblemsSuccessTest3(){

        LocalDateTime createdAt = LocalDateTime.now();

        Problem problem = Problem.builder()
                .id(1L)
                .title("title1")
                .difficultyLevel("easy")
                .problemType(ProblemType.GREEDY)
                .description("description1")
                .inputDescription("inputDescription1")
                .outputDescription("outputDescription1")
                .exampleInput("exampleInput1")
                .exampleOutput("exampleOutput1")
                .testCases(new ArrayList<>())
                .build();

        PostPagingDto.PagingDto pagingDto = PostPagingDto.PagingDto.builder()
                .page(1)
                .size(10)
                .sort("DESC")
                .build();

        ProblemResponseDto.AllProblemDto problemDto = ProblemResponseDto.AllProblemDto.builder()
                .id(1L)
                .title("title1")
                .difficultyLevel("easy")
                .problemType(ProblemType.GREEDY)
                .createdAt(createdAt)
                .build();


        ProblemResponseDto.SearchProblems searchProblems = ProblemResponseDto.SearchProblems.builder()
                .problemResDtos(List.of(problemDto))
                .isFirst(true)
                .isLast(false)
                .listSize(1)
                .totalElements(1)
                .build();

        Sort sort = Sort.by(Sort.Direction.fromString(pagingDto.getSort()),"id");
        Pageable pageable = PageRequest.of(pagingDto.getPage(), pagingDto.getSize(), sort);

        List<Problem> problems1 = List.of(problem);
        Page<Problem> problems2 = new PageImpl<>(problems1,pageable, problems1.size());

        String search = "title1";


        when(problemRepository.findByTitleContaining(search,pageable)).thenReturn(problems2);

        try(MockedStatic<ProblemConverter> mockedStatic = mockStatic(ProblemConverter.class)) {

            mockedStatic.when(()->ProblemConverter.toSearchProblemDto(problems2)).thenReturn(searchProblems);

            ProblemResponseDto.SearchProblems result = problemService.getAllProblems(pagingDto,null,search);

            assertNotNull(result);
            assertEquals(searchProblems.getProblemResDtos().size(),result.getProblemResDtos().size());
            assertEquals(searchProblems.isFirst(),result.isFirst());
            assertEquals(searchProblems.isLast(),result.isLast());
            assertEquals(searchProblems.getListSize(),result.getListSize());
            assertEquals(searchProblems.getTotalElements(),result.getTotalElements());

        }
    }

    @Test
    @DisplayName("getAllProblems 테스트 - (성공 페이징만 존재)")
    public void getAllProblemsSuccessTest4(){

        LocalDateTime createdAt = LocalDateTime.now();

        Problem problem = Problem.builder()
                .id(1L)
                .title("title1")
                .difficultyLevel("easy")
                .problemType(ProblemType.GREEDY)
                .description("description1")
                .inputDescription("inputDescription1")
                .outputDescription("outputDescription1")
                .exampleInput("exampleInput1")
                .exampleOutput("exampleOutput1")
                .testCases(new ArrayList<>())
                .build();

        PostPagingDto.PagingDto pagingDto = PostPagingDto.PagingDto.builder()
                .page(1)
                .size(10)
                .sort("DESC")
                .build();

        ProblemResponseDto.AllProblemDto problemDto = ProblemResponseDto.AllProblemDto.builder()
                .id(1L)
                .title("title1")
                .difficultyLevel("easy")
                .problemType(ProblemType.GREEDY)
                .createdAt(createdAt)
                .build();


        ProblemResponseDto.SearchProblems searchProblems = ProblemResponseDto.SearchProblems.builder()
                .problemResDtos(List.of(problemDto))
                .isFirst(true)
                .isLast(false)
                .listSize(1)
                .totalElements(1)
                .build();

        Sort sort = Sort.by(Sort.Direction.fromString(pagingDto.getSort()),"id");
        Pageable pageable = PageRequest.of(pagingDto.getPage(), pagingDto.getSize(), sort);

        List<Problem> problems1 = List.of(problem);
        Page<Problem> problems2 = new PageImpl<>(problems1,pageable, problems1.size());

        when(problemRepository.findAll(pageable)).thenReturn(problems2);

        try(MockedStatic<ProblemConverter> mockedStatic = mockStatic(ProblemConverter.class)) {

            mockedStatic.when(()->ProblemConverter.toSearchProblemDto(problems2)).thenReturn(searchProblems);

            ProblemResponseDto.SearchProblems result = problemService.getAllProblems(pagingDto,null,null);

            assertNotNull(result);
            assertEquals(searchProblems.getProblemResDtos().size(),result.getProblemResDtos().size());
            assertEquals(searchProblems.isFirst(),result.isFirst());
            assertEquals(searchProblems.isLast(),result.isLast());
            assertEquals(searchProblems.getListSize(),result.getListSize());
            assertEquals(searchProblems.getTotalElements(),result.getTotalElements());

        }

    }

    @Test
    @DisplayName("getAllProblems 테스트 - (실패 잘못된 문제 유형)")
    public void getAllProblemsFailTest(){

        PostPagingDto.PagingDto pagingDto = PostPagingDto.PagingDto.builder()
                .page(1)
                .size(10)
                .sort("DESC")
                .build();

        String problemType = "NOT_VALID";

        GeneralException exception = assertThrows(GeneralException.class, () ->
                problemService.getAllProblems(pagingDto, problemType, null)
        );

        assertEquals(false,exception.getErrorReasonHttpStatus().getIsSuccess());
        assertEquals("PROBLEM400_3",exception.getErrorReasonHttpStatus().getCode());
        assertEquals("잘못된 문제 유형입니다.",exception.getErrorReasonHttpStatus().getMessage());
        assertEquals(HttpStatus.BAD_REQUEST,exception.getErrorReasonHttpStatus().getHttpStatus());

    }

    @Test
    @DisplayName("getProblemById 테스트 - (성공)")
    public void getProblemByIdSuccessTest(){

        Long problemId = 1L;

        Problem problem = Problem.builder()
                .id(1L)
                .title("title1")
                .difficultyLevel("easy")
                .problemType(ProblemType.GREEDY)
                .description("description1")
                .inputDescription("inputDescription1")
                .outputDescription("outputDescription1")
                .exampleInput("exampleInput1")
                .exampleOutput("exampleOutput1")
                .testCases(new ArrayList<>())
                .build();

        ProblemResponseDto.ProblemDto problemDto = ProblemResponseDto.ProblemDto.builder()
                .id(problemId)
                .title("title1")
                .difficultyLevel("easy")
                .problemType(ProblemType.GREEDY)
                .description("description1")
                .inputDescription("inputDescription1")
                .outputDescription("outputDescription1")
                .createdAt(problem.getCreatedAt())
                .exampleOutput("exampleOutput1")
                .exampleInput("exampleInput1")
                .build();

        when(problemRepository.findById(problemId)).thenReturn(Optional.ofNullable(problem));

        try(MockedStatic<ProblemConverter> staticMock = Mockito.mockStatic(ProblemConverter.class)) {
            staticMock.when(()->ProblemConverter.toProblemDto(problem)).thenReturn(problemDto);

            ProblemResponseDto.ProblemDto savedDto = problemService.getProblemById(problemId);

            assertEquals(problemDto.getTitle(),savedDto.getTitle());
            assertEquals(problemDto.getId(),savedDto.getId());
            assertEquals(problemDto.getProblemType(),savedDto.getProblemType());

        }

    }

    @Test
    @DisplayName("getProblemById 테스트 - (실패 문제 없음)")
    public void getProblemByIdFailTest(){

        Long problemId = 1L;
        when(problemRepository.findById(problemId)).thenReturn(Optional.empty());

        GeneralException exception = assertThrows(GeneralException.class,()->problemService.getProblemById(problemId));

        assertEquals(false,exception.getErrorReasonHttpStatus().getIsSuccess());
        assertEquals("PROBLEM400_2",exception.getErrorReasonHttpStatus().getCode());
        assertEquals("문제를 찾지 못했습니다.",exception.getErrorReasonHttpStatus().getMessage());
        assertEquals(HttpStatus.NOT_FOUND,exception.getErrorReasonHttpStatus().getHttpStatus());

    }

    @Test
    @DisplayName("deleteProblem 테스트 - (성공)")
    public void deleteProblemSuccessTest(){

        Long problemId = 1L;

        Problem problem = Problem.builder()
                .id(1L)
                .title("title1")
                .difficultyLevel("easy")
                .problemType(ProblemType.GREEDY)
                .description("description1")
                .inputDescription("inputDescription1")
                .outputDescription("outputDescription1")
                .exampleInput("exampleInput1")
                .exampleOutput("exampleOutput1")
                .testCases(new ArrayList<>())
                .build();

        when(problemRepository.findById(problemId)).thenReturn(Optional.ofNullable(problem));

        problemService.deleteProblem(problemId);

        verify(problemRepository).deleteById(problemId);

    }

    @Test
    @DisplayName("deleteProblem 테스트 - (실패 문제 없음)")
    public void deleteProblemFailTest(){

        Long problemId = 1L;
        when(problemRepository.findById(problemId)).thenReturn(Optional.empty());

        GeneralException exception = assertThrows(GeneralException.class,()->problemService.deleteProblem(problemId));

        assertEquals(false,exception.getErrorReasonHttpStatus().getIsSuccess());
        assertEquals("PROBLEM400_2",exception.getErrorReasonHttpStatus().getCode());
        assertEquals("문제를 찾지 못했습니다.",exception.getErrorReasonHttpStatus().getMessage());
        assertEquals(HttpStatus.NOT_FOUND,exception.getErrorReasonHttpStatus().getHttpStatus());


    }

}
