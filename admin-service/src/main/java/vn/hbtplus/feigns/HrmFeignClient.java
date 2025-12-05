package vn.hbtplus.feigns;

import feign.Response;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import vn.hbtplus.models.BaseResponse;
import vn.hbtplus.models.response.BaseResponseEntity;

@FeignClient(value = "HrmFeignClient", url = "${service.properties.hrm-service-url:http://localhost}")
public interface HrmFeignClient {
    String BREAKER_NAME = "HrmFeignClient";

    @Retry(name = "loginRetry")
    @CircuitBreaker(name = BREAKER_NAME + "report", fallbackMethod = "login")
    @GetMapping(value = "/v1/employee/org-by-level-manage", produces = {MediaType.APPLICATION_JSON_VALUE})
    BaseResponse<Long> getOrgByOrgLevelManage(@RequestHeader HttpHeaders httpHeaders,
                                                    @RequestParam String employeeCode,
                                                    @RequestParam Long orgLevelManage);
}
