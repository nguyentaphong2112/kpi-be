package vn.kpi.annotations;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class EmailFormatValidator implements ConstraintValidator<EmailFormat, String> {

    private static final Logger LOGGER = LoggerFactory.getLogger(EmailFormatValidator.class);

    @Override
    public void initialize(EmailFormat emailFormat) {
        LOGGER.info("initialize EmailFormatValidator");
    }

    @Override
    public boolean isValid(String email, ConstraintValidatorContext constraintValidatorContext) {
        /**
         * Sua validate theo yeu cau giai phap
         * 1.Phai co ki tu @ o giua
         * 2. Cho phep nhap chu, so va .-_+
         * 3. email khong chua ki tu khoang trang
         */
        return StringUtils.isEmpty(email) || email.matches("[a-zA-Z0-9_\\.\\+-]+@[a-zA-Z0-9-]+\\.[a-zA-Z0-9-\\.]+");
    }
}
