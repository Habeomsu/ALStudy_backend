package main.als.apiPayload.code;

import org.springframework.web.ErrorResponse;

public interface BaseErrorCode {
    public ErrorReasonDto getReason();
    public ErrorReasonDto getReasonHttpStatus();
}
