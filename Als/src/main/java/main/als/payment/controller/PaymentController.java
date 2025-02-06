package main.als.payment.controller;

import main.als.apiPayload.ApiResult;
import main.als.payment.dto.PaymentRequestDto;
import main.als.payment.service.PaymentService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/payment")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/confirm")
    public ApiResult<?> confirm(@RequestBody PaymentRequestDto.PaymentDto paymentDto) {

        paymentService.confirmPayment(paymentDto);
        return ApiResult.onSuccess();
    }

}
