package vn.hbtplus.schedule;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import vn.hbtplus.services.SendNotifyService;
import vn.hbtplus.utils.Utils;

import java.util.Date;

@Component
@RequiredArgsConstructor
@Slf4j
public class SendEmailSchedule {

    private final SendNotifyService sendNotifyService;

    @Scheduled(cron = "${send.email.crone:-}")
    @SchedulerLock(name = "send-sms-email", lockAtLeastFor = "PT1M", lockAtMostFor = "PT20M")
    public void run() {
        log.info("data time: {}", Utils.formatDate(new Date(), "dd/MM/yyyy HH:mm:ss"));
        try {
            sendNotifyService.processSendNotify();
        } catch (Exception e) {
            log.error("[SendEmailSchedule run] has error", e);
        }
        log.info("end");
    }
}
