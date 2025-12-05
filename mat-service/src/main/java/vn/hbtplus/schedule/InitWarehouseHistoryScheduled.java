package vn.hbtplus.schedule;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import vn.hbtplus.services.WarehouseEquipmentsService;
import vn.hbtplus.utils.Utils;

import java.util.Date;

@Component
@RequiredArgsConstructor
@Slf4j
public class InitWarehouseHistoryScheduled{
    private final WarehouseEquipmentsService warehouseEquipmentsService;

    @Scheduled(cron = "${stk.warhouse-init-history.cron:0 0 1 * * *}")
    @SchedulerLock(name = "warhouse-init-history", lockAtLeastFor = "PT1M", lockAtMostFor = "PT20M")
    public void run() {
        log.info("data time: {}", Utils.formatDate(new Date(), "dd/MM/yyyy HH:mm:ss"));
        try {
            warehouseEquipmentsService.initHistory(Utils.getLastDay(new Date()));
        } catch (Exception e) {
            log.error("[InitWarehouseHistoryScheduled run] has error", e);
        }
        log.info("end");
    }
}
