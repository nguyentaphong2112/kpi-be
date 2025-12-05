package vn.kpi.annotations;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.multipart.MultipartFile;
import vn.kpi.configs.MdcForkJoinPool;
import vn.kpi.models.dto.UserLogActivityDto;
import vn.kpi.feigns.PermissionFeignClient;
import vn.kpi.utils.JsonUtil;
import vn.kpi.utils.Utils;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Aspect
@Component
public class UserLogActivityAspect {

    @Autowired
    private PermissionFeignClient permissionFeignClient;

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private MdcForkJoinPool forkJoinPool;

    @Autowired
    public UserLogActivityAspect() {
    }

    @Before("@annotation(vn.kpi.annotations.UserLogActivity)")
    public void before(JoinPoint joinPoint) {
        try {
            Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
            String methodName = request.getMethod();
            String uri[];
            switch (methodName) {
                case "GET":
                    GetMapping getMapping = method.getAnnotation(GetMapping.class);
                    uri = getMapping.value();
                    break;
                case "POST":
                    PostMapping postMapping = method.getAnnotation(PostMapping.class);
                    uri = postMapping.value();
                    break;
                case "PUT":
                    PutMapping putMapping = method.getAnnotation(PutMapping.class);
                    uri = putMapping.value();
                    break;
                case "DELETE":
                    DeleteMapping deleteMapping = method.getAnnotation(DeleteMapping.class);
                    uri = deleteMapping.value();
                    break;
                default:
                    uri = new String[]{};
                    break;
            }
            Object[] args = joinPoint.getArgs();
            Map<String, Object> logData = new HashMap<>();
            int index = 0;
            for (Object arg : args) {
                if (arg instanceof HttpServletRequest) {
                    HttpServletRequest request = (HttpServletRequest) arg;
                    logData.put("request" + (index++), getHttpRequestData(request));
                } else if (arg instanceof MultipartFile) {
                    MultipartFile file = (MultipartFile) arg;
                    logData.put("file" + (index++), Map.of("fileName", file.getOriginalFilename(), "size", file.getSize()));
                } else if (arg instanceof List) {
                    List<Object> objects = (List<Object>) arg;
                    for (Object obj : objects) {
                        if(obj instanceof MultipartFile) {
                            MultipartFile file = (MultipartFile) obj;
                            logData.put("file" + (index++), Map.of("fileName", file.getOriginalFilename(), "size", file.getSize()));
                        } else {
                            logData.put("data" + (index++), JsonUtil.toJson(obj));
                        }
                    }
                } else {
                    logData.put("data" + (index++), arg);
                }
            }

            UserLogActivityDto dto = UserLogActivityDto.builder().loginName(Utils.getUserNameLogin())
                    .method(methodName).uri(StringUtils.join(uri, ",")).ipAddress(request.getRemoteAddr()).data(JsonUtil.toJson(logData)).build();

            HttpHeaders headers = Utils.getRequestHeader(request);
            forkJoinPool.execute(() -> saveUserLogActivity(headers, dto));
        } catch (Exception ex) {
            log.error("[UserLogActivityAspect.before] error ", ex);
        }
    }

    private void saveUserLogActivity(HttpHeaders headers, UserLogActivityDto dto) {
        permissionFeignClient.saveUserLogActivity(headers, dto);
    }

    private Map<String, Object> getHttpRequestData(HttpServletRequest request) {
        Map<String, Object> requestData = new HashMap<>();
        requestData.put("url", request.getRequestURL().toString());
        requestData.put("method", request.getMethod());

        return requestData;
    }
}
