package vn.kpi.scheduled;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import vn.kpi.constants.BaseConstants;
import vn.kpi.services.ContractProcessService;
import vn.kpi.utils.Utils;

import java.util.Date;

@Component(BaseConstants.APP_NAME + "EmployeeUpdateDailyScheduled")
@Slf4j
@RequiredArgsConstructor
public class EmployeeUpdateDailyScheduled {

    private final ContractProcessService contractProcessService;

    @Scheduled(cron = "${schedule.employee.updateDaily.cron:-}")
    @SchedulerLock(name = "employee.updateDaily")
    public void updateEmployeeDaily() {
        String curDate = Utils.formatDate(new Date());
        log.info("employee.updateDaily: running|date=" + curDate);
        contractProcessService.autoUpdateEmpType();
        log.info("employee.updateDaily: success|date=" + curDate);
    }

}
