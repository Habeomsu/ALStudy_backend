package main.als.valid.annotation;


import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import main.als.valid.validator.StudyEndDateValidator;

import java.lang.annotation.*;


@Constraint(validatedBy = StudyEndDateValidator.class)
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidStudyEndDate {
    String message() default "스터디 종료일은 모집 기간 이후여야 합니다.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
