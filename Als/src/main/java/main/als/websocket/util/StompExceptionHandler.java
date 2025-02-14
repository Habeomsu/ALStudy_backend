package main.als.websocket.util;

import com.github.dockerjava.api.exception.UnauthorizedException;
import main.als.apiPayload.ApiResult;
import main.als.apiPayload.code.status.ErrorStatus;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.StompSubProtocolErrorHandler;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

// 기타 import 생략...

@Component
public class StompExceptionHandler extends StompSubProtocolErrorHandler {

    private static final byte[] EMPTY_PAYLOAD = new byte[0];

    public StompExceptionHandler() {
        super();
    }

    @Override
    public Message<byte[]> handleClientMessageProcessingError(Message<byte[]> clientMessage, Throwable ex) {
        final Throwable exception = converterTrowException(ex);

        if (exception instanceof UnauthorizedException) {
            return handleUnauthorizedException(clientMessage, exception);
        }

        return super.handleClientMessageProcessingError(clientMessage, ex);
    }

    private Throwable converterTrowException(final Throwable exception) {
        if (exception instanceof MessageDeliveryException) {
            return exception.getCause();
        }
        return exception;
    }

    private Message<byte[]> handleUnauthorizedException(Message<byte[]> clientMessage, Throwable ex) {
        // ApiResult를 사용하여 에러 메시지 생성
        ApiResult<Object> apiResult = ApiResult.onFailure(ErrorStatus._UNAUTHORIZED.getCode(), ex.getMessage(), null);
        return prepareErrorMessage(clientMessage, apiResult);
    }

    private Message<byte[]> prepareErrorMessage(final Message<byte[]> clientMessage, ApiResult<Object> apiResult) {
        final StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.ERROR);
        accessor.setLeaveMutable(true);
        setReceiptIdForClient(clientMessage, accessor);

        // ApiResult를 JSON으로 변환하여 메시지 본문으로 설정
        String json = convertApiResultToJson(apiResult);
        return MessageBuilder.createMessage(
                json.getBytes(StandardCharsets.UTF_8),
                accessor.getMessageHeaders()
        );
    }

    private String convertApiResultToJson(ApiResult<Object> apiResult) {
        // ObjectMapper 등을 사용하여 ApiResult를 JSON 문자열로 변환
        // 예시로 간단히 toString() 사용
        return apiResult.toString(); // 적절한 JSON 변환 로직으로 변경
    }

    private void setReceiptIdForClient(final Message<byte[]> clientMessage, final StompHeaderAccessor accessor) {
        if (Objects.isNull(clientMessage)) {
            return;
        }

        final StompHeaderAccessor clientHeaderAccessor = MessageHeaderAccessor.getAccessor(clientMessage, StompHeaderAccessor.class);
        final String receiptId = Objects.isNull(clientHeaderAccessor) ? null : clientHeaderAccessor.getReceipt();

        if (receiptId != null) {
            accessor.setReceiptId(receiptId);
        }
    }

    @Override
    protected Message<byte[]> handleInternal(StompHeaderAccessor errorHeaderAccessor,
                                             byte[] errorPayload, Throwable cause, StompHeaderAccessor clientHeaderAccessor) {
        return MessageBuilder.createMessage(errorPayload, errorHeaderAccessor.getMessageHeaders());
    }
}
