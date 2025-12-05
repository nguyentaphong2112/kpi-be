package vn.hbtplus.repositories.impl;

import org.springframework.stereotype.Repository;
import vn.hbtplus.models.Attachment;
import vn.hbtplus.models.AttachmentFileDto;
import vn.hbtplus.models.dto.AttachmentDto;
import vn.hbtplus.repositories.BaseRepository;
import vn.hbtplus.utils.QueryUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class AttachmentRepository extends BaseRepository {
    public <T> List<T> getAttachments(String tableName, String fileType, List<Long> objectIds, Class<T> tClass) {
        String sql = """
                SELECT a.*
                FROM crm_attachments a
                WHERE a.table_name = :tableName
                    and a.function_code = :functionCode
                    and a.object_id IN (:objectIds)
                    and a.is_deleted = 'N'
                ORDER BY a.file_name
                """;
        Map mapParams = new HashMap<>();
        mapParams.put("tableName", tableName);
        mapParams.put("functionCode", fileType);
        mapParams.put("objectIds", objectIds);
        return getListData(sql, mapParams, tClass);
    }

    public List<AttachmentFileDto> getListAttachments(String functionCode, String employeeId, String tableName) {
        StringBuilder sql = new StringBuilder("""
                    SELECT
                        a.*
                    FROM crm_attachments a
                    WHERE a.is_deleted = 'N'
                    AND a.table_name = :tableName
                """);
        Map<String, Object> params = new HashMap<>();
        params.put("tableName", tableName);
        QueryUtils.filter(functionCode, sql, params, "a.function_code");
        QueryUtils.filter(employeeId, sql, params, "a.object_id");
        return getListData(sql.toString(), params, AttachmentFileDto.class);
    }
}
