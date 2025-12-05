package vn.kpi.utils.validates;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class PhoneNumberFormatValidator implements ConstraintValidator<PhoneNumberFormat, String> {
    private static final Logger LOGGER = LoggerFactory.getLogger(PhoneNumberFormatValidator.class);

    @Override
    public void initialize(PhoneNumberFormat phoneNumberFormat) {
        LOGGER.info("initialize PhoneNumberFormat");
    }

    @Override
    public boolean isValid(String phoneNumber, ConstraintValidatorContext constraintValidatorContext) {
        return StringUtils.isEmpty(phoneNumber) || phoneNumber.matches("^(0|\\+84)[0-9]{9}$");
    }
}
