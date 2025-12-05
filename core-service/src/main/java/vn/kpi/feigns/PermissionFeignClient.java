package vn.kpi.feigns;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import vn.kpi.models.BaseResponse;
import vn.kpi.models.PermissionDataDto;
import vn.kpi.models.dto.UserLogActivityDto;

import java.util.List;

@FeignClient(value = "check-permission-client", url = "${service.properties.permission-client-url:default}")
public interface PermissionFeignClient {
    String BREAKER_NAME = "permission";

    @Retry(name = "personInfoRetry")
    @CircuitBreaker(name = BREAKER_NAME + "checkPermission", fallbackMethod = "retryCheckPermission")
    @GetMapping(value = "/v1/user/check-permission", consumes = MediaType.APPLICATION_JSON_VALUE)
    BaseResponse<Boolean> checkPermission(@RequestHeader HttpHeaders httpHeaders, @RequestParam String scope,
                                          @RequestParam String resource,
                                          @RequestParam String userName);

    @Retry(name = "hasRoleRetry")
    @CircuitBreaker(name = BREAKER_NAME + "hasRole", fallbackMethod = "retryHasRole")
    @GetMapping(value = "/v1/user/has-role", consumes = MediaType.APPLICATION_JSON_VALUE)
    BaseResponse<Boolean> hasRole(@RequestHeader HttpHeaders httpHeaders, @RequestParam String roleCode,
                                          @RequestParam String userName);

    @Retry(name = "personInfoRetry")
    @CircuitBreaker(name = BREAKER_NAME + "getPermissionData", fallbackMethod = "retryGetPermissionData")
    @GetMapping(value = "/v1/user/permission-data", consumes = MediaType.APPLICATION_JSON_VALUE)
    BaseResponse<List<PermissionDataDto>> getPermissionData(@RequestHeader HttpHeaders httpHeaders, @RequestParam String scope,
                                                            @RequestParam String resource,
                                                            @RequestParam String userName);

    @Retry(name = "personInfoRetry")
    @CircuitBreaker(name = BREAKER_NAME + "saveUserLogActivity", fallbackMethod = "saveUserLogActivity")
    @PostMapping(value = "/v1/user/save-log-activity", consumes = MediaType.APPLICATION_JSON_VALUE)
    void saveUserLogActivity(@RequestHeader HttpHeaders httpHeaders, @RequestBody UserLogActivityDto dto);


    @Retry(name = "personInfoRetry")
    @CircuitBreaker(name = BREAKER_NAME + "getOrgHasPermissionIds", fallbackMethod = "getOrgHasPermissionIds")
    @GetMapping(value = "/v1/permission/org-granted", consumes = MediaType.APPLICATION_JSON_VALUE)
    BaseResponse<List<Long>> getGrantedDomain(@RequestHeader HttpHeaders httpHeaders, @RequestParam String scope, @RequestParam String resource, @RequestParam Long orgId);
}
