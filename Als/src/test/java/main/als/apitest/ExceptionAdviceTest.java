package main.als.apitest;


import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import main.als.apiPayload.ApiResult;
import main.als.apiPayload.code.status.ErrorStatus;
import main.als.apiPayload.exception.ExceptionAdvice;
import main.als.apiPayload.exception.GeneralException;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

import java.util.Collections;
import java.util.NoSuchElementException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest
@AutoConfigureMockMvc
public class ExceptionAdviceTest {

    @Autowired
    private ExceptionAdvice exceptionAdvice;

    @Test //일반 예외처리 테스트
    public void testHandleGeneralException() {
        // Given
        GeneralException exception = new GeneralException(ErrorStatus._INTERNAL_SERVER_ERROR);

        // Mocking HttpServletRequest
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        ServletWebRequest request = new ServletWebRequest(mockRequest);

        // When
        ResponseEntity<?> response = exceptionAdvice.onThrowException(exception, mockRequest);

        // Then
        assertThat(response.getStatusCode(), is(HttpStatus.INTERNAL_SERVER_ERROR));
        ApiResult<?> body = (ApiResult<?>) response.getBody();
        assertThat(body.getCode(), is(ErrorStatus._INTERNAL_SERVER_ERROR.getCode()));
        assertThat(body.getMessage(), is(ErrorStatus._INTERNAL_SERVER_ERROR.getMessage()));
    }

    @Test //변수 전달 오류 테스트
    public void testHandleNoSuchElementException(){
        // Given
        NoSuchElementException exception = new NoSuchElementException("잘못된 요청입니다");

        // When
        ApiResult<?> response = exceptionAdvice.noSuchElementException(exception, null);

        // Then
        assertThat(response.getCode(), is(ErrorStatus._BAD_REQUEST.getCode()));
        assertThat(response.getMessage(), is("잘못된 요청입니다"));
        assertThat(response.getIsSuccess(), is(false));

    }

    @Test //Vilolation 예외처리
    public void testHandleConstraintViolationException() {
        // Given
        ConstraintViolation<?> violation = Mockito.mock(ConstraintViolation.class);
        when(violation.getMessage()).thenReturn("_BAD_REQUEST");
        ConstraintViolationException exception = new ConstraintViolationException(Collections.singleton(violation));

        // Mocking WebRequest
        WebRequest mockRequest = mock(WebRequest.class);

        // When
        ResponseEntity<?> response = exceptionAdvice.handleConstraintViolationException(exception, mockRequest);

        // Then
        assertThat(response.getStatusCode(), is(HttpStatus.BAD_REQUEST));
        ApiResult<?> body = (ApiResult<?>) response.getBody();
        assertThat(body.getCode(), is(ErrorStatus._BAD_REQUEST.getCode()));
        assertThat(body.getMessage(), is("잘못된 요청입니다."));
        assertThat(body.getIsSuccess(), is(false));
    }


    @Test //valid 오류 처리
    public void testHandleMethodArgumentNotValid() {

        //Given
        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.getFieldErrors()).thenReturn(Collections.emptyList()); // 필드 오류가 없다고 설정

        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        when(exception.getBindingResult()).thenReturn(bindingResult);

        HttpHeaders headers = new HttpHeaders();
        WebRequest mockRequest = mock(WebRequest.class);

        // When
        ResponseEntity<?> response = exceptionAdvice.handleMethodArgumentNotValid(exception, headers, HttpStatus.BAD_REQUEST, mockRequest);

        // Then
        assertThat(response.getStatusCode(), is(HttpStatus.BAD_REQUEST));
        ApiResult<?> body = (ApiResult<?>) response.getBody();
        assertThat(body.getCode(), is(ErrorStatus._BAD_REQUEST.getCode()));
        assertThat(body.getMessage(),is(ErrorStatus._BAD_REQUEST.getMessage()));
    }

}
