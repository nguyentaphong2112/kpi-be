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
import vn.hbtplus.feigns.AdminFeignClient;
import vn.hbtplus.models.dto.BaseCategoryDto;
import vn.hbtplus.models.dto.CategoryDto;
import vn.hbtplus.models.dto.EmployeeDto;
import vn.hbtplus.models.dto.ParticiDTO;
import vn.hbtplus.models.request.MentoringTrainersRequest;
import vn.hbtplus.models.response.BaseResponseEntity;
import vn.hbtplus.models.response.MentoringTrainersResponse;
import vn.hbtplus.models.response.TableResponseEntity;
import vn.hbtplus.repositories.entity.CategoryEntity;
import vn.hbtplus.repositories.entity.MentoringTrainersEntity;
import vn.hbtplus.repositories.impl.MentoringTrainersRepository;
import vn.hbtplus.repositories.impl.TrainingProcessRepository;
import vn.hbtplus.repositories.jpa.MentoringTrainersRepositoryJPA;
import vn.hbtplus.services.MentoringTrainersService;
import vn.hbtplus.utils.AsyncUtils;
import vn.hbtplus.utils.ExportExcel;
import vn.hbtplus.utils.I18n;
import vn.hbtplus.utils.ImportExcel;
import vn.hbtplus.utils.ResponseUtils;
import vn.hbtplus.utils.Utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Lop impl service ung voi bang lms_mentoring_trainers
 *
 * @author tudd
 * @version 1.0
 * @since 1.0
 */

@Service
@RequiredArgsConstructor
public class MentoringTrainersServiceImpl implements MentoringTrainersService {

    private final MentoringTrainersRepository mentoringTrainersRepository;
    private final MentoringTrainersRepositoryJPA mentoringTrainersRepositoryJPA;
    private final TrainingProcessRepository trainingProcessRepository;
    private final AdminFeignClient adminFeignClient;
    @Qualifier("taskExecutor")
    private final ThreadPoolTaskExecutor taskExecutor;

    @Override
    @Transactional(readOnly = true)
    public TableResponseEntity<MentoringTrainersResponse> searchData(MentoringTrainersRequest.SearchForm dto) {
        return ResponseUtils.ok(mentoringTrainersRepository.searchData(dto));
    }

    @Override
    @Transactional
    public ResponseEntity saveData(MentoringTrainersRequest.SubmitForm dto, Long id) throws BaseAppException {
        List<Long> savedmentoringTrainersIds = new ArrayList<>();

        for (ParticiDTO partici : dto.getListParticipating()) {
            MentoringTrainersEntity entity;
            if (id != null && id > 0L) {
                entity = mentoringTrainersRepositoryJPA.getById(id);
                entity.setModifiedTime(new Date());
                entity.setModifiedBy(Utils.getUserNameLogin());
            } else {
                entity = new MentoringTrainersEntity();
                entity.setCreatedTime(new Date());
                entity.setCreatedBy(Utils.getUserNameLogin());
            }
            Utils.copyProperties(dto, entity);
            entity.setEmployeeId(partici.getEmployeeId());
            entity.setRoleId(partici.getRoleId());
            entity.setIsDeleted(BaseConstants.STATUS.NOT_DELETED);
            mentoringTrainersRepositoryJPA.save(entity);
            savedmentoringTrainersIds.add(entity.getMentoringTrainerId());
        }

        return ResponseUtils.ok(savedmentoringTrainersIds);
    }

    @Override
    @Transactional
    public ResponseEntity deleteData(Long id) throws RecordNotExistsException {
        Optional<MentoringTrainersEntity> optional = mentoringTrainersRepositoryJPA.findById(id);
        if (!optional.isPresent() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new RecordNotExistsException(id, MentoringTrainersEntity.class);
        }
        mentoringTrainersRepository.deActiveObject(MentoringTrainersEntity.class, id);
        return ResponseUtils.ok(id);
    }

    @Override
    @Transactional(readOnly = true)
    public BaseResponseEntity<MentoringTrainersResponse> getDataById(Long id) throws RecordNotExistsException {
        Optional<MentoringTrainersEntity> optional = mentoringTrainersRepositoryJPA.findById(id);
        if (!optional.isPresent() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new RecordNotExistsException(id, MentoringTrainersEntity.class);
        }
        MentoringTrainersResponse dto = new MentoringTrainersResponse();
        Utils.copyProperties(optional.get(), dto);
        return ResponseUtils.ok(dto);
    }

    @Override
    public ResponseEntity<Object> exportData(MentoringTrainersRequest.SearchForm dto) throws Exception {
        String pathTemplate = "template/export/med/BM_Xuat_chi_dao_tuyen_duoi.xlsx";
        ExportExcel dynamicExport = new ExportExcel(pathTemplate, 2, true);
        List<Map<String, Object>> listDataExport = mentoringTrainersRepository.getListExport(dto);

        if (Utils.isNullOrEmpty(listDataExport)) {
            throw new BaseAppException(I18n.getMessage("global.notFound"));
        }
        dynamicExport.replaceKeys(listDataExport);
        return ResponseUtils.ok(dynamicExport, "BM_Xuat_chi_dao_tuyen_duoi.xlsx");
    }

    @Override
    public ResponseEntity<Object> downloadTemplate() throws Exception {
        String importTemplateName = "BM_import_chi_dao_tuyen_duoi.xlsx";
        String pathTemplate = "template/import/" + importTemplateName;
        ExportExcel dynamicExport = new ExportExcel(pathTemplate, 2, true);
        String fileName = Utils.getFilePathExport(importTemplateName);

        List<CategoryDto> listType = new ArrayList<>();
        List<CategoryDto> listJob = new ArrayList<>();
        List<CategoryDto> listHospital = new ArrayList<>();
        List<EmployeeDto> listEmployee = new ArrayList<>();
        AsyncUtils.runParallel(taskExecutor.getThreadPoolExecutor(),
                () -> listType.addAll(mentoringTrainersRepository.getListCategories(Constant.CATEGORY_CODES.CDT_VAI_TRO)),
                () -> listJob.addAll(mentoringTrainersRepository.getListCategories(Constant.CATEGORY_CODES.CDT_CHUYEN_MON)),
                () -> listHospital.addAll(mentoringTrainersRepository.getListCategories(Constant.CATEGORY_CODES.CDT_BENH_VIEN)),
                () -> listEmployee.addAll(mentoringTrainersRepository.getListEmployee())
        );
        int row = 1;
        int activeSheet = 1;
        dynamicExport.setActiveSheet(activeSheet++);
        for (CategoryDto categoryDto : listType) {
            dynamicExport.setText(String.valueOf(row), 0, row);
            dynamicExport.setText(categoryDto.getName(), 1, row++);
        }
        dynamicExport.setActiveSheet(activeSheet++);
        row = 1;
        for (CategoryDto categoryDto : listJob) {
            dynamicExport.setText(String.valueOf(row), 0, row);
            dynamicExport.setText(categoryDto.getName(), 1, row++);
        }
        dynamicExport.setActiveSheet(activeSheet++);
        row = 1;
        for (CategoryDto categoryDto : listHospital) {
            dynamicExport.setText(String.valueOf(row), 0, row);
            dynamicExport.setText(categoryDto.getName(), 1, row++);
        }
        dynamicExport.setActiveSheet(activeSheet);
        row = 1;
        for (EmployeeDto employeeDto : listEmployee) {
            dynamicExport.setText(String.valueOf(row), 0, row);
            dynamicExport.setText(employeeDto.getEmployeeCode(), 1, row);
            dynamicExport.setText(employeeDto.getEmployeeName(), 2, row++);
        }

        dynamicExport.setActiveSheet(0);
        dynamicExport.exportFile(fileName);
        return ResponseUtils.ok(dynamicExport, "BM_import_chi_dao_tuyen_duoi.xlsx", false);
    }

    @Override
    public ResponseEntity importProcess(MultipartFile file) throws IOException {
        ImportExcel importExcel = new ImportExcel("template/import/BM_import_chi_dao_tuyen_duoi.xml");
        List<Object[]> dataList = new ArrayList<>();

        if (importExcel.validateCommon(file.getInputStream(), dataList)) {

            Map<String, String> mapRoleName = new ConcurrentHashMap<>();
            Map<String, String> mapMajorName = new ConcurrentHashMap<>();
            Map<String, String> mapHospitalName = new ConcurrentHashMap<>();
            Map<String, EmployeeDto> mapEmps = new ConcurrentHashMap<>();

            AsyncUtils.runParallel(taskExecutor.getThreadPoolExecutor(),
                    () -> {
                        List<CategoryEntity> listRole = mentoringTrainersRepository.findByProperties(CategoryEntity.class, "categoryType", Constant.CATEGORY_CODES.CDT_VAI_TRO);
                        listRole.forEach(item -> mapRoleName.put(item.getName().toLowerCase(), item.getValue()));
                    },
                    () -> {
                        List<CategoryEntity> listMajor = mentoringTrainersRepository.findByProperties(CategoryEntity.class, "categoryType", Constant.CATEGORY_CODES.CDT_CHUYEN_MON);
                        listMajor.forEach(item -> mapMajorName.put(item.getName().toLowerCase(), item.getValue()));
                    },
                    () -> {
                        List<CategoryEntity> listHospital = mentoringTrainersRepository.findByProperties(CategoryEntity.class, "categoryType", Constant.CATEGORY_CODES.CDT_BENH_VIEN);
                        listHospital.forEach(item -> mapHospitalName.put(item.getName().toLowerCase(), item.getValue()));
                    },
                    () -> {
                        List<EmployeeDto> employeeDtoList = trainingProcessRepository.getListEmployee();
                        employeeDtoList.forEach(dto -> {
                            mapEmps.put(dto.getEmployeeCode(), dto);
                            mapEmps.put(dto.getIdentityNo(), dto);
                        });
                    }
            );

            List<MentoringTrainersEntity> listInsert = new ArrayList<>();
            String userName = Utils.getUserNameLogin();
            Date curDate = new Date();

            int row = 0;
            for (Object[] obj : dataList) {
                MentoringTrainersEntity entity = new MentoringTrainersEntity();
                entity.setCreatedBy(userName);
                entity.setCreatedTime(curDate);

                int col = 1;
                String employeeCode = (String) obj[col++];
                EmployeeDto empDto = mapEmps.get(employeeCode);
                if (empDto == null) {
                    importExcel.addError(row, 1, I18n.getMessage("error.lms.employee"), employeeCode);
                } else if (!empDto.getEmployeeName().equalsIgnoreCase((String) obj[2])) {
                    importExcel.addError(row, 1, "Tên nhân viên không đúng theo cơ sở dữ liệu", String.format("%s (Thực tế: %s)", obj[2], empDto.getEmployeeName()));
                }
                entity.setEmployeeId(empDto.getEmployeeId());
                col++;

                String roleName = Utils.NVL(((String) obj[col])).trim();
                String roleId = mapRoleName.get(roleName.toLowerCase());
                if (!Utils.isNullOrEmpty(roleName) && roleId == null) {
                    importExcel.addError(row, col, I18n.getMessage("error.import.category.invalid"), roleName);
                } else {
                    entity.setRoleId(roleId);
                }
                col++;
                entity.setStartDate((Date) obj[col++]);
                entity.setEndDate((Date) obj[col++]);

                String majorName = Utils.NVL(((String) obj[col])).trim();
                String majorId = mapMajorName.get(majorName.toLowerCase());
                if (!Utils.isNullOrEmpty(majorName) && majorId == null) {
                    try {
                        BaseCategoryDto saveCategoryDto = adminFeignClient.createCategory(Utils.getHeader(),
                                new BaseCategoryDto(null, majorName), Constant.CATEGORY_CODES.CDT_CHUYEN_MON).getData();
                        mapMajorName.put(majorName.toLowerCase(), saveCategoryDto.getValue());
                        entity.setMajorId(saveCategoryDto.getValue());
                    } catch (Exception ex) {
                        importExcel.addError(row, col, I18n.getMessage("error.import.category.invalid"), majorName);
                    }
                } else {
                    entity.setMajorId(majorId);
                }
                col++;

                String hospitalName = Utils.NVL(((String) obj[col])).trim();
                String hospitalId = mapHospitalName.get(hospitalName.toLowerCase());
                if (!Utils.isNullOrEmpty(hospitalName) && hospitalId == null) {
                    try {
                        BaseCategoryDto saveCategoryDto = adminFeignClient.createCategory(Utils.getHeader(),
                                new BaseCategoryDto(null, hospitalName), Constant.CATEGORY_CODES.CDT_BENH_VIEN).getData();
                        mapHospitalName.put(hospitalName.toLowerCase(), saveCategoryDto.getValue());
                        entity.setHospitalId(saveCategoryDto.getValue());
                    } catch (Exception ex) {
                        importExcel.addError(row, col, I18n.getMessage("error.import.category.invalid"), hospitalName);
                    }
                } else {
                    entity.setHospitalId(hospitalId);
                }
                col++;
                entity.setContent((String) obj[col++]);
                entity.setClassName((String) obj[col++]);
                entity.setTotalLessons((Long) obj[col++]);
                entity.setTotalClasses((Long) obj[col++]);
                entity.setTotalStudents((Long) obj[col++]);
                entity.setTotalExaminations((Long) obj[col++]);
                entity.setTotalSurgeries((Long) obj[col++]);
                entity.setTotalTests((Long) obj[col]);

                listInsert.add(entity);
                row++;
            }

            if (importExcel.hasError()) {
                throw new ErrorImportException(file, importExcel);
            } else {
                mentoringTrainersRepository.insertBatch(MentoringTrainersEntity.class, listInsert, userName);
            }
        } else {
            throw new ErrorImportException(file, importExcel);
        }
        return ResponseUtils.ok(true);
    }

}
