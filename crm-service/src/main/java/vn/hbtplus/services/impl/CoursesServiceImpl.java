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
import vn.hbtplus.exceptions.ErrorImportException;
import vn.hbtplus.models.request.CourseLessonResultsRequest;
import vn.hbtplus.models.request.CourseLessonsRequest;
import vn.hbtplus.models.request.CourseTraineesRequest;
import vn.hbtplus.models.request.CoursesRequest;
import vn.hbtplus.models.response.*;
import vn.hbtplus.repositories.entity.*;
import vn.hbtplus.repositories.impl.*;
import vn.hbtplus.repositories.jpa.CourseLessonResultsRepositoryJPA;
import vn.hbtplus.repositories.jpa.CourseLessonsRepositoryJPA;
import vn.hbtplus.repositories.jpa.CourseTraineesRepositoryJPA;
import vn.hbtplus.repositories.jpa.CoursesRepositoryJPA;
import vn.hbtplus.services.*;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.utils.*;
import vn.hbtplus.exceptions.BaseAppException;
import vn.hbtplus.exceptions.RecordNotExistsException;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Lop impl service ung voi bang crm_courses
 *
 * @author tudd
 * @version 1.0
 * @since 1.0
 */

@Service
@RequiredArgsConstructor
public class CoursesServiceImpl implements CoursesService {

    private final CoursesRepository coursesRepository;
    private final CoursesRepositoryJPA coursesRepositoryJPA;
    private final ObjectAttributesService objectAttributesService;
    private final CourseLessonsRepositoryJPA courseLessonsRepositoryJPA;
    private final CourseTraineesRepositoryJPA courseTraineesRepositoryJPA;
    private final CourseLessonsRepository courseLessonsRepository;
    private final CourseLessonResultsService courseLessonResultsService;
    private final CourseLessonResultsRepository courseLessonResultsRepository;
    private final CourseLessonResultsRepositoryJPA courseLessonResultsRepositoryJPA;
    private final CourseTraineesRepository courseTraineesRepository;
    private final CustomersRepository customersRepository;

    @Override
    @Transactional(readOnly = true)
    public TableResponseEntity<CoursesResponse.SearchResult> searchData(CoursesRequest.SearchForm dto) {
        return ResponseUtils.ok(coursesRepository.searchData(dto));
    }

    @Override
    @Transactional
    public ResponseEntity saveData(CoursesRequest.SubmitForm dto, Long id) throws BaseAppException {
        CoursesEntity entity;
        if (dto.getCourseId() != null && dto.getCourseId() > 0L) {
            entity = coursesRepositoryJPA.getById(dto.getCourseId());
            entity.setModifiedTime(new Date());
            entity.setModifiedBy(Utils.getUserNameLogin());
        } else {
            entity = new CoursesEntity();
            entity.setCreatedTime(new Date());
            entity.setCreatedBy(Utils.getUserNameLogin());
        }
        Utils.copyProperties(dto, entity);
        entity.setIsDeleted(BaseConstants.STATUS.NOT_DELETED);
        coursesRepositoryJPA.save(entity);
        courseLessonsRepository.deleteLesson(dto.getLessons().stream().map(it -> it.getCourseLessonId()).toList(), entity.getCourseId());
        dto.getLessons().forEach(it -> {
            CourseLessonsEntity courseLessonsEntity = new CourseLessonsEntity();
            courseLessonsEntity.setCourseId(entity.getCourseId());
            courseLessonsEntity.setName(it.getName());
            courseLessonsEntity.setCourseLessonId(it.getCourseLessonId());
            courseLessonsRepositoryJPA.save(courseLessonsEntity);
        });
        List<Long> oldIds = new ArrayList<>();
        dto.getListCoursesTrainees().forEach(item -> {
            if (item.getCourseTraineeId() != null) {
                oldIds.add(item.getCourseTraineeId());
            }
        });
//        courseTraineesRepository.deleteTrainee(dto.getListCoursesTrainees().stream().map(it -> it.getCourseTraineeId()).toList(), entity.getCourseId());
        courseTraineesRepositoryJPA.deleteByCourseIdAndCourseTraineeIdNotIn(entity.getCourseId(), oldIds);
        List<CourseTraineesEntity> listCourseTraineesEntities = courseTraineesRepositoryJPA.findByCourseId(entity.getCourseId());
        Map<Long, CourseTraineesEntity> mapCourseTraineesEntity = new HashMap<>();
        listCourseTraineesEntities.forEach(it -> {
            mapCourseTraineesEntity.put(it.getTraineeId(), it);
        });
        dto.getListCoursesTrainees().forEach(it -> {
            if (it.getCourseTraineeId() == null) {
                CourseTraineesEntity courseTraineesEntity = mapCourseTraineesEntity.get(it.getTraineeId());
                if (courseTraineesEntity == null) {
                    courseTraineesEntity = new CourseTraineesEntity();
                    courseTraineesEntity.setCreatedTime(new Date());
                    courseTraineesEntity.setCreatedBy(Utils.getUserNameLogin());
                } else {
                    courseTraineesEntity.setModifiedTime(new Date());
                    courseTraineesEntity.setModifiedBy(Utils.getUserNameLogin());
                }
                courseTraineesEntity.setCourseId(entity.getCourseId());
                courseTraineesEntity.setTraineeId(it.getTraineeId());
                courseTraineesEntity.setInstructorId(it.getInstructorId());
                courseTraineesEntity.setCourseTraineeId(it.getCourseTraineeId());
                courseTraineesEntity.setIsDeleted(BaseConstants.STATUS.NOT_DELETED);
                courseTraineesRepositoryJPA.save(courseTraineesEntity);
                mapCourseTraineesEntity.put(courseTraineesEntity.getTraineeId(), courseTraineesEntity);
            }
        });
        objectAttributesService.saveObjectAttributes(entity.getCourseId(), dto.getListAttributes(), CoursesEntity.class, null);
        return ResponseUtils.ok(entity.getCourseId());
    }

    @Override
    @Transactional
    public ResponseEntity deleteData(Long id) throws RecordNotExistsException {
        Optional<CoursesEntity> optional = coursesRepositoryJPA.findById(id);
        if (!optional.isPresent() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new RecordNotExistsException(id, CoursesEntity.class);
        }
        coursesRepository.deActiveObject(CoursesEntity.class, id);
        return ResponseUtils.ok(id);
    }

    @Override
    @Transactional(readOnly = true)
    public BaseResponseEntity<CoursesResponse.DetailBean> getDataById(Long id) throws RecordNotExistsException {
        Optional<CoursesEntity> optional = coursesRepositoryJPA.findById(id);
        if (!optional.isPresent() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new RecordNotExistsException(id, CoursesEntity.class);
        }
        List<CourseLessonsEntity> courseLessonsEntityList = courseLessonsRepository.findAllByProperties(CourseLessonsEntity.class, "courseId", id, "isDeleted", "N");
        List<CourseTraineesResponse> courseTraineesEntityList = courseLessonsRepository.getListCourseTrainees(id);
        CoursesResponse.DetailBean dto = new CoursesResponse.DetailBean();
        Utils.copyProperties(optional.get(), dto);
        List<CourseLessonsRequest.SubmitForm> listCourseLesson = new ArrayList<>();
        courseLessonsEntityList.forEach(it -> {
            CourseLessonsRequest.SubmitForm courseLesson = new CourseLessonsRequest.SubmitForm();
            courseLesson.setName(it.getName());
            courseLesson.setCourseLessonId(it.getCourseLessonId());
            courseLesson.setCourseId(it.getCourseId());
            listCourseLesson.add(courseLesson);
        });
        List<CourseTraineesRequest.SubmitForm> listCourseTrainees = new ArrayList<>();
        courseTraineesEntityList.forEach(it -> {
            CourseTraineesRequest.SubmitForm courseTrainee =
                    Utils.copyProperties(it, new CourseTraineesRequest.SubmitForm());
            listCourseTrainees.add(courseTrainee);
        });
        dto.setLessons(listCourseLesson);
        dto.setListCoursesTrainees(listCourseTrainees);
        dto.setListAttributes(objectAttributesService.getAttributes(id, coursesRepository.getSQLTableName(CoursesEntity.class)));
        return ResponseUtils.ok(dto);
    }

    @Override
    public ResponseEntity<Object> exportData(CoursesRequest.SearchForm dto) throws Exception {
        String pathTemplate = "template/export/BM_Xuat_DS_tuyen_dung.xlsx";
        ExportExcel dynamicExport = new ExportExcel(pathTemplate, 2, true);
        List<Map<String, Object>> listDataExport = coursesRepository.getListExport(dto);

        dynamicExport.replaceKeys(listDataExport);
        return ResponseUtils.ok(dynamicExport, "BM_Xuat_DS_tuyen_dung.xlsx");
    }

    @Override
    public ListResponseEntity<CoursesResponse.UserDataSelected> getListUserData() {
        return ResponseUtils.ok(coursesRepository.getListUserData());
    }

    @Override
    public ListResponseEntity<CoursesResponse.DataSelected> getListData(CoursesRequest.SearchForm dto) {
        return ResponseUtils.ok(coursesRepository.getListData(dto));
    }

    @Override
    public ResponseEntity saveLessonResult(CoursesRequest.SubmitLessonResult dto) throws BaseAppException {
        dto.getListLessonResult().forEach(it -> {
            courseLessonResultsService.saveData(it, it.getCourseLessonResultId());
        });
        return ResponseUtils.ok();
    }

    @Override
    public ListResponseEntity<CourseLessonResultsRequest.SubmitForm> getListLessonResult(List<Long> listCourseLessonId, Long traineeId) {
        List<CourseLessonResultsRequest.SubmitForm> lessonResultList = courseLessonResultsRepository.getListLessonResult(listCourseLessonId, traineeId);
        return ResponseUtils.ok(lessonResultList);
    }

    @Override
    public String getTemplateTrainee() throws Exception {
        String importTemplateName = "BM_Import_danh-sach-hoc-vien.xlsx";
        String pathTemplate = "template/import/" + importTemplateName;
        ExportExcel dynamicExport = new ExportExcel(pathTemplate, 2, true);
        String fileName = Utils.getFilePathExport(importTemplateName);
//        List<CustomersResponse.DataSelected> listCustomer = customersRepository.getListData();
//        List<CoursesResponse.UserDataSelected> listUser = coursesRepository.getListUserData();
//        dynamicExport.setActiveSheet(1);
//        int row = 0;
//        for (CustomersResponse.DataSelected customer : listCustomer) {
//            dynamicExport.setText(customer.getFullName(), 1, row++);
//            dynamicExport.increaseRow();
//        }
//        row = 0;
//        dynamicExport.setActiveSheet(2);
//        for (CoursesResponse.UserDataSelected user : listUser) {
//            dynamicExport.setText(user.getFullName(), 1, row++);
//            dynamicExport.increaseRow();
//        }
        dynamicExport.setActiveSheet(0);
        dynamicExport.exportFile(fileName);
        return fileName;
    }

    @Override
    public String getTemplate(CoursesRequest.ImportRequest dto) throws Exception {
        String importTemplateName = "BM_Import_cham-diem.xlsx";
        String pathTemplate = "template/import/" + importTemplateName;
        ExportExcel dynamicExport = new ExportExcel(pathTemplate, 2, true);
        String fileName = Utils.getFilePathExport(importTemplateName);
        List<CustomersResponse.DataSelected> listTrainee = courseTraineesRepository.getListData(dto.getCourseId());
        List<CoursesResponse.StatusData> listStatus = coursesRepository.getListCategories(Constant.CATEGORY_TYPES.CRM_TRANG_THAI_TGIA_HOC);
        dynamicExport.setActiveSheet(1);
        int row = 0;
        for (CustomersResponse.DataSelected trainee : listTrainee) {
            dynamicExport.setText(trainee.getFullName(), 1, row++);
            dynamicExport.increaseRow();
        }
        dynamicExport.setActiveSheet(2);
        row = 0;
        for (CoursesResponse.StatusData status : listStatus) {
            dynamicExport.setText(status.getName(), 1, row++);
            dynamicExport.increaseRow();
        }
        dynamicExport.setActiveSheet(0);
        dynamicExport.exportFile(fileName);
        return fileName;
    }

    @Override
    public List<CourseTraineesRequest.SubmitForm> importDataTrainee(MultipartFile fileImport) throws Exception {
        String fileConfigName = "BM_Import_danh-sach-hoc-vien.xml";
        ImportExcel importExcel = new ImportExcel("template/import/" + fileConfigName);
        List<Object[]> dataList = new ArrayList<>();

        List<CourseTraineesRequest.SubmitForm> listCourseTrainee = new ArrayList<>();


        if (importExcel.validateCommon(fileImport.getInputStream(), dataList)) {
            List<String> mobileList = new ArrayList<>();
            dataList.forEach(item -> {
                if (!mobileList.contains((String) item[2])) {
                    mobileList.add((String) item[2]);
                }
                if (!mobileList.contains((String) item[4])) {
                    mobileList.add((String) item[4]);
                }
            });

            Map<String, Long> mapCustomers = new HashMap<>();
            customersRepository.getListData(mobileList).stream().forEach(item -> {
                mapCustomers.put(item.getFullName().toLowerCase(), item.getCustomerId());
            });
            int index = 0;
            for (Object[] obj : dataList) {
                int col = 1;
                String customer = obj[1] + " - " + obj[2];
                Long traineeId = mapCustomers.get(customer.toLowerCase());
                if (traineeId == null) {
                    importExcel.addError(index, col - 1, I18n.getMessage("error.customers.import.invalid", customer), customer);
                }
                String userName = obj[3] + " - " + obj[4];
                Long instructorId = mapCustomers.get(userName.toLowerCase());
                if (instructorId == null) {
                    importExcel.addError(index, col - 1, I18n.getMessage("error.customers.import.invalid", userName), userName);
                }
                CourseTraineesRequest.SubmitForm data = new CourseTraineesRequest.SubmitForm();
                data.setInstructorId(instructorId);
                data.setTraineeId(traineeId);
                data.setTraineeName(obj[2] + " - " + obj[1]);
                data.setInstructorName(obj[4] + " - " + obj[3]);
                listCourseTrainee.add(data);
                index++;
            }
            if (importExcel.hasError()) {
                throw new ErrorImportException(fileImport, importExcel);
            } else {
                return listCourseTrainee;
            }
        } else {
            throw new ErrorImportException(fileImport, importExcel);
        }
    }

    @Override
    public boolean importData(CoursesRequest.ImportRequest dto) throws Exception {
        String fileConfigName = "BM_Import_cham-diem.xml";
        ImportExcel importExcel = new ImportExcel("template/import/" + fileConfigName);
        List<Object[]> dataList = new ArrayList<>();
        List<CourseLessonResultsEntity> entityList = new ArrayList<>();
        Map<String, Long> listTrainee = courseTraineesRepository.getListData(dto.getCourseId()).stream()
                .collect(Collectors.toMap(CustomersResponse.DataSelected::getFullName, CustomersResponse.DataSelected::getCustomerId));
        Map<String, String> listStatus = coursesRepository.getListCategories(Constant.CATEGORY_TYPES.CRM_TRANG_THAI_TGIA_HOC).stream()
                .collect(Collectors.toMap(CoursesResponse.StatusData::getName, CoursesResponse.StatusData::getValue));
        if (importExcel.validateCommon(dto.getFile().getInputStream(), dataList)) {
            int index = 0;
            for (Object[] obj : dataList) {
                int col = 1;
                String traineeName = (String) obj[col++];
                Long traineeId = listTrainee.get(traineeName);
                if (traineeId == null) {
                    importExcel.addError(index, col - 1, I18n.getMessage("error.import.category.invalid"), traineeName);
                }
                String status = (String) obj[col++];
                String statusId = listStatus.get(status);
                if (statusId == null && status != null) {
                    importExcel.addError(index, col - 1, I18n.getMessage("error.import.category.invalid"), status);
                }
                Double point = 0.0;
                if (obj[col++] != null) {
                    point = (Double) obj[col - 1];
                }
                if (point > 100) {
                    importExcel.addError(index, col - 1, I18n.getMessage("import.error.point"), point.toString());
                }
                String note = (String) obj[col];
                CourseLessonResultsEntity entity = new CourseLessonResultsEntity();
                List<CourseLessonResultsEntity> findEntity = courseLessonResultsRepository.findByProperties(CourseLessonResultsEntity.class, "courseLessonId", dto.getCourseLessonId(), "traineeId", traineeId);
                if (findEntity.size() > 0) {
                    entity = findEntity.get(0);
                }
                entity.setTraineeId(traineeId);
                entity.setCourseLessonId(dto.getCourseLessonId());
                entity.setNote(note);
                entity.setPoint(point);
                entity.setStatusId(statusId);
                entityList.add(entity);
                index++;
            }
            if (importExcel.hasError()) {
                throw new ErrorImportException(dto.getFile(), importExcel);
            } else {
                entityList.forEach(entity -> {
                    courseLessonResultsRepositoryJPA.save(entity);
                });
            }
        } else {
            throw new ErrorImportException(dto.getFile(), importExcel);
        }
        return true;
    }

}
