package vn.hbtplus.feigns;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import vn.hbtplus.models.dto.AdmUsersDTO;

@FeignClient(value = "AdminFeignClient", url = "${service.properties.permission-client-url:default}")
public interface AdminFeignClient {

    @GetMapping(value = "/v1/user/info", consumes = MediaType.APPLICATION_JSON_VALUE)
    AdmUsersDTO getUserInfo(@RequestHeader HttpHeaders headers, @RequestParam("loginName") String loginName);
}
