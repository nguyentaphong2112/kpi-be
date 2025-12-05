package vn.hbtplus.feigns;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import vn.hbtplus.models.BaseResponse;
import vn.hbtplus.models.dto.ChartConfigDto;

@FeignClient(value = "ChartFeignClient", url = "${service.properties.config-report-client-url:default}")
public interface ChartFeignClient {
    String BREAKER_NAME = "report";

    @Retry(name = "personInfoRetry")
    @CircuitBreaker(name = BREAKER_NAME + "report", fallbackMethod = "report")
    @GetMapping(value = "/v1/config-charts/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    BaseResponse<ChartConfigDto> getReportConfig(@RequestHeader HttpHeaders httpHeaders,
                                                 @PathVariable Long id);

}
