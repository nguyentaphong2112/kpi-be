package vn.kpi.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.kpi.constants.BaseConstants;
import vn.kpi.constants.Constant;
import vn.kpi.exceptions.BaseAppException;
import vn.kpi.models.BaseDataTableDto;
import vn.kpi.models.request.FeedbackRequest;
import vn.kpi.models.response.FeedbackResponse;
import vn.kpi.repositories.entity.FeedbackCommentsEntity;
import vn.kpi.repositories.entity.FeedbacksEntity;
import vn.kpi.repositories.impl.FeedbackRepository;
import vn.kpi.repositories.jpa.FeedbackCommentRepositoryJPA;
import vn.kpi.repositories.jpa.FeedbackRepositoryJPA;
import vn.kpi.services.FeedbackService;
import vn.kpi.services.FileService;
import vn.kpi.utils.ExportExcel;
import vn.kpi.utils.I18n;
import vn.kpi.utils.ResponseUtils;
import vn.kpi.utils.Utils;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class FeedbackServiceImpl implements FeedbackService {
    private final FeedbackRepository feedbackRepository;
    private final FeedbackRepositoryJPA feedbackRepositoryJPA;
    private final FeedbackCommentRepositoryJPA feedbackCommentRepositoryJPA;
    private final FileService fileService;
    private final AttachmentServiceImpl attachmentService;

    @Override
    public BaseDataTableDto<FeedbackResponse.SearchResult> searchData(FeedbackRequest.SearchForm dto) {
        return feedbackRepository.searchData(dto);
    }

    @Override
    public BaseDataTableDto<FeedbackResponse.SearchResult> adminSearchData(FeedbackRequest.SearchForm dto) {
        BaseDataTableDto<FeedbackResponse.SearchResult> tableData = feedbackRepository.adminSearchData(dto);
        for (FeedbackResponse.SearchResult searchResult : tableData.getListData()) {
            searchResult.setAttachFileList(attachmentService.getAttachmentListByObjectId(Constant.ATTACHMENT.TABLE_NAMES.SYS_FEEDBACKS, Constant.ATTACHMENT.FILE_TYPES.SYS_FEEDBACKS, searchResult.getFeedbackId()));
        }
        return tableData;
    }

    @Override
    @Transactional
    public boolean saveData(Long id, FeedbackRequest.SubmitForm dto) {
        FeedbacksEntity feedbacksEntity;
        if (id != null) {
            feedbacksEntity = feedbackRepositoryJPA.getById(id);
            feedbacksEntity.setModifiedBy(Utils.getUserNameLogin());
            feedbacksEntity.setModifiedTime(new Date());
        } else {
            feedbacksEntity = new FeedbacksEntity();
            feedbacksEntity.setCreatedBy(Utils.getUserNameLogin());
            feedbacksEntity.setCreatedTime(new Date());
        }
        Utils.copyProperties(dto, feedbacksEntity);
        feedbacksEntity.setUserId(Utils.getUserIdLogin());
        feedbacksEntity.setIsDeleted(BaseConstants.STATUS.NOT_DELETED);
        feedbacksEntity.setStatus(FeedbacksEntity.STATUS.NEW);
        feedbackRepositoryJPA.save(feedbacksEntity);
        fileService.uploadFiles(dto.getAttachments(), feedbacksEntity.getFeedbackId(), Constant.ATTACHMENT.TABLE_NAMES.SYS_FEEDBACKS, Constant.ATTACHMENT.FILE_TYPES.SYS_FEEDBACKS, Constant.ATTACHMENT.MODULE);
        return true;
    }

    @Override
    @Transactional
    public boolean deleteData(Long id) {
        feedbackRepository.deActiveObject(FeedbacksEntity.class, id);
        return true;
    }

    @Override
    public FeedbackResponse.DetailBean getDataById(Long id) {
        FeedbacksEntity feedbacksEntity = feedbackRepositoryJPA.getById(id);
        if (feedbacksEntity != null) {
            FeedbackResponse.DetailBean result = new FeedbackResponse.DetailBean();
            Utils.copyProperties(feedbacksEntity, result);
            result.setComments(feedbackRepository.getListComment(id));
//            result.setAttachFileList(attachmentService.getAttachmentEntities(Constant.ATTACHMENT.TABLE_NAMES.SYS_FEEDBACKS, Constant.ATTACHMENT.FILE_TYPES.SYS_FEEDBACKS, id));
            return result;
        }
        return null;
    }

    @Override
    public FeedbackResponse.DetailBean adminGetDataById(Long id) {
        FeedbackResponse.DetailBean result = feedbackRepository.getDataById(id);
        result.setComments(feedbackRepository.getListComment(id));
        result.setAttachFileList(attachmentService.getAttachmentListByObjectId(Constant.ATTACHMENT.TABLE_NAMES.SYS_FEEDBACKS, Constant.ATTACHMENT.FILE_TYPES.SYS_FEEDBACKS, id));
        return result;
    }

    @Override
    public boolean saveComment(Long id, FeedbackRequest.SubmitForm dto) {
        FeedbackCommentsEntity feedbackCommentsEntity = new FeedbackCommentsEntity();
        feedbackCommentsEntity.setFeedbackId(id);
        feedbackCommentsEntity.setCreatedBy(Utils.getUserNameLogin());
        feedbackCommentsEntity.setCreatedTime(new Date());
        feedbackCommentsEntity.setContent(dto.getContent());
        feedbackCommentsEntity.setIsDeleted(BaseConstants.STATUS.NOT_DELETED);
        feedbackCommentRepositoryJPA.save(feedbackCommentsEntity);
        return true;
    }

    @Override
    public ResponseEntity<Object> processFeedBack(Long id, FeedbackRequest.SubmitForm dto) {
        FeedbacksEntity feedbacksEntity = feedbackRepository.get(FeedbacksEntity.class, id);
        if (feedbacksEntity == null || feedbacksEntity.getIsDeleted().equals(BaseConstants.STATUS.DELETED)) {
            throw new BaseAppException("FEEDBACK_NOT_FOUND", "Feedback not found");
        }
        feedbacksEntity.setModifiedBy(Utils.getUserNameLogin());
        feedbacksEntity.setModifiedTime(new Date());
        feedbacksEntity.setStatus(dto.getStatus());
        feedbacksEntity.setType(dto.getType());
        feedbackRepositoryJPA.save(feedbacksEntity);
        if (!Utils.isNullOrEmpty(dto.getContent())) {
            this.saveComment(id, dto);
        }
        return ResponseEntity.ok(id);
    }

    @Override
    public ResponseEntity<Object> exportData(FeedbackRequest.SearchForm dto) throws Exception {
        String pathTemplate = "template/export/thong_tin_phan_anh.xlsx";
        ExportExcel dynamicExport = new ExportExcel(pathTemplate, 2, true);
        List<Map<String, Object>> listDataExport = feedbackRepository.getListExport(dto);

        if (Utils.isNullOrEmpty(listDataExport)) {
            throw new BaseAppException(I18n.getMessage("global.notFound"));
        }

        dynamicExport.replaceKeys(listDataExport);
        return ResponseUtils.ok(dynamicExport, "thong_tin_phan_anh.xlsx");
    }
}
