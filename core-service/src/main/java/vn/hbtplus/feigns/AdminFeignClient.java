package vn.hbtplus.feigns;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import vn.hbtplus.models.BaseResponse;
import vn.hbtplus.models.dto.BaseCategoryDto;

@FeignClient(value = "AdminFeignClient", url = "${service.properties.config-report-client-url:default}")
public interface AdminFeignClient {
    String BREAKER_NAME = "report";

    @Retry(name = "personInfoRetry")
    @CircuitBreaker(name = BREAKER_NAME + "report", fallbackMethod = "report")
    @PostMapping(value = "/v1/category/{categoryType}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    BaseResponse<BaseCategoryDto> createCategory(
            @RequestHeader HttpHeaders headers,
            @ModelAttribute BaseCategoryDto dto,
            @PathVariable String categoryType);




}
