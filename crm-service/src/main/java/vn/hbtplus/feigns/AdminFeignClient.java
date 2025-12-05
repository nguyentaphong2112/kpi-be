package vn.hbtplus.feigns;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import vn.hbtplus.models.request.EmployeesRequest;

import javax.validation.Valid;
import java.util.List;

@FeignClient(value = "AdminFeignClient", url = "${service.properties.permission-client-url:default}")
public interface AdminFeignClient {
    String BREAKER_NAME = "report";

    @Retry(name = "createUserRetry")
    @CircuitBreaker(name = BREAKER_NAME + "report", fallbackMethod = "createUser")
    @GetMapping(value = "/v1/user", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    ResponseEntity createUser(@RequestHeader HttpHeaders httpHeaders, @Valid @RequestBody EmployeesRequest.CreateUser dto);

    @Retry(name = "createListUserRetry")
    @CircuitBreaker(name = BREAKER_NAME + "report", fallbackMethod = "createListUserRetry")
    @PostMapping(value = "/v1/user/list", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_JSON_VALUE})
    ResponseEntity createUserList(@RequestHeader HttpHeaders httpHeaders, @RequestBody List<EmployeesRequest.CreateUser> userList);

}
