package main.als.payment.controller;

import main.als.apiPayload.ApiResult;
import main.als.payment.dto.PaymentRequestDto;
import main.als.payment.service.PaymentService;
import main.als.user.dto.CustomUserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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

    @GetMapping("/refund/{userGroupId}")
    public ApiResult<?> refund(@PathVariable Long userGroupId,
                               @AuthenticationPrincipal CustomUserDetails userDetails) {

        String username = userDetails.getUsername();
        paymentService.refundPayment(username,userGroupId);
        return ApiResult.onSuccess();
    }



}
