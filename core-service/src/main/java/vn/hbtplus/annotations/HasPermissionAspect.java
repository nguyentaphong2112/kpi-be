package vn.hbtplus.annotations;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.exceptions.BaseAppException;
import vn.hbtplus.feigns.PermissionFeignClient;
import vn.hbtplus.models.BaseResponse;
import vn.hbtplus.models.PermissionDataDto;
import vn.hbtplus.services.AuthorizationService;
import vn.hbtplus.utils.Utils;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.text.MessageFormat;
import java.util.List;

@Slf4j
@Aspect
@Component
public class HasPermissionAspect {
    @Autowired
    private PermissionFeignClient permissionFeignClient;
    @Autowired
    private HttpServletRequest request;
    @Autowired
    private AuthorizationService authorizationService;

    @Autowired
    public HasPermissionAspect() {
    }

    @Before("@annotation(vn.hbtplus.annotations.HasPermission)")
    public void beforeCheckPermission(JoinPoint joinPoint) throws NoSuchFieldException, IllegalAccessException {
        Class<?> targetClass = joinPoint.getTarget().getClass();
        Resource resourceAnnotation = AnnotationUtils.findAnnotation(targetClass, Resource.class);
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        HasPermission hasPermission = method.getAnnotation(HasPermission.class);

        String[] scopes = hasPermission.scope();
        String[] resources = Utils.isNullOrEmpty(hasPermission.resource()) ?
                (resourceAnnotation == null ? null : new String[]{resourceAnnotation.value()}) : hasPermission.resource();
        String domainId = hasPermission.domainId();
        String domainType = hasPermission.domainType();

        Parameter[] parameters = method.getParameters();
        Long id = null;
        Object[] args = joinPoint.getArgs();
        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];
            if (parameter.getName().equalsIgnoreCase(domainId.split("\\.")[0])) {
                id = getValue(domainId, args[i]);
            }
        }
        String userName = Utils.getUserNameLogin();
        if (scopes != null && scopes.length > 0 && !Utils.isNullOrEmpty(resources)) {
            if (Utils.isNullOrEmpty(domainId)) {
                boolean checkPermissison = false;
                for (String scope : scopes) {
                    for (String resource : resources) {
                        log.info(MessageFormat.format("Check quyen {0} - {1}", scope, resource));
                        if (authorizationService.checkPermission(scope, resource, userName)) {
                            return;
                        }
                    }
                }
                if (!checkPermissison) {
                    throw new BaseAppException(HttpStatus.FORBIDDEN, String.valueOf(BaseConstants.RESPONSE_STATUS.ERROR), "Bạn không có quyền thao tác với chức năng này!");
                }
            } else {
                boolean checkPermissison = false;
                for (String scope : scopes) {
                    for (String resource : resources) {
                        List<PermissionDataDto> permissionDataDtos = authorizationService.getPermissionData(scope, resource, userName);
                        log.info(MessageFormat.format("Check quyen {0} - {1} với {2} : {3}", scope, resource, domainType, id));
                        if (!Utils.isNullOrEmpty(permissionDataDtos)) {
                            return;
                        }
                    }
                }
                if (!checkPermissison) {
                    throw new BaseAppException("Bạn không có quyền thao tác với miền dữ liệu này!");
                }
            }
        }

    }

    private Long getValue(String domainId, Object obj) throws NoSuchFieldException, IllegalAccessException {
        if (domainId.contains(".")) {
            if (obj == null) {
                return null;
            }
            String variable = domainId.split("\\.")[1];
            Class<?> clazz = obj.getClass();
            // Lấy thuộc tính employeeId
            Field field = clazz.getDeclaredField("variable");
            field.setAccessible(true); // Cho phép truy cập thuộc tính private
            // Lấy giá trị của thuộc tính từ đối tượng
            return (Long) field.get(obj);
        } else {
            return (Long) obj;
        }
    }
}
