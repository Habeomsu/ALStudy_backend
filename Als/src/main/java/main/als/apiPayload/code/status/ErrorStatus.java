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
    _EXFIRED_ACCESS_TOKEN(HttpStatus.UNAUTHORIZED,"JWT400_1","만료된 access 토큰입니다."),
    _INVALID_ACCESS_TOKEN(HttpStatus.UNAUTHORIZED,"JWT400_2","유효하지 않는 access 토큰입니다."),
    _NOTFOUND_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED,"JWT400_3","refresh 토큰이 존재하지않습니다."),
    _EXFIRED_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED,"JWT400_4","만료된 refresh 토큰입니다."),
    _INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED,"JWT400_5","유효하지 않는 refresh 토큰입니다."),
    _NOFOUND_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED,"JWT400_6","DB에 refresh 토큰이 존재하지 않습니다."),
    _STAL_OBJECT_STATE(HttpStatus.UNAUTHORIZED,"JWT400_7","트렌젝션 오류 입니다."),

    //username
    _EXIST_USERNAME(HttpStatus.BAD_REQUEST,"USER400_1","아이디가 존재합니다."),
    _USERNAME_NOT_FOUND(HttpStatus.NOT_FOUND,"USER400_2","회원가입된 아이디가 아닙니다."),

    //group
    _NOT_OVER_DEADLINE(HttpStatus.BAD_REQUEST,"GROUP400_1","스터디 종료일은 마감일보다 이후여야 합니다."),
    _NOT_FOUND_GROUP(HttpStatus.NOT_FOUND,"GROUP400_2","그룹이 존재하지 않습니다."),
    _NOT_MATCH_GROUPPASSWORD(HttpStatus.BAD_REQUEST,"GROUP400_3","그룹 비밀번호가 일치하지 않습니다."),
    _NOT_MATCH_LEADER(HttpStatus.BAD_REQUEST,"GROUP400_4","리더가 일치하지 않습니다."),
    _NOT_MATCH_GROUP(HttpStatus.BAD_REQUEST,"GROUP400_5","그룹이 일치하지 않습니다."),

    //usergroup
    _DEADLINE_EXCEEDED(HttpStatus.BAD_REQUEST,"USERGROUP400_1","모집기간이 지났습니다."),
    _USER_ALREADY_IN_GROUP(HttpStatus.BAD_REQUEST,"USERGROUP400_2","이미 그룹에 포함된 사용자입니다."),
    _NOT_IN_USERGROUP(HttpStatus.BAD_REQUEST,"USERGROUP400_3","그룹에 속해 있지 않은 사용자입니다."),
    _NOT_FOUND_USERGROUP(HttpStatus.BAD_REQUEST,"USERGROUP400_4","그룹이 존재하지 않습니다."),
    _LEADER_NOT_RESIGN(HttpStatus.BAD_REQUEST,"USERGROUP400_5","리더는 그룹 탈퇴가 불가능합니다."),


    // problem
    _NOT_CREATED_PROBLEM(HttpStatus.BAD_REQUEST,"PROBLEM400_1","문제 생성에 실패하였습니다."),
    _NOT_FOUND_PROBLEM(HttpStatus.NOT_FOUND,"PROBLEM400_2","문제를 찾지 못했습니다."),
    _INVALID_PROBLEM_TYPE(HttpStatus.BAD_REQUEST,"PROBLEM400_3","잘못된 문제 유형입니다."),
    _NOT_UPDATED_PROBLEM(HttpStatus.BAD_REQUEST,"PROBLEM400_4","문제 수정에 실패하였습니다."),
    //testcase
    _NOT_FOUND_TESTCASE(HttpStatus.NOT_FOUND,"TESTCASE400_1","테스트케이스가 존재하지 않습니다."),

    // groupProblem
    _DEADLINE_NOT_PASSED(HttpStatus.BAD_REQUEST,"GROUPPROBLEM400_1","모집기간이 지나야 문제생성이 가능합니다."),
    _NOT_FOUND_GROUPPROBLEM(HttpStatus.NOT_FOUND,"GROUPPROBLEM400_2","그룹문제가 존재하지 않습니다."),
    _DUPLICATE_GROUP_PROBLEM(HttpStatus.BAD_REQUEST,"GROUPPROBLEM400_3","중복된 문제입니다."),
    _DEADLINE_EXPIRED(HttpStatus.BAD_REQUEST,"GROUPPROBLEM400_4","마감일이 지난 문제입니다."),


    // file
    _FILE_NOT_FOUND(HttpStatus.NOT_FOUND,"FILE400_1","파일이 없습니다."),
    _FILE_WRITE_ERROR(HttpStatus.BAD_REQUEST,"FILE400_2","파일 저장 에러"),

    // submission
    _NOT_FOUND_SUBMISSION(HttpStatus.NOT_FOUND,"SUBMISSION400_1","제출내역이 없습니다."),
    _JSON_PROCESSING_ERROR(HttpStatus.BAD_REQUEST,"SUBMISSION400_2","테스트케이스 변환 오류입니다."),
    _SUBMISSION_DEADLINE_EXCEEDED(HttpStatus.BAD_REQUEST,"SUBMISSION400_3","제출 기한이 지났습니다."),
    _NO_SUCCEEDED_SUBMISSION(HttpStatus.BAD_REQUEST,"SUBMISSION400_4","문제를 풀지 못하였습니다."),

    // resttemplate 오류
    _FLASK_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR,"COMMUNICATION400_1","플라스크 통신 오류입니다."),

    // payment 오류
    _TOSS_CONFIRM_FAIL(HttpStatus.INTERNAL_SERVER_ERROR,"PAYMENT400_1","결제 실패입니다."),
    _TOSS_SAVE_FAIL(HttpStatus.INTERNAL_SERVER_ERROR,"PAYMENT400_2","결제 정보 저장 에러입니다."),
    _ALREADY_CHARGED(HttpStatus.BAD_REQUEST,"PAYMENT400_3","이미 충전을 하셨습니다."),
    _PAYMENT_KEY_NOT_FOUND(HttpStatus.NOT_FOUND,"PAYMENT400_4","환급의 필요한 payment KEY가 없습니다."),
    _NO_AVAILABLE_DEPOSIT(HttpStatus.BAD_REQUEST,"PAYMENT400_5","예치금이 없습니다."),
    _REFUND_FAILED(HttpStatus.BAD_REQUEST,"PAYMENT400_6","토스페이먼츠 api 오류"),
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
