package vn.hbtplus.feigns;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import vn.hbtplus.models.BaseResponse;
import vn.hbtplus.models.dto.BaseCategoryDto;
import vn.hbtplus.models.request.CreateUserRequest;

import javax.validation.Valid;

@FeignClient(value = "AdminFeignClient", url = "${service.properties.permission-client-url:default}")
public interface AdminFeignClient {
    String BREAKER_NAME = "report";

    @Retry(name = "createUserRetry")
    @CircuitBreaker(name = BREAKER_NAME + "report", fallbackMethod = "createUser")
    @GetMapping(value = "/v1/user", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    ResponseEntity createUser(@RequestHeader HttpHeaders httpHeaders, @Valid @RequestBody CreateUserRequest dto);

    @Retry(name = "personInfoRetry")
    @CircuitBreaker(name = BREAKER_NAME + "report", fallbackMethod = "report")
    @GetMapping(value = "/v1/category/{categoryType}/by-value/{value}", produces = MediaType.APPLICATION_JSON_VALUE)
    BaseResponse<BaseCategoryDto.DetailBean> getCategory(
            @RequestHeader HttpHeaders headers,
            @PathVariable String categoryType,
            @PathVariable String value);

}
