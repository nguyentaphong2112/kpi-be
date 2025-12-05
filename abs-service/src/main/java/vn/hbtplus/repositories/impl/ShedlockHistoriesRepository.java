package vn.hbtplus.repositories.impl;

import org.springframework.stereotype.Repository;
import vn.hbtplus.repositories.BaseRepository;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Repository
public class ShedlockHistoriesRepository extends BaseRepository {

    public Date getLastRunSuccess(String name) {
        String sql = "SELECT MAX(start_time) FROM shedlock_histories " +
                     " WHERE shedlock_name = :name AND status = 'COMPLETED'";
        Map params = new HashMap<>();
        params.put("name", name);
        Date localDateTime = getFirstData(sql, params, Date.class);
        return localDateTime;
    }
}
