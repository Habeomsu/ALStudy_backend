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
import main.als.payment.util.PaymentUtil;
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
import java.time.LocalDateTime;
import java.util.Base64;

@Service
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final UserGroupRepository userGroupRepository;

    public PaymentServiceImpl(PaymentRepository paymentRepository,UserGroupRepository userGroupRepository
                              ) {
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

        // 결제 확인 요청
        JSONObject jsonResponse = PaymentUtil.confirmPayment(orderId, amount, paymentKey);

        if (jsonResponse == null) {
            throw new GeneralException(ErrorStatus._TOSS_CONFIRM_FAIL);
        }

        // 결제 정보를 저장
        Payment payment = Payment.builder()
                .paymentKey(jsonResponse.get("paymentKey").toString())
                .orderId(jsonResponse.get("orderId").toString())
                .requestedAt(jsonResponse.get("requestedAt").toString())
                .totalAmount(jsonResponse.get("totalAmount").toString())
                .build();

        try {
            paymentRepository.save(payment); // 결제 정보 저장

            // UserGroup 업데이트
            userGroup.setUserDepositAmount(new BigDecimal(payment.getTotalAmount()));
            userGroup.setCharged(true);
            userGroup.setPaymentKey(payment.getPaymentKey());

            userGroupRepository.save(userGroup); // UserGroup 저장

        } catch (Exception e) {
            // 결제 저장 또는 UserGroup 업데이트 중 에러 발생 시 환불 처리
            BigDecimal refundAmount = new BigDecimal(amount);
            JSONObject refundResponse = PaymentUtil.processRefund(paymentKey, refundAmount);
            throw new GeneralException(ErrorStatus._TOSS_SAVE_FAIL); // 예외 던지기
        }


    }

    @Override
    public void refundPayment(String username, Long userGroupId) {

        UserGroup userGroup = userGroupRepository.findById(userGroupId)
                .orElseThrow(() -> new GeneralException(ErrorStatus._NOT_FOUND_USERGROUP));

        if (!userGroup.getUser().getUsername().equals(username)) {
            throw new GeneralException(ErrorStatus._NOT_IN_USERGROUP);
        }

        String paymentKey = userGroup.getPaymentKey();
        if (paymentKey == null || paymentKey.isEmpty()) {
            throw new GeneralException(ErrorStatus._PAYMENT_KEY_NOT_FOUND);
        }

        BigDecimal refundAmount = userGroup.getUserDepositAmount();
        if (refundAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new GeneralException(ErrorStatus._NO_AVAILABLE_DEPOSIT);
        }

        LocalDateTime now = LocalDateTime.now();

        if (userGroup.getGroup().getStudyEndDate().isAfter(now)) {
            refundAmount = refundAmount.multiply(BigDecimal.valueOf(0.5)); // 50% 환급
        }

        JSONObject refundResponse = PaymentUtil.processRefund(paymentKey, refundAmount);

        // 환급 응답 콘솔 출력
        log.info("환급 응답: {}", refundResponse);

        // 환급 상태 확인
        String status = (String) refundResponse.get("status");
        if (status == null || (!status.equals("CANCELED") && !status.equals("PARTIAL_CANCELED"))) {
            String errorMessage = (String) refundResponse.get("message");
            log.error("환급 요청 실패: {}", errorMessage != null ? errorMessage : "환급 요청에 실패했습니다.");
            throw new GeneralException(ErrorStatus._REFUND_FAILED);
        }

        userGroup.setUserDepositAmount(BigDecimal.ZERO); // 환급 후 예치금을 0으로 설정
        userGroup.setRefunded(true); // 환급 후 charged 상태를 false로 설정

        userGroupRepository.save(userGroup);



    }
}
