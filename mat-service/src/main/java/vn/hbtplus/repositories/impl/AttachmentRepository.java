package vn.hbtplus.repositories.impl;

import org.springframework.stereotype.Repository;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.models.Attachment;
import vn.hbtplus.models.dto.AttachmentDto;
import vn.hbtplus.repositories.BaseRepository;
import vn.hbtplus.utils.Utils;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class AttachmentRepository extends BaseRepository {
    public void deleteByListObjId(List<Long> listObjId, String tableName, String fileType){
        String sql = """
                    UPDATE mat_attachments
                    SET is_deleted = :deActiveStatus, modified_by = :userName, modified_time = :currentDate
                    WHERE object_id IN (:objIds)
                    AND function_code = :dgrCode
                    AND table_name = :tableName
                    AND NVL(is_deleted, :activeStatus) = :activeStatus
                """;
        HashMap<String, Object> paramMap = new HashMap<>();
        paramMap.put("deActiveStatus", BaseConstants.STATUS.DELETED);
        paramMap.put("userName", Utils.getUserNameLogin());
        paramMap.put("currentDate", new Date());
        paramMap.put("dgrCode", fileType);
        paramMap.put("tableName", tableName);
        paramMap.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        paramMap.put("objIds", listObjId);
        executeSqlDatabase(sql, paramMap);
    }

    public List<Attachment> getAttachments(String tableName, String fileType, Long objectId) {
        String sql = """
                select a.* from mat_attachments a
                where a.table_name = :tableName
                and a.function_code = :functionCode
                and a.object_id = :objectId
                and a.is_deleted = 'N'
                order by a.file_name
                """;
        Map mapParams = new HashMap<>();
        mapParams.put("tableName", tableName);
        mapParams.put("functionCode", fileType);
        mapParams.put("objectId", objectId);
        return getListData(sql, mapParams, AttachmentDto.class);
    }

    public <T> List<T> getAttachments(String tableName, String fileType, List<Long> objectIds, Class<T> tClass) {
        String sql = """
                SELECT a.file_id, a.file_name, a.object_id, a.attachment_id 
                FROM mat_attachments a
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
}
