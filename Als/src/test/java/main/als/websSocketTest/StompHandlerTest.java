package main.als.websSocketTest;

import io.jsonwebtoken.ExpiredJwtException;
import main.als.apiPayload.code.status.ErrorStatus;
import main.als.apiPayload.exception.GeneralException;
import main.als.user.security.JWTUtil;
import main.als.websocket.util.StompHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

public class StompHandlerTest {

    @Mock
    private JWTUtil jwtUtil;

    @InjectMocks
    private StompHandler stompHandler;

    @Mock
    private MessageChannel messageChannel;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    private Message<?> createStompConnectMessageWithToken(String token) {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
        accessor.setNativeHeader("access", token != null ? token : null);
        return MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());
    }

    @Test
    @DisplayName("preSend 테스트 - (성공)")
    public void preSendSuccessTest() {
        String token = "valid-token";
        Message<?> message = createStompConnectMessageWithToken(token);

        when(jwtUtil.isExpired(token)).thenReturn(true); // 검증 통과

        Message<?> result = stompHandler.preSend(message, messageChannel);

        assertEquals(message, result);
    }

    @Test
    @DisplayName("preSend 테스트 - (실패 - 토큰없음)")
    public void preSendFailTest1(){

        String token = null;
        Message<?> message = createStompConnectMessageWithToken(token);

        GeneralException exception = assertThrows(GeneralException.class, () ->stompHandler.preSend(message, messageChannel));

        assertFalse(exception.getErrorReasonHttpStatus().getIsSuccess());
        assertEquals(ErrorStatus._INVALID_ACCESS_TOKEN.getCode(), exception.getErrorReasonHttpStatus().getCode());
        assertEquals(ErrorStatus._INVALID_ACCESS_TOKEN.getMessage(), exception.getErrorReasonHttpStatus().getMessage());
        assertEquals(ErrorStatus._INVALID_ACCESS_TOKEN.getHttpStatus(), exception.getErrorReasonHttpStatus().getHttpStatus());

    }

    @Test
    @DisplayName("preSend 테스트 - (실패 - 만료된 토큰)")
    public void preSendFailTest2() {
        String token = "expired-token";
        Message<?> message = createStompConnectMessageWithToken(token);

        doThrow(new ExpiredJwtException(null, null, "expired"))
                .when(jwtUtil).isExpired(token);

        GeneralException exception = assertThrows(GeneralException.class,
                () -> stompHandler.preSend(message, messageChannel));

        assertFalse(exception.getErrorReasonHttpStatus().getIsSuccess());
        assertEquals(ErrorStatus._EXFIRED_ACCESS_TOKEN.getCode(), exception.getErrorReasonHttpStatus().getCode());
        assertEquals(ErrorStatus._EXFIRED_ACCESS_TOKEN.getMessage(), exception.getErrorReasonHttpStatus().getMessage());
        assertEquals(ErrorStatus._EXFIRED_ACCESS_TOKEN.getHttpStatus(), exception.getErrorReasonHttpStatus().getHttpStatus());
    }

    @Test
    @DisplayName("preSend 테스트 - (실패 - 유효하지 않은 토큰)")
    public void preSendFailTest3() {
        String token = "invalid-token";
        Message<?> message = createStompConnectMessageWithToken(token);

        doThrow(new RuntimeException("invalid")).when(jwtUtil).isExpired(token);

        GeneralException exception = assertThrows(GeneralException.class,
                () -> stompHandler.preSend(message, messageChannel));

        assertFalse(exception.getErrorReasonHttpStatus().getIsSuccess());
        assertEquals(ErrorStatus._INVALID_ACCESS_TOKEN.getCode(), exception.getErrorReasonHttpStatus().getCode());
        assertEquals(ErrorStatus._INVALID_ACCESS_TOKEN.getMessage(), exception.getErrorReasonHttpStatus().getMessage());
        assertEquals(ErrorStatus._INVALID_ACCESS_TOKEN.getHttpStatus(), exception.getErrorReasonHttpStatus().getHttpStatus());
    }

}
