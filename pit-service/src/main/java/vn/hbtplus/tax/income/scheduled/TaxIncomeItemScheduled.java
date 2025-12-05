package vn.hbtplus.tax.income.scheduled;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import vn.hbtplus.tax.income.services.IncomeItemsService;
import vn.hbtplus.utils.Utils;

import java.util.Date;

@Component("TaxIncomeItemScheduled")
@Slf4j
@RequiredArgsConstructor
public class TaxIncomeItemScheduled {
    private final IncomeItemsService incomeItemsService;

    @Scheduled(cron = "${schedule.cron.taxIncomeItem.autoCreate}")
    @SchedulerLock(name = "schedule.taxIncomeItem.autoCreate", lockAtLeastFor = "5M", lockAtMostFor = "60M")
    public void autoCreate() {
        incomeItemsService.autoCreateForPeriod(Utils.getLastDay(DateUtils.addDays(new Date(), -5)));
//        incomeItemsService.autoCreateForPeriod(Utils.getLastDay(Utils.stringToDate("30/04/2024")));
//        incomeItemsService.autoCreateForPeriod(Utils.getLastDay(Utils.stringToDate("31/03/2024")));
//        incomeItemsService.autoCreateForPeriod(Utils.getLastDay(Utils.stringToDate("01/02/2024")));
    }
}
