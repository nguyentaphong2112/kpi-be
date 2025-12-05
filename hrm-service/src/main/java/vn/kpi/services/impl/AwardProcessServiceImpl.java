/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.kpi.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import vn.kpi.constants.BaseConstants;
import vn.kpi.constants.Constant;
import vn.kpi.exceptions.BaseAppException;
import vn.kpi.exceptions.ErrorImportException;
import vn.kpi.exceptions.RecordNotExistsException;
import vn.kpi.feigns.FileStorageFeignClient;
import vn.kpi.feigns.ReportFeignClient;
import vn.kpi.models.BaseDataTableDto;
import vn.kpi.models.BaseResponse;
import vn.kpi.models.BaseSearchRequest;
import vn.kpi.models.ReportConfigDto;
import vn.kpi.models.request.AwardProcessRequest;
import vn.kpi.models.request.EmployeesRequest;
import vn.kpi.models.response.AwardProcessResponse;
import vn.kpi.models.response.BaseResponseEntity;
import vn.kpi.models.response.EmployeesResponse;
import vn.kpi.models.response.ObjectAttributesResponse;
import vn.kpi.models.response.TableResponseEntity;
import vn.kpi.repositories.entity.AwardProcessEntity;
import vn.kpi.repositories.entity.CategoryEntity;
import vn.kpi.repositories.impl.AwardProcessRepository;
import vn.kpi.repositories.impl.EmployeesRepository;
import vn.kpi.repositories.jpa.AwardProcessRepositoryJPA;
import vn.kpi.services.AwardProcessService;
import vn.kpi.services.ObjectAttributesService;
import vn.kpi.utils.ExportExcel;
import vn.kpi.utils.I18n;
import vn.kpi.utils.ImportExcel;
import vn.kpi.utils.ResponseUtils;
import vn.kpi.utils.Utils;

import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayInputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Lop impl service ung voi bang hr_award_process
 *
 * @author tudd
 * @version 1.0
 * @since 1.0
 */

@Service
@RequiredArgsConstructor
public class AwardProcessServiceImpl implements AwardProcessService {

    private final AwardProcessRepository awardProcessRepository;
    private final AwardProcessRepositoryJPA awardProcessRepositoryJPA;
    private final ObjectAttributesService objectAttributesService;
    private final EmployeesRepository employeesRepository;
    private final FileStorageFeignClient storageFeignClient;
    private final ReportFeignClient reportFeignClient;
    private final HttpServletRequest request;

    @Override
    @Transactional(readOnly = true)
    public TableResponseEntity<AwardProcessResponse.SearchResult> searchData(EmployeesRequest.SearchForm dto) {
        return ResponseUtils.ok(awardProcessRepository.searchData(null, dto));
    }

    @Override
    @Transactional
    public BaseResponseEntity<Long> saveData(AwardProcessRequest.SubmitForm dto, Long employeeId, Long id) throws BaseAppException {
        boolean isDuplicate = awardProcessRepository.checkDuplicate(dto, employeeId, id);
        if (isDuplicate) {
            throw new BaseAppException("ERROR_AWARD_PROCESS_DUPLICATE", I18n.getMessage("error.awardProcess.validate.duplicate"));
        }
        AwardProcessEntity entity;
        if (id != null && id > 0L) {
            entity = awardProcessRepositoryJPA.getById(id);
            if (!entity.getEmployeeId().equals(employeeId)) {
                throw new BaseAppException("awardProcessId and employeeId not match!");
            }
            entity.setModifiedTime(new Date());
            entity.setModifiedBy(Utils.getUserNameLogin());
        } else {
            entity = new AwardProcessEntity();
            entity.setCreatedTime(new Date());
            entity.setCreatedBy(Utils.getUserNameLogin());
            entity.setEmployeeId(employeeId);
        }
        Utils.copyProperties(dto, entity);
        entity.setIsDeleted(BaseConstants.STATUS.NOT_DELETED);
        awardProcessRepositoryJPA.save(entity);
        objectAttributesService.saveObjectAttributes(entity.getAwardProcessId(), dto.getListAttributes(), AwardProcessEntity.class, null);

        return ResponseUtils.ok(entity.getAwardProcessId());
    }

    @Override
    @Transactional
    public BaseResponseEntity<Long> deleteData(Long employeeId, Long id) throws BaseAppException {
        Optional<AwardProcessEntity> optional = awardProcessRepositoryJPA.findById(id);
        if (optional.isEmpty() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new RecordNotExistsException(id, AwardProcessEntity.class);
        }
        if (!optional.get().getEmployeeId().equals(employeeId)) {
            throw new BaseAppException("awardProcessId and employeeId not match!");
        }
        awardProcessRepository.deActiveObject(AwardProcessEntity.class, id);
        return ResponseUtils.ok(id);
    }

    @Override
    @Transactional(readOnly = true)
    public BaseResponseEntity<AwardProcessResponse.DetailBean> getDataById(Long employeeId, Long id) throws BaseAppException {
        Optional<AwardProcessEntity> optional = awardProcessRepositoryJPA.findById(id);
        if (optional.isEmpty() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new RecordNotExistsException(id, AwardProcessEntity.class);
        }
        if (!optional.get().getEmployeeId().equals(employeeId)) {
            throw new BaseAppException("awardProcessId and employeeId not match!");
        }
        AwardProcessResponse.DetailBean dto = new AwardProcessResponse.DetailBean();
        Utils.copyProperties(optional.get(), dto);
        dto.setListAttributes(objectAttributesService.getAttributes(id, awardProcessRepository.getSQLTableName(AwardProcessEntity.class)));
        return ResponseUtils.ok(dto);
    }

    @Override
    public ResponseEntity<Object> exportData(EmployeesRequest.SearchForm dto) throws Exception {
        BaseResponse<ReportConfigDto> response = reportFeignClient.getReportConfig(
                Utils.getRequestHeader(request), Constant.REPORT_CONFIG_CODES.DANH_SACH_QT_KHEN_THUONG);
        ExportExcel dynamicExport;
        List<Map<String, Object>> listDataExport;
        if (response != null && response.getData() != null) {
            ReportConfigDto reportConfigDto = response.getData();
            byte[] bytes = storageFeignClient.downloadFile(Utils.getRequestHeader(request), Constant.ATTACHMENT.ADMIN_MODULE,
                    reportConfigDto.getAttachmentFileList().get(0).getFileId());
            dynamicExport = new ExportExcel(new ByteArrayInputStream(bytes), 2, true);
            listDataExport = awardProcessRepository.getListExport(reportConfigDto.getQueryResponseList().get(0).getSqlQuery(), dto);
        } else {
            String pathTemplate = "template/export/employee/thong-tin-khen-thuong.xlsx";
            dynamicExport = new ExportExcel(pathTemplate, 2, true);
            listDataExport = awardProcessRepository.getListExport(null, dto);
        }
        dynamicExport.replaceKeys(listDataExport);
        return ResponseUtils.ok(dynamicExport, "thong-tin-khen-thuong.xlsx");
    }

    @Override
    public BaseDataTableDto getTableList(Long employeeId, BaseSearchRequest request) {
        BaseDataTableDto<AwardProcessResponse.SearchResult> tableDto = awardProcessRepository.getTableList(employeeId, request);
        List<Long> ids = new ArrayList<>();
        tableDto.getListData().forEach(item -> {
            ids.add(item.getAwardProcessId());
        });
        Map<Long, List<ObjectAttributesResponse>> mapAttr = objectAttributesService.getListMapAttributes(ids, "hr_work_process");
        tableDto.getListData().forEach(item -> {
            item.setListAttributes(mapAttr.get(item.getAwardProcessId()));
        });
        return tableDto;
    }

    @Override
    public ResponseEntity<Object> processImport(MultipartFile file, boolean isForceUpdate) throws Exception {
        ImportExcel importExcel = new ImportExcel("template/import/BM_import_thong_tin_khen_thuong.xml");
        List<Object[]> dataList = new ArrayList<>();
        String userName = Utils.getUserNameLogin();

        if (importExcel.validateCommon(file.getInputStream(), dataList)) {
            List<String> empCodeList = new ArrayList<>();
            for (Object[] obj : dataList) {
                String empCode = ((String) obj[1]).toUpperCase();
                if (!empCodeList.contains(empCode)) {
                    empCodeList.add(empCode);
                }
            }
            Map<String, EmployeesResponse.BasicInfo> mapEmp = employeesRepository.getMapEmpByCode(empCodeList);
            List<AwardProcessEntity> listProcess = awardProcessRepository.getListProcessByEmpCode(empCodeList);
            Map<String, AwardProcessEntity> mapProcess = new HashMap<>();
            listProcess.forEach(item -> {
                mapProcess.put(item.getEmployeeId() + "-" + item.getAwardYear() + "-" + item.getAwardFormId(), item);
            });
            List<CategoryEntity> categoryEntities = employeesRepository.findByProperties(CategoryEntity.class, "categoryType", Constant.CATEGORY_CODES.HINH_THUC_KHEN_THUONG);
            Map<String, CategoryEntity> mapCategoryEntities = new HashMap<>();
            categoryEntities.forEach(item -> {
                mapCategoryEntities.put(item.getName().toLowerCase(), item);
                mapCategoryEntities.put(Utils.NVL(item.getCode()).toLowerCase(), item);
            });

            List<AwardProcessEntity> listInsert = new ArrayList<>();
            List<AwardProcessEntity> listUpdate = new ArrayList<>();
            List<Long> employeeIds = new ArrayList<>();
            int row = 0;
            int col;
            for (Object[] obj : dataList) {
                col = 1;
                AwardProcessEntity entity = null;
                String employeeCode = (String) obj[col];
                Long employeeId = 0L;
                if (mapEmp.get(employeeCode.toLowerCase()) == null) {
                    importExcel.addError(row, col, "Mã nhân viên không tồn tại", employeeCode);
                } else if (!mapEmp.get(employeeCode.toLowerCase()).getFullName().equalsIgnoreCase((String) obj[2])) {
                    importExcel.addError(row, col, MessageFormat.format("Họ tên nhân viên không khớp với {0} - {1}", employeeCode, mapEmp.get(employeeCode.toLowerCase()).getFullName()), (String) obj[2]);
                } else {
                    employeeId = mapEmp.get(employeeCode.toLowerCase()).getEmployeeId();
                    employeeIds.add(employeeId);
                }
                col = 3;
                String awardTitle = (String) obj[col];
                CategoryEntity categoryEntity = mapCategoryEntities.get(awardTitle.toLowerCase());
                if (categoryEntity == null) {
                    importExcel.addError(row, col,
                            "Dữ liệu danh hiệu/hình thức khen thưởng không đúng theo danh mục",
                            awardTitle);
                }
                if (mapProcess.get(employeeId + "-" + (obj[4]) + "-" + (categoryEntity == null ? "" : categoryEntity.getValue())) != null) {
                    if (!isForceUpdate) {
                        importExcel.addError(row, col, "Đã tồn tại dữ liệu khen thưởng", awardTitle + ", năm " + obj[4]);
                    } else {
                        entity = mapProcess.get(employeeId + "-" + (obj[4]) + "-" + (categoryEntity == null ? "" : categoryEntity.getValue()));
                        entity.setModifiedTime(new Date());
                        entity.setModifiedBy(userName);
                        listUpdate.add(entity);
                    }
                }
                if (entity == null) {
                    entity = new AwardProcessEntity();
                    entity.setCreatedTime(new Date());
                    entity.setCreatedBy(userName);
                    entity.setEmployeeId(employeeId);
                    listInsert.add(entity);
                }
                entity.setAwardFormId(categoryEntity == null ? null : categoryEntity.getValue());
                entity.setAwardYear((Long) obj[4]);
                entity.setDocumentNo((String) obj[5]);
                entity.setDocumentSignedDate((Date) obj[6]);
                row++;
            }

            if (importExcel.hasError()) {
                throw new ErrorImportException(file, importExcel);
            }
            awardProcessRepository.insertBatch(AwardProcessEntity.class, listInsert, userName);
            awardProcessRepository.updateBatch(AwardProcessEntity.class, listUpdate, true);

        } else {
            throw new ErrorImportException(file, importExcel);
        }

        return ResponseUtils.ok();
    }

    @Override
    public ResponseEntity<Object> downloadImportTemplate() throws Exception {
        String pathTemplate = "template/import/BM_import_thong_tin_khen_thuong.xlsx";
        ExportExcel dynamicExport = new ExportExcel(pathTemplate, 2, true);
        List<CategoryEntity> salaryRanksEntities = awardProcessRepository.findByProperties(CategoryEntity.class, "categoryType", Constant.CATEGORY_CODES.HINH_THUC_KHEN_THUONG, "order_number, name");

        dynamicExport.setActiveSheet(1);
        int row = 0;
        for (CategoryEntity entity : salaryRanksEntities) {
            int col = 0;
            dynamicExport.setEntry(String.valueOf(row++), col++);
            dynamicExport.setText(entity.getCode(), col++);
            dynamicExport.setText(entity.getName(), col);
            dynamicExport.increaseRow();
        }
        dynamicExport.setCellFormat(1, 0, dynamicExport.getLastRow(), 3, ExportExcel.BORDER_FORMAT);
        dynamicExport.setActiveSheet(0);
        return ResponseUtils.ok(dynamicExport, "BM_import_thong_tin_khen_thuong.xlsx", false);
    }

}
