/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import vn.hbtplus.constants.Constant;
import vn.hbtplus.feigns.FileStorageFeignClient;
import vn.hbtplus.models.AttachmentFileDto;
import vn.hbtplus.models.BaseResponse;
import vn.hbtplus.models.dto.AbsRequestDTO;
import vn.hbtplus.models.dto.AbsTimekeepingDTO;
import vn.hbtplus.models.request.RequestsRequest;
import vn.hbtplus.models.response.*;
import vn.hbtplus.repositories.entity.RequestsEntity;
import vn.hbtplus.repositories.impl.RequestsRepository;
import vn.hbtplus.repositories.jpa.RequestsApproversJPA;
import vn.hbtplus.repositories.jpa.RequestsHandoversJPA;
import vn.hbtplus.repositories.jpa.RequestsRepositoryJPA;
import vn.hbtplus.services.AttachmentService;
import vn.hbtplus.services.RequestsService;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.utils.I18n;
import vn.hbtplus.utils.ResponseUtils;
import vn.hbtplus.utils.ExportExcel;
import vn.hbtplus.exceptions.BaseAppException;
import vn.hbtplus.exceptions.RecordNotExistsException;
import vn.hbtplus.utils.Utils;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * Lop impl service ung voi bang abs_requests
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Service
@RequiredArgsConstructor
public class RequestsServiceImpl implements RequestsService {

    private final RequestsRepository requestsRepository;
    private final RequestsRepositoryJPA requestsRepositoryJPA;
    private final AttachmentService attachmentService;
    private final FileStorageFeignClient storageFeignClient;
    private final HttpServletRequest request;

    @Override
    @Transactional(readOnly = true)
    public TableResponseEntity<RequestsResponse> searchData(RequestsRequest.SearchForm dto) {
        return ResponseUtils.ok(requestsRepository.searchData(dto));
    }

    @Override
    @Transactional
    public ResponseEntity saveData(RequestsRequest.SubmitForm dto, MultipartFile fileRequest, Long id) throws BaseAppException {
        List<Long> savedRequestIds = new ArrayList<>();
        Long firstRequestId = null;
        RequestsEntity firstEntity = null;

        // Xử lý danh sách các yêu cầu (AbsRequest)
        for (AbsRequestDTO absRequest : dto.getListAbsRequest()) {
            RequestsEntity entity = saveOrUpdateRequest(absRequest, dto.getEmployeeId(), dto.getNote(), dto.getReason());

            if (firstRequestId == null) {
                firstRequestId = entity.getRequestId();
                firstEntity = entity;
            }

            // Chỉ cập nhật requestNo nếu id không null hoặc requestNo chưa có
            if (id != null || entity.getRequestNo() == null) {
                String requestNo = String.format("%010d", firstRequestId);
                entity.setRequestNo(requestNo);
                requestsRepositoryJPA.save(entity);
            }

            savedRequestIds.add(entity.getRequestId());
        }


        // Gửi yêu cầu phê duyệt nếu trạng thái là WAIT_APPROVE
        if (Constant.REQUEST_STATUS.WAIT_APPROVE.equals(dto.getStatus())) {
            // requestApproversService.sendApproveRequest(entity);
        }

        if (fileRequest != null && firstEntity != null) {
            uploadAndSaveEntity(fileRequest, Constant.ATTACHMENT.FILE_TYPES.REQUEST_CONTENT, firstEntity);
        }


        return ResponseUtils.ok(savedRequestIds);
    }

    private void uploadAndSaveEntity(MultipartFile fileRequest,
                                     String fileType,
                                     RequestsEntity entity) {
        BaseResponse<AttachmentFileDto> response = storageFeignClient.uploadFile(Utils.getRequestHeader(request), fileRequest, Constant.ATTACHMENT.MODULE, fileType, entity);
        AttachmentFileDto fileResponse = response.getData();
        fileResponse.getFileId();

        attachmentService.inactiveAttachment(
                Constant.ATTACHMENT.TABLE_NAMES.REQUEST,
                fileType,
                Long.valueOf(entity.getRequestNo()));

        attachmentService.saveAttachment(Constant.ATTACHMENT.TABLE_NAMES.REQUEST,
                fileType,
                Long.valueOf(entity.getRequestNo()),
                fileResponse
        );
    }

    private RequestsEntity saveOrUpdateRequest(AbsRequestDTO absRequest, Long employeeId, String note, String reason) {
        RequestsEntity entity;

        if (absRequest.getRequestId() != null && absRequest.getRequestId() > 0L) {
            entity = requestsRepositoryJPA.getById(absRequest.getRequestId());
            entity.setModifiedTime(new Date());
            entity.setModifiedBy(Utils.getUserNameLogin());
        } else {
            entity = new RequestsEntity();
            entity.setCreatedTime(new Date());
            entity.setCreatedBy(Utils.getUserNameLogin());
        }

        entity.setEmployeeId(employeeId);
        entity.setNote(note);
        entity.setReason(reason);
        entity.setStatus(String.valueOf(Constant.ABS_REQUEST_STATUS.CHO_PHE_DUYET));
        entity.setStartTime(Utils.stringToDate(absRequest.getStartTime() , "dd/MM/yyyy HH:mm:ss"));
        entity.setEndTime(Utils.stringToDate(absRequest.getEndTime(), "dd/MM/yyyy HH:mm:ss"));
        entity.setReasonTypeId(absRequest.getReasonTypeId());
        entity.setIsDeleted(BaseConstants.STATUS.NOT_DELETED);

        return requestsRepositoryJPA.save(entity);
    }


    @Override
    @Transactional
    public ResponseEntity deleteData(Long id) throws RecordNotExistsException {
        Optional<RequestsEntity> optional = requestsRepositoryJPA.findById(id);
        if (!optional.isPresent() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new RecordNotExistsException(id, RequestsEntity.class);
        }
        requestsRepository.deActiveObject(RequestsEntity.class, id);
        return ResponseUtils.ok(id);
    }

    @Override
    @Transactional(readOnly = true)
    public BaseResponseEntity<RequestsResponse> getDataById(Long id) throws RecordNotExistsException {
        // Lấy dữ liệu từ requestsRepositoryJPA
        RequestsEntity foundRequest = requestsRepositoryJPA.findById(id)
                .filter(request -> !BaseConstants.STATUS.DELETED.equals(request.getIsDeleted()))
                .orElseThrow(() -> new RecordNotExistsException(id, RequestsEntity.class));

        // Tạo RequestsResponse và ánh xạ dữ liệu
        RequestsResponse responseDTO = new RequestsResponse();
        responseDTO.setEmployeeId(foundRequest.getEmployeeId());
        responseDTO.setNote(foundRequest.getNote());
        responseDTO.setReason(foundRequest.getReason());

        // Xử lý danh sách yêu cầu abs_request
        List<RequestsEntity> requests = (foundRequest.getRequestNo() != null)
                ? requestsRepositoryJPA.findByRequestNo(foundRequest.getRequestNo())
                : Collections.singletonList(foundRequest);

        List<AbsRequestDTO> absRequestList = new ArrayList<>();
        for (RequestsEntity request : requests) {
            AbsRequestDTO absRequestDTO = new AbsRequestDTO();
            absRequestDTO.setRequestId(request.getRequestId());
            absRequestDTO.setReasonTypeId(request.getReasonTypeId());
            absRequestDTO.setStartTime(Utils.formatDate(request.getStartTime() , "dd/MM/yyyy HH:mm"));
            absRequestDTO.setEndTime(Utils.formatDate(request.getEndTime(), "dd/MM/yyyy HH:mm"));
            absRequestList.add(absRequestDTO);
        }
        responseDTO.setListAbsRequest(absRequestList);
        responseDTO.setFileRequest(attachmentService.getAttachmentEntities(Constant.ATTACHMENT.TABLE_NAMES.REQUEST, Constant.ATTACHMENT.FILE_TYPES.REQUEST_CONTENT, Long.valueOf(foundRequest.getRequestNo())));

        return ResponseUtils.ok(responseDTO);
    }


    @Override
    public ResponseEntity<Object> exportData(RequestsRequest.SearchForm dto) throws Exception {
        String pathTemplate = "template/export/BM_Xuat_DS_NV_NGHI_PHEP.xlsx";
        ExportExcel dynamicExport = new ExportExcel(pathTemplate, 2, true);
        List<Map<String, Object>> listDataExport = requestsRepository.getListExport(dto);

        if (Utils.isNullOrEmpty(listDataExport)) {
            throw new BaseAppException(I18n.getMessage("global.notFound"));
        }
        dynamicExport.replaceKeys(listDataExport);
        return ResponseUtils.ok(dynamicExport, "BM_Xuat_DS_NV_NGHI_PHEP.xlsx");
    }

    @Override
    public List<AbsTimekeepingDTO> getListRequestChange(Date lastRun) {
        return requestsRepository.getListRequestChange(lastRun);
    }

}
