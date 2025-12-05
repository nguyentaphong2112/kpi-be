/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.constants.Constant;
import vn.hbtplus.exceptions.BaseAppException;
import vn.hbtplus.exceptions.ErrorImportException;
import vn.hbtplus.exceptions.RecordNotExistsException;
import vn.hbtplus.models.BaseDataTableDto;
import vn.hbtplus.models.BaseSearchRequest;
import vn.hbtplus.models.request.EmployeesRequest;
import vn.hbtplus.models.request.PlanningAssignmentsRequest;
import vn.hbtplus.models.response.BaseResponseEntity;
import vn.hbtplus.models.response.EmployeesResponse;
import vn.hbtplus.models.response.ObjectAttributesResponse;
import vn.hbtplus.models.response.PlanningAssignmentsResponse;
import vn.hbtplus.models.response.TableResponseEntity;
import vn.hbtplus.repositories.entity.CategoryEntity;
import vn.hbtplus.repositories.entity.PlanningAssignmentsEntity;
import vn.hbtplus.repositories.impl.EmployeesRepository;
import vn.hbtplus.repositories.impl.PlanningAssignmentsRepository;
import vn.hbtplus.repositories.jpa.PlanningAssignmentsRepositoryJPA;
import vn.hbtplus.services.AttachmentService;
import vn.hbtplus.services.FileService;
import vn.hbtplus.services.ObjectAttributesService;
import vn.hbtplus.services.PlanningAssignmentsService;
import vn.hbtplus.utils.AsyncUtils;
import vn.hbtplus.utils.ExportExcel;
import vn.hbtplus.utils.ImportExcel;
import vn.hbtplus.utils.ResponseUtils;
import vn.hbtplus.utils.Utils;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Lop impl service ung voi bang hr_planning_assignments
 *
 * @author tudd
 * @version 1.0
 * @since 1.0
 */

@Service
@RequiredArgsConstructor
public class PlanningAssignmentsServiceImpl implements PlanningAssignmentsService {

    private final PlanningAssignmentsRepository planningAssignmentsRepository;
    private final PlanningAssignmentsRepositoryJPA planningAssignmentsRepositoryJPA;
    private final ObjectAttributesService objectAttributesService;
    private final FileService fileService;
    private final AttachmentService attachmentService;
    private final EmployeesRepository employeesRepository;
    @Qualifier("taskExecutor")
    private final ThreadPoolTaskExecutor taskExecutor;

    @Override
    @Transactional(readOnly = true)
    public TableResponseEntity<PlanningAssignmentsResponse.SearchResult> searchData(EmployeesRequest.SearchForm dto) {
        return ResponseUtils.ok(planningAssignmentsRepository.searchData(dto));
    }

    @Override
    @Transactional
    public ResponseEntity saveData(PlanningAssignmentsRequest.SubmitForm dto, List<MultipartFile> files, Long employeeId, Long id) throws BaseAppException {
        PlanningAssignmentsEntity entity;
        if (id != null && id > 0L) {
            entity = planningAssignmentsRepositoryJPA.getById(id);
            if (!entity.getEmployeeId().equals(employeeId)) {
                throw new BaseAppException("planningAssignmentId and employeeId not matching!");
            }
            entity.setModifiedTime(new Date());
            entity.setModifiedBy(Utils.getUserNameLogin());
        } else {
            entity = new PlanningAssignmentsEntity();
            entity.setCreatedTime(new Date());
            entity.setCreatedBy(Utils.getUserNameLogin());
            entity.setEmployeeId(employeeId);
        }
        Utils.copyProperties(dto, entity);
        entity.setIsDeleted(BaseConstants.STATUS.NOT_DELETED);
        planningAssignmentsRepositoryJPA.saveAndFlush(entity);
        objectAttributesService.saveObjectAttributes(entity.getPlanningAssignmentId(), dto.getListAttributes(), PlanningAssignmentsEntity.class, null);
        fileService.deActiveFileByAttachmentId(dto.getAttachmentDeleteIds(), Constant.ATTACHMENT.TABLE_NAMES.HR_PLANNING_ASSIGNMENTS, Constant.ATTACHMENT.FILE_TYPES.PLANNING_ASSIGNMENTS_EMP);
        fileService.uploadFiles(files, entity.getPlanningAssignmentId(), Constant.ATTACHMENT.TABLE_NAMES.HR_PLANNING_ASSIGNMENTS, Constant.ATTACHMENT.FILE_TYPES.PLANNING_ASSIGNMENTS_EMP, Constant.ATTACHMENT.MODULE);
        return ResponseUtils.ok(entity.getPlanningAssignmentId());
    }

    @Override
    @Transactional
    public ResponseEntity deleteData(Long employeeId, Long id) throws BaseAppException {
        Optional<PlanningAssignmentsEntity> optional = planningAssignmentsRepositoryJPA.findById(id);
        if (optional.isEmpty() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new RecordNotExistsException(id, PlanningAssignmentsEntity.class);
        }
        if (!optional.get().getEmployeeId().equals(employeeId)) {
            throw new BaseAppException("planningAssignmentId and employeeId not matching!");
        }
        planningAssignmentsRepository.deActiveObject(PlanningAssignmentsEntity.class, id);
        fileService.deActiveFile(List.of(id), Constant.ATTACHMENT.TABLE_NAMES.HR_PLANNING_ASSIGNMENTS, Constant.ATTACHMENT.FILE_TYPES.PLANNING_ASSIGNMENTS_EMP);
        return ResponseUtils.ok(id);
    }

    @Override
    @Transactional(readOnly = true)
    public BaseResponseEntity<PlanningAssignmentsResponse.DetailBean> getDataById(Long employeeId, Long id) throws BaseAppException {
        Optional<PlanningAssignmentsEntity> optional = planningAssignmentsRepositoryJPA.findById(id);
        if (optional.isEmpty() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new RecordNotExistsException(id, PlanningAssignmentsEntity.class);
        }
        if (!optional.get().getEmployeeId().equals(employeeId)) {
            throw new BaseAppException("planningAssignmentId and employeeId not matching!");
        }
        PlanningAssignmentsResponse.DetailBean dto = new PlanningAssignmentsResponse.DetailBean();
        Utils.copyProperties(optional.get(), dto);
        dto.setListAttributes(objectAttributesService.getAttributes(id, planningAssignmentsRepository.getSQLTableName(PlanningAssignmentsEntity.class)));
        dto.setAttachFileList(attachmentService.getAttachmentEntities(Constant.ATTACHMENT.TABLE_NAMES.HR_PLANNING_ASSIGNMENTS, Constant.ATTACHMENT.FILE_TYPES.PLANNING_ASSIGNMENTS_EMP, id));
        return ResponseUtils.ok(dto);
    }

    @Override
    public ResponseEntity<Object> exportData(EmployeesRequest.SearchForm dto) throws Exception {
        String pathTemplate = "template/export/employee/thong-tin-quy-hoach.xlsx";
        ExportExcel dynamicExport = new ExportExcel(pathTemplate, 2, true);
        List<Map<String, Object>> listDataExport = planningAssignmentsRepository.getListExport(dto);

        dynamicExport.replaceKeys(listDataExport);
        return ResponseUtils.ok(dynamicExport, "thong-tin-quy-hoach.xlsx");
    }

    @Override
    public BaseDataTableDto getTableList(Long employeeId, BaseSearchRequest request) {
        BaseDataTableDto<PlanningAssignmentsResponse.SearchResult> tableDto = planningAssignmentsRepository.getTableList(employeeId, request);
        List<Long> ids = new ArrayList<>();
        tableDto.getListData().forEach(item -> {
            ids.add(item.getPlanningAssignmentId());
        });
        Map<Long, List<ObjectAttributesResponse>> mapAttr = objectAttributesService.getListMapAttributes(ids, "hr_work_process");
        tableDto.getListData().forEach(item -> {
            item.setListAttributes(mapAttr.get(item.getPlanningAssignmentId()));
        });
        return tableDto;
    }

    @Override
    public ResponseEntity<Object> downloadImportTemplate() throws Exception {
        String pathTemplate = "template/import/BM_import_qua_trinh_quy_hoach.xlsx";
        ExportExcel dynamicExport = new ExportExcel(pathTemplate, 2, true);
        return ResponseUtils.ok(dynamicExport, "BM-Import-Du-lieu-quy-hoach.xlsx", false);
    }

    @Override
    public ResponseEntity<Object> processImport(MultipartFile file, boolean isForceUpdate) throws IOException {
        ImportExcel importExcel = new ImportExcel("template/import/BM_import_qua_trinh_quy_hoach.xml");
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
            Map<String, EmployeesResponse.BasicInfo> mapEmp = new HashMap<>();
            Map<String, CategoryEntity> mapGiaiDoan = new HashMap<>();
            Map<String, CategoryEntity> mapChucvu = new HashMap<>();
            Map<String, CategoryEntity> mapLyDo = new HashMap<>();
            Map<String, PlanningAssignmentsEntity> mapPlanningAssignments = new HashMap<>();

            AsyncUtils.runParallel(taskExecutor.getThreadPoolExecutor(),
                    () -> {
                        mapEmp.putAll(employeesRepository.getMapEmpByCode(empCodeList));
                    },
                    () -> {
                        employeesRepository.findByProperties(CategoryEntity.class, "categoryType", Constant.CATEGORY_CODES.HR_GIAI_DOAN_QUY_HOACH).forEach(item -> {
                            mapGiaiDoan.put(item.getName().toLowerCase(), item);
                        });
                    },
                    () -> {
                        employeesRepository.findByProperties(CategoryEntity.class, "categoryType", Constant.CATEGORY_CODES.HR_CHUC_VU_QUY_HOACH).forEach(item -> {
                            mapChucvu.put(item.getName().toLowerCase(), item);
                        });
                    },
                    () -> {
                        planningAssignmentsRepository.getListPlanningAssignments(empCodeList).forEach(item -> {
                            String key = Utils.join(",", List.of( item.getEmployeeId(),
                                    item.getPositionId(), item.getPlanningPeriodId(), item.getStartDate()));
                            mapPlanningAssignments.put(key, item);
                        });
                    },
                    () -> {
                        employeesRepository.findByProperties(CategoryEntity.class, "categoryType", Constant.CATEGORY_CODES.HR_LY_DO_RA_QUY_HOACH).forEach(item -> {
                            mapLyDo.put(item.getName().toLowerCase(), item);
                        });
                    }
            );

            List<PlanningAssignmentsEntity> listInsert = new ArrayList<>();
            List<PlanningAssignmentsEntity> listUpdate = new ArrayList<>();
            int row = 0;
            int col;
            for (Object[] obj : dataList) {
                col = 1;
                PlanningAssignmentsEntity entity =  new PlanningAssignmentsEntity();
                String employeeCode = (String) obj[col];
                Long employeeId = 0L;
                if (mapEmp.get(employeeCode.toLowerCase()) == null) {
                    importExcel.addError(row, col, "Mã nhân viên không tồn tại", employeeCode);
                } else if (!mapEmp.get(employeeCode.toLowerCase()).getFullName().equalsIgnoreCase((String) obj[2])) {
                    importExcel.addError(row, col, MessageFormat.format("Họ tên nhân viên không khớp với {0} - {1}", employeeCode, mapEmp.get(employeeCode.toLowerCase()).getFullName()), (String) obj[2]);
                } else {
                    employeeId = mapEmp.get(employeeCode.toLowerCase()).getEmployeeId();
                }
                String giaiDoan = ((String) obj[3]).toLowerCase();
                if (mapGiaiDoan.get(giaiDoan) == null) {
                    importExcel.addError(row, 3, "Dữ liệu giai đoạn quy hoạch không hợp lệ!", (String) obj[3]);
                } else {
                    entity.setPlanningPeriodId(mapGiaiDoan.get(giaiDoan).getValue());
                }
                String chucVu = ((String) obj[4]).toLowerCase();
                if (mapChucvu.get(chucVu) == null) {
                    importExcel.addError(row, 4, "Dữ liệu chức vụ quy hoạch không hợp lệ!", (String) obj[4]);
                } else {
                    entity.setPositionId(mapChucvu.get(chucVu).getValue());
                }
                entity.setStartDate((Date) obj[7]);

                String key = Utils.join(",", List.of( entity.getEmployeeId(),
                        entity.getPositionId(), entity.getPlanningPeriodId(), entity.getStartDate()));
                if(mapPlanningAssignments.get(key) != null) {
                    entity = mapPlanningAssignments.get(key);
                    entity.setModifiedBy(userName);
                    entity.setModifiedTime(new Date());
                } else {
                    entity.setCreatedBy(userName);
                    entity.setCreatedTime(new Date());
                }


                if (Utils.isNullOrEmpty((String) obj[9])) {
                    String lydo = ((String) obj[9]).toLowerCase();
                    if (mapLyDo.get(lydo) == null) {
                        importExcel.addError(row, 9, "Dữ liệu lý do không hợp lệ!", (String) obj[9]);
                    } else  {
                        entity.setEndReasonId(mapLyDo.get(lydo).getValue());
                    }
                }
                entity.setEmployeeId(employeeId);
                listInsert.add(entity);

                entity.setDocumentNo((String) obj[5]);
                entity.setDocumentSignedDate((Date) obj[6]);
                entity.setStartDate((Date) obj[7]);


                entity.setEndDate((Date) obj[8]);
                entity.setEndDocumentNo((String) obj[10]);
                entity.setEndDocumentSignedDate((Date) obj[11]);
                row++;
            }

            if (importExcel.hasError()) {
                throw new ErrorImportException(file, importExcel);
            }
            planningAssignmentsRepository.insertBatch(PlanningAssignmentsEntity.class, listInsert, userName);
            planningAssignmentsRepository.updateBatch(PlanningAssignmentsEntity.class, listUpdate, true);

        } else {
            throw new ErrorImportException(file, importExcel);
        }

        return ResponseUtils.ok();
    }

}
