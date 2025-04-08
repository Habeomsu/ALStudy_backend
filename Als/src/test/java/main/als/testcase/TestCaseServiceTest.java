package main.als.testcase;

import main.als.apiPayload.code.status.ErrorStatus;
import main.als.apiPayload.exception.GeneralException;
import main.als.problem.converter.TestCaseConverter;
import main.als.problem.dto.TestCaseRequestDto;
import main.als.problem.dto.TestCaseResponseDto;
import main.als.problem.entity.Problem;
import main.als.problem.entity.TestCase;
import main.als.problem.repository.ProblemRepository;
import main.als.problem.repository.TestCaseRepository;
import main.als.problem.service.TestCaseService;
import main.als.problem.service.TestCaseServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TestCaseServiceTest {

    @InjectMocks
    private TestCaseServiceImpl testCaseService;

    @Mock
    private TestCaseRepository testCaseRepository;

    @Mock
    private ProblemRepository problemRepository;

    @Captor
    private ArgumentCaptor<TestCase> testCaseCaptor;

    @Test
    @DisplayName("createTestCase 테스트 - (성공)")
    public void createTestCaseSuccessTest(){

        TestCaseRequestDto.TestCaseDto testCaseDto = TestCaseRequestDto.TestCaseDto.builder()
                .input("input")
                .expectedOutput("output")
                .build();

        Problem problem = Problem.builder()
                .id(1L)
                .title("title")
                .build();

        Long problemId = 1L;

        when(problemRepository.findById(problemId)).thenReturn(Optional.of(problem));

        testCaseService.createTestCase(testCaseDto, problemId);

        verify(testCaseRepository,times(1)).save(testCaseCaptor.capture());

        TestCase savedTestCase = testCaseCaptor.getValue();

        assertEquals(1, problem.getTestCases().size());
        assertTrue(problem.getTestCases().contains(savedTestCase));

    }

    @Test
    @DisplayName("createTestCase 테스트 - (실패 - 문제 못 찾음)")
    public void createTestCaseFailTest(){

        Long problemId = 1L;
        TestCaseRequestDto.TestCaseDto testCaseDto = TestCaseRequestDto.TestCaseDto.builder()
                .input("input")
                .expectedOutput("output")
                .build();

        when(problemRepository.findById(problemId)).thenReturn(Optional.empty());

        GeneralException exception = assertThrows(GeneralException.class, () -> testCaseService.createTestCase(testCaseDto, problemId));

        assertEquals(false,exception.getErrorReasonHttpStatus().getIsSuccess());
        assertEquals(ErrorStatus._NOT_FOUND_PROBLEM.getCode(),exception.getErrorReasonHttpStatus().getCode());
        assertEquals(ErrorStatus._NOT_FOUND_PROBLEM.getMessage(),exception.getErrorReasonHttpStatus().getMessage());
        assertEquals(ErrorStatus._NOT_FOUND_PROBLEM.getHttpStatus(),exception.getErrorReasonHttpStatus().getHttpStatus());

    }

    @Test
    @DisplayName("getTestCaseById 테스트 - (성공)")
    public void getTestCaseByIdSuccessTest(){

        Long testCaseId = 1L;

        TestCase testCase = TestCase.builder()
                .id(testCaseId)
                .input("input")
                .expectedOutput("output")
                .build();

        TestCaseResponseDto.TestCaseDto mockedDto = TestCaseResponseDto.TestCaseDto.builder()
                .id(testCaseId)
                .problemId(1L)
                .input("input")
                .expectedOutput("output")
                .build();

        when(testCaseRepository.findById(testCaseId)).thenReturn(Optional.of(testCase));

        try(MockedStatic<TestCaseConverter> mockedStatic = Mockito.mockStatic(TestCaseConverter.class)) {

            mockedStatic.when(()->TestCaseConverter.toTestCase(testCase)).thenReturn(mockedDto);

            TestCaseResponseDto.TestCaseDto result = testCaseService.getTestCaseById(testCaseId);

            assertEquals(mockedDto.getId(),result.getId());
            assertEquals(mockedDto.getInput(),result.getInput());
            assertEquals(mockedDto.getExpectedOutput(),result.getExpectedOutput());
            assertEquals(mockedDto.getProblemId(), result.getProblemId());

        }
    }

    @Test
    @DisplayName("getTestCaseById 테스트 - (실패 - 테스트케이스 없음)")
    public void getTestCaseByIdFailTest(){

        Long testCaseId = 1L;

        when(testCaseRepository.findById(testCaseId)).thenReturn(Optional.empty());

        GeneralException exception = assertThrows(GeneralException.class, () -> testCaseService.getTestCaseById(testCaseId));

        assertEquals(false,exception.getErrorReasonHttpStatus().getIsSuccess());
        assertEquals(ErrorStatus._NOT_FOUND_TESTCASE.getCode(),exception.getErrorReasonHttpStatus().getCode());
        assertEquals(ErrorStatus._NOT_FOUND_TESTCASE.getMessage(),exception.getErrorReasonHttpStatus().getMessage());
        assertEquals(ErrorStatus._NOT_FOUND_TESTCASE.getHttpStatus(),exception.getErrorReasonHttpStatus().getHttpStatus());

    }

    @Test
    @DisplayName("getTestCasesByProblemId 테스트 - (성공)")
    public void getTestCasesByProblemIdSuccessTest(){

        Long problemId = 1L;

        TestCase testCase1 = TestCase.builder()
                .id(1L)
                .input("input1")
                .expectedOutput("output1")
                .build();

        TestCase testCase2 = TestCase.builder()
                .id(2L)
                .input("input2")
                .expectedOutput("output2")
                .build();

        List<TestCase> testCases = Arrays.asList(testCase1, testCase2);

        TestCaseResponseDto.TestCaseDto mockedDto1 = TestCaseResponseDto.TestCaseDto.builder()
                .id(testCase1.getId())
                .problemId(1L)
                .input("input1")
                .expectedOutput("output1")
                .build();

        TestCaseResponseDto.TestCaseDto mockedDto2 = TestCaseResponseDto.TestCaseDto.builder()
                .id(testCase2.getId())
                .problemId(1L)
                .input("input2")
                .expectedOutput("output2")
                .build();

        List<TestCaseResponseDto.TestCaseDto> testCaseDtos = Arrays.asList(mockedDto1,mockedDto2);

        when(problemRepository.existsById(problemId)).thenReturn(true);
        when(testCaseRepository.findByProblemId(problemId)).thenReturn(testCases);

        try(MockedStatic<TestCaseConverter> mockedStatic = Mockito.mockStatic(TestCaseConverter.class)) {

            mockedStatic.when(()->TestCaseConverter.toTestCase(testCases)).thenReturn(testCaseDtos);

            List<TestCaseResponseDto.TestCaseDto> result = testCaseService.getTestCasesByProblemId(problemId);

            assertEquals(testCaseDtos.size(),result.size());
            assertEquals(testCaseDtos.get(0).getId(),result.get(0).getId());
            assertEquals(testCaseDtos.get(0).getProblemId(),result.get(0).getProblemId());
            assertEquals(testCaseDtos.get(0).getInput(),result.get(0).getInput());
            assertEquals(testCaseDtos.get(0).getExpectedOutput(),result.get(0).getExpectedOutput());

        }
    }

    @Test
    @DisplayName("getTestCasesByProblemId 테스트 - (실패 - 문제 못 찾음)")
    public void getTestCasesByProblemIdFailTest(){

        Long problemId = 1L;

        when(problemRepository.existsById(problemId)).thenReturn(false);

        GeneralException exception = assertThrows(GeneralException.class, () -> testCaseService.getTestCasesByProblemId(problemId));

        assertEquals(false,exception.getErrorReasonHttpStatus().getIsSuccess());
        assertEquals(ErrorStatus._NOT_FOUND_PROBLEM.getCode(),exception.getErrorReasonHttpStatus().getCode());
        assertEquals(ErrorStatus._NOT_FOUND_PROBLEM.getMessage(),exception.getErrorReasonHttpStatus().getMessage());
        assertEquals(ErrorStatus._NOT_FOUND_PROBLEM.getHttpStatus(),exception.getErrorReasonHttpStatus().getHttpStatus());

    }

    @Test
    @DisplayName("updateTestCase 테스트 - (성공)")
    public void updateTestCaseSuccessTest(){

        Long testCaseId = 1L;

        TestCase testCase1 = TestCase.builder()
                .id(1L)
                .input("input1")
                .expectedOutput("output1")
                .build();

        TestCaseRequestDto.TestCaseDto mockedDto = TestCaseRequestDto.TestCaseDto.builder()
                .input("input2")
                .expectedOutput("output2")
                .build();

        when(testCaseRepository.findById(testCaseId)).thenReturn(Optional.of(testCase1));

        testCaseService.updateTestCase(mockedDto, testCaseId);

        verify(testCaseRepository, times(1)).save(testCaseCaptor.capture());

        TestCase capturedTestCase = testCaseCaptor.getValue();

        assertEquals(testCase1.getId(), capturedTestCase.getId());
        assertEquals("input2", capturedTestCase.getInput());
        assertEquals("output2", capturedTestCase.getExpectedOutput());

    }

    @Test
    @DisplayName("updateTestCase 테스트 - (실패 - 테스트 케이스 없음)")
    public void updateTestCaseFailTest(){

        Long testCaseId = 1L;
        TestCaseRequestDto.TestCaseDto mockedDto = TestCaseRequestDto.TestCaseDto.builder()
                .input("input2")
                .expectedOutput("output2")
                .build();

        when(testCaseRepository.findById(testCaseId)).thenReturn(Optional.empty());

        GeneralException exception = assertThrows(GeneralException.class, () -> testCaseService.updateTestCase(mockedDto, testCaseId));

        assertEquals(false,exception.getErrorReasonHttpStatus().getIsSuccess());
        assertEquals(ErrorStatus._NOT_FOUND_TESTCASE.getCode(),exception.getErrorReasonHttpStatus().getCode());
        assertEquals(ErrorStatus._NOT_FOUND_TESTCASE.getMessage(),exception.getErrorReasonHttpStatus().getMessage());
        assertEquals(ErrorStatus._NOT_FOUND_TESTCASE.getHttpStatus(),exception.getErrorReasonHttpStatus().getHttpStatus());

    }

    @Test
    @DisplayName("deleteTestCase 테스트 - (성공)")
    public void deleteTestCaseSuccessTest(){

        Long testCaseId = 1L;

        TestCase testCase1 = TestCase.builder()
                .id(1L)
                .input("input1")
                .expectedOutput("output1")
                .build();

        when(testCaseRepository.findById(testCaseId)).thenReturn(Optional.of(testCase1));

        testCaseService.deleteTestCase(testCaseId);

        verify(testCaseRepository, times(1)).delete(testCase1);

    }

    @Test
    @DisplayName("deleteTestCase 테스트 - (실패 - 테스트케이스 없음)")
    public void deleteTestCaseFailTest(){

        Long testCaseId = 1L;

        when(testCaseRepository.findById(testCaseId)).thenReturn(Optional.empty());

        GeneralException exception = assertThrows(GeneralException.class, () -> testCaseService.deleteTestCase(testCaseId));

        assertEquals(false,exception.getErrorReasonHttpStatus().getIsSuccess());
        assertEquals(ErrorStatus._NOT_FOUND_TESTCASE.getCode(),exception.getErrorReasonHttpStatus().getCode());
        assertEquals(ErrorStatus._NOT_FOUND_TESTCASE.getMessage(),exception.getErrorReasonHttpStatus().getMessage());
        assertEquals(ErrorStatus._NOT_FOUND_TESTCASE.getHttpStatus(),exception.getErrorReasonHttpStatus().getHttpStatus());

    }

}
