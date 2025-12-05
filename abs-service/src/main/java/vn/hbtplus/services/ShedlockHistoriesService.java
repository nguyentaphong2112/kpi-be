package vn.hbtplus.services;

import java.util.Date;

public interface ShedlockHistoriesService {
    Long saveHistory(String name);

    void updateEndTime(Long shedlockHistoryId, String status);

    Date getLastRunSuccess(String name);
}
