package vn.hbtplus.insurance.repositories.impl;

import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import vn.hbtplus.insurance.models.request.InsuranceContributionsRequest;
import vn.hbtplus.utils.Utils;

import java.util.Map;

@Repository
public class CommonRepository {

    public static void addFilter(StringBuilder sql, Map<String, Object> params, InsuranceContributionsRequest.ReportForm dto) {
        if (!Utils.isNullOrEmpty(dto.getListLocationJoin())) {
            sql.append(" AND a.insurance_agency IN (:insuranceAgency) ");
            params.put("insuranceAgency", dto.getListLocationJoin());
        }
        if (!Utils.isNullOrEmpty(dto.getListStatus())) {
            sql.append(" AND a.status IN (:lstStatusFilter) ");
            params.put("lstStatusFilter", dto.getListStatus());
        }

        if (dto.getPeriodType().equals("MONTH")) {
            sql.append(" AND a.period_date BETWEEN :startMonth and :endMonth ");
            params.put("startMonth", Utils.getFirstDay(dto.getStartDate()));
            params.put("endMonth", Utils.getLastDay(dto.getEndDate()));
        } else if (dto.getPeriodType().equals("YEAR")) {
            sql.append(" AND a.period_date BETWEEN :startMonth and :endMonth ");
            params.put("startMonth", Utils.stringToDate("01/01/" + dto.getStartYear()));
            params.put("endMonth", Utils.stringToDate("31/12/" + dto.getEndYear()));
        } else {
            sql.append(" AND a.period_date BETWEEN :startMonth and :endMonth ");
            params.put("startMonth", Utils.stringToDate(("01/" + (dto.getQuarter() * 3 - 2) + "/" + dto.getYear())));
            params.put("endMonth", Utils.getLastDay(Utils.stringToDate(("01/" + (dto.getQuarter() * 3) + "/" + dto.getYear()))));
        }
    }

}
