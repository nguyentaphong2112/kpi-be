package vn.hbtplus.insurance.repositories.impl;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.insurance.models.JobDto;
import vn.hbtplus.repositories.BaseRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class JobRepositoryImpl extends BaseRepository {

    public List<Map<String, Object>> getAllJobs() {
        StringBuilder sql = new StringBuilder();
        Map<String, Object> params = new HashMap<>();
        buildSql(sql,params);
        List result = getListData(sql.toString(), params);
        if (result.isEmpty()) {
            result.add(getMapEmptyAliasColumns(sql.toString()));
        }

        return result;
    }

    private void buildSql(StringBuilder sql, Map<String, Object> params) {
        sql.append("""
                select job_name, job_code, job_id
                from hr_jobs 
                where ifnull(is_deleted, :isDeleted) = :isDeleted
                order by job_name
                """);
        params.put("isDeleted", BaseConstants.STATUS.NOT_DELETED);
    }

    public Map<String, Long> createMapJob() {
        StringBuilder sql = new StringBuilder();
        Map<String, Object> params = new HashMap<>();
        buildSql(sql,params);
        List<JobDto> resultList = getListData(sql.toString(), params, JobDto.class);

        Map<String, Long> mapJob = new HashMap<>();
        resultList.forEach(item -> mapJob.put(StringUtils.lowerCase(item.getJobCode()), item.getJobId()));
        return mapJob;
    }
}
