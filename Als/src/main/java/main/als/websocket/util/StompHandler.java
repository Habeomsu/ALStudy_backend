package main.als.websocket.util;

import io.jsonwebtoken.ExpiredJwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import main.als.apiPayload.code.status.ErrorStatus;
import main.als.apiPayload.exception.GeneralException;
import main.als.user.security.JWTUtil;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
@Order(Ordered.HIGHEST_PRECEDENCE + 99)
@Slf4j
public class StompHandler implements ChannelInterceptor {

    private final JWTUtil jwtUtil;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor headerAccessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (headerAccessor.getCommand() == StompCommand.CONNECT) { // 연결 시에 토큰 검증
            String token = headerAccessor.getNativeHeader("access") != null ?
                    headerAccessor.getNativeHeader("access").get(0) : null;

            if (token == null) {
                log.error("Access token is missing");
                throw new GeneralException(ErrorStatus._INVALID_ACCESS_TOKEN); // 토큰이 없을 경우 처리
            }

            try {
                // JWT 검증 로직
                jwtUtil.isExpired(token); // 만료 여부 확인
            } catch (ExpiredJwtException e) {
                log.error("Expired access token: {}", token);
                throw new GeneralException(ErrorStatus._EXFIRED_ACCESS_TOKEN); // 만료된 토큰 처리
            } catch (Exception e) {
                log.error("Invalid access token: {}", token);
                throw new GeneralException(ErrorStatus._INVALID_ACCESS_TOKEN); // 유효하지 않은 토큰 처리
            }

            // 토큰 검증이 성공적으로 완료된 후 추가 로직을 여기에 작성할 수 있습니다.
            log.info("Access token is valid: {}", token);
        }

        return message; // 메시지를 그대로 반환
    }

}
