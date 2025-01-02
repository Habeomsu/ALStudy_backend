package main.als.apiPayload.code.status;

import lombok.AllArgsConstructor;
import lombok.Getter;
import main.als.apiPayload.code.BaseErrorCode;
import main.als.apiPayload.code.ErrorReasonDto;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorStatus implements BaseErrorCode {
    // 일반 상태
    _INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR,"COMMON500","서버 에러"),
    _BAD_REQUEST(HttpStatus.BAD_REQUEST,"COMMON400","잘못된 요청입니다."),
    _UNAUTHORIZED(HttpStatus.UNAUTHORIZED,"COMMON401","인증이 필요합니다"),
    _FORBIDDEN(HttpStatus.FORBIDDEN,"COMMON402","금지된 요청입니다."),
    _NOT_FOUND(HttpStatus.NOT_FOUND,"COMMON403","데이터를 찾지 못했습니다."),

    // 토큰
    _EXFIRED_ACCESS_TOKEN(HttpStatus.BAD_REQUEST,"JWT400_1","만료된 access 토큰입니다."),
    _INVALID_ACCESS_TOKEN(HttpStatus.NOT_FOUND,"JWT400_2","유효하지 않는 access 토큰입니다."),
    _NOTFOUND_REFRESH_TOKEN(HttpStatus.NOT_FOUND,"JWT400_3","refresh 토큰이 존재하지않습니다."),
    _EXFIRED_REFRESH_TOKEN(HttpStatus.BAD_REQUEST,"JWT400_4","만료된 refresh 토큰입니다."),
    _INVALID_REFRESH_TOKEN(HttpStatus.NOT_FOUND,"JWT400_5","유효하지 않는 refresh 토큰입니다."),
    _NOFOUND_REFRESH_TOKEN(HttpStatus.NOT_FOUND,"JWT400_6","DB에 refresh 토큰이 존재하지 않습니다."),
    //username
    _EXIST_USERNAME(HttpStatus.BAD_REQUEST,"USER400_1","아이디가 존재합니다."),
    _USERNAME_NOT_FOUND(HttpStatus.NOT_FOUND,"USER400_2","회원가입된 아이디가 아닙니다.")
    ;

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    @Override
    public ErrorReasonDto getReason() {
        return ErrorReasonDto.builder()
                .code(code)
                .message(message)
                .isSuccess(false)
                .build();
    }

    @Override
    public ErrorReasonDto getReasonHttpStatus() {
        return ErrorReasonDto.builder()
                .code(code)
                .message(message)
                .isSuccess(false)
                .httpStatus(httpStatus)
                .build();
    }
}
