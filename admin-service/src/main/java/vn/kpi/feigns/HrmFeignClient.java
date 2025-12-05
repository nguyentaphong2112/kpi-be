package vn.kpi.feigns;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import vn.kpi.models.BaseResponse;

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
