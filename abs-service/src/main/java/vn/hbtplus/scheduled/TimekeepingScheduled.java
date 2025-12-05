package vn.hbtplus.scheduled;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.models.dto.AbsTimekeepingDTO;
import vn.hbtplus.repositories.entity.ShedlockHistoriesEntity;
import vn.hbtplus.services.RequestsService;
import vn.hbtplus.services.ShedlockHistoriesService;
import vn.hbtplus.services.TimekeepingsService;
import vn.hbtplus.utils.Utils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component(BaseConstants.APP_NAME + "TimekeepingScheduled")
@Slf4j
@RequiredArgsConstructor
public class TimekeepingScheduled {
    private final TimekeepingsService timekeepingsService;
    private final ShedlockHistoriesService shedlockHistoriesService;
    private final RequestsService requestsService;

    @Scheduled(cron = "${schedule.timekeeping.autoCalculate.cron:0 0 1 * * *}")
    @SchedulerLock(name = "timekeeping.autoCalculate")
    public void autoCalculate() {
        Calendar calendar = Calendar.getInstance();
        String curDate = Utils.formatDate(new Date());
        log.info("timekeeping.autoCalculate: running|date=" + curDate);
        if (calendar.get(Calendar.MONTH) == Calendar.JANUARY) {
            timekeepingsService.autoSetTimekeeping(Utils.truncDate(calendar.getTime()), null);
        }
        log.info("timekeeping.autoCalculate: success|date=" + curDate);

    }

    @Scheduled(cron = "${schedule.timekeeping.autoCalculate.cron:0 */1 * * * *}")
    @SchedulerLock(name = "timekeeping.autoCalculate")
    public void autoSetTimekeepingByRequest() {
        //lay thoi diem chay tien trinh lan truoc do
        Long shedlockHistoryId = shedlockHistoriesService.saveHistory("timekeeping.autoCalculate");
        Date lastRun = shedlockHistoriesService.getLastRunSuccess("timekeeping.autoCalculate");
        //Lay du lieu thay doi
        List<AbsTimekeepingDTO> listRequestChange = requestsService.getListRequestChange(lastRun);
        Map<String, List<Long>> mapDates = new HashMap<>();
        for (AbsTimekeepingDTO absTimekeepingDTO : listRequestChange) {
            String key = Utils.formatDate(absTimekeepingDTO.getDateTimekeeping());
            if (!mapDates.containsKey(key)) {
                mapDates.put(key, new ArrayList<>());
            }
            mapDates.get(key).add(absTimekeepingDTO.getEmployeeId());
        }
        for (Map.Entry<String, List<Long>> entry : mapDates.entrySet()) {
            timekeepingsService.autoSetTimekeeping(Utils.stringToDate(entry.getKey()), entry.getValue());
        }
        shedlockHistoriesService.updateEndTime(shedlockHistoryId, ShedlockHistoriesEntity.STATUS.COMPLETED);
    }
}
