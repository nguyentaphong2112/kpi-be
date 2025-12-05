/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.services.impl;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import vn.hbtplus.models.request.TrainingProcessRequest;
import vn.hbtplus.models.response.BaseResponseEntity;
import vn.hbtplus.models.response.TableResponseEntity;
import vn.hbtplus.models.response.TrainingProcessResponse;
import vn.hbtplus.repositories.entity.TrainingProcessBudgetsEntity;
import vn.hbtplus.repositories.entity.TrainingProcessEntity;
import vn.hbtplus.repositories.impl.InternshipSessionsRepository;
import vn.hbtplus.repositories.impl.TrainingProcessRepository;
import vn.hbtplus.repositories.jpa.TrainingProcessBudgetsRepositoryJPA;
import vn.hbtplus.repositories.jpa.TrainingProcessRepositoryJPA;
import vn.hbtplus.services.AttachmentService;
import vn.hbtplus.services.FileService;
import vn.hbtplus.services.ObjectAttributesService;
import vn.hbtplus.services.TrainingProcessService;
import vn.hbtplus.utils.AsyncUtils;
import vn.hbtplus.utils.ExportExcel;
import vn.hbtplus.utils.I18n;
import vn.hbtplus.utils.ImportExcel;
import vn.hbtplus.utils.ResponseUtils;
import vn.hbtplus.utils.Utils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Lop impl service ung voi bang lms_training_process
 *
 * @author tudd
 * @version 1.0
 * @since 1.0
 */

@Service
@RequiredArgsConstructor
public class TrainingProcessServiceImpl implements TrainingProcessService {

    private static final Logger log = LoggerFactory.getLogger(TrainingProcessServiceImpl.class);
    private final TrainingProcessRepository trainingProcessRepository;
    private final TrainingProcessRepositoryJPA trainingProcessRepositoryJPA;
    private final TrainingProcessBudgetsRepositoryJPA trainingProcessBudgetsRepositoryJPA;
    private final ObjectAttributesService objectAttributesService;
    private final FileService fileService;
    private final AttachmentService attachmentService;
    private final InternshipSessionsRepository internshipSessionsRepository;
    @Qualifier("taskExecutor")
    private final ThreadPoolTaskExecutor taskExecutor;
    private final AdminFeignClient adminFeignClient;

    @Override
    @Transactional(readOnly = true)
    public TableResponseEntity<TrainingProcessResponse.SearchResult> searchData(TrainingProcessRequest.SearchForm dto) {
        return ResponseUtils.ok(trainingProcessRepository.searchData(dto));
    }

    @Override
    @Transactional
    public ResponseEntity saveData(TrainingProcessRequest.SubmitForm dto, List<MultipartFile> files, Long id) throws BaseAppException {
        TrainingProcessEntity entity;
        if (id != null && id > 0L) {
            entity = trainingProcessRepositoryJPA.getById(id);
            entity.setModifiedTime(new Date());
            entity.setModifiedBy(Utils.getUserNameLogin());
        } else {
            entity = new TrainingProcessEntity();
            entity.setCreatedTime(new Date());
            entity.setCreatedBy(Utils.getUserNameLogin());
        }
        Utils.copyProperties(dto, entity);
        entity.setIsDeleted(BaseConstants.STATUS.NOT_DELETED);
        trainingProcessRepositoryJPA.save(entity);
        List<TrainingProcessBudgetsEntity> entityList = trainingProcessRepository.findByProperties(TrainingProcessBudgetsEntity.class, "trainingProcessId", entity.getTrainingProcessId());
        dto.getBudgetsList().forEach(it -> {
            boolean exists = false;
            for (TrainingProcessBudgetsEntity entityChild : entityList) {
                if (entityChild.getBudgetTypeId().equals(it.getBudgetTypeId())) {
                    exists = true;
                    entityChild.setAmount(it.getAmount());
                    entityChild.setModifiedTime(new Date());
                    entityChild.setModifiedBy(Utils.getUserNameLogin());
                    trainingProcessBudgetsRepositoryJPA.save(entityChild);
                    break;
                }
            }
            if (it.getAmount() != null && it.getAmount() > 0L && !exists) {
                TrainingProcessBudgetsEntity budgetsEntity = new TrainingProcessBudgetsEntity();
                budgetsEntity.setBudgetTypeId(it.getBudgetTypeId());
                budgetsEntity.setAmount(it.getAmount());
                budgetsEntity.setTrainingProcessId(entity.getTrainingProcessId());
                budgetsEntity.setCreatedTime(new Date());
                budgetsEntity.setCreatedBy(Utils.getUserNameLogin());
                budgetsEntity.setIsDeleted(BaseConstants.STATUS.NOT_DELETED);
                trainingProcessBudgetsRepositoryJPA.save(budgetsEntity);
            }
        });
        objectAttributesService.saveObjectAttributes(entity.getTrainingProcessId(), dto.getListAttributes(), TrainingProcessEntity.class, null);
        fileService.deActiveFileByAttachmentId(dto.getAttachmentDeleteIds(), Constant.ATTACHMENT.TABLE_NAMES.LMS_TRAINING_PROCESS, Constant.ATTACHMENT.FILE_TYPES.LMS_TRAINING_PROCESS);
        fileService.uploadFiles(files, entity.getTrainingProcessId(), Constant.ATTACHMENT.TABLE_NAMES.LMS_TRAINING_PROCESS, Constant.ATTACHMENT.FILE_TYPES.LMS_TRAINING_PROCESS, Constant.ATTACHMENT.MODULE);
        return ResponseUtils.ok(entity.getTrainingProcessId());
    }

    @Override
    @Transactional
    public ResponseEntity deleteData(Long id) throws RecordNotExistsException {
        Optional<TrainingProcessEntity> optional = trainingProcessRepositoryJPA.findById(id);
        if (!optional.isPresent() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new RecordNotExistsException(id, TrainingProcessEntity.class);
        }
        trainingProcessRepository.deActiveObject(TrainingProcessEntity.class, id);
        return ResponseUtils.ok(id);
    }

    @Override
    @Transactional(readOnly = true)
    public BaseResponseEntity<TrainingProcessResponse.Detail> getDataById(Long id) throws RecordNotExistsException {
        Optional<TrainingProcessEntity> optional = trainingProcessRepositoryJPA.findById(id);
        if (!optional.isPresent() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new RecordNotExistsException(id, TrainingProcessEntity.class);
        }
        TrainingProcessResponse.Detail dto = new TrainingProcessResponse.Detail();
        List<TrainingProcessBudgetsEntity> entityList = trainingProcessRepository.findByProperties(TrainingProcessBudgetsEntity.class, "trainingProcessId", id);
        List<TrainingProcessRequest.Budgets> budgetsList = new ArrayList<>();
        entityList.forEach(it -> {
            TrainingProcessRequest.Budgets budgets = new TrainingProcessRequest.Budgets();
            budgets.setBudgetTypeId(it.getBudgetTypeId());
            budgets.setAmount(it.getAmount());
            budgetsList.add(budgets);
        });
        dto.setBudgetsList(budgetsList);
        dto.setListAttributes(objectAttributesService.getAttributes(id, Constant.ATTACHMENT.TABLE_NAMES.LMS_TRAINING_PROCESS));
        dto.setAttachFileList(attachmentService.getAttachmentEntities(Constant.ATTACHMENT.TABLE_NAMES.LMS_TRAINING_PROCESS, Constant.ATTACHMENT.FILE_TYPES.LMS_TRAINING_PROCESS, id));
        Utils.copyProperties(optional.get(), dto);
        return ResponseUtils.ok(dto);
    }

    @Override
    public ResponseEntity<Object> exportData(TrainingProcessRequest.SearchForm dto) throws Exception {
        String pathTemplate = "template/export/BM_Xuat_DS_dao_tao_noi_vien.xlsx";
        ExportExcel dynamicExport = new ExportExcel(pathTemplate, 2, true);
        List<Map<String, Object>> listDataExport = trainingProcessRepository.getListExport(dto);

        dynamicExport.replaceKeys(listDataExport);
        return ResponseUtils.ok(dynamicExport, "BM_Xuat_DS_dao_tao_noi_vien.xlsx");
    }

    @Override
    public String getImportTemplate() throws Exception {
        String importTemplateName = "BM_import_DS_dao_tao_noi_vien.xlsx";
        String pathTemplate = "template/import/" + importTemplateName;
        ExportExcel dynamicExport = new ExportExcel(pathTemplate, 2, true);
        String fileName = Utils.getFilePathExport(importTemplateName);
        List<CategoryDto> listMajor = new ArrayList<>();
        List<CategoryDto> listTrainingPlace = new ArrayList<>();
        List<CategoryDto> listTrainingPlan = new ArrayList<>();
        List<CategoryDto> listTrainingCourse = new ArrayList<>();
        List<CategoryDto> listSource = new ArrayList<>();

        AsyncUtils.runParallel(taskExecutor.getThreadPoolExecutor(),
                () -> {
                    listMajor.addAll(internshipSessionsRepository.getListCategories(Constant.CATEGORY_CODES.LMS_NOI_DUNG_DAO_TAO));
                },
                () -> {
                    listTrainingPlace.addAll(internshipSessionsRepository.getListCategories(Constant.CATEGORY_CODES.LMS_NOI_DAO_TAO));
                },
                () -> {
                    listTrainingPlan.addAll(internshipSessionsRepository.getListCategories(Constant.CATEGORY_CODES.LMS_KE_HOACH_DAO_TAO));
                },
                () -> {
                    listTrainingCourse.addAll(internshipSessionsRepository.getListCategories(Constant.CATEGORY_CODES.LMS_KHOA_DAO_TAO));
                },
                () -> {
                    listSource.addAll(internshipSessionsRepository.getListCategories(Constant.CATEGORY_CODES.LMS_NGUON_KINH_PHI));
                }
        );
        int col = 12;
        for (CategoryDto categoryDto : listSource) {
            dynamicExport.setText(categoryDto.getName(), col, 4);
            dynamicExport.mergeCell(4, col, 5, col);
            dynamicExport.setCellFormat(4, col, 5, col, ExportExcel.BOLD_FORMAT);
            dynamicExport.setCellFormat(4, col, 5, col, ExportExcel.BORDER_FORMAT);
            dynamicExport.setCellFormat(4, col, 5, col, ExportExcel.CENTER_FORMAT);
            dynamicExport.setFontName(4, col, 5, col, ExportExcel.TIMES_NEW_ROMAN);
            dynamicExport.setFontSize(4, col, 5, col, 12.0);
            dynamicExport.setColumnWidth(col, 20);
            col++;
        }
        int row = 0;
        dynamicExport.setActiveSheet(1);
        for (CategoryDto categoryDto : listMajor) {
            dynamicExport.setText(categoryDto.getName(), 1, row++);
        }
        row = 0;
        dynamicExport.setActiveSheet(2);
        for (CategoryDto categoryDto : listTrainingPlace) {
            dynamicExport.setText(categoryDto.getName(), 1, row++);
        }
        row = 0;
        dynamicExport.setActiveSheet(3);
        for (CategoryDto categoryDto : listTrainingPlan) {
            dynamicExport.setText(categoryDto.getName(), 1, row++);
        }
        row = 0;
        dynamicExport.setActiveSheet(4);
        for (CategoryDto categoryDto : listTrainingCourse) {
            dynamicExport.setText(categoryDto.getName(), 1, row++);
        }
        dynamicExport.setActiveSheet(0);
        dynamicExport.exportFile(fileName);
        return fileName;
    }

    @Override
    public boolean importData(MultipartFile fileImport) throws Exception {
        String fileConfigName = "BM_import_DS_dao_tao_noi_vien.xml";
        ImportExcel importExcel = new ImportExcel("template/import/" + fileConfigName);
        List<Object[]> dataList = new ArrayList<>();
        String userName = Utils.getUserNameLogin();
        List<TrainingProcessEntity> entityList = new ArrayList<>();
        List<TrainingProcessBudgetsEntity> entityBudgetList = new ArrayList<>();


        Map<String, EmployeeDto> mapEmps = new ConcurrentHashMap<>();
        Map<String, String> mapNoiDungDaoTao = new ConcurrentHashMap<>();
        Map<String, String> listTrainingPlace = new ConcurrentHashMap<>();
        Map<String, String> mapKeHoachDaoTao = new ConcurrentHashMap<>();
        Map<String, String> mapKhoaDaoTao = new ConcurrentHashMap<>();
        List<CategoryDto> listSource = new ArrayList();

        AsyncUtils.runParallel(taskExecutor.getThreadPoolExecutor(),
                () -> {
                    List<EmployeeDto> employeeDtoList = trainingProcessRepository.getListEmployee();
                    employeeDtoList.forEach(dto -> {
                        mapEmps.put(dto.getEmployeeCode(), dto);
                        mapEmps.put(dto.getIdentityNo(), dto);
                    });
                },
                () -> {
                    mapNoiDungDaoTao.putAll(internshipSessionsRepository.getListCategories(Constant.CATEGORY_CODES.LMS_NOI_DUNG_DAO_TAO).stream()
                            .collect(Collectors.toMap(c -> c.getName().toLowerCase(), CategoryDto::getValue)));
                },
                () -> {
                    listTrainingPlace.putAll(internshipSessionsRepository.getListCategories(Constant.CATEGORY_CODES.LMS_NOI_DAO_TAO).stream()
                            .collect(Collectors.toMap(c -> c.getName().toLowerCase(), CategoryDto::getValue)));
                },
                () -> {
                    mapKeHoachDaoTao.putAll(internshipSessionsRepository.getListCategories(Constant.CATEGORY_CODES.LMS_KE_HOACH_DAO_TAO).stream()
                            .collect(Collectors.toMap(c -> c.getName().toLowerCase(), CategoryDto::getValue)));
                },
                () -> {
                    mapKhoaDaoTao.putAll(internshipSessionsRepository.getListCategories(Constant.CATEGORY_CODES.LMS_KHOA_DAO_TAO).stream()
                            .collect(Collectors.toMap(c -> c.getName().toLowerCase(), CategoryDto::getValue)));
                },
                () -> {
                    listSource.addAll(internshipSessionsRepository.getListCategories(Constant.CATEGORY_CODES.LMS_NGUON_KINH_PHI));
                });


        if (importExcel.validateCommon(fileImport.getInputStream(), dataList)) {
            int index = 0;
            for (Object[] obj : dataList) {
                int col = 1;
                String employeeCode = (String) obj[col++];
                EmployeeDto empDto = mapEmps.get(employeeCode);
                if (empDto == null) {
                    importExcel.addError(index, 1, I18n.getMessage("error.lms.employee"), employeeCode);
                } else if (!empDto.getEmployeeName().equalsIgnoreCase((String) obj[2])) {
                    importExcel.addError(index, 1, "Tên nhân viên không đúng theo cơ sở dữ liệu", String.format("%s (Thực tế: %s)", obj[2], empDto.getEmployeeName()));
                }
                col++;

                Date startDate = (Date) obj[col++];
                Date endDate = (Date) obj[col++];
                if (endDate != null && startDate.after(endDate)) {
                    importExcel.addError(index, 2, I18n.getMessage("error.rangeDate"), startDate.toLocaleString());
                }
                String majorName = (String) obj[col++];
                String majorId = mapNoiDungDaoTao.get(majorName.toLowerCase().trim());
                if (majorId == null) {
                    try {
                        BaseCategoryDto saveCategoryDto = adminFeignClient.createCategory(Utils.getHeader(),
                                new BaseCategoryDto(null, majorName), Constant.CATEGORY_CODES.LMS_NOI_DUNG_DAO_TAO).getData();
                        mapNoiDungDaoTao.put(majorName.toLowerCase(), saveCategoryDto.getValue());
                        majorId = saveCategoryDto.getValue();
                    } catch (Exception ex) {
                        log.error("", ex);
                        importExcel.addError(index, 1, I18n.getMessage("error.internship.import.invalid", majorName), majorName);
                    }
                }
                String trainingPlaceName = (String) obj[col++];
                String trainingPlaceId = listTrainingPlace.get(trainingPlaceName.toLowerCase().trim());
                if (trainingPlaceId == null) {
                    try {
                        BaseCategoryDto saveCategoryDto = adminFeignClient.createCategory(Utils.getHeader(),
                                new BaseCategoryDto(null, trainingPlaceName), Constant.CATEGORY_CODES.LMS_NOI_DAO_TAO).getData();
                        listTrainingPlace.put(trainingPlaceName.toLowerCase(), saveCategoryDto.getValue());
                        trainingPlaceId = saveCategoryDto.getValue();
                    } catch (Exception ex) {
                        log.error("", ex);
                        importExcel.addError(index, 1, I18n.getMessage("error.internship.import.invalid", trainingPlaceName), trainingPlaceName);
                    }
                }
                String trainingPlanName = (String) obj[col++];
                String trainingPlanId = null;
                if (trainingPlanName != null) {
                    trainingPlanId = mapKeHoachDaoTao.get(trainingPlanName.toLowerCase().trim());
                    if (trainingPlanId == null) {
                        try {
                            BaseCategoryDto saveCategoryDto = adminFeignClient.createCategory(Utils.getHeader(),
                                    new BaseCategoryDto(null, trainingPlanName), Constant.CATEGORY_CODES.LMS_KE_HOACH_DAO_TAO).getData();
                            mapKeHoachDaoTao.put(trainingPlanName.toLowerCase(), saveCategoryDto.getValue());
                            trainingPlanId = saveCategoryDto.getValue();
                        } catch (Exception ex) {
                            log.error("", ex);
                            importExcel.addError(index, 1, I18n.getMessage("error.internship.import.invalid", trainingPlanName), trainingPlanName);
                        }
                    }
                }
                String trainingCourseName = (String) obj[col++];
                String trainingCourseId = null;
                if (trainingCourseName != null) {
                    trainingCourseId = mapKhoaDaoTao.get(trainingCourseName.toLowerCase().trim());
                    if (trainingCourseId == null) {
                        try {
                            BaseCategoryDto saveCategoryDto = adminFeignClient.createCategory(Utils.getHeader(),
                                    new BaseCategoryDto(null, trainingCourseName), Constant.CATEGORY_CODES.LMS_KHOA_DAO_TAO).getData();
                            mapKhoaDaoTao.put(trainingCourseName.toLowerCase(), saveCategoryDto.getValue());
                            trainingCourseId = saveCategoryDto.getValue();
                        } catch (Exception ex) {
                            log.error("", ex);
                            importExcel.addError(index, 1, I18n.getMessage("error.internship.import.invalid", trainingCourseName), trainingCourseName);
                        }
                    }
                }
                String documentNo = (String) obj[col++];
                Date documentSignedDate = (Date) obj[col++];
                Double totalHours = (Double) obj[col++];
                Long totalBudget = (Long) obj[col];
                TrainingProcessEntity entity = new TrainingProcessEntity();
                entity.setTrainingProcessId(internshipSessionsRepository.getNextId(TrainingProcessEntity.class));
                entity.setEmployeeId(empDto.getEmployeeId());
                entity.setStartDate(startDate);
                entity.setEndDate(endDate);
                entity.setMajorId(majorId);
                entity.setTrainingPlaceId(trainingPlaceId);
                entity.setTrainingPlanId(trainingPlanId);
                entity.setTrainingCourseId(trainingCourseId);
                entity.setDocumentNo(documentNo);
                entity.setDocumentSignedDate(documentSignedDate);
                entity.setTotalHours(totalHours);
                entity.setTotalBudget(totalBudget);
                entityList.add(entity);
                Double total = 0.0;
                for (CategoryDto source : listSource) {
                    col++;
                    Double amount = (Double) obj[col];
                    if (amount != null) {
                        total += amount;
                        Long budgetTypeId = Long.valueOf(source.getValue());
                        TrainingProcessBudgetsEntity budgetsEntity = new TrainingProcessBudgetsEntity();
                        budgetsEntity.setBudgetTypeId(budgetTypeId);
                        budgetsEntity.setAmount(amount);
                        budgetsEntity.setTrainingProcessId(entity.getTrainingProcessId());
                        entityBudgetList.add(budgetsEntity);
                    }
                }
                Double totalResult = 0.0;
                if (totalBudget != null && totalBudget > 0) {
                    totalResult = Double.valueOf(totalBudget);
                }
                if (!total.equals(totalResult)) {
                    importExcel.addError(index, 1, I18n.getMessage("error.trainingProcess.import.total", totalBudget), totalBudget.toString());
                }
                index++;
            }
            if (importExcel.hasError()) {
                throw new ErrorImportException(fileImport, importExcel);
            } else {
                internshipSessionsRepository.insertBatch(TrainingProcessEntity.class, entityList, userName);
                internshipSessionsRepository.insertBatch(TrainingProcessBudgetsEntity.class, entityBudgetList, userName);
            }
        } else {
            throw new ErrorImportException(fileImport, importExcel);
        }
        return true;
    }

}
