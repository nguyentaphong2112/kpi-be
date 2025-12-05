package vn.kpi.feigns;


import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@FeignClient(name = "telegramClient", url = "${telegram.api:https://api.telegram.org}")
public interface TelegramFeignClient {

    @PostMapping("/bot{token}/sendMessage")
    Map<String, Object> sendMessage(
            @PathVariable("token") String token,
            @RequestBody Map<String, Object> body);
}