package main.als.group.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

public class UserGroupRequestDto {

    @Getter
    @Builder
    @AllArgsConstructor
    public static class joinGroupDto{

        @NotBlank(message = "그룹 비밀번호는 필수입니다.")
        private String password;

    }


}
