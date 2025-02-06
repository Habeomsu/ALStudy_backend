package main.als.payment.service;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import main.als.apiPayload.ApiResult;
import main.als.apiPayload.code.status.ErrorStatus;
import main.als.apiPayload.exception.GeneralException;
import main.als.group.entity.UserGroup;
import main.als.group.repository.UserGroupRepository;
import main.als.group.service.UserGroupService;
import main.als.payment.dto.PaymentRequestDto;
import main.als.payment.entity.Payment;
import main.als.payment.repository.PaymentRepository;
import org.glassfish.hk2.api.Self;
import org.json.simple.parser.JSONParser;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.springframework.stereotype.Service;

import java.io.*;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Service
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final UserGroupRepository userGroupRepository;

    public PaymentServiceImpl(PaymentRepository paymentRepository,UserGroupRepository userGroupRepository) {
        this.paymentRepository = paymentRepository;
        this.userGroupRepository = userGroupRepository;
    }

    @Override
    @Transactional
    public void confirmPayment(PaymentRequestDto.PaymentDto paymentDto) {
        JSONParser parser = new JSONParser();
        String orderId = paymentDto.getOrderId();
        String amount = paymentDto.getAmount();
        String paymentKey = paymentDto.getPaymentKey();
        Long userGroupId = paymentDto.getUserGroupId();

        UserGroup userGroup = userGroupRepository.findById(userGroupId)
                .orElseThrow(()->new GeneralException(ErrorStatus._NOT_FOUND_USERGROUP));

        // 충전이 이미 완료된 경우 예외 발생
        if (userGroup.isCharged()) {
            throw new GeneralException(ErrorStatus._ALREADY_CHARGED);
        }

        JSONObject obj = new JSONObject();
        obj.put("orderId", orderId);
        obj.put("amount", amount);
        obj.put("paymentKey", paymentKey);

        String widgetSecretKey = "test_gsk_docs_OaPz8L5KdmQXkzRz3y47BMw6";
        // 인증 정보 설정
        Base64.Encoder encoder = Base64.getEncoder();
        byte[] encodedBytes = encoder.encode((widgetSecretKey + ":").getBytes(StandardCharsets.UTF_8));
        String authorizations = "Basic " + new String(encodedBytes);

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

            // 응답 데이터 로그
            log.info("Received response from Toss Payments API: {}", jsonObject.toJSONString());


            // 결제 정보를 저장
            Payment payment = Payment.builder()
                    .paymentKey(jsonObject.get("paymentKey").toString())
                    .orderId(jsonObject.get("orderId").toString())
                    .requestedAt(jsonObject.get("requestedAt").toString())
                    .totalAmount(jsonObject.get("totalAmount").toString())
                    .build();

            paymentRepository.save(payment);

            userGroup.setUserDepositAmount(new BigDecimal(payment.getTotalAmount()));
            userGroup.setCharged(true);
            userGroup.setPaymentKey(payment.getPaymentKey());

            userGroupRepository.save(userGroup);


        } catch (IOException | ParseException e) {
            throw new GeneralException(ErrorStatus._TOSS_CONFIRM_FAIL);
        }


    }
}
