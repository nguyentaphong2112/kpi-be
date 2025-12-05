package vn.kpi.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import vn.kpi.exceptions.BaseAppException;
import vn.kpi.models.Attachment;
import vn.kpi.models.AttachmentFileDto;
import vn.kpi.services.AttachmentService;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AttachmentServiceImpl implements AttachmentService {
    @Override
    public void inactiveAttachment(String book, String bookAvatar, Long bookId) {

    }

    @Override
    public void saveAttachment(String book, String bookAvatar, Long bookId, AttachmentFileDto fileId) {

    }

    @Override
    public Attachment getAttachmentEntity(String tableName, String fileType, Long bookId) {
        return null;
    }

    @Override
    public List<Attachment> getAttachmentEntities(String tableName, String fileType, Long bookId) {
        return null;
    }

    @Override
    public List<AttachmentFileDto> getAttachmentList(String tableName, String fileType, List<Long> objIds) {
        return null;
    }

    @Override
    public List<Attachment> getAttachmentListByObjectId(String tableName, String fileType, Long objId) {
        return null;
    }

    @Override
    public ResponseEntity<Object> downloadAttachment(Long fileId, String checksum) throws BaseAppException, IOException {
        return null;
    }

    @Override
    public InputStream downloadFileInputStream(Long fileId) throws BaseAppException, IOException {
        return null;
    }
}
