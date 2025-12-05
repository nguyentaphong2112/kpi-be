/*
 * Copyright (C) 2022 EcoIT. All rights reserved.
 * EcoIT. Use is subject to license terms.
 */
package vn.hbtplus.tax.personal.repositories.impl;

import org.springframework.stereotype.Repository;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.tax.personal.constants.Constant;
import vn.hbtplus.tax.personal.models.request.LockRegistrationsDTO;
import vn.hbtplus.tax.personal.models.response.LockRegistrationsResponse;
import vn.hbtplus.repositories.BaseRepository;
import vn.hbtplus.utils.QueryUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Lop repository Impl ung voi bang PTX_LOCK_REGISTRATIONS
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Repository
public class LockRegistrationsRepositoryImpl extends BaseRepository {

    public List<LockRegistrationsResponse> searchData(LockRegistrationsDTO dto) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                     a.lock_registration_id,
                     a.registration_type,
                     a.year,
                     a.from_date,
                     a.to_date,
                     a.is_deleted,
                     a.created_by,
                     a.created_time,
                     a.modified_by,
                     a.modified_time,
                     a.from_remind_date,
                     a.to_remind_date
                 FROM ptx_lock_registrations a
                 WHERE IFNULL(a.is_deleted, :activeStatus) = :activeStatus
                """);
        HashMap<String, Object> params = new HashMap<>();
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        QueryUtils.filter(dto.getRegistrationType(), sql, params, "a.registration_type");
        QueryUtils.filter(dto.getYear(), sql, params, "a.year");
        sql.append(" ORDER BY a.year");
        return getListData(sql.toString(), params, LockRegistrationsResponse.class);
    }

    public int countRemindRegisterTax(Date remindDate) {
        StringBuilder sql = new StringBuilder();
        sql.append(" select count(1) from PTX_LOCK_REGISTRATIONS plr ");
        sql.append(" where IFNULL(plr.is_deleted, :activeStatus) = :activeStatus ");
        sql.append(" and :remindDate between plr.from_remind_date and plr.to_remind_date ");

        Map<String, Object> params = new HashMap<>();
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        params.put("remindDate", remindDate);
        return getFirstData(sql.toString(), params, Integer.class);
    }

    public Map<Integer, LockRegistrationsResponse> getMapYear() {
        Map<Integer, LockRegistrationsResponse> mapResult = new HashMap<>();
        LockRegistrationsDTO dto = new LockRegistrationsDTO();
        dto.setRegistrationType(Constant.DECLARATION_REGISTER);
        List<LockRegistrationsResponse> responseList = searchData(dto);
        for (LockRegistrationsResponse response : responseList) {
            mapResult.put(response.getYear(), response);
        }
        return mapResult;
    }

    public List<LockRegistrationsResponse> getListLockRegistrationByRemindDate(Date remindDate) {
        StringBuilder sql = new StringBuilder();
        sql.append(" SELECT plr.* from ptx_lock_registrations plr ");
        sql.append(" WHERE IFNULL(plr.is_deleted, :activeStatus) = :activeStatus ");
        sql.append(" AND date(:remindDate) BETWEEN plr.from_remind_date and plr.to_remind_date ");

        Map<String, Object> params = new HashMap<>();
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        params.put("remindDate", remindDate);
        return getListData(sql.toString(), params, LockRegistrationsResponse.class);
    }

    public List<LockRegistrationsResponse> getListLockRegistrationByRegisterDate(Date registerDate) {
        StringBuilder sql = new StringBuilder();
        sql.append(" SELECT plr.* from ptx_lock_registrations plr ");
        sql.append(" WHERE IFNULL(plr.is_deleted, :activeStatus) = :activeStatus ");
        sql.append(" AND date(:registerDate) BETWEEN plr.from_date and plr.to_date ");

        Map<String, Object> params = new HashMap<>();
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        params.put("registerDate", registerDate);
        return getListData(sql.toString(), params, LockRegistrationsResponse.class);
    }

    public boolean hasEffectiveDate(Date registerDate, int yearRegister) {
        StringBuilder sql = new StringBuilder("""
                select count(1) from ptx_lock_registrations plr
                where IFNULL(plr.is_deleted, :activeStatus) = :activeStatus
                and :registerDate between plr.from_date and plr.to_date
                and plr.year = :yearRegister
                """);

        Map<String, Object> params = new HashMap<>();
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        params.put("registerDate", registerDate);
        params.put("yearRegister", yearRegister);
        int count =  queryForObject(sql.toString(), params, Integer.class);
        return count > 0;
    }
}
