/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.services.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.constants.Constant;
import vn.hbtplus.exceptions.BaseAppException;
import vn.hbtplus.exceptions.ErrorImportException;
import vn.hbtplus.exceptions.RecordNotExistsException;
import vn.hbtplus.feigns.FileStorageFeignClient;
import vn.hbtplus.feigns.ReportFeignClient;
import vn.hbtplus.models.BaseDataTableDto;
import vn.hbtplus.models.BaseResponse;
import vn.hbtplus.models.BaseSearchRequest;
import vn.hbtplus.models.ReportConfigDto;
import vn.hbtplus.models.request.EmployeesRequest;
import vn.hbtplus.models.request.InsuranceSalaryProcessRequest;
import vn.hbtplus.models.response.BaseResponseEntity;
import vn.hbtplus.models.response.EmployeesResponse;
import vn.hbtplus.models.response.InsuranceSalaryProcessResponse;
import vn.hbtplus.models.response.ObjectAttributesResponse;
import vn.hbtplus.models.response.SalaryRanksResponse;
import vn.hbtplus.models.response.TableResponseEntity;
import vn.hbtplus.repositories.entity.EmpTypesEntity;
import vn.hbtplus.repositories.entity.InsuranceSalaryProcessEntity;
import vn.hbtplus.repositories.entity.SalaryGradesEntity;
import vn.hbtplus.repositories.entity.SalaryRanksEntity;
import vn.hbtplus.repositories.impl.EmployeesRepository;
import vn.hbtplus.repositories.impl.InsuranceSalaryProcessRepository;
import vn.hbtplus.repositories.impl.SalaryRanksRepository;
import vn.hbtplus.repositories.jpa.InsuranceSalaryProcessRepositoryJPA;
import vn.hbtplus.services.AttachmentService;
import vn.hbtplus.services.FileService;
import vn.hbtplus.services.InsuranceSalaryProcessService;
import vn.hbtplus.services.ObjectAttributesService;
import vn.hbtplus.utils.ExportExcel;
import vn.hbtplus.utils.I18n;
import vn.hbtplus.utils.ImportExcel;
import vn.hbtplus.utils.ResponseUtils;
import vn.hbtplus.utils.Utils;

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
 * Lop impl service ung voi bang hr_insurance_salary_process
 *
 * @author tudd
 * @version 1.0
 * @since 1.0
 */

@Service
@RequiredArgsConstructor
@Slf4j
public class InsuranceSalaryProcessServiceImpl implements InsuranceSalaryProcessService {

    private final InsuranceSalaryProcessRepository insuranceSalaryProcessRepository;
    private final InsuranceSalaryProcessRepositoryJPA insuranceSalaryProcessRepositoryJPA;
    private final ObjectAttributesService objectAttributesService;
    private final AttachmentService attachmentService;
    private final FileService fileService;

    private final EmployeesRepository employeesRepository;
    private final SalaryRanksRepository salaryRanksRepository;
    private final FileStorageFeignClient storageFeignClient;
    private final ReportFeignClient reportFeignClient;
    private final HttpServletRequest request;


    @Override
    @Transactional(readOnly = true)
    public TableResponseEntity<InsuranceSalaryProcessResponse.SearchResult> searchData(EmployeesRequest.SearchForm dto) {
        return ResponseUtils.ok(insuranceSalaryProcessRepository.searchData(dto));
    }

    @Override
    @Transactional
    public BaseResponseEntity<Long> saveData(InsuranceSalaryProcessRequest.SubmitForm dto, List<MultipartFile> files, Long employeeId, Long id) throws BaseAppException {
        boolean isConflictProcess = insuranceSalaryProcessRepository.checkConflictProcess(dto, employeeId, id);
        if (isConflictProcess) {
            throw new BaseAppException("ERROR_SALARY_PROCESS_CONFLICT", I18n.getMessage("error.salaryProcess.validate.process"));
        }

        InsuranceSalaryProcessEntity entity;
        if (id != null && id > 0L) {
            entity = insuranceSalaryProcessRepositoryJPA.getById(id);
            if (!entity.getEmployeeId().equals(employeeId)) {
                throw new BaseAppException("insuranceSalaryProcessId and employeeId not matching!");
            }
            entity.setModifiedTime(new Date());
            entity.setModifiedBy(Utils.getUserNameLogin());
        } else {
            entity = new InsuranceSalaryProcessEntity();
            entity.setCreatedTime(new Date());
            entity.setCreatedBy(Utils.getUserNameLogin());
            entity.setEmployeeId(employeeId);
        }
        Utils.copyProperties(dto, entity);
        entity.setIsDeleted(BaseConstants.STATUS.NOT_DELETED);
        insuranceSalaryProcessRepositoryJPA.saveAndFlush(entity);
        objectAttributesService.saveObjectAttributes(entity.getInsuranceSalaryProcessId(), dto.getListAttributes(), InsuranceSalaryProcessEntity.class, null);
        insuranceSalaryProcessRepository.updateSalaryProcess(entity.getEmployeeId());
        fileService.deActiveFileByAttachmentId(dto.getAttachmentDeleteIds(), Constant.ATTACHMENT.TABLE_NAMES.HR_INSURANCE_SALARY_PROCESS, Constant.ATTACHMENT.FILE_TYPES.INSURANCE_SALARY_PROCESS_EMP);
        fileService.uploadFiles(files, entity.getInsuranceSalaryProcessId(), Constant.ATTACHMENT.TABLE_NAMES.HR_INSURANCE_SALARY_PROCESS, Constant.ATTACHMENT.FILE_TYPES.INSURANCE_SALARY_PROCESS_EMP, Constant.ATTACHMENT.MODULE);
        return ResponseUtils.ok(entity.getInsuranceSalaryProcessId());
    }

    @Override
    @Transactional
    public BaseResponseEntity<Long> deleteData(Long employeeId, Long id) throws BaseAppException {
        Optional<InsuranceSalaryProcessEntity> optional = insuranceSalaryProcessRepositoryJPA.findById(id);
        if (optional.isEmpty() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new RecordNotExistsException(id, InsuranceSalaryProcessEntity.class);
        }
        if (!optional.get().getEmployeeId().equals(employeeId)) {
            throw new BaseAppException("insuranceSalaryProcessId and employeeId not matching!");
        }
        insuranceSalaryProcessRepository.deActiveObject(InsuranceSalaryProcessEntity.class, id);
        fileService.deActiveFile(List.of(id), Constant.ATTACHMENT.TABLE_NAMES.HR_INSURANCE_SALARY_PROCESS, Constant.ATTACHMENT.FILE_TYPES.INSURANCE_SALARY_PROCESS_EMP);
        insuranceSalaryProcessRepository.updateSalaryProcess(optional.get().getEmployeeId());
        return ResponseUtils.ok(id);
    }

    @Override
    @Transactional(readOnly = true)
    public BaseResponseEntity<InsuranceSalaryProcessResponse.DetailBean> getDataById(Long employeeId, Long id) throws BaseAppException {
        Optional<InsuranceSalaryProcessEntity> optional = insuranceSalaryProcessRepositoryJPA.findById(id);
        if (optional.isEmpty() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new RecordNotExistsException(id, InsuranceSalaryProcessEntity.class);
        }
        if (!optional.get().getEmployeeId().equals(employeeId)) {
            throw new BaseAppException("insuranceSalaryProcessId and employeeId not matching!");
        }
        InsuranceSalaryProcessResponse.DetailBean dto = new InsuranceSalaryProcessResponse.DetailBean();
        Utils.copyProperties(optional.get(), dto);
        dto.setListAttributes(objectAttributesService.getAttributes(id, insuranceSalaryProcessRepository.getSQLTableName(InsuranceSalaryProcessEntity.class)));
        dto.setAttachFileList(attachmentService.getAttachmentEntities(Constant.ATTACHMENT.TABLE_NAMES.HR_INSURANCE_SALARY_PROCESS, Constant.ATTACHMENT.FILE_TYPES.INSURANCE_SALARY_PROCESS_EMP, id));
        return ResponseUtils.ok(dto);
    }

    @Override
    public ResponseEntity<Object> exportData(EmployeesRequest.SearchForm dto) throws Exception {
        BaseResponse<ReportConfigDto> response = reportFeignClient.getReportConfig(
                Utils.getRequestHeader(request), Constant.REPORT_CONFIG_CODES.DANH_SACH_QT_LUONG_NHA_NUOC);
        ExportExcel dynamicExport;
        List<Map<String, Object>> listDataExport;
        if (response != null && response.getData() != null) {
            ReportConfigDto reportConfigDto = response.getData();
            byte[] bytes = storageFeignClient.downloadFile(Utils.getRequestHeader(request), Constant.ATTACHMENT.ADMIN_MODULE,
                    reportConfigDto.getAttachmentFileList().get(0).getFileId());
            dynamicExport = new ExportExcel(new ByteArrayInputStream(bytes), 2, true);
            listDataExport = insuranceSalaryProcessRepository.getListExport(reportConfigDto.getQueryResponseList().get(0).getSqlQuery(), dto);
        } else {
            String pathTemplate = "template/export/employee/dien-bien-luong.xlsx";
            dynamicExport = new ExportExcel(pathTemplate, 2, true);
            listDataExport = insuranceSalaryProcessRepository.getListExport(null, dto);
        }

        dynamicExport.replaceKeys(listDataExport);
        return ResponseUtils.ok(dynamicExport, "thong_tin_luong_co_ban.xlsx");
    }

    @Override
    public BaseDataTableDto getTableList(Long employeeId, BaseSearchRequest request) {
        BaseDataTableDto<InsuranceSalaryProcessResponse.SearchResult> tableDto = insuranceSalaryProcessRepository.getTableList(employeeId, request);
        List<Long> ids = new ArrayList<>();
        tableDto.getListData().forEach(item -> {
            ids.add(item.getInsuranceSalaryProcessId());
        });
        Map<Long, List<ObjectAttributesResponse>> mapAttr = objectAttributesService.getListMapAttributes(ids, "hr_work_process");
        tableDto.getListData().forEach(item -> {
            item.setListAttributes(mapAttr.get(item.getInsuranceSalaryProcessId()));
        });
        return tableDto;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseEntity<Object> processImport(MultipartFile file, boolean isForceUpdate) throws Exception {
        ImportExcel importExcel = new ImportExcel("template/import/BM_import_qua_trinh_luong.xml");
        List<Object[]> dataList = new ArrayList<>();
        String userName = Utils.getUserNameLogin();

        if (importExcel.validateCommon(file.getInputStream(), dataList)) {
            List<String> empCodeList = new ArrayList<>();
            List<String> salaryRankCodes = new ArrayList<>();
            for (Object[] obj : dataList) {
                String empCode = ((String) obj[1]).toUpperCase();
                if (!empCodeList.contains(empCode)) {
                    empCodeList.add(empCode);
                }
                String salaryRankCode = (String) obj[4];
                if (!salaryRankCodes.contains(salaryRankCode.toUpperCase())) {
                    salaryRankCodes.add(salaryRankCode.toUpperCase());
                }
            }
            Map<String, EmployeesResponse.BasicInfo> mapEmp = employeesRepository.getMapEmpByCode(empCodeList);
            List<InsuranceSalaryProcessEntity> listProcess = insuranceSalaryProcessRepository.getListProcessByEmpCode(empCodeList);
            Map<String, InsuranceSalaryProcessEntity> mapProcess = new HashMap<>();
            listProcess.forEach(item -> {
                mapProcess.put(item.getEmployeeId() + "-" + Utils.formatDate(item.getStartDate()), item);
            });
            List<SalaryRanksResponse.SalaryGradeDto> listSalaryGrades = salaryRanksRepository.getSalaryGradeByRankCode(salaryRankCodes, "CO_BAN");
            Map<String, SalaryRanksResponse.SalaryGradeDto> mapSalaryGrades = new HashMap<>();
            listSalaryGrades.forEach(item -> {
                mapSalaryGrades.put(Utils.join(",",
                        item.getSalaryRankCode().toUpperCase(),
                        item.getSalaryRankName().toUpperCase(),
                        item.getGradeName().toUpperCase(),
                        Utils.formatNumber(item.getAmount(), "###.00")
                ), item);
            });
            List<EmpTypesEntity> empTypesEntities = salaryRanksRepository.findAll(EmpTypesEntity.class);
            Map<String, Long> mapEmpTyeIds = new HashMap<>();
            empTypesEntities.forEach(item -> {
                mapEmpTyeIds.put(item.getName().toLowerCase(), item.getEmpTypeId());
            });

            List<InsuranceSalaryProcessEntity> listInsert = new ArrayList<>();
            List<InsuranceSalaryProcessEntity> listUpdate = new ArrayList<>();
            List<Long> employeeIds = new ArrayList<>();
            int row = 0;
            int col;
            for (Object[] obj : dataList) {
                col = 1;
                InsuranceSalaryProcessEntity entity = null;
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
                String empType = ((String) obj[3]).toLowerCase();
                if (mapEmpTyeIds.get(empType) == null) {
                    importExcel.addError(row, 3, "Dữ liệu đối tượng không hợp lệ!", (String) obj[3]);
                }

                String key = Utils.join(",",
                        ((String) obj[4]).toUpperCase(),
                        ((String) obj[5]).toUpperCase(),
                        ((String) obj[6]).toUpperCase(),
                        Utils.formatNumber((Double) obj[7], "###.00"));
                String nangLuongTruocHan = obj[12] != null ? (String) obj[12] : "N";
                if (!StringUtils.equalsAnyIgnoreCase(nangLuongTruocHan, "Y", "N", "Có", "Không")) {
                    importExcel.addError(row, 12, "Chỉ được nhập các giá trị (Y, N, Có, Không)", nangLuongTruocHan);
                } else if (StringUtils.equalsAnyIgnoreCase(nangLuongTruocHan, "Y", "Có")) {
                    obj[12] = "Y";
                } else {
                    obj[12] = "N";
                }

                SalaryRanksResponse.SalaryGradeDto salaryGradeDto = mapSalaryGrades.get(key);
                if (mapSalaryGrades.get(key) == null) {
                    key = Utils.join(",",
                            ((String) obj[4]).toUpperCase(),
                            ((String) obj[5]).toUpperCase(),
                            ("Bậc " + obj[6]).toUpperCase(),
                            Utils.formatNumber((Double) obj[7], "###.00"));
                    salaryGradeDto = mapSalaryGrades.get(key);
                }
                if (salaryGradeDto == null) {
                    importExcel.addError(row, 5, "Dữ liệu ngạch - bậc - hệ số không khớp với dữ liệu danh mục",
                            Utils.join(",",
                                    ((String) obj[4]),
                                    ((String) obj[5]),
                                    ((String) obj[6]),
                                    Utils.formatNumber((Double) obj[7], "###.00"))
                    );
                }
                if (mapProcess.get(employeeId + "-" + Utils.formatDate((Date) obj[10])) != null) {
                    if (!isForceUpdate) {
                        importExcel.addError(row, col, "Đã tồn tại dữ liệu hệ số lương hiệu lực bắt đầu từ ngày áp dụng", Utils.formatDate((Date) obj[10]));
                    } else {
                        entity = mapProcess.get(employeeId + "-" + Utils.formatDate((Date) obj[10]));
                        entity.setModifiedTime(new Date());
                        entity.setModifiedBy(userName);
                        listUpdate.add(entity);
                    }
                }
                if (entity == null) {
                    entity = new InsuranceSalaryProcessEntity();
                    entity.setCreatedTime(new Date());
                    entity.setCreatedBy(userName);
                    entity.setEmployeeId(employeeId);
                    listInsert.add(entity);
                }
                entity.setEmpTypeId(mapEmpTyeIds.get(empType));
                entity.setSalaryRankId(salaryGradeDto == null ? null : salaryGradeDto.getSalaryRankId());
                entity.setSalaryGradeId(salaryGradeDto == null ? null : salaryGradeDto.getSalaryGradeId());
                entity.setStartDate((Date) obj[10]);
                entity.setIncrementDate((Date) obj[11]);
                entity.setReserveFactor((Double) obj[8]);
                entity.setPercent(obj[9] == null ? 100L : (Long) obj[9]);
                entity.setIsEarlyIncreased((String) obj[12]);
                entity.setDocumentNo((String) obj[13]);
                entity.setDocumentSignedDate((Date) obj[14]);
                row++;
            }

            if (importExcel.hasError()) {
                throw new ErrorImportException(file, importExcel);
            }
            insuranceSalaryProcessRepository.insertBatch(InsuranceSalaryProcessEntity.class, listInsert, userName);
            insuranceSalaryProcessRepository.updateBatch(InsuranceSalaryProcessEntity.class, listUpdate, true);

            insuranceSalaryProcessRepository.updateSalaryProcess(employeeIds);
        } else {
            throw new ErrorImportException(file, importExcel);
        }

        return ResponseUtils.ok();
    }

    @Override
    public ResponseEntity<Object> downloadImportTemplate() throws Exception {
        String pathTemplate = "template/import/BM_import_qua_trinh_luong_nha_nuoc.xlsx";
        ExportExcel dynamicExport = new ExportExcel(pathTemplate, 2, true);
        List<SalaryRanksEntity> salaryRanksEntities = salaryRanksRepository.findByProperties(SalaryRanksEntity.class, "salaryType", "CO_BAN", "order_number, name");
        List<SalaryGradesEntity> salaryGradesEntities = salaryRanksRepository.findByProperties(SalaryGradesEntity.class, "amount");
        Map<Long, List<Double>> mapSalaryGrades = new HashMap<>();
        salaryGradesEntities.forEach(item -> {
            if (mapSalaryGrades.get(item.getSalaryRankId()) == null) {
                mapSalaryGrades.put(item.getSalaryRankId(), new ArrayList<>());
            }
            if (Utils.NVL(item.getSeniorityPercent()) == 0) {
                mapSalaryGrades.get(item.getSalaryRankId()).add(item.getAmount());
            }
        });
        dynamicExport.setActiveSheet(1);
        int row = 0;
        for (SalaryRanksEntity salaryRanksEntity : salaryRanksEntities) {
            int col = 0;
            dynamicExport.setEntry(String.valueOf(row++), col++);
            dynamicExport.setText(salaryRanksEntity.getCode(), col++);
            dynamicExport.setText(salaryRanksEntity.getName(), col++);
            for (int i = 0; i < 12; i++) {
                List<Double> listFactor = mapSalaryGrades.get(salaryRanksEntity.getSalaryRankId());
                if (listFactor != null && listFactor.size() > i) {
                    dynamicExport.setEntry(Utils.formatNumber(listFactor.get(i)), col++);
                }
            }
            dynamicExport.increaseRow();
        }
        dynamicExport.setCellFormat(1, 0, dynamicExport.getLastRow(), 15, ExportExcel.BORDER_FORMAT);
        dynamicExport.setActiveSheet(0);
        return ResponseUtils.ok(dynamicExport, "BM_import_qua_trinh_luong_nha_nuoc.xlsx", false);
    }

}
