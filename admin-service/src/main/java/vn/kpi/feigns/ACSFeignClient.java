package vn.kpi.feigns;

import feign.Response;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(value = "ACSFeignClient", url = "${service.properties.acs-auth-url:http://localhost}")
public interface ACSFeignClient {
    String BREAKER_NAME = "report";

    @Retry(name = "loginRetry")
    @CircuitBreaker(name = BREAKER_NAME + "report", fallbackMethod = "login")
    @GetMapping(value = "/Token/Login", produces = {MediaType.APPLICATION_JSON_VALUE})
    Response login(@RequestHeader HttpHeaders httpHeaders);

    @Retry(name = "changePassRetry")
    @CircuitBreaker(name = BREAKER_NAME + "report", fallbackMethod = "changePass")
    @PostMapping(value = "/Token/ChangePassword", produces = {MediaType.APPLICATION_JSON_VALUE})
    Response changePass(@RequestHeader HttpHeaders httpHeaders, @RequestBody String body);
}
