package vn.hbtplus.configs;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.ThreadContext;
import org.springframework.core.MethodParameter;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdviceAdapter;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;
import vn.hbtplus.utils.JsonUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@ControllerAdvice
@RequiredArgsConstructor
@Slf4j
public class HttpMonitoringInterceptor extends RequestBodyAdviceAdapter
        implements HandlerInterceptor, ResponseBodyAdvice<Object> {
    private static final String START_TIME = "startTime";
    public static final String SERVICE_HEADER = "serviceHeader";
    private static final String SERVICE_MESSAGE_ID = "serviceMessageId";
    private static final String LOG_TYPE = "logType";
    private static final String DURATION = "duration";
    private static final String HTTP_REQUEST = "httprequest";
    private static final String HTTP_RESPONSE = "httpresponse";
    private static final String RESPONSE_CODE = "responseCode";
    public static final String CLIENT_MESSAGE_ID = "clientMessageId";
    public static final String TRANSACTION_ID = "transactionId";

    private final HttpServletRequest httpServletRequest;
    private final HttpServletResponse httpServletResponse;

    private static String getFullURL(HttpServletRequest request) {
        var requestURL = new StringBuilder(request.getRequestURL().toString());
        var queryString = request.getQueryString();
        return queryString == null
                ? requestURL.toString()
                : requestURL.append('?').append(queryString).toString();
    }

    private static ServiceHeader createServiceHeader(HttpServletRequest httpRequest) {
        var servicePath =
                httpRequest.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        var xForwardedFor = httpRequest.getHeader("x-forwarded-for");
        String currentUser = null;
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) currentUser = String.valueOf(auth.getPrincipal());
        var clientMessageId = httpRequest.getHeader(CLIENT_MESSAGE_ID);
        clientMessageId = clientMessageId == null ? UUID.randomUUID().toString() : clientMessageId;
        var transactionId = httpRequest.getHeader(TRANSACTION_ID);
        transactionId = transactionId == null ? UUID.randomUUID().toString() : transactionId;

        return ServiceHeader.builder()
                .messageTimeStamp(new Date())
                .servicePath(servicePath != null ? servicePath.toString() : null)
                .sourceAppIp(xForwardedFor == null ? httpRequest.getRemoteHost() : xForwardedFor)
                .destAppIp(httpRequest.getLocalAddr())
                .destAppPort(httpRequest.getLocalPort())
                .httpMethod(httpRequest.getMethod())
                .httpPath(getFullURL(httpRequest))
                .clientMessageId(clientMessageId)
                .transactionId(transactionId)
                .authorization(httpRequest.getHeader(HttpHeaders.AUTHORIZATION))
                .authenticationUser(currentUser)
                .build();
    }

    @Override
    public boolean preHandle(
            HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) {
        if (StringUtils.equalsAny(
                request.getMethod(), HttpMethod.GET.name(), HttpMethod.POST.name(), HttpMethod.DELETE.name())) {

            request.setAttribute(START_TIME, System.nanoTime());
            var serviceHeader = createServiceHeader(request);
            request.setAttribute(SERVICE_HEADER, serviceHeader);
            ThreadContext.put(SERVICE_MESSAGE_ID, serviceHeader.getServiceMessageId());
            ThreadContext.put(SERVICE_HEADER, serviceHeader.toString());
            ThreadContext.put(LOG_TYPE, HTTP_REQUEST);
            ThreadContext.remove(DURATION);
            log.info(
                    HttpMessageObject.builder()
                            .sourceAppIp(request.getRemoteHost())
                            .destAppIp(request.getLocalAddr())
                            .destAppPort(request.getLocalPort())
                            .httpMethod(request.getMethod())
                            .httpPath(request.getRequestURI())
                            .header(
                                    Collections.list(request.getHeaderNames()).stream()
                                            .collect(
                                                    Collectors.toMap(
                                                            k -> k,
                                                            k -> {
                                                                if (StringUtils.equalsIgnoreCase(k, HttpHeaders.AUTHORIZATION))
                                                                    return "<<Not recorded to log>>";
                                                                return request.getHeader(k);
                                                            })))
                            .build()
                            .toString());
            ThreadContext.remove(LOG_TYPE);
        }
        return true;
    }

    @Override
    public boolean supports(
            @NonNull MethodParameter methodParameter,
            @NonNull Type targetType,
            @NonNull Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

    @Override
    public @NonNull Object afterBodyRead(
            @NonNull Object body,
            @NonNull HttpInputMessage inputMessage,
            @NonNull MethodParameter parameter,
            @NonNull Type targetType,
            @NonNull Class<? extends HttpMessageConverter<?>> converterType) {
        this.httpServletRequest.setAttribute(START_TIME, System.nanoTime());
        var serviceHeader = createServiceHeader(this.httpServletRequest);
        this.httpServletRequest.setAttribute(SERVICE_HEADER, serviceHeader);
        ThreadContext.put(SERVICE_MESSAGE_ID, serviceHeader.getServiceMessageId());
        ThreadContext.put(SERVICE_HEADER, serviceHeader.toString());
        ThreadContext.put(LOG_TYPE, HTTP_REQUEST);
        ThreadContext.remove(DURATION);
		// Nếu body là file hoặc byte thì KHÔNG log nội dung
        String bodyString;
        if (body instanceof MultipartFile
            || body instanceof MultipartFile[]
            || body instanceof byte[]
            || body instanceof Byte[]
            || body instanceof InputStream
            || body instanceof Resource) {
            bodyString = "[binary content omitted]";
        } else {
            try {
                bodyString = JsonUtil.toJson(body);
            } catch (Exception e) {
                bodyString = "[unserializable body]";
            }
        }
        log.info(
                HttpMessageObject.builder()
                        .body(bodyString)
                        .sourceAppIp(httpServletRequest.getRemoteHost())
                        .destAppIp(httpServletRequest.getLocalAddr())
                        .destAppPort(httpServletRequest.getLocalPort())
                        .httpMethod(httpServletRequest.getMethod())
                        .httpPath(httpServletRequest.getRequestURI())
                        .header(
                                Collections.list(httpServletRequest.getHeaderNames()).stream()
                                        .collect(
                                                Collectors.toMap(
                                                        k -> k,
                                                        k -> {
                                                            if (StringUtils.equalsIgnoreCase(k, HttpHeaders.AUTHORIZATION))
                                                                return "<<Not recorded to log>>";
                                                            return httpServletRequest.getHeader(k);
                                                        })))
                        .build()
                        .toString());
        ThreadContext.remove(LOG_TYPE);

        return super.afterBodyRead(body, inputMessage, parameter, targetType, converterType);
    }

    @Override
    public boolean supports(
            @NonNull MethodParameter returnType,
            @NonNull Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

    @Override
    public Object beforeBodyWrite(
            Object body,
            @NonNull MethodParameter returnType,
            @NonNull MediaType selectedContentType,
            @NonNull Class<? extends HttpMessageConverter<?>> selectedConverterType,
            @NonNull ServerHttpRequest request,
            @NonNull ServerHttpResponse response) {
        var serviceMessageId = ThreadContext.get(SERVICE_MESSAGE_ID);
        if (serviceMessageId == null) return body;

        long elapsed = -1;
        var startTime = httpServletRequest.getAttribute(START_TIME);
        if (startTime != null) elapsed = System.nanoTime() - (Long) startTime;
        ThreadContext.put(LOG_TYPE, HTTP_RESPONSE);
        ThreadContext.put(DURATION, String.format("%.3f", (double) elapsed / 1000000.0D));
        ThreadContext.put(RESPONSE_CODE, String.valueOf(httpServletResponse.getStatus()));
        // Nếu body là file hoặc byte thì KHÔNG log nội dung
        String bodyString;
        if (body instanceof MultipartFile
            || body instanceof MultipartFile[]
            || body instanceof byte[]
            || body instanceof Byte[]
            || body instanceof InputStream
            || body instanceof Resource) {
            bodyString = "[binary content omitted]";
        } else {
            try {
                bodyString = JsonUtil.toJson(body);
            } catch (Exception e) {
                bodyString = "[unserializable body]";
            }
        }

        log.info(
                HttpMessageObject.builder()
                        .sourceAppIp(httpServletRequest.getRemoteHost())
                        .destAppIp(httpServletRequest.getLocalAddr())
                        .destAppPort(httpServletRequest.getLocalPort())
                        .httpMethod(httpServletRequest.getMethod())
                        .httpPath(httpServletRequest.getRequestURI())
                        .header(
                                httpServletResponse.getHeaderNames().stream()
                                        .distinct()
                                        .collect(
                                                Collectors.toMap(
                                                        k -> k,
                                                        k ->
                                                                httpServletResponse.getHeaders(k).parallelStream()
                                                                        .collect(Collectors.joining(";")))))
                        .responseCode(httpServletResponse.getStatus())
                        .body(bodyString)
                        .build()
                        .toString());
        ThreadContext.remove(LOG_TYPE);
        ThreadContext.remove(SERVICE_MESSAGE_ID);
        ThreadContext.remove(RESPONSE_CODE);

        return body;
    }

    @Data
    @Builder
    private static class HttpMessageObject {
        private String sourceAppIp;
        private String destAppIp;
        private int destAppPort;
        private String httpPath;
        private String httpMethod;
        private int responseCode;
        private Map<String, String> header;
        private String body;

        @Override
        public String toString() {
            return JsonUtil.toJson(this);
        }
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class ServiceHeader {
        private String servicePath;
        private String httpMethod;
        private String clientMessageId;
        private String transactionId;

        @Setter(AccessLevel.NONE)
        private String serviceMessageId;

        private Date messageTimeStamp;
        private String sourceAppId;
        private String sourceAppIp;
        private String destAppIp;
        private int destAppPort;
        private String httpPath;
        private String authenticationUser;
        @JsonIgnore
        private String authorization;

        public String getServiceMessageId() {
            return (sourceAppId != null || clientMessageId != null)
                    ? String.format("%s-%s", sourceAppId, clientMessageId)
                    : null;
        }

        @Override
        public String toString() {
            return JsonUtil.toJson(this);
        }
    }
}
