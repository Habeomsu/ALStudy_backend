package main.als.payment.service;

import main.als.payment.dto.PaymentRequestDto;

public interface PaymentService {
    public void confirmPayment(PaymentRequestDto.PaymentDto paymentDto);
    public void refundPayment(String username,Long userGroupId);
}
