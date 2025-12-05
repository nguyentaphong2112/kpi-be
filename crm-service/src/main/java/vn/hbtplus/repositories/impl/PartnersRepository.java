/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.repositories.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.constants.Constant;
import vn.hbtplus.models.BaseDataTableDto;
import vn.hbtplus.models.request.PartnersRequest;
import vn.hbtplus.models.response.PartnersResponse;
import vn.hbtplus.repositories.BaseRepository;
import vn.hbtplus.utils.QueryUtils;
import vn.hbtplus.utils.Utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Lop repository Impl ung voi bang crm_partners
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Repository
@RequiredArgsConstructor
public class PartnersRepository extends BaseRepository {

    public BaseDataTableDto searchData(PartnersRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    a.partner_id,
                    a.full_name,
                    a.date_of_birth,
                    a.mobile_number,
                    a.zalo_account,
                    a.email,
                    a.partner_type,
                    a.current_address,
                    a.job,
                    a.department_name,
                    a.province_id,
                    a.district_id,
                    a.ward_id,
                    a.village_address,
                    a.bank_account,
                    a.bank_name,
                    a.bank_branch,
                    a.gender_id,
                    a.is_deleted,
                    a.created_by,
                    a.created_time,
                    a.modified_by,
                    a.modified_time,
                    a.last_update_time,
                    (SELECT sc.name FROM sys_categories sc WHERE sc.value = a.province_id AND sc.category_type = :provinceCode) provinceName,
                    (SELECT sc.name FROM sys_categories sc WHERE sc.value = a.district_id AND sc.category_type = :districtCode) districtName,
                    (SELECT sc.name FROM sys_categories sc WHERE sc.value = a.ward_id AND sc.category_type = :wardCode) wardName,
                    (SELECT sc.name FROM sys_categories sc WHERE sc.value = a.partner_type AND sc.category_type = :partnerType) partnerTypeName
                """);
        HashMap<String, Object> params = new HashMap<>();
        addCondition(sql, params, dto);
        return getListPagination(sql.toString(), params, dto, PartnersResponse.SearchResult.class);
    }

    public List<Map<String, Object>> getListExport(PartnersRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    a.partner_id,
                    a.full_name,
                    a.date_of_birth,
                    a.mobile_number,
                    a.zalo_account,
                    a.email,
                    a.partner_type,
                    a.current_address,
                    a.job,
                    a.department_name,
                    a.province_id,
                    a.district_id,
                    a.ward_id,
                    a.village_address,
                    a.bank_account,
                    a.bank_name,
                    a.bank_branch,
                    a.gender_id,
                    a.is_deleted,
                    a.created_by,
                    a.created_time,
                    a.modified_by,
                    a.modified_time,
                    a.last_update_time,
                    (SELECT sc.name FROM sys_categories sc WHERE sc.value = a.province_id AND sc.category_type = :provinceCode) provinceName,
                    (SELECT sc.name FROM sys_categories sc WHERE sc.value = a.district_id AND sc.category_type = :districtCode) districtName,
                    (SELECT sc.name FROM sys_categories sc WHERE sc.value = a.ward_id AND sc.category_type = :wardCode) wardName,
                    (SELECT sc.name FROM sys_categories sc WHERE sc.value = a.partner_type AND sc.category_type = :partnerType) partnerTypeName,
                    (SELECT sc.name FROM sys_categories sc WHERE sc.value = a.gender_id AND sc.category_type = :genderCode) genderName
                """);
        Map<String, Object> params = new HashMap<>();
        addCondition(sql, params, dto);
        return getListData(sql.toString(), params);
    }

    private void addCondition(StringBuilder sql, Map<String, Object> params, PartnersRequest.SearchForm dto) {
        sql.append("""
            FROM crm_partners a
            WHERE IFNULL(a.is_deleted, :activeStatus) = :activeStatus
        """);
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        params.put("provinceCode", Constant.CATEGORY_TYPES.TINH);
        params.put("districtCode", Constant.CATEGORY_TYPES.HUYEN);
        params.put("wardCode", Constant.CATEGORY_TYPES.XA);
        params.put("partnerType", Constant.CATEGORY_TYPES.CRM_LOAI_DOI_TAC);
        params.put("genderCode", Constant.CATEGORY_TYPES.GIOI_TINH);
        QueryUtils.filter(dto.getKeySearch(), sql, params, "a.full_name", "a.mobile_number", "a.email");
        QueryUtils.filter(dto.getDateOfBirth(), sql, params, "a.date_of_birth");
        QueryUtils.filter(dto.getPartnerType(), sql, params, "a.partner_type");
        sql.append(" ORDER BY a.created_time desc");
    }

    public Map<String, PartnersResponse.DetailBean> getMapPartnerByMobileNumber(List<String> listMobileNumbers) {
        Map<String, PartnersResponse.DetailBean> mapResults = new HashMap<>();
        if (Utils.isNullOrEmpty(listMobileNumbers)) {
            return mapResults;
        }
        String sql = """
                    SELECT a.*
                    FROM crm_partners a
                    WHERE a.is_deleted = 'N'
                    AND a.mobile_number IN (:listMobileNumbers)
                """;
        Map<String, Object> params = new HashMap<>();
        params.put("listMobileNumbers", listMobileNumbers);
        List<PartnersResponse.DetailBean> listData = getListData(sql, params, PartnersResponse.DetailBean.class);
        if (!Utils.isNullOrEmpty(listData)) {
            listData.forEach(item -> mapResults.put(item.getMobileNumber(), item));
        }
        return mapResults;
    }

    public List<PartnersResponse.DetailBean> getListData() {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    a.partner_id,
                    a.full_name,
                    a.mobile_number,
                    a.date_of_birth
                FROM crm_partners a
                WHERE IFNULL(a.is_deleted, :activeStatus) = :activeStatus
                """);
        HashMap<String, Object> params = new HashMap<>();
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        return getListData(sql.toString(), params, PartnersResponse.DetailBean.class);
    }
}
