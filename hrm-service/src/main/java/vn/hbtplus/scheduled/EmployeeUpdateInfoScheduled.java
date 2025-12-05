package vn.hbtplus.scheduled;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.services.WorkProcessService;
import vn.hbtplus.utils.Utils;

import java.util.Date;

@Component(BaseConstants.APP_NAME + "EmployeeUpdateInfoScheduled")
@Slf4j
@RequiredArgsConstructor
public class EmployeeUpdateInfoScheduled {

    private final WorkProcessService workProcessService;

    @Scheduled(cron = "${schedule.employee.updateInfo.cron:-}")
    @SchedulerLock(name = "employee.updateInfo")
    public void updateEmployeeInfo() {
        log.info("employee.updateInfo: running|date=" + Utils.formatDate(new Date()));
        workProcessService.autoUpdateWorkProcess(null, true);
        log.info("employee.updateInfo: success|date=" + Utils.formatDate(new Date()));

    }

}
