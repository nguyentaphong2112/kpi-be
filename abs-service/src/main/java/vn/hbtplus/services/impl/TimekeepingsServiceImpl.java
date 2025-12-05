/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.services.impl;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.constants.Constant;
import vn.hbtplus.exceptions.BaseAppException;
import vn.hbtplus.exceptions.RecordNotExistsException;
import vn.hbtplus.models.BaseDataTableDto;
import vn.hbtplus.models.dto.AbsRequestDTO;
import vn.hbtplus.models.dto.BaseCategoryDto;
import vn.hbtplus.models.dto.EmployeeDto;
import vn.hbtplus.models.dto.WorkCalendarDetailsDTO;
import vn.hbtplus.models.request.TimekeepingsRequest;
import vn.hbtplus.models.response.BaseResponseEntity;
import vn.hbtplus.models.response.TableResponseEntity;
import vn.hbtplus.models.response.TimekeepingsResponse;
import vn.hbtplus.repositories.entity.TimekeepingsEntity;
import vn.hbtplus.repositories.entity.WorkdayTypesEntity;
import vn.hbtplus.repositories.impl.TimekeepingsRepository;
import vn.hbtplus.repositories.impl.WorkCalendarDetailsRepository;
import vn.hbtplus.repositories.impl.WorkdayTypesRepository;
import vn.hbtplus.repositories.jpa.TimekeepingsRepositoryJPA;
import vn.hbtplus.repositories.jpa.WorkdayTypesRepositoryJPA;
import vn.hbtplus.services.TimekeepingsService;
import vn.hbtplus.services.UtilsService;
import vn.hbtplus.utils.ExportExcel;
import vn.hbtplus.utils.ResponseUtils;
import vn.hbtplus.utils.Utils;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Lop impl service ung voi bang abs_timekeepings
 *
 * @author tudd
 * @version 1.0
 * @since 1.0
 */

@Service
@RequiredArgsConstructor
public class TimekeepingsServiceImpl implements TimekeepingsService {

    private final TimekeepingsRepository timekeepingsRepository;
    private final TimekeepingsRepositoryJPA timekeepingsRepositoryJPA;
    private final WorkdayTypesRepositoryJPA workdayTypesRepositoryJPA;
    private final WorkCalendarDetailsRepository workCalendarDetailsRepository;
    private final WorkdayTypesRepository workdayTypesRepository;
    private final UtilsService utilsService;

    @Override
    @Transactional(readOnly = true)
    public TableResponseEntity<TimekeepingsResponse.SearchResult> searchData(String type, TimekeepingsRequest.SearchForm dto) {
        if (Utils.daysBetween(dto.getStartDate(), dto.getEndDate()) > 31) {
            throw new BaseAppException(String.format("Từ ngày phải cách đến ngày nhỏ hơn 31 ngày"));
        }
        BaseDataTableDto resultSelect = timekeepingsRepository.searchEmployee(dto);
        if (!resultSelect.isEmpty()) {
            //set du lieu timkeeping
            List<Long> empIds = new ArrayList<>();
            resultSelect.getListData().forEach(emp -> {
                empIds.add(((TimekeepingsResponse.SearchResult) emp).getEmployeeId());
            });
            List<TimekeepingsResponse.TimekeepingBean> listTimekeepings = timekeepingsRepository.getListTimekeeping(empIds, dto.getStartDate(), dto.getEndDate(), List.of(type));
            Map<Long, Map<String, String>> mapTimekeepings = new HashMap<>();
            listTimekeepings.forEach(bean -> {
                if (!mapTimekeepings.containsKey(bean.getEmployeeId())) {
                    mapTimekeepings.put(bean.getEmployeeId(), new HashMap<>());
                }
                mapTimekeepings.get(bean.getEmployeeId()).put(Utils.formatDate(bean.getDateTimekeeping(), "dd"), bean.getDisplayWorkdayType(type));
            });
            TimekeepingsResponse.SearchResult searchResult = (TimekeepingsResponse.SearchResult) resultSelect.getListData().get(0);
            Date pDate = dto.getStartDate();
            while (!pDate.after(dto.getEndDate())) {
                searchResult.getDateList().add(new TimekeepingsResponse.CalendarDate(pDate));
                pDate = DateUtils.addDays(pDate, 1);
            }

            resultSelect.getListData().forEach(emp -> {
                TimekeepingsResponse.SearchResult bean = (TimekeepingsResponse.SearchResult) emp;
                bean.setTimekeepings(mapTimekeepings.get(bean.getEmployeeId()));
            });
        }

        return ResponseUtils.ok(resultSelect);
    }

    @Override
    @Transactional
    public ResponseEntity saveData(String type, TimekeepingsRequest.SubmitForm dto) throws BaseAppException {
        Map<String, Long> workdayTypes = workdayTypesRepository.findAll(WorkdayTypesEntity.class)
                .stream()
                .collect(Collectors.toMap(
                        WorkdayTypesEntity::getCode,   // Key mapper (String)
                        WorkdayTypesEntity::getWorkdayTypeId      // Value mapper (Long)
                ));
        Long organizationId = timekeepingsRepository.getOrganizationOfDate(dto.getEmployeeId(), dto.getDateTimekeeping());
        //neu nhan vien cong tac chinh tai don vi duoc phan quyen thi set organization_id = null
        //neu nhan vien khong cong tac chinh tai don vi duoc phan quyen thi set organization_id = don vi cong tac phu
        List<TimekeepingsEntity> saveTimekeepings = new ArrayList<>();
        if (!Utils.isNullOrEmpty(dto.getWorkdayType())) {
            //neu nhan vien khong thuoc don vi thi khong duoc cap nhat cong
            if (organizationId != null && organizationId.equals(-1L)) {
                throw new BaseAppException("Nhân viên không thuộc đơn vị được phân quyền tại ngày chấm công!");
            }

            String[] str = dto.getWorkdayType().split(",");
            for (int i = 0; i < str.length; i++) {
                String workdayType = str[i];
                if (workdayType.contains("/")) {
                    String[] cong = workdayType.split("/");
                    TimekeepingsEntity timekeepingsEntity = new TimekeepingsEntity(dto.getEmployeeId(), dto.getDateTimekeeping(), organizationId);
                    timekeepingsEntity.setWorkdayTypeId(workdayTypes.get(cong[0]));
                    if (timekeepingsEntity.getWorkdayTypeId() == null) {
                        throw new BaseAppException("Ký hiệu chấm công không hợp lệ: " + cong[0]);
                    }
                    if (cong[1].equalsIgnoreCase("2")) {
                        timekeepingsEntity.setTotalHours(4d);
                        saveTimekeepings.add(timekeepingsEntity);
                    } else {
                        timekeepingsEntity.setTotalHours(8d);
                        saveTimekeepings.add(timekeepingsEntity);
                        timekeepingsEntity = new TimekeepingsEntity(dto.getEmployeeId(), dto.getDateTimekeeping(), organizationId);
                        timekeepingsEntity.setWorkdayTypeId(workdayTypes.get(cong[1]));
                        if (timekeepingsEntity.getWorkdayTypeId() == null) {
                            throw new BaseAppException("Ký hiệu chấm công không hợp lệ: " + cong[1]);
                        }
                        timekeepingsEntity.setTotalHours(8d);
                        saveTimekeepings.add(timekeepingsEntity);
                    }
                } else {
                    TimekeepingsEntity timekeepingsEntity = new TimekeepingsEntity(dto.getEmployeeId(), dto.getDateTimekeeping(), organizationId);
                    timekeepingsEntity.setWorkdayTypeId(workdayTypes.get(workdayType));
                    if (timekeepingsEntity.getWorkdayTypeId() == null) {
                        throw new BaseAppException("Ký hiệu chấm công không hợp lệ: " + workdayType);
                    }
                    timekeepingsEntity.setTotalHours(8d);
                    saveTimekeepings.add(timekeepingsEntity);
                }
            }
        }
        //inactive du lieu cham cong cu
        timekeepingsRepository.deleteOldData(dto.getEmployeeId(), dto.getDateTimekeeping());
        for (TimekeepingsEntity timekeepingsEntity : saveTimekeepings) {
            timekeepingsRepositoryJPA.save(timekeepingsEntity);
        }
        List<TimekeepingsResponse.TimekeepingBean> lst = timekeepingsRepository.getListTimekeeping(List.of(dto.getEmployeeId()), dto.getDateTimekeeping(), dto.getDateTimekeeping(), List.of(type));
        if (lst.isEmpty()) {
            return ResponseUtils.ok("");
        } else {
            return ResponseUtils.ok(lst.get(0).getDisplayWorkdayType(type));
        }
    }

    @Override
    @Transactional
    public ResponseEntity deleteData(Long id) throws RecordNotExistsException {
        Optional<TimekeepingsEntity> optional = timekeepingsRepositoryJPA.findById(id);
        if (!optional.isPresent() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new RecordNotExistsException(id, TimekeepingsEntity.class);
        }
        timekeepingsRepository.deActiveObject(TimekeepingsEntity.class, id);
        return ResponseUtils.ok(id);
    }

    @Override
    @Transactional(readOnly = true)
    public BaseResponseEntity<TimekeepingsResponse> getDataById(Long id) throws RecordNotExistsException {
        Optional<TimekeepingsEntity> optional = timekeepingsRepositoryJPA.findById(id);
        if (!optional.isPresent() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new RecordNotExistsException(id, TimekeepingsEntity.class);
        }
        TimekeepingsResponse dto = new TimekeepingsResponse();
        Utils.copyProperties(optional.get(), dto);
        return ResponseUtils.ok(dto);
    }

    @Override
    public ResponseEntity<Object> exportData(String type, TimekeepingsRequest.SearchForm dto) throws Exception {

        String pathTemplate = "template/export/BM_Bang_cham_cong_thang.xlsx";
        ExportExcel dynamicExport = utilsService.initExportExcel(
                WorkdayTypesEntity.TYPE.LAM_THEM.equalsIgnoreCase(type) ? "BANG_CONG_LAM_THEM" : "BANG_CONG_THANG", pathTemplate, 2
        );

        Map<String, Object> reportParams = new HashMap<>();
        reportParams.put("ky_cham_cong", String.format("Tháng %s năm %s", Utils.formatDate(dto.getEndDate(), "MM"), Utils.formatDate(dto.getEndDate(), "yyyy")));
        reportParams.put("ngay_bao_cao", Utils.formatDate(new Date(), "dd"));
        reportParams.put("thang_bao_cao", Utils.formatDate(new Date(), "MM"));
        reportParams.put("nam_bao_cao", Utils.formatDate(new Date(), "yyyy"));
        dynamicExport.hideColumn(dynamicExport.findPosition("${ngay_31}").getCol());

        List<EmployeeDto> listEmp = timekeepingsRepository.getListEmployee(dto);
        List<Long> empIds = new ArrayList<>();
        listEmp.forEach(emp -> {
            empIds.add(emp.getEmployeeId());
        });
        List<TimekeepingsResponse.TimekeepingBean> listTimekeepings = timekeepingsRepository.getListTimekeeping(empIds, dto.getStartDate(), dto.getEndDate(), List.of(type));
        //group thong tin ky hieu cong
        List<TimekeepingsResponse.GroupTimekeepingBean> listGroupTimekeepings = timekeepingsRepository.getListGroupTimekeeping(empIds, dto.getStartDate(), dto.getEndDate(), type);


        Map<Long, Map<String, String>> mapTimekeepings = new HashMap<>();
        Map<Long, Map<String, Double>> mapGroupTimekeepings = new HashMap<>();
        listTimekeepings.forEach(bean -> {
            if (!mapTimekeepings.containsKey(bean.getEmployeeId())) {
                mapTimekeepings.put(bean.getEmployeeId(), new HashMap<>());
            }
            mapTimekeepings.get(bean.getEmployeeId()).put(Utils.formatDate(bean.getDateTimekeeping(), "dd"), bean.getDisplayWorkdayType(type));
        });
        listGroupTimekeepings.forEach(bean -> {
            if (!mapGroupTimekeepings.containsKey(bean.getEmployeeId())) {
                mapGroupTimekeepings.put(bean.getEmployeeId(), new HashMap<>());
            }
            mapGroupTimekeepings.get(bean.getEmployeeId())
                    .put(convertToSqlColumn(bean.getGroupName().toLowerCase()), bean.getTotalHours() / (WorkdayTypesEntity.TYPE.LAM_THEM.equalsIgnoreCase(type) ? 1 : 8));
        });
        List<BaseCategoryDto> categoryDtos = timekeepingsRepository.getListCategory(Constant.CATEGORY_CODES.PHAN_LOAI_CONG, BaseCategoryDto.class);
        List<Map<String, Object>> listDataExport = new ArrayList<>();
        listEmp.forEach(emp -> {
            Map timekeepings = mapTimekeepings.get(emp.getEmployeeId());
            Map groupTimekeepings = mapGroupTimekeepings.get(emp.getEmployeeId());
            Map map = new HashMap();
            map.put("ma_nhan_vien", emp.getEmployeeCode());
            map.put("ho_va_ten", emp.getFullName());
            map.put("ngay_sinh", emp.getDateOfBirth());
            for (int i = 1; i < 32; i++) {
                map.put(String.format("ngay_%02d", i), timekeepings == null ? null : timekeepings.get(String.format("%02d", i)));
            }
            categoryDtos.forEach(item -> {
                String key = convertToSqlColumn(item.getName().toLowerCase());
                map.put(key, groupTimekeepings == null ? null : groupTimekeepings.get(key));
            });
            listDataExport.add(map);
        });

        dynamicExport.replaceKeys(reportParams);
        dynamicExport.replaceKeys(listDataExport);
        return ResponseUtils.ok(dynamicExport, "BM_Bang_cham_cong_thang.xlsx");
    }

    private String convertToSqlColumn(String name) {
        return Utils.removeSign(name).replaceAll("[^a-zA-Z0-9]", "_");
    }

    @Override
    @Transactional
    public void autoSetTimekeeping(Date timekeepingDate, List<Long> empCodes) {
        Map<Long, String> mapWorkdayTypes = timekeepingsRepository.findAll(WorkdayTypesEntity.class).stream()
                .collect(Collectors.toMap(WorkdayTypesEntity::getWorkdayTypeId, WorkdayTypesEntity::getCode));
        //Lay thong tin lich lam viec
        WorkCalendarDetailsDTO calendarDetailsDTO = workCalendarDetailsRepository.getWorkCalendar(timekeepingDate);
        //Lay danh sach nhan vien
        List<Long> employeeIds = timekeepingsRepository.getListEmployeeId(timekeepingDate, empCodes);
        //Lay thong tin dang ky nghi
        List<AbsRequestDTO> listRequest = timekeepingsRepository.getListRequestLeave(timekeepingDate, empCodes);
        List<TimekeepingsEntity> listTimekeepings = new ArrayList<>();
        List<TimekeepingsEntity> saveTimekeepings = new ArrayList<>();
        Map<Long, List<TimekeepingsEntity>> mapNewTimekeepings = new HashMap<>();
        //insert du lieu cham cong
        if (calendarDetailsDTO != null
            && calendarDetailsDTO.getWorkdayTypeId() != null) {
            Map<Long, Double> mapCongNghi = new HashMap<>();
            //insert cong nghi
            listRequest.forEach(requestDTO -> {
                Double totalHours = calendarDetailsDTO.getTotalHours()
                                    - (mapCongNghi.get(requestDTO.getEmployeeId()) == null ? 0d : mapCongNghi.get(requestDTO.getEmployeeId()));
                if (totalHours > 0) {
                    TimekeepingsEntity timekeepingsEntity = new TimekeepingsEntity(
                            requestDTO.getEmployeeId(),
                            timekeepingDate,
                            null
                    );
                    timekeepingsEntity.setWorkdayTypeId(requestDTO.getWorkdayTypeId());
                    if (requestDTO.getTotalHours() != null
                        && requestDTO.getTotalHours() >= 8
                        && totalHours >= 8) {
                        timekeepingsEntity.setTotalHours(8d);
                    } else {
                        timekeepingsEntity.setTotalHours(4d);
                    }
                    mapCongNghi.put(requestDTO.getEmployeeId(),
                            Utils.NVL(mapCongNghi.get(requestDTO.getEmployeeId())) + timekeepingsEntity.getTotalHours()
                    );
                    listTimekeepings.add(timekeepingsEntity);
                }
            });
            //insert cong mac dinh
            for (Long employeeId : employeeIds) {
                Double totalHours = calendarDetailsDTO.getTotalHours() - Utils.NVL(mapCongNghi.get(employeeId));
                if (totalHours > 0) {
                    TimekeepingsEntity timekeepingsEntity = new TimekeepingsEntity(
                            employeeId,
                            timekeepingDate,
                            null
                    );
                    timekeepingsEntity.setWorkdayTypeId(calendarDetailsDTO.getWorkdayTypeId());
                    timekeepingsEntity.setTotalHours(totalHours);
                    listTimekeepings.add(timekeepingsEntity);
                }
            }
            listTimekeepings.forEach(item -> {
                if (mapNewTimekeepings.get(item.getEmployeeId()) == null) {
                    mapNewTimekeepings.put(item.getEmployeeId(), new ArrayList<>());
                }
                item.setWorkdayTypeCode(mapWorkdayTypes.get(item.getWorkdayTypeId()));
                mapNewTimekeepings.get(item.getEmployeeId()).add(item);
            });
        } else {
            //thuc hien xoa du lieu do chua setup chấm công
            timekeepingsRepository.inactiveTimekeeping(timekeepingDate, WorkdayTypesEntity.TYPE.THUONG, "Không thiết lập lịch làm việc");
        }
        //Lay du lieu cham cong cu cua nhan vien
        List<TimekeepingsEntity> oldTimekeepings = timekeepingsRepository.getListTimekeeping(empCodes, timekeepingDate, WorkdayTypesEntity.TYPE.THUONG);
        Map<Long, List<TimekeepingsEntity>> mapOldTimekeepings = new HashMap<>();
        oldTimekeepings.forEach(item -> {
            if (mapOldTimekeepings.get(item.getEmployeeId()) == null) {
                mapOldTimekeepings.put(item.getEmployeeId(), new ArrayList<>());
            }
            mapOldTimekeepings.get(item.getEmployeeId()).add(item);
        });

        //thuc hien luu du lieu neu cong thay doi
        List<Long> empIdChanges = new ArrayList<>();
        for (Long employeeId : employeeIds) {
            if (isChange(mapOldTimekeepings.get(employeeId), mapNewTimekeepings.get(employeeId))) {
                empIdChanges.add(employeeId);
                if (mapNewTimekeepings.get(employeeId) != null) {
                    saveTimekeepings.addAll(mapNewTimekeepings.get(employeeId));
                }
            }
        }
        //Thuc hien xoa du lieu cham cong bi thay doi
        timekeepingsRepository.inactiveTimekeeping(timekeepingDate, WorkdayTypesEntity.TYPE.THUONG, "Thay đổi theo tổng hợp công");
        //thuc hien them moi du lieu cham cong
        timekeepingsRepository.insertBatch(TimekeepingsEntity.class, saveTimekeepings, Utils.getUserNameLogin());
        //thuc hien luu lich su
        timekeepingsRepository.insertLogTimekeeping(empIdChanges, mapOldTimekeepings, mapNewTimekeepings, timekeepingDate, WorkdayTypesEntity.TYPE.THUONG);
    }

    @Override
    @Transactional
    public void autoSetOverTimekeeping(Date timekeepingDate, List<Long> empIds) {
        Map<Long, String> mapWorkdayTypes = workdayTypesRepositoryJPA.findAll().stream()
                .collect(Collectors.toMap(WorkdayTypesEntity::getWorkdayTypeId, WorkdayTypesEntity::getCode));
        List<TimekeepingsEntity> oldTimekeepings = timekeepingsRepository.getListTimekeeping(empIds, timekeepingDate, WorkdayTypesEntity.TYPE.LAM_THEM);
        List<TimekeepingsEntity> overtimes = timekeepingsRepository.getListOvertime(empIds, timekeepingDate);
        List<TimekeepingsEntity> saveTimekeepings = new ArrayList<>();
        Set<Long> employeeIds = new HashSet<>();
        Map<Long, List<TimekeepingsEntity>> mapNewTimekeepings = new HashMap<>();
        overtimes.forEach(item -> {
            item.setWorkdayTypeCode(mapWorkdayTypes.get(item.getWorkdayTypeId()));
            if (mapNewTimekeepings.get(item.getEmployeeId()) == null) {
                mapNewTimekeepings.put(item.getEmployeeId(), new ArrayList<>());
            }
            mapNewTimekeepings.get(item.getEmployeeId()).add(item);

        });
        Map<Long, List<TimekeepingsEntity>> mapOldTimekeepings = new HashMap<>();
        oldTimekeepings.forEach(item -> {
            if (mapOldTimekeepings.get(item.getEmployeeId()) == null) {
                mapOldTimekeepings.put(item.getEmployeeId(), new ArrayList<>());
            }
            mapOldTimekeepings.get(item.getEmployeeId()).add(item);
        });
        employeeIds.addAll(mapOldTimekeepings.keySet());
        employeeIds.addAll(mapNewTimekeepings.keySet());

        //thuc hien luu du lieu neu cong thay doi
        List<Long> empIdChanges = new ArrayList<>();
        for (Long employeeId : employeeIds) {
            if (isChange(mapOldTimekeepings.get(employeeId), mapNewTimekeepings.get(employeeId))) {
                empIdChanges.add(employeeId);
                if (mapNewTimekeepings.get(employeeId) != null) {
                    saveTimekeepings.addAll(mapNewTimekeepings.get(employeeId));
                }
            }
        }
        //Thuc hien xoa du lieu cham cong bi thay doi
        timekeepingsRepository.inactiveTimekeeping(empIdChanges, timekeepingDate, WorkdayTypesEntity.TYPE.LAM_THEM, "Thay đổi theo tổng hợp công");
        //thuc hien them moi du lieu cham cong
        timekeepingsRepository.insertBatch(TimekeepingsEntity.class, saveTimekeepings, Utils.getUserNameLogin());
        //thuc hien luu lich su
        timekeepingsRepository.insertLogTimekeeping(empIdChanges, mapOldTimekeepings, mapNewTimekeepings, timekeepingDate, WorkdayTypesEntity.TYPE.LAM_THEM);

    }

    private boolean isChange(List<TimekeepingsEntity> oldData, List<TimekeepingsEntity> newData) {
        if (oldData == null || newData == null) {
            return true;
        }
        if (oldData.size() != newData.size()) {
            return true;
        }
        for (TimekeepingsEntity oldItem : oldData) {
            boolean exists = false;
            for (TimekeepingsEntity newItem : newData) {
                if (oldItem.getWorkdayTypeId().equals(newItem.getWorkdayTypeId())
                    && oldItem.getTotalHours().equals(newItem.getTotalHours())) {
                    exists = true;
                    break;
                }
            }
            if (!exists) {
                return true;
            }
        }
        return false;
    }


}
