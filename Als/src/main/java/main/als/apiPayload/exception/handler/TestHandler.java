package main.als.apiPayload.exception.handler;

import main.als.apiPayload.code.BaseErrorCode;
import main.als.apiPayload.exception.GeneralException;

public class TestHandler extends GeneralException {

    public TestHandler(BaseErrorCode code) {
        super(code);
    }

}
