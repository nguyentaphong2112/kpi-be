package vn.hbtplus.feigns;

import feign.Response;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import vn.hbtplus.models.request.UserRequest;

@FeignClient(value = "SsoFeignClient", url = "${service.properties.sso-service-url:http://localhost}")
public interface SsoFeignClient {
    String BREAKER_NAME = "SsoFeignClient";

    @Retry(name = "loginRetry")
    @CircuitBreaker(name = BREAKER_NAME + "user", fallbackMethod = "update-password")
    @PostMapping(value = "/v1/user", produces = {MediaType.APPLICATION_JSON_VALUE})
    Response updatePassword(@RequestHeader HttpHeaders httpHeaders, @RequestBody UserRequest.SubmitFormToSso form);
}
