package main.als.apitest;

import main.als.apiPayload.ApiResult;
import main.als.apiPayload.code.BaseCode;
import main.als.apiPayload.code.status.SuccessStatus;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;


// 왜 spinrgtest가 없을까? --> apiresult는 단일 클래스여서 다른 클래스를 사용할 필요가 없다.
public class ApiResultTest {

    @Test //result 없은 결과 값 테스트
    public void testOnSuccessWithoutResult() {
        // When
        ApiResult<Void> response = ApiResult.onSuccess();

        // Then
        assertThat(response.getIsSuccess(), is(true));
        assertThat(response.getCode(), is(SuccessStatus._OK.getCode()));
        assertThat(response.getMessage(), is(SuccessStatus._OK.getMessage()));
        assertThat(response.getResult(), is((Void) null)); // 결과가 null임을 확인
    }

    @Test // result 있는 결과 값 테스트
    public void testOnSuccessWithResult(){
        // Given
        String resultValue = "Success Data";

        // When
        ApiResult<String> response = ApiResult.onSuccess(resultValue);

        // Then
        assertThat(response.getIsSuccess(), is(true));
        assertThat(response.getCode(), is(SuccessStatus._OK.getCode()));
        assertThat(response.getMessage(), is(SuccessStatus._OK.getMessage()));
        assertThat(response.getResult(), is(resultValue));

    }

    @Test //cusomt eunm 클래스 테스트
    public void testOnSuccessCustomCode(){
        // Given
        BaseCode code = SuccessStatus._CREATED;

        String resultValue = "Custom Result Data";

        // When
        ApiResult<String> response = ApiResult.onSuccess(code, resultValue);

        // Then
        assertThat(response.getIsSuccess(), is(true));
        assertThat(response.getCode(), is(SuccessStatus._CREATED.getCode()));
        assertThat(response.getMessage(), is(SuccessStatus._CREATED.getMessage()));
        assertThat(response.getResult(), is(resultValue));

    }

}
