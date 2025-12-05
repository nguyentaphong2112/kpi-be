package vn.hbtplus.repositories.impl;

import org.springframework.stereotype.Repository;
import vn.hbtplus.repositories.BaseRepository;

import java.util.Date;
import java.util.HashMap;

@Repository
public class DBChangeLogRepository extends BaseRepository {

    public Date getLastEventTime() {
        String sql = "select max(event_time) from ddl_changes_log";
        return queryForObject(sql, new HashMap<>(), Date.class);
    }
}
