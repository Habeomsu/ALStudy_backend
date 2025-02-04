package main.als.problem.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import main.als.apiPayload.code.status.ErrorStatus;
import main.als.apiPayload.exception.GeneralException;
import main.als.problem.entity.TestCase;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FlaskCommunicationUtil {

    private static final String FLASK_URL = "http://localhost:5001/grade"; // Flask 서버 주소

    public static ResponseEntity<Map> submitToFlask(MultipartFile file, List<TestCase> testCases) throws IOException, JsonProcessingException {
        // Flask 서버와 통신
        RestTemplate restTemplate = new RestTemplate();

        // 임시 파일 생성
        File tempFile = createTempFile(file);

        // 요청 본체 생성
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("code", new FileSystemResource(tempFile)); // 실제 파일을 전송

        // 테스트 케이스 리스트 생성
        List<Map<String, String>> testCaseList = createTestCaseList(testCases);

        // 테스트 케이스 JSON 변환
        ObjectMapper objectMapper = new ObjectMapper();
        String testCasesJson = objectMapper.writeValueAsString(testCaseList);

        // JSON 문자열을 MultiValueMap에 추가
        body.add("test_cases", testCasesJson); // JSON 형태로 추가

        // HTTP 헤더 설정
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        try {
            // Flask 서버에 POST 요청
            return restTemplate.postForEntity(FLASK_URL, requestEntity, Map.class);
        } catch (ResourceAccessException e) {
            throw new GeneralException(ErrorStatus._FLASK_SERVER_ERROR);
        } catch (HttpClientErrorException e) {
            throw new GeneralException(ErrorStatus._FLASK_SERVER_ERROR);
        }
    }

    private static File createTempFile(MultipartFile file) throws IOException {
        File tempFile = File.createTempFile("submission", "-" + file.getOriginalFilename());
        file.transferTo(tempFile); // MultipartFile 내용을 임시 파일로 저장
        return tempFile;
    }

    private static List<Map<String, String>> createTestCaseList(List<TestCase> testCases) {
        List<Map<String, String>> testCaseList = new ArrayList<>();
        for (TestCase testCase : testCases) {
            Map<String, String> caseMap = new HashMap<>();
            caseMap.put("input", testCase.getInput());
            caseMap.put("expected_output", testCase.getExpectedOutput());
            testCaseList.add(caseMap);
        }
        return testCaseList;
    }
}
