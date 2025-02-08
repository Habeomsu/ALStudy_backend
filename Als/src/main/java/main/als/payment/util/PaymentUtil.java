package main.als.payment.util;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class PaymentUtil {
    private static final Logger log = LoggerFactory.getLogger(PaymentUtil.class);
    private static final String WIDGET_SECRET_KEY = "test_gsk_docs_OaPz8L5KdmQXkzRz3y47BMw6"; // 시크릿 키 설정

    public static JSONObject confirmPayment(String orderId, String amount, String paymentKey) {
        JSONParser parser = new JSONParser();
        JSONObject obj = new JSONObject();
        obj.put("orderId", orderId);
        obj.put("amount", amount);
        obj.put("paymentKey", paymentKey);

        String authorizations = createAuthorizationHeader();

        try {
            URL url = new URL("https://api.tosspayments.com/v1/payments/confirm");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty("Authorization", authorizations);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);

            // 요청 본문 전송
            OutputStream outputStream = connection.getOutputStream();
            outputStream.write(obj.toString().getBytes(StandardCharsets.UTF_8));

            int code = connection.getResponseCode();
            InputStream responseStream = (code == 200) ? connection.getInputStream() : connection.getErrorStream();

            // 응답 처리
            Reader reader = new InputStreamReader(responseStream, StandardCharsets.UTF_8);
            JSONObject jsonObject = (JSONObject) parser.parse(reader);
            responseStream.close();

            log.info("Received response from Toss Payments API: {}", jsonObject.toJSONString());
            return jsonObject;
        } catch (Exception e) {
            log.error("결제 확인 중 오류 발생: ", e);
            return null;
        }
    }

    public static JSONObject processRefund(String paymentKey, BigDecimal refundAmount) {
        try {
            String refundApiUrl = "https://api.tosspayments.com/v1/payments/" + paymentKey + "/cancel";
            String authorizations = createAuthorizationHeader();

            // HTTP 연결 설정
            HttpURLConnection connection = (HttpURLConnection) new URL(refundApiUrl).openConnection();
            connection.setRequestProperty("Authorization", authorizations);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);

            // 요청 본문 작성
            JSONObject obj = new JSONObject();
            obj.put("cancelReason", "환급");
            obj.put("cancelAmount", refundAmount.toString());

            // 요청 본문 전송
            OutputStream outputStream = connection.getOutputStream();
            outputStream.write(obj.toString().getBytes(StandardCharsets.UTF_8));

            int code = connection.getResponseCode();
            InputStream responseStream = (code == 200) ? connection.getInputStream() : connection.getErrorStream();

            // 응답 처리
            Reader reader = new InputStreamReader(responseStream, StandardCharsets.UTF_8);
            JSONObject jsonResponse = (JSONObject) new JSONParser().parse(reader);
            responseStream.close();

            return jsonResponse;
        } catch (Exception e) {
            log.error("환급 요청 처리 중 오류 발생: ", e);
            return null;
        }
    }

    private static String createAuthorizationHeader() {
        Base64.Encoder encoder = Base64.getEncoder();
        byte[] encodedBytes = encoder.encode((WIDGET_SECRET_KEY + ":").getBytes(StandardCharsets.UTF_8));
        return "Basic " + new String(encodedBytes);
    }
}
