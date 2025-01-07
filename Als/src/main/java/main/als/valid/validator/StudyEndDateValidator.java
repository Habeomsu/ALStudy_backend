package main.als.valid.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import main.als.apiPayload.code.status.ErrorStatus;
import main.als.group.dto.GroupRequestDto;
import main.als.valid.annotation.ValidStudyEndDate;

public class StudyEndDateValidator implements ConstraintValidator<ValidStudyEndDate, GroupRequestDto.CreateGroupDto> {
    @Override
    public boolean isValid(GroupRequestDto.CreateGroupDto groupRequestDto, ConstraintValidatorContext context) {
        if (groupRequestDto.getStudyEndDate() == null || groupRequestDto.getDeadline() == null) {
            return true; // null 값은 검증하지 않음
        }
        // 종료일이 모집 마감일 이후인지 확인
        boolean isValid = groupRequestDto.getStudyEndDate().isAfter(groupRequestDto.getDeadline());

        // 유효성 검사 실패 시 사용자 정의 오류 메시지 설정
        if (!isValid) {
            context.disableDefaultConstraintViolation(); // 기본 오류 메시지 비활성화
            context.buildConstraintViolationWithTemplate(ErrorStatus._NOT_OVER_DEADLINE.toString()) // 사용자 정의 메시지
                    .addConstraintViolation(); // 오류 메시지 추가
        }

        return isValid; // 유효성 검사 결과 반환
    }
}
