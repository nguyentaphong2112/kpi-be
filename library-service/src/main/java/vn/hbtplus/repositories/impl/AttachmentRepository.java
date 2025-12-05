package vn.hbtplus.repositories.impl;

import org.springframework.stereotype.Repository;
import vn.hbtplus.models.Attachment;
import vn.hbtplus.models.dto.AttachmentDto;
import vn.hbtplus.repositories.BaseRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class AttachmentRepository extends BaseRepository {

    public List<Attachment> getAttachments(String tableName, String fileType, Long objectId) {
        String sql = """
                select a.* from lib_attachments a
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
}
