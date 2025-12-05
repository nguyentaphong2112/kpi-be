package vn.hbtplus.scheduled;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.services.DBChangeLogService;

@Component(BaseConstants.APP_NAME + "DBChangeLogScheduled")
@Slf4j
@RequiredArgsConstructor
public class DBChangeLogScheduled {
    private final DBChangeLogService dbChangeLogService;

    @Scheduled(cron = "${schedule.db-change-log.cron:0 */1 * * * *}")
    @SchedulerLock(name = "db-change-log.auto-push-db")
    public void autoPushDb() {
        dbChangeLogService.auditChangeLog();
    }
}

