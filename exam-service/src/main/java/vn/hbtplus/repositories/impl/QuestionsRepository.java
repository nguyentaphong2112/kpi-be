/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.repositories.impl;

import org.springframework.stereotype.Repository;
import lombok.RequiredArgsConstructor;
import vn.hbtplus.models.BaseDataTableDto;
import vn.hbtplus.repositories.BaseRepository;
import vn.hbtplus.models.request.QuestionsRequest;
import vn.hbtplus.models.response.QuestionsResponse;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.utils.QueryUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Lop repository Impl ung voi bang exm_questions
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Repository
@RequiredArgsConstructor
public class QuestionsRepository extends BaseRepository {

    public BaseDataTableDto searchData(QuestionsRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    a.*,
                    (select sc.name from sys_categories sc where sc.category_type = 'EXAM_SUBJECT' and sc.value = a.subject_code) subjectName,
                    (select sc.name from sys_categories sc where sc.category_type = 'EXAM_TOPIC' and sc.value = a.topic_code) topicName,
                    (select sc.name from sys_categories sc where sc.category_type = 'EXAM_QUESTION_TYPE' and sc.value = a.type_code) typeName,
                    (select sc.name from sys_categories sc where sc.category_type = 'EXAM_SECTION' and sc.value = a.section_code) sectionName,
                    (select sc.name from sys_categories sc where sc.category_type = 'EXAM_QUESTION_LEVEL' and sc.value = a.level_code) levelName,
                    (select sc.name from sys_categories sc where sc.category_type = 'EXAM_SKILL_TYPE' and sc.value = a.skill_type) skillTypeName,
                    (select sc.name from sys_categories sc where sc.category_type = 'EXAM_QUESTION_STATUS' and sc.value = a.status_code) statusName
                
                """);
        HashMap<String, Object> params = new HashMap<>();
        addCondition(sql, params, dto);
        return getListPagination(sql.toString(), params, dto, QuestionsResponse.class);
    }

    public List<Map<String, Object>> getListExport(QuestionsRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    a.*,
                    (select sc.name from sys_categories sc where sc.category_type = 'EXAM_SUBJECT' and sc.value = a.subject_code) subjectName,
                    (select sc.name from sys_categories sc where sc.category_type = 'EXAM_TOPIC' and sc.value = a.topic_code) topicName,
                    (select sc.name from sys_categories sc where sc.category_type = 'EXAM_QUESTION_TYPE' and sc.value = a.type_code) typeName,
                    (select sc.name from sys_categories sc where sc.category_type = 'EXAM_SECTION' and sc.value = a.section_code) sectionName,
                    (select sc.name from sys_categories sc where sc.category_type = 'EXAM_QUESTION_LEVEL' and sc.value = a.level_code) levelName,
                    (select sc.name from sys_categories sc where sc.category_type = 'EXAM_SKILL_TYPE' and sc.value = a.skill_type) skillTypeName,
                    (select sc.name from sys_categories sc where sc.category_type = 'EXAM_QUESTION_STATUS' and sc.value = a.status_code) statusName
                """);
        Map<String, Object> params = new HashMap<>();
        addCondition(sql, params, dto);
        return getListData(sql.toString(), params);
    }

    private void addCondition(StringBuilder sql, Map<String, Object> params, QuestionsRequest.SearchForm dto) {
        sql.append("""
            FROM exm_questions a
            WHERE a.is_deleted = :activeStatus
        """);
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        QueryUtils.filter(dto.getKeySearch(), sql, params, "a.code", "a.content");
        QueryUtils.filter(dto.getSubjectCodes(), sql, params, "a.subject_code");
        QueryUtils.filter(dto.getTopicCodes(), sql, params, "a.topic_code");
        QueryUtils.filter(dto.getTypeCodes(), sql, params, "a.type_code");
        QueryUtils.filter(dto.getLevelCodes(), sql, params, "a.level_code");
        QueryUtils.filter(dto.getSkillTypes(), sql, params, "a.skill_type");
        QueryUtils.filter(dto.getStatusCodes(), sql, params, "a.status_code");
        sql.append(" ORDER BY NVL(a.modified_time, a.created_time) DESC)");
    }
}
