package vn.hbtplus.scheduled;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.services.AnnualLeavesService;
import vn.hbtplus.utils.Utils;

import java.util.Calendar;
import java.util.Date;

@Component(BaseConstants.APP_NAME + "CalculateAnnualLeaveScheduled")
@Slf4j
@RequiredArgsConstructor
public class CalculateAnnualLeaveScheduled {
    private final AnnualLeavesService annualLeavesService;

    @Scheduled(cron = "${schedule.annualLeave.autoCalculate.cron:0 0 2 * * *}")
    @SchedulerLock(name = "annualLeave.autoCalculate")
    public void autoCalculate() {
        Calendar calendar = Calendar.getInstance();
        String curDate = Utils.formatDate(new Date());
        log.info("annualLeave.autoCalculate: running|date=" + curDate);
        if (calendar.get(Calendar.MONTH) == Calendar.JANUARY) {
            annualLeavesService.calculate(calendar.get(Calendar.YEAR) - 1, null);
        }

        annualLeavesService.calculate(calendar.get(Calendar.YEAR), null);

        if (calendar.get(Calendar.MONTH) == Calendar.DECEMBER) {
            annualLeavesService.calculate(calendar.get(Calendar.YEAR) + 1, null);
        }
        log.info("annualLeave.autoCalculate: success|date=" + curDate);

    }
}
