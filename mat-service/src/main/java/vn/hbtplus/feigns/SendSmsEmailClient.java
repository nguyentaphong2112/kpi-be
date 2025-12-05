package vn.hbtplus.feigns;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import vn.hbtplus.models.BaseResponse;
import vn.hbtplus.models.request.sendNotify.SendRequest;

@FeignClient(value = "send-sms-email-client", url = "${notification.serviceUrl}")
public interface SendSmsEmailClient {

    @PostMapping(value = "/api-ver2/send", consumes = MediaType.APPLICATION_JSON_VALUE)
    BaseResponse<Object> send(@RequestHeader HttpHeaders httpHeaders,
                              @RequestBody SendRequest sendRequest);
}
