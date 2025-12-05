package vn.hbtplus.annotations;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import vn.hbtplus.utils.Utils;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

@Slf4j
@Aspect
@Component
public class CheckOtpAspect {
    @Autowired
    private HttpServletRequest httpServletRequest;

    @Before("@annotation(vn.hbtplus.annotations.CheckOtp)")
    public void checkOtp(JoinPoint joinPoint) throws NoSuchFieldException, IllegalAccessException {
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        CheckOtp checkOtp = method.getAnnotation(CheckOtp.class);
        String otpKey = null;
        if (Utils.isNullOrEmpty(checkOtp.value())) {
            //lay gia tri otp tu request
            otpKey = httpServletRequest.getHeader("Client-Otp-Key");
        } else {
            String variable = checkOtp.value().substring(1);
            //lay gia tri otp tu tham so cua method
            java.lang.reflect.Parameter[] parameters = method.getParameters();
            Object[] args = joinPoint.getArgs();
            for (int i = 0; i < parameters.length; i++) {
                Parameter parameter = parameters[i];
                if (parameter.getName().equalsIgnoreCase(variable.split("\\.")[0])) {
                    otpKey = getValue(variable, args[i]);
                    break;
                }
            }
        }

        //thuc hien validate voi otpkey
        System.out.println(otpKey);
    }

    private String getValue(String domainId, Object obj) throws NoSuchFieldException, IllegalAccessException {
        if (obj == null) {
            return null;
        }
        if (domainId.contains(".")) {
            String variable = domainId.split("\\.")[1];
            Class<?> clazz = obj.getClass();
            Field field = clazz.getDeclaredField(variable);
            field.setAccessible(true); // Cho phép truy cập thuộc tính private
            // Lấy giá trị của thuộc tính từ đối tượng
            return field.get(obj) == null ? null : String.valueOf(field.get(obj));
        } else {
            return String.valueOf(obj);
        }
    }
}
