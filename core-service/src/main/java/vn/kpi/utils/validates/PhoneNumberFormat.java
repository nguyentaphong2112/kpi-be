package vn.kpi.utils.validates;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(
        validatedBy = PhoneNumberFormatValidator.class
)
public @interface PhoneNumberFormat {
    String message() default "Số điện thoại không đúng định dạng";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
