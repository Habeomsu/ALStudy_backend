package main.als.websSocketTest;

import main.als.apiPayload.code.status.ErrorStatus;
import main.als.apiPayload.exception.GeneralException;
import main.als.websocket.util.StompExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;

import static org.junit.jupiter.api.Assertions.*;

public class StompExceptionHandlerTest {

    private StompExceptionHandler stompExceptionHandler;

    @BeforeEach
    void setUp() {
        stompExceptionHandler = new StompExceptionHandler();
    }

    private Message<byte[]> createDummyClientMessage() {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.SEND);
        accessor.setReceipt("receipt-id-123");
        return MessageBuilder.createMessage("dummy".getBytes(), accessor.getMessageHeaders());
    }

    @Test
    @DisplayName("handleClientMessageProcessingError - GeneralException 처리 테스트")
    void testHandleClientMessageProcessingErrorWithGeneralException() {
        // given
        GeneralException ex = new GeneralException(ErrorStatus._INVALID_ACCESS_TOKEN);
        Message<byte[]> clientMessage = createDummyClientMessage();

        // when
        Message<byte[]> errorMessage = stompExceptionHandler.handleClientMessageProcessingError(clientMessage, ex);

        // then
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(errorMessage);
        assertEquals(StompCommand.ERROR, accessor.getCommand());
        assertEquals("UNAUTHORIZED", accessor.getMessage());
        assertEquals("receipt-id-123", accessor.getReceiptId());
        assertFalse(new String(errorMessage.getPayload()).contains(ErrorStatus._INVALID_ACCESS_TOKEN.getMessage()));
    }

    @Test
    @DisplayName("handleClientMessageProcessingError - MessageDeliveryException 래핑된 GeneralException 처리")
    void testWrappedGeneralException() {
        // given
        Message<byte[]> dummyMessage = createDummyClientMessage();
        GeneralException innerEx = new GeneralException(ErrorStatus._EXFIRED_ACCESS_TOKEN);
        MessageDeliveryException wrappedEx = new MessageDeliveryException(dummyMessage, "wrapped", innerEx);

        // when
        Message<byte[]> errorMessage = stompExceptionHandler.handleClientMessageProcessingError(dummyMessage, wrappedEx);

        // then
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(errorMessage);
        assertEquals(StompCommand.ERROR, accessor.getCommand());
        assertEquals("UNAUTHORIZED", accessor.getMessage());
        assertEquals("receipt-id-123", accessor.getReceiptId());
        assertFalse(new String(errorMessage.getPayload()).contains(ErrorStatus._EXFIRED_ACCESS_TOKEN.getMessage()));
    }

    @Test
    @DisplayName("prepareErrorMessage - null 메시지일 때 payload EMPTY")
    void testPrepareErrorMessageWithNull() {
        // given
        Message<byte[]> message = stompExceptionHandler.handleClientMessageProcessingError(createDummyClientMessage(), new GeneralException(ErrorStatus._INVALID_ACCESS_TOKEN));

        // when
        String payload = new String(message.getPayload());

        // then
        assertNotNull(payload);
        assertFalse(payload.contains(ErrorStatus._INVALID_ACCESS_TOKEN.getMessage()));
    }
}
