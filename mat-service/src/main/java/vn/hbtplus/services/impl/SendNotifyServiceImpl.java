package vn.hbtplus.services.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import vn.hbtplus.services.SendNotifyService;

@Service
@RequiredArgsConstructor
@Slf4j
public class SendNotifyServiceImpl implements SendNotifyService {

    @Value("${pos.config.send.mail}")
    private String positionIds;

    @Value("${white.list.send.mail}")
    private String whiteListEmail;

    @Value("${email.subject.temp}")
    private String subject;

    @Value("${number.send.warning}")
    private Long numberSendWarning;

    @Value("${time.to.sleep}")
    private Long timeToSleep;

    @Override
    public void processSendNotify() throws Exception {

    }
}
