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
import vn.kpi.feigns.AdminFeignClient;
import vn.kpi.models.BaseDataTableDto;
import vn.kpi.models.BaseSearchRequest;
import vn.kpi.models.dto.BaseCategoryDto;
import vn.kpi.models.request.EmployeesRequest;
import vn.kpi.models.request.EvaluationResultsRequest;
import vn.kpi.models.response.BaseResponseEntity;
import vn.kpi.models.response.EmployeesResponse;
import vn.kpi.models.response.EvaluationResultsResponse;
import vn.kpi.models.response.ObjectAttributesResponse;
import vn.kpi.models.response.TableResponseEntity;
import vn.kpi.repositories.entity.EvaluationPeriodsEntity;
import vn.kpi.repositories.entity.EvaluationResultsEntity;
import vn.kpi.repositories.impl.EmployeesRepository;
import vn.kpi.repositories.impl.EvaluationResultsRepository;
import vn.kpi.repositories.jpa.EvaluationResultsRepositoryJPA;
import vn.kpi.services.EvaluationResultsService;
import vn.kpi.services.ObjectAttributesService;
import vn.kpi.utils.ExportExcel;
import vn.kpi.utils.ImportExcel;
import vn.kpi.utils.ResponseUtils;
import vn.kpi.utils.Utils;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Lop impl service ung voi bang hr_evaluation_results
 *
 * @author tudd
 * @version 1.0
 * @since 1.0
 */

@Service
@RequiredArgsConstructor
public class EvaluationResultsServiceImpl implements EvaluationResultsService {

    private final EvaluationResultsRepository evaluationResultsRepository;
    private final ObjectAttributesService objectAttributesService;
    private final EvaluationResultsRepositoryJPA evaluationResultsRepositoryJPA;
    private final EmployeesRepository employeesRepository;
    private final AdminFeignClient adminFeignClient;

    @Override
    @Transactional(readOnly = true)
    public TableResponseEntity<EvaluationResultsResponse.SearchResult> searchData(EmployeesRequest.SearchForm dto) {
        return ResponseUtils.ok(evaluationResultsRepository.searchData(dto));
    }

    @Override
    @Transactional
    public ResponseEntity saveData(EvaluationResultsRequest.SubmitForm dto, Long employeeId, Long evaluationResultId) throws BaseAppException {
        boolean isDuplicate = evaluationResultsRepository.duplicate(EvaluationResultsEntity.class, evaluationResultId, "year", dto.getYear(), "evaluationPeriodId", dto.getEvaluationPeriodId(), "evaluationType", dto.getEvaluationType(), "employeeId", employeeId);
        if (isDuplicate) {
            throw new BaseAppException("error.evaluationResults.name.duplicate");
        }
        EvaluationResultsEntity entity;
        if (evaluationResultId != null && evaluationResultId > 0L) {
            entity = evaluationResultsRepositoryJPA.getById(evaluationResultId);
            if (!entity.getEmployeeId().equals(employeeId)) {
                throw new BaseAppException("evaluationResultId and employeeId not match!");
            }
            entity.setModifiedTime(new Date());
            entity.setModifiedBy(Utils.getUserNameLogin());
        } else {
            entity = new EvaluationResultsEntity();
            entity.setCreatedTime(new Date());
            entity.setCreatedBy(Utils.getUserNameLogin());
            entity.setEmployeeId(employeeId);
        }
        Utils.copyProperties(dto, entity);
        entity.setIsDeleted(BaseConstants.STATUS.NOT_DELETED);
        evaluationResultsRepositoryJPA.save(entity);

        objectAttributesService.saveObjectAttributes(entity.getEvaluationResultId(), dto.getListAttributes(), EvaluationResultsEntity.class, null);

        return ResponseUtils.ok(entity.getEvaluationResultId());
    }

    @Override
    @Transactional
    public ResponseEntity deleteData(Long employeeId, Long id) throws BaseAppException {
        Optional<EvaluationResultsEntity> optional = evaluationResultsRepositoryJPA.findById(id);
        if (!optional.isPresent() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new RecordNotExistsException(id, EvaluationResultsEntity.class);
        }
        if (!optional.get().getEmployeeId().equals(employeeId)) {
            throw new BaseAppException("evaluationResultId not mapping employeeId");
        }
        evaluationResultsRepository.deActiveObject(EvaluationResultsEntity.class, id);
        return ResponseUtils.ok(id);
    }

    @Override
    @Transactional(readOnly = true)
    public BaseResponseEntity<EvaluationResultsResponse.DetailBean> getDataById(Long employeeId, Long id) throws BaseAppException {
        Optional<EvaluationResultsEntity> optional = evaluationResultsRepositoryJPA.findById(id);
        if (optional.isEmpty() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new RecordNotExistsException(id, EvaluationResultsEntity.class);
        }
        if (!optional.get().getEmployeeId().equals(employeeId)) {
            throw new BaseAppException("evaluationResultId not mapping employeeId");
        }
        EvaluationResultsResponse.DetailBean dto = new EvaluationResultsResponse.DetailBean();
        Utils.copyProperties(optional.get(), dto);
        dto.setListAttributes(objectAttributesService.getAttributes(id, evaluationResultsRepository.getSQLTableName(EvaluationResultsEntity.class)));
        return ResponseUtils.ok(dto);
    }

    @Override
    public ResponseEntity<Object> exportData(EmployeesRequest.SearchForm dto) throws Exception {
        String pathTemplate = "template/export/employee/qua-trinh-danh-gia.xlsx";
        ExportExcel dynamicExport = new ExportExcel(pathTemplate, 2, true);
        List<Map<String, Object>> listDataExport = evaluationResultsRepository.getListExport(dto);

        dynamicExport.replaceKeys(listDataExport);
        return ResponseUtils.ok(dynamicExport, "qua-trinh-danh-gia.xlsx");
    }

    @Override
    public BaseDataTableDto getTableList(Long employeeId, BaseSearchRequest request) {
        BaseDataTableDto<EvaluationResultsResponse.SearchResult> tableDto = evaluationResultsRepository.getTableList(employeeId, request);
        List<Long> ids = new ArrayList<>();
        tableDto.getListData().forEach(item -> {
            ids.add(item.getEvaluationResultId());
        });
        Map<Long, List<ObjectAttributesResponse>> mapAttr = objectAttributesService.getListMapAttributes(ids, "hr_evaluation_results");
        tableDto.getListData().forEach(item -> {
            item.setListAttributes(mapAttr.get(item.getEvaluationResultId()));
        });
        return tableDto;
    }

    @Override
    public List<EvaluationResultsResponse.EvaluationPeriods> getListEvaluationPeriods(Integer year, String evaluationType) {
        return evaluationResultsRepository.getListEvaluationPeriods(year, evaluationType);
    }

    @Override
    public ResponseEntity<Object> downloadImportTemplate(Long periodId) throws Exception {
        String pathTemplate = "template/import/BM_import_thong_tin_danh_gia.xlsx";
        ExportExcel dynamicExport = new ExportExcel(pathTemplate, 2, true);
        EvaluationPeriodsEntity evaluationPeriodsEntity = evaluationResultsRepository.get(EvaluationPeriodsEntity.class, periodId);
        List<String> configResults = this.getConfigResultOfEvaluationType(evaluationPeriodsEntity.getEvaluationType());
        dynamicExport.setActiveSheet(1);
        int row = 1;
        for (String item : configResults) {
            dynamicExport.setText(item, 1, row++);
        }
        dynamicExport.setActiveSheet(0);
        return ResponseUtils.ok(dynamicExport, "BM_import_thong_tin_danh_gia.xlsx", false);
    }

    @Override
    public ResponseEntity<Object> processImport(MultipartFile file, Long periodId, boolean isForceUpdate) throws IOException {
        ImportExcel importExcel = new ImportExcel("template/import/BM_import_qua_trinh_luong.xml");
        List<Object[]> dataList = new ArrayList<>();
        String userName = Utils.getUserNameLogin();
        EvaluationPeriodsEntity evaluationPeriodsEntity = evaluationResultsRepository.get(EvaluationPeriodsEntity.class, periodId);

        if (importExcel.validateCommon(file.getInputStream(), dataList)) {
            List<String> empCodeList = new ArrayList<>();
            for (Object[] obj : dataList) {
                String empCode = ((String) obj[1]).toUpperCase();
                if (!empCodeList.contains(empCode)) {
                    empCodeList.add(empCode);
                }
            }
            Map<String, EmployeesResponse.BasicInfo> mapEmp = employeesRepository.getMapEmpByCode(empCodeList);
            //Lay thong tin danh gia
            List<EvaluationResultsEntity> listOldData = evaluationResultsRepository.getListEntities(periodId, empCodeList);
            Map<Long, EvaluationResultsEntity> mapProcess = new HashMap();
            listOldData.forEach(item -> {
                mapProcess.put(item.getEvaluationResultId(), item);
            });

            List<EvaluationResultsEntity> listInsert = new ArrayList<>();
            List<EvaluationResultsEntity> listUpdate = new ArrayList<>();
            List<Long> employeeIds = new ArrayList<>();
            int row = 0;
            int col;
            List configResults = this.getConfigResultOfEvaluationType(evaluationPeriodsEntity.getEvaluationType());
            for (Object[] obj : dataList) {
                col = 1;
                EvaluationResultsEntity entity = null;
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

                if (mapProcess.get(employeeId) != null) {
                    if (!isForceUpdate) {
                        importExcel.addError(row, col, "Đã tồn tại dữ liệu đánh giá của nhân viên", employeeCode);
                    } else {
                        entity = mapProcess.get(employeeId);
                        entity.setModifiedTime(new Date());
                        entity.setModifiedBy(userName);
                        listUpdate.add(entity);
                    }
                }
                if (entity == null) {
                    entity = new EvaluationResultsEntity();
                    entity.setEvaluationPeriodId(periodId);
                    entity.setEvaluationType(evaluationPeriodsEntity.getEvaluationType());
                    entity.setCreatedTime(new Date());
                    entity.setCreatedBy(userName);
                    entity.setEmployeeId(employeeId);
                    listInsert.add(entity);
                }
                if (!configResults.isEmpty() && !configResults.contains(obj[3])) {
                    importExcel.addError(row, 3, "Dữ liệu kết quả chỉ được nhập các giá trị: " + configResults.toString(), (String) obj[3]);
                }
                entity.setKpiResult((String) obj[3]);
                entity.setKpiPoint((Double) obj[4]);
                entity.setNote((String) obj[5]);
                row++;
            }

            if (importExcel.hasError()) {
                throw new ErrorImportException(file, importExcel);
            }
            evaluationResultsRepository.insertBatch(EvaluationResultsEntity.class, listInsert, userName);
            evaluationResultsRepository.updateBatch(EvaluationResultsEntity.class, listUpdate, true);

        } else {
            throw new ErrorImportException(file, importExcel);
        }

        return ResponseUtils.ok();
    }

    private List getConfigResultOfEvaluationType(String evaluationType) {
        BaseCategoryDto.DetailBean dto = adminFeignClient.getCategory(Utils.getHeader(),
                Constant.CATEGORY_CODES.KPI_LOAI_DANH_GIA,
                evaluationType
        ).getData();
        List<String> results = new ArrayList<>();
        for (ObjectAttributesResponse attr : dto.getListAttributes()) {
            if (attr.getAttributeCode().equals("KET_QUA")) {
                String[] temps = attr.getAttributeValue().split(",");
                for (String temp : temps) {
                    results.add(temp.trim());
                }
            }
        }
        return results;
    }
}
