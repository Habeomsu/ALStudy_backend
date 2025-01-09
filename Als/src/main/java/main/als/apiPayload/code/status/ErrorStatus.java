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
    _USERNAME_NOT_FOUND(HttpStatus.NOT_FOUND,"USER400_2","회원가입된 아이디가 아닙니다."),

    //group
    _NOT_OVER_DEADLINE(HttpStatus.BAD_REQUEST,"GROUP400_1","스터디 종료일은 마감일보다 이후여야 합니다."),
    _NOT_FOUND_GROUP(HttpStatus.NOT_FOUND,"GROUP400_2","그룹이 존재하지 않습니다."),
    _NOT_MATCH_GROUPPASSWORD(HttpStatus.BAD_REQUEST,"GROUP400_3","그룹 비밀번호가 일치하지 않습니다."),
    _NOT_MATCH_LEADER(HttpStatus.BAD_REQUEST,"GROUP400_4","리더가 일치하지 않습니다."),

    //usergroup
    _DEADLINE_EXCEEDED(HttpStatus.BAD_REQUEST,"USERGROUP400_1","모집기간이 지났습니다."),
    _USER_ALREADY_IN_GROUP(HttpStatus.BAD_REQUEST,"USERGROUP400_2","이미 그룹에 포함된 사용자입니다."),

    // problem
    _NOT_CREATED_PROBLEM(HttpStatus.BAD_REQUEST,"PROBLEM400_1","문제 생성에 실패하였습니다."),
    _NOT_FOUND_PROBLEM(HttpStatus.NOT_FOUND,"PROBLEM400_2","문제를 찾지 못했습니다."),

    //testcase
    _NOT_FOUND_TESTCASE(HttpStatus.NOT_FOUND,"TESTCASE400_1","테스트케이스가 존재하지 않습니다."),


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
