/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.kpi.repositories.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import vn.kpi.constants.BaseConstants;
import vn.kpi.constants.Constant;
import vn.kpi.models.BaseDataTableDto;
import vn.kpi.models.request.CardTemplatesRequest;
import vn.kpi.models.response.CardTemplatesResponse;
import vn.kpi.repositories.BaseRepository;
import vn.kpi.utils.QueryUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Lop repository Impl ung voi bang sys_card_templates
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Repository
@RequiredArgsConstructor
public class CardTemplatesRepository extends BaseRepository {

    public BaseDataTableDto searchData(CardTemplatesRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    a.card_template_id,
                    a.template_type,
                    a.title,
                    a.default_parameters,
                    a.parameters,
                    a.is_deleted,
                    a.created_by,
                    a.created_time,
                    a.modified_by,
                    a.modified_time,
                    a.last_update_time,
                    a.is_apply_all,
                    (select sc.name from sys_categories sc where sc.value = a.template_type and sc.category_type = :templateType) templateTypeName,
                    CASE
                        WHEN a.is_apply_all = 'Y'
                        THEN 'Có' ELSE 'Không'
                    END isApplyAllName
                """);
        HashMap<String, Object> params = new HashMap<>();
        addCondition(sql, params, dto);
        return getListPagination(sql.toString(), params, dto, CardTemplatesResponse.SearchResult.class);
    }

    public List<Map<String, Object>> getListExport(CardTemplatesRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    a.card_template_id,
                    a.template_type,
                    a.title,
                    a.default_parameters,
                    a.parameters,
                    a.is_deleted,
                    a.created_by,
                    a.created_time,
                    a.modified_by,
                    a.modified_time,
                    a.last_update_time,
                    a.is_apply_all
                """);
        Map<String, Object> params = new HashMap<>();
        addCondition(sql, params, dto);
        return getListData(sql.toString(), params);
    }

    private void addCondition(StringBuilder sql, Map<String, Object> params, CardTemplatesRequest.SearchForm dto) {
        sql.append("""
            FROM sys_card_templates a
            WHERE IFNULL(a.is_deleted, :activeStatus) = :activeStatus
        """);
        QueryUtils.filter(dto.getTemplateType(), sql, params, "a.template_type");
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        params.put("templateType", Constant.CATEGORY_TYPE.LOAI_BIEU_MAU);
    }

    public CardTemplatesResponse.DetailBean getCardTemplateByType(String templateType) {
        String sql = """
                    SELECT
                        a.*
                    FROM sys_card_templates a
                    WHERE a.is_deleted = 'N'
                    AND a.template_type = :templateType
                """;
        Map<String, Object> params = new HashMap<>();
        params.put("templateType", templateType);
        return getFirstData(sql, params, CardTemplatesResponse.DetailBean.class);
    }
}
