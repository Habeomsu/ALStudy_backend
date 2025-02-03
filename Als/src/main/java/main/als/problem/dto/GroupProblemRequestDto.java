package main.als.problem.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class GroupProblemRequestDto {


    @Getter
    @Builder
    @RequiredArgsConstructor
    @AllArgsConstructor
    public static class GroupProblemDto{

        @NotNull(message = "문제 번호는 필수입니다.")
        private Long problem_id;

        @FutureOrPresent(message = "모집 기간은 오늘 이후여야 합니다.")
        private LocalDateTime deadline;

        @NotNull(message = "차감액은 필수입니다.")
        private BigDecimal deductionAmount;


    }

    @Getter
    @Builder
    @RequiredArgsConstructor
    @AllArgsConstructor
    public static class UpdateGroupProblemDto{

        @FutureOrPresent(message = "모집 기간은 오늘 이후여야 합니다.")
        private LocalDateTime deadline;

        @NotNull(message = "차감액은 필수입니다.")
        private BigDecimal deductionAmount;


    }

}
