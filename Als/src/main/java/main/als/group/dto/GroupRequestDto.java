package main.als.group.dto;


import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import main.als.valid.annotation.ValidStudyEndDate;

import java.math.BigDecimal;
import java.time.LocalDateTime;


public class GroupRequestDto {


    @Getter
    @Builder
    @AllArgsConstructor
    @ValidStudyEndDate
    public static class CreateGroupDto{

        @NotBlank(message = "그룹 이름은 필수입니다.")
        private String groupname;

        @NotBlank(message = "그룹 비밀번호는 필수입니다.")
        private String password;

        @DecimalMin(value = "10000", message = "예치금은 10,000원 이상이어야 합니다.")
        private BigDecimal depositAmount;

        @FutureOrPresent(message = "모집 기간은 오늘 이후여야 합니다.")
        @NotNull(message = "모집 마감기간은 필수입니다.")
        private LocalDateTime deadline; // 모집 마감일

        @Future(message = "스터디 종료일은 모집 기간 이후여야 합니다.")
        @NotNull(message = "스터디 종료일은 필수입니다.")
        private LocalDateTime studyEndDate; // 스터디 종료일

    }




}
