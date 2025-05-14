package main.als.payment;


import main.als.apiPayload.code.status.ErrorStatus;
import main.als.apiPayload.exception.GeneralException;
import main.als.group.entity.Group;
import main.als.group.entity.UserGroup;
import main.als.group.repository.UserGroupRepository;
import main.als.payment.dto.PaymentRequestDto;
import main.als.payment.entity.Payment;
import main.als.payment.repository.PaymentRepository;
import main.als.payment.service.PaymentServiceImpl;
import main.als.payment.util.PaymentUtil;
import main.als.user.entity.User;
import org.json.simple.JSONObject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class PaymentServiceTest {

    @InjectMocks
    private PaymentServiceImpl paymentService;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private UserGroupRepository userGroupRepository;

    @Test
    @DisplayName("confirmPayment 테스트 - ( 성공 )")
    public void confirmPaymentSuceessTest(){

        Long userGroupId = 1L;
        String orderId = "orderId";
        BigDecimal amount = new BigDecimal("10000");
        String paymentKey = "paymentKey";

        User user = User.builder().build();

        Group group = Group.builder().build();

        UserGroup usergroup = UserGroup.builder()
                .id(userGroupId)
                .group(group)
                .charged(false)
                .build();

        PaymentRequestDto.PaymentDto paymentDto = PaymentRequestDto.PaymentDto.builder()
                .orderId(orderId)
                .amount(amount.toString())
                .paymentKey(paymentKey)
                .userGroupId(userGroupId)
                .build();

        JSONObject jsonResponse = new JSONObject();
        jsonResponse.put("paymentKey", paymentDto.getPaymentKey());
        jsonResponse.put("totalAmount", paymentDto.getAmount());
        jsonResponse.put("orderId", paymentDto.getOrderId());
        jsonResponse.put("requestedAt","2024-01-01T00:00:00Z");

        when(userGroupRepository.findById(userGroupId)).thenReturn(Optional.of(usergroup));

        try(MockedStatic<PaymentUtil> mockedStatic = mockStatic(PaymentUtil.class)) {

            mockedStatic.when(()->PaymentUtil.confirmPayment(orderId,amount.toString(),paymentKey))
                    .thenReturn(jsonResponse);

            paymentService.confirmPayment(paymentDto);

            // then
            verify(paymentRepository).save(any(Payment.class));
            verify(userGroupRepository).save(any(UserGroup.class));

            assertTrue(usergroup.isCharged());
            assertEquals(paymentKey,usergroup.getPaymentKey());
            assertEquals(amount, usergroup.getUserDepositAmount());

        }

    }

    @Test
    @DisplayName("confirmPayment 테스트 - ( 실패 - 유저그룹 없음 )")
    public void confirmPaymentFailTest1(){

        Long userGroupId = 1L;
        String orderId = "orderId";
        BigDecimal amount = new BigDecimal("10000");
        String paymentKey = "paymentKey";

        PaymentRequestDto.PaymentDto paymentDto = PaymentRequestDto.PaymentDto.builder()
                .orderId(orderId)
                .amount(amount.toString())
                .paymentKey(paymentKey)
                .userGroupId(userGroupId)
                .build();

        when(userGroupRepository.findById(userGroupId)).thenReturn(Optional.empty());

        GeneralException exception = assertThrows(GeneralException.class, () -> paymentService.confirmPayment(paymentDto));

        assertFalse(exception.getErrorReasonHttpStatus().getIsSuccess());
        assertEquals(ErrorStatus._NOT_FOUND_USERGROUP.getCode(), exception.getErrorReasonHttpStatus().getCode());
        assertEquals(ErrorStatus._NOT_FOUND_USERGROUP.getMessage(), exception.getErrorReasonHttpStatus().getMessage());
        assertEquals(ErrorStatus._NOT_FOUND_USERGROUP.getHttpStatus(), exception.getErrorReasonHttpStatus().getHttpStatus());

    }

    @Test
    @DisplayName("confirmPayment 테스트 - ( 실패 - 이미 충전함 )")
    public void confirmPaymentFailTest2(){

        Long userGroupId = 1L;
        String orderId = "orderId";
        BigDecimal amount = new BigDecimal("10000");
        String paymentKey = "paymentKey";

        User user = User.builder().build();

        Group group = Group.builder().build();

        UserGroup usergroup = UserGroup.builder()
                .id(userGroupId)
                .group(group)
                .charged(true)
                .build();

        PaymentRequestDto.PaymentDto paymentDto = PaymentRequestDto.PaymentDto.builder()
                .orderId(orderId)
                .amount(amount.toString())
                .paymentKey(paymentKey)
                .userGroupId(userGroupId)
                .build();

        when(userGroupRepository.findById(userGroupId)).thenReturn(Optional.of(usergroup));

        GeneralException exception = assertThrows(GeneralException.class, () -> paymentService.confirmPayment(paymentDto));

        assertFalse(exception.getErrorReasonHttpStatus().getIsSuccess());
        assertEquals(ErrorStatus._ALREADY_CHARGED.getCode(), exception.getErrorReasonHttpStatus().getCode());
        assertEquals(ErrorStatus._ALREADY_CHARGED.getMessage(), exception.getErrorReasonHttpStatus().getMessage());
        assertEquals(ErrorStatus._ALREADY_CHARGED.getHttpStatus(), exception.getErrorReasonHttpStatus().getHttpStatus());

    }

    @Test
    @DisplayName("confirmPayment 테스트 - ( 실패 - 응닶값 없음 )")
    public void confirmPaymentFailTest3(){

        Long userGroupId = 1L;
        String orderId = "orderId";
        BigDecimal amount = new BigDecimal("10000");
        String paymentKey = "paymentKey";

        User user = User.builder().build();

        Group group = Group.builder().build();

        UserGroup usergroup = UserGroup.builder()
                .id(userGroupId)
                .group(group)
                .charged(false)
                .build();

        PaymentRequestDto.PaymentDto paymentDto = PaymentRequestDto.PaymentDto.builder()
                .orderId(orderId)
                .amount(amount.toString())
                .paymentKey(paymentKey)
                .userGroupId(userGroupId)
                .build();
        when(userGroupRepository.findById(userGroupId)).thenReturn(Optional.of(usergroup));

        try(MockedStatic<PaymentUtil> mockedStatic = mockStatic(PaymentUtil.class)) {

            mockedStatic.when(()->PaymentUtil.confirmPayment(orderId,amount.toString(),paymentKey))
                    .thenReturn(null);

            GeneralException exception = assertThrows(GeneralException.class, () -> paymentService.confirmPayment(paymentDto));

            assertFalse(exception.getErrorReasonHttpStatus().getIsSuccess());
            assertEquals(ErrorStatus._TOSS_CONFIRM_FAIL.getCode(), exception.getErrorReasonHttpStatus().getCode());
            assertEquals(ErrorStatus._TOSS_CONFIRM_FAIL.getMessage(), exception.getErrorReasonHttpStatus().getMessage());
            assertEquals(ErrorStatus._TOSS_CONFIRM_FAIL.getHttpStatus(), exception.getErrorReasonHttpStatus().getHttpStatus());

        }

    }

    @Test
    @DisplayName("confirmPayment 테스트 - ( 실패 - 정보 저장 오류 )")
    public void confirmPaymentFailTest4(){

        Long userGroupId = 1L;
        String orderId = "orderId";
        BigDecimal amount = new BigDecimal("10000");
        String paymentKey = "paymentKey";

        User user = User.builder().build();

        Group group = Group.builder().build();

        UserGroup usergroup = UserGroup.builder()
                .id(userGroupId)
                .group(group)
                .charged(false)
                .build();

        PaymentRequestDto.PaymentDto paymentDto = PaymentRequestDto.PaymentDto.builder()
                .orderId(orderId)
                .amount(amount.toString())
                .paymentKey(paymentKey)
                .userGroupId(userGroupId)
                .build();

        JSONObject jsonResponse = new JSONObject();
        jsonResponse.put("paymentKey", paymentDto.getPaymentKey());
        jsonResponse.put("totalAmount", paymentDto.getAmount());
        jsonResponse.put("orderId", paymentDto.getOrderId());
        jsonResponse.put("requestedAt","2024-01-01T00:00:00Z");

        when(userGroupRepository.findById(userGroupId)).thenReturn(Optional.of(usergroup));

        try(MockedStatic<PaymentUtil> mockedStatic = mockStatic(PaymentUtil.class)) {

            mockedStatic.when(()->PaymentUtil.confirmPayment(orderId,amount.toString(),paymentKey))
                    .thenReturn(jsonResponse);

            mockedStatic.when(() -> PaymentUtil.processRefund(paymentKey,amount))
                    .thenReturn(jsonResponse);

            // payment 저장 시 예외 발생
            when(paymentRepository.save(any(Payment.class))).thenThrow(new RuntimeException("DB error"));

            GeneralException exception = assertThrows(GeneralException.class, () -> paymentService.confirmPayment(paymentDto));

            assertFalse(exception.getErrorReasonHttpStatus().getIsSuccess());
            assertEquals(ErrorStatus._TOSS_SAVE_FAIL.getCode(), exception.getErrorReasonHttpStatus().getCode());
            assertEquals(ErrorStatus._TOSS_SAVE_FAIL.getMessage(), exception.getErrorReasonHttpStatus().getMessage());
            assertEquals(ErrorStatus._TOSS_SAVE_FAIL.getHttpStatus(), exception.getErrorReasonHttpStatus().getHttpStatus());

        }

    }

    @Test
    @DisplayName("refundPayment 테스트 - ( 성공 )")
    public void refundPaymentSuccessTest1(){

        Long userGroupId = 1L;
        String userName = "userName";
        String paymentKey = "paymentKey";
        BigDecimal amount = new BigDecimal("10000");

        LocalDateTime now = LocalDateTime.now();

        User user = User.builder()
                .username(userName)
                .build();

        Group group = Group.builder()
                .studyEndDate(now.minusDays(1))
                .build();

        UserGroup usergroup = UserGroup.builder()
                .id(userGroupId)
                .user(user)
                .group(group)
                .paymentKey(paymentKey)
                .charged(false)
                .userDepositAmount(amount)
                .build();

        JSONObject jsonResponse = new JSONObject();
        jsonResponse.put("status", "CANCELED");
        jsonResponse.put("message", "success");

        when(userGroupRepository.findById(userGroupId)).thenReturn(Optional.of(usergroup));

        try(MockedStatic<PaymentUtil> mockedStatic = mockStatic(PaymentUtil.class)) {
            mockedStatic.when(()->PaymentUtil.processRefund(paymentKey,amount))
                    .thenReturn(jsonResponse);

            paymentService.refundPayment(userName,userGroupId);

            verify(userGroupRepository).save(any(UserGroup.class));

            assertEquals(BigDecimal.ZERO, usergroup.getUserDepositAmount());
            assertTrue(usergroup.isRefunded());

        }

    }

    @Test
    @DisplayName("refundPayment 테스트 - ( 성공 - 50% 환불 )")
    public void refundPaymentSuccessTest2() {
        Long userGroupId = 1L;
        String userName = "userName";
        String paymentKey = "paymentKey";
        BigDecimal originalAmount = new BigDecimal("10000");
        BigDecimal expectedRefund = originalAmount.multiply(BigDecimal.valueOf(0.5));

        User user = User.builder().username(userName).build();
        Group group = Group.builder().studyEndDate(LocalDateTime.now().plusDays(5)).build(); // 아직 종료되지 않음
        UserGroup usergroup = UserGroup.builder()
                .id(userGroupId)
                .user(user)
                .group(group)
                .paymentKey(paymentKey)
                .userDepositAmount(originalAmount)
                .build();

        JSONObject response = new JSONObject();
        response.put("status", "PARTIAL_CANCELED");

        when(userGroupRepository.findById(userGroupId)).thenReturn(Optional.of(usergroup));

        try (MockedStatic<PaymentUtil> mockedStatic = mockStatic(PaymentUtil.class)) {
            mockedStatic.when(() -> PaymentUtil.processRefund(paymentKey, expectedRefund)).thenReturn(response);

            paymentService.refundPayment(userName, userGroupId);

            assertEquals(BigDecimal.ZERO, usergroup.getUserDepositAmount());
            assertTrue(usergroup.isRefunded());
            verify(userGroupRepository).save(usergroup);
        }
    }

    @Test
    @DisplayName("refundPayment 테스트 - ( 실패 - 유저그룹 없음 )")
    public void refundPaymentFailTest1(){

        Long userGroupId = 1L;
        String userName = "userName";
        String paymentKey = "paymentKey";
        BigDecimal amount = new BigDecimal("10000");

        when(userGroupRepository.findById(userGroupId)).thenReturn(Optional.empty());

        GeneralException exception = assertThrows(GeneralException.class, () -> paymentService.refundPayment(userName,userGroupId));

        assertFalse(exception.getErrorReasonHttpStatus().getIsSuccess());
        assertEquals(ErrorStatus._NOT_FOUND_USERGROUP.getCode(), exception.getErrorReasonHttpStatus().getCode());
        assertEquals(ErrorStatus._NOT_FOUND_USERGROUP.getMessage(), exception.getErrorReasonHttpStatus().getMessage());
        assertEquals(ErrorStatus._NOT_FOUND_USERGROUP.getHttpStatus(), exception.getErrorReasonHttpStatus().getHttpStatus());

    }

    @Test
    @DisplayName("refundPayment 테스트 - ( 실패 - 그룹 사용자가 아님 )")
    public void refundPaymentFailTest2(){

        Long userGroupId = 1L;
        String userName = "userName";
        String paymentKey = "paymentKey";
        BigDecimal amount = new BigDecimal("10000");

        LocalDateTime now = LocalDateTime.now();

        User user = User.builder()
                .username("test")
                .build();

        Group group = Group.builder()
                .studyEndDate(now.minusDays(1))
                .build();

        UserGroup usergroup = UserGroup.builder()
                .id(userGroupId)
                .user(user)
                .group(group)
                .paymentKey(paymentKey)
                .charged(false)
                .userDepositAmount(amount)
                .build();

        when(userGroupRepository.findById(userGroupId)).thenReturn(Optional.of(usergroup));

        GeneralException exception = assertThrows(GeneralException.class, () -> paymentService.refundPayment(userName,userGroupId));

        assertFalse(exception.getErrorReasonHttpStatus().getIsSuccess());
        assertEquals(ErrorStatus._NOT_IN_USERGROUP.getCode(), exception.getErrorReasonHttpStatus().getCode());
        assertEquals(ErrorStatus._NOT_IN_USERGROUP.getMessage(), exception.getErrorReasonHttpStatus().getMessage());
        assertEquals(ErrorStatus._NOT_IN_USERGROUP.getHttpStatus(), exception.getErrorReasonHttpStatus().getHttpStatus());

    }

    @Test
    @DisplayName("refundPayment 테스트 - ( 실패 - 페이먼트키가 없음 )")
    public void refundPaymentFailTest3(){

        Long userGroupId = 1L;
        String userName = "userName";
        String paymentKey = "paymentKey";
        BigDecimal amount = new BigDecimal("10000");

        LocalDateTime now = LocalDateTime.now();

        User user = User.builder()
                .username(userName)
                .build();

        Group group = Group.builder()
                .studyEndDate(now.minusDays(1))
                .build();

        UserGroup usergroup = UserGroup.builder()
                .id(userGroupId)
                .user(user)
                .group(group)
                .paymentKey(null)
                .charged(false)
                .userDepositAmount(amount)
                .build();

        when(userGroupRepository.findById(userGroupId)).thenReturn(Optional.of(usergroup));

        GeneralException exception = assertThrows(GeneralException.class, () -> paymentService.refundPayment(userName,userGroupId));

        assertFalse(exception.getErrorReasonHttpStatus().getIsSuccess());
        assertEquals(ErrorStatus._PAYMENT_KEY_NOT_FOUND.getCode(), exception.getErrorReasonHttpStatus().getCode());
        assertEquals(ErrorStatus._PAYMENT_KEY_NOT_FOUND.getMessage(), exception.getErrorReasonHttpStatus().getMessage());
        assertEquals(ErrorStatus._PAYMENT_KEY_NOT_FOUND.getHttpStatus(), exception.getErrorReasonHttpStatus().getHttpStatus());

    }

    @Test
    @DisplayName("refundPayment 테스트 - ( 실패 - 예치금은 0원보다 작거나 같음 )")
    public void refundPaymentFailTest4(){

        Long userGroupId = 1L;
        String userName = "userName";
        String paymentKey = "paymentKey";
        BigDecimal amount = new BigDecimal("0");

        LocalDateTime now = LocalDateTime.now();

        User user = User.builder()
                .username(userName)
                .build();

        Group group = Group.builder()
                .studyEndDate(now.minusDays(1))
                .build();

        UserGroup usergroup = UserGroup.builder()
                .id(userGroupId)
                .user(user)
                .group(group)
                .paymentKey(paymentKey)
                .charged(false)
                .userDepositAmount(amount)
                .build();

        when(userGroupRepository.findById(userGroupId)).thenReturn(Optional.of(usergroup));

        GeneralException exception = assertThrows(GeneralException.class, () -> paymentService.refundPayment(userName,userGroupId));

        assertFalse(exception.getErrorReasonHttpStatus().getIsSuccess());
        assertEquals(ErrorStatus._NO_AVAILABLE_DEPOSIT.getCode(), exception.getErrorReasonHttpStatus().getCode());
        assertEquals(ErrorStatus._NO_AVAILABLE_DEPOSIT.getMessage(), exception.getErrorReasonHttpStatus().getMessage());
        assertEquals(ErrorStatus._NO_AVAILABLE_DEPOSIT.getHttpStatus(), exception.getErrorReasonHttpStatus().getHttpStatus());

    }

    @Test
    @DisplayName("refundPayment 테스트 - ( 실패 - 환불 실패 )")
    public void refundPaymentFailTest5(){

        Long userGroupId = 1L;
        String userName = "userName";
        String paymentKey = "paymentKey";
        BigDecimal amount = new BigDecimal("10000");

        LocalDateTime now = LocalDateTime.now();

        User user = User.builder()
                .username(userName)
                .build();

        Group group = Group.builder()
                .studyEndDate(now.minusDays(1))
                .build();

        UserGroup usergroup = UserGroup.builder()
                .id(userGroupId)
                .user(user)
                .group(group)
                .paymentKey(paymentKey)
                .charged(false)
                .userDepositAmount(amount)
                .build();

        JSONObject jsonResponse = new JSONObject();
        jsonResponse.put("status", "success");
        jsonResponse.put("message", "success");

        when(userGroupRepository.findById(userGroupId)).thenReturn(Optional.of(usergroup));

        try(MockedStatic<PaymentUtil> mockedStatic = mockStatic(PaymentUtil.class)) {
            mockedStatic.when(()->PaymentUtil.processRefund(paymentKey,amount))
                    .thenReturn(jsonResponse);

            GeneralException exception = assertThrows(GeneralException.class, () -> paymentService.refundPayment(userName,userGroupId));

            assertFalse(exception.getErrorReasonHttpStatus().getIsSuccess());
            assertEquals(ErrorStatus._REFUND_FAILED.getCode(), exception.getErrorReasonHttpStatus().getCode());
            assertEquals(ErrorStatus._REFUND_FAILED.getMessage(), exception.getErrorReasonHttpStatus().getMessage());
            assertEquals(ErrorStatus._REFUND_FAILED.getHttpStatus(), exception.getErrorReasonHttpStatus().getHttpStatus());

        }

    }

}
