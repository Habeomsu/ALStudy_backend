package main.als.payment.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

public class PaymentRequestDto {


    @Getter
    public static class PaymentDto{

        @NotNull
        String orderId;
        @NotNull
        String amount;
        @NotNull
        String paymentKey;
        @NotNull
        Long userGroupId;

    }
}
