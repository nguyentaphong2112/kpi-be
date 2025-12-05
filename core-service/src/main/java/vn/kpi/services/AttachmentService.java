package vn.kpi.services;

import org.springframework.http.ResponseEntity;
import vn.kpi.exceptions.BaseAppException;
import vn.kpi.models.Attachment;
import vn.kpi.models.AttachmentFileDto;
import vn.kpi.utils.PlainTextEncoder;
import vn.kpi.utils.Utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.List;

public interface AttachmentService {
    void inactiveAttachment(String book, String bookAvatar, Long bookId);

    void saveAttachment(String book, String bookAvatar, Long bookId, AttachmentFileDto fileId);

    Attachment getAttachmentEntity(String tableName, String fileType, Long objectId);

    List<Attachment> getAttachmentEntities(String tableName, String fileType, Long objectId);

    List<AttachmentFileDto> getAttachmentList(String tableName, String fileType, List<Long> objIds);
    List<Attachment> getAttachmentListByObjectId(String tableName, String fileType, Long objId);

    default void validateChecksum(Long text, String checkSum) throws BaseAppException {
        String salt = Utils.getUserNameLogin() + Utils.formatDate(new Date(), "ddMMyyyy");
        if (!checkSum.equals(PlainTextEncoder.encode(text.toString(), salt))) {
            throw new BaseAppException("request is invalid");
        }
    }

    ResponseEntity<Object> downloadAttachment(Long fileId, String checksum) throws BaseAppException, IOException;

    InputStream downloadFileInputStream(Long fileId) throws BaseAppException, IOException;
}
