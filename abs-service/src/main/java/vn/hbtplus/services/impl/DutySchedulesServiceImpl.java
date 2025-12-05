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
import vn.hbtplus.exceptions.BaseAppException;
import vn.hbtplus.exceptions.RecordNotExistsException;
import vn.hbtplus.models.request.DutySchedulesRequest;
import vn.hbtplus.models.response.BaseResponseEntity;
import vn.hbtplus.models.response.DutySchedulesResponse;
import vn.hbtplus.models.response.TableResponseEntity;
import vn.hbtplus.repositories.entity.DutySchedulesEntity;
import vn.hbtplus.repositories.impl.DutySchedulesRepository;
import vn.hbtplus.repositories.jpa.DutySchedulesRepositoryJPA;
import vn.hbtplus.services.DutySchedulesService;
import vn.hbtplus.services.UtilsService;
import vn.hbtplus.utils.ExportExcel;
import vn.hbtplus.utils.ResponseUtils;
import vn.hbtplus.utils.Utils;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Lop impl service ung voi bang abs_duty_schedules
 *
 * @author tudd
 * @version 1.0
 * @since 1.0
 */

@Service
@RequiredArgsConstructor
public class DutySchedulesServiceImpl implements DutySchedulesService {

    private final DutySchedulesRepository dutySchedulesRepository;
    private final DutySchedulesRepositoryJPA dutySchedulesRepositoryJPA;
    private final String[] DAY_OF_WEEKS = new String[]{
            "", "Chủ nhật", "Thứ 2", "Thứ 3", "Thứ 4", "Thứ 5", "Thứ 6", "Thứ 7"
    };
    private final UtilsService utilsService;

    @Override
    @Transactional(readOnly = true)
    public TableResponseEntity<DutySchedulesResponse.SearchResult> searchData(DutySchedulesRequest.SearchForm dto) {
        return ResponseUtils.ok(dutySchedulesRepository.searchData(dto));
    }

    @Override
    @Transactional(readOnly = true)
    public TableResponseEntity<DutySchedulesResponse.SearchResultMonth> searchDataMonth(DutySchedulesRequest.SearchForm dto) {
        return ResponseUtils.ok(dutySchedulesRepository.searchDataMonth(dto));
    }

    @Override
    @Transactional
    public ResponseEntity saveData(DutySchedulesRequest.SubmitForm dto) throws BaseAppException {
        Date endDate = DateUtils.addDays(dto.getDateValue(), 6);
        List<Long> listIdAll = dutySchedulesRepository.getListData(dto.getDateValue(), endDate, dto.getListOrganizationId());
        List<Long> listIdAdd = new ArrayList<>();
        if (!Utils.isNullOrEmpty(dto.getListDutySchedule())) {
            dto.getListDutySchedule().forEach(it -> {
                handleEmployee(it, it.getEmployeeId0(), 0, listIdAdd);
                handleEmployee(it, it.getEmployeeId1(), 1, listIdAdd);
                handleEmployee(it, it.getEmployeeId2(), 2, listIdAdd);
                handleEmployee(it, it.getEmployeeId3(), 3, listIdAdd);
                handleEmployee(it, it.getEmployeeId4(), 4, listIdAdd);
                handleEmployee(it, it.getEmployeeId5(), 5, listIdAdd);
                handleEmployee(it, it.getEmployeeId6(), 6, listIdAdd);
            });
        }
        List<Long> listIdDeleted = listIdAll.stream()
                .filter(id -> !listIdAdd.contains(id))
                .collect(Collectors.toList());
        dutySchedulesRepository.deActiveObjectByListId(DutySchedulesEntity.class, listIdDeleted);
        return ResponseUtils.ok();
    }


    private void handleEmployee(DutySchedulesRequest.DutyBean data, List<Long> listEmp, int index, List<Long> listIdAdd) {
        Date dateTimekeeping = DateUtils.addDays(data.getDateValue(), index);
        List<DutySchedulesEntity> listEntity = dutySchedulesRepository.findByProperties(
                DutySchedulesEntity.class, "dutyPositionId", data.getDutyPositionId(),
                "organizationId", data.getOrganizationId(), "dateTimekeeping", dateTimekeeping, "orderNumber", data.getOrderNumber());
        List<Long> listId = handleData(listEntity, listEmp, listIdAdd);
        listId.forEach(j -> {
            DutySchedulesEntity entity = new DutySchedulesEntity();
            entity.setDutyPositionId(data.getDutyPositionId());
            entity.setEmployeeId(j);
            entity.setDateTimekeeping(dateTimekeeping);
            entity.setOrganizationId(data.getOrganizationId());
            entity.setOrderNumber(data.getOrderNumber());
            entity.setCreatedTime(new Date());
            entity.setCreatedBy(Utils.getUserNameLogin());
            entity.setIsDeleted(BaseConstants.STATUS.NOT_DELETED);
            dutySchedulesRepositoryJPA.saveAndFlush(entity);
            listIdAdd.add(entity.getDutyScheduleId());
        });
    }


    private List<Long> handleData(List<DutySchedulesEntity> listEntity, List<Long> listEmp, List<Long> listIdAdd) {
        List<Long> listId = new ArrayList<>(listEmp);
        if (!Utils.isNullOrEmpty(listEntity)) {
            listEntity.forEach(entity -> {
                if (listId.contains(entity.getEmployeeId())) {
                    listIdAdd.add(entity.getDutyScheduleId());
                    listId.remove(entity.getEmployeeId());
                }
            });
        }
        return listId;
    }

    @Override
    public ResponseEntity saveDataMonth(DutySchedulesRequest.SubmitFormMonth dto) throws BaseAppException {
        Date endMonth = Utils.getLastDayOfMonth(dto.getMonthValue());
        Map<String, List<DutySchedulesEntity>> mapData = dutySchedulesRepository.getMapData(dto.getMonthValue(), endMonth, dto.getListOrganizationId());
        Date currentDate = new Date();
        String userName = Utils.getUserNameLogin();
        List<DutySchedulesEntity> listAddData = new ArrayList<>();
        List<DutySchedulesEntity> listUpdateData = new ArrayList<>();
        List<Long> listIdDeleted = new ArrayList<>();
        for (DutySchedulesRequest.DutyBeanMonth item : dto.getListData()) {
            String key = item.getOrganizationId() + "_" + item.getDutyPositionId() + "_" + Utils.formatDate(item.getDateTimekeeping());
            if (mapData.get(key) != null) {
                List<DutySchedulesEntity> listEntity = mapData.get(key);
                for (Long empId : item.getEmployeeIds()) {
                    boolean found = false;
                    for (DutySchedulesEntity entityCheck : listEntity) {
                        if (empId.equals(entityCheck.getEmployeeId())) {
                            entityCheck.setOrderNumber(entityCheck.getOrderNumber());
                            entityCheck.setModifiedBy(userName);
                            entityCheck.setModifiedTime(currentDate);
                            listUpdateData.add(entityCheck);
                            found = true;
                            break;
                        }
                    }

                    if (!found) {
                        DutySchedulesEntity entity = new DutySchedulesEntity();
                        entity.setDutyPositionId(item.getDutyPositionId());
                        entity.setEmployeeId(empId);
                        entity.setDateTimekeeping(item.getDateTimekeeping());
                        entity.setOrganizationId(item.getOrganizationId());
                        entity.setOrderNumber(item.getOrderNumber());
                        entity.setIsDeleted(BaseConstants.STATUS.NOT_DELETED);
                        listAddData.add(entity);
                    }
                }
            } else {
                if (!Utils.isNullOrEmpty(item.getEmployeeIds())) {
                    for (Long empId : item.getEmployeeIds()) {
                        DutySchedulesEntity entity = new DutySchedulesEntity();
                        entity.setDutyPositionId(item.getDutyPositionId());
                        entity.setEmployeeId(empId);
                        entity.setDateTimekeeping(item.getDateTimekeeping());
                        entity.setOrganizationId(item.getOrganizationId());
                        entity.setOrderNumber(item.getOrderNumber());
                        entity.setIsDeleted(BaseConstants.STATUS.NOT_DELETED);
                        listAddData.add(entity);
                    }
                }
            }
        }

        Set<Long> updatedIdSet = listUpdateData.stream()
                .map(DutySchedulesEntity::getDutyScheduleId)
                .collect(Collectors.toSet());

        for (List<DutySchedulesEntity> entityList : mapData.values()) {
            for (DutySchedulesEntity entity : entityList) {
                if (!updatedIdSet.contains(entity.getDutyScheduleId())) {
                    listIdDeleted.add(entity.getDutyScheduleId());
                }
            }
        }

        dutySchedulesRepository.deActiveObjectByListId(DutySchedulesEntity.class, listIdDeleted);
        dutySchedulesRepository.insertBatch(DutySchedulesEntity.class, listAddData, userName);
        dutySchedulesRepository.updateBatch(DutySchedulesEntity.class, listUpdateData, true);
        return ResponseUtils.ok();
    }

    public static Date truncateTime(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    @Override
    @Transactional
    public ResponseEntity deleteData(Long id) throws RecordNotExistsException {
        Optional<DutySchedulesEntity> optional = dutySchedulesRepositoryJPA.findById(id);
        if (!optional.isPresent() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new RecordNotExistsException(id, DutySchedulesEntity.class);
        }
        dutySchedulesRepository.deActiveObject(DutySchedulesEntity.class, id);
        return ResponseUtils.ok(id);
    }

    @Override
    @Transactional(readOnly = true)
    public BaseResponseEntity<DutySchedulesResponse.SearchResult> getDataById(Long id) throws RecordNotExistsException {
        Optional<DutySchedulesEntity> optional = dutySchedulesRepositoryJPA.findById(id);
        if (!optional.isPresent() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new RecordNotExistsException(id, DutySchedulesEntity.class);
        }
        DutySchedulesResponse.SearchResult dto = new DutySchedulesResponse.SearchResult();
        Utils.copyProperties(optional.get(), dto);
        return ResponseUtils.ok(dto);
    }

    @Override
    public List<DutySchedulesResponse.DetailBean> getListData(DutySchedulesRequest.SearchForm dto) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(dto.getDateValue());
        calendar.add(Calendar.DAY_OF_MONTH, 6);
        dto.setEndDateValue(calendar.getTime());
        List<DutySchedulesResponse.DetailBean2> data = dutySchedulesRepository.getListData(dto);
        List<DutySchedulesResponse.DetailBean> result = groupData(data, dto.getDateValue());
        return result;
    }

    @Override
    public List<DutySchedulesResponse.DetailBean> getListCopyData(DutySchedulesRequest.SearchForm dto) {
        dto.setEndDateValue(dto.getDateValue());
        List<DutySchedulesResponse.DetailBean2> data = dutySchedulesRepository.getListData(dto);
        List<DutySchedulesResponse.DetailBean> result = groupData(data, dto.getDateValue());
        return result;
    }

    public static List<DutySchedulesResponse.DetailBean> groupData(List<DutySchedulesResponse.DetailBean2> data, Date dateValue) {
        return data.stream()
                .collect(Collectors.groupingBy(item -> Arrays.asList(
                                item.getOrganizationId(),
                                item.getDutyPositionId(),
                                item.getOrderNumber()
                        ),
                        Collectors.collectingAndThen(Collectors.toList(), list -> {
                            DutySchedulesResponse.DetailBean groupedItem = new DutySchedulesResponse.DetailBean();
                            groupedItem.setOrganizationId(list.get(0).getOrganizationId());
                            groupedItem.setDutyPositionId(list.get(0).getDutyPositionId());
                            groupedItem.setOrderNumber(list.get(0).getOrderNumber());
                            groupedItem.setDateValue(dateValue);

                            List<DutySchedulesResponse.EmployeeBean> employeeId0 = new ArrayList<>();
                            List<DutySchedulesResponse.EmployeeBean> employeeId1 = new ArrayList<>();
                            List<DutySchedulesResponse.EmployeeBean> employeeId2 = new ArrayList<>();
                            List<DutySchedulesResponse.EmployeeBean> employeeId3 = new ArrayList<>();
                            List<DutySchedulesResponse.EmployeeBean> employeeId4 = new ArrayList<>();
                            List<DutySchedulesResponse.EmployeeBean> employeeId5 = new ArrayList<>();
                            List<DutySchedulesResponse.EmployeeBean> employeeId6 = new ArrayList<>();

                            for (DutySchedulesResponse.DetailBean2 detail : list) {
                                for (int j = 0; j < detail.getEmployeeIds().size(); j++) {
                                    DutySchedulesResponse.EmployeeBean employee = new DutySchedulesResponse.EmployeeBean();
                                    employee.setEmployeeId(detail.getEmployeeIds().get(j));
                                    employee.setLabel(detail.getFullNames().get(j));

                                    long daysDifference = TimeUnit.DAYS.convert(
                                            detail.getDateTimekeeping().getTime() - dateValue.getTime(),
                                            TimeUnit.MILLISECONDS
                                    );

                                    if (daysDifference == 0) {
                                        employeeId0.add(employee);
                                    } else if (daysDifference == 1) {
                                        employeeId1.add(employee);
                                    } else if (daysDifference == 2) {
                                        employeeId2.add(employee);
                                    } else if (daysDifference == 3) {
                                        employeeId3.add(employee);
                                    } else if (daysDifference == 4) {
                                        employeeId4.add(employee);
                                    } else if (daysDifference == 5) {
                                        employeeId5.add(employee);
                                    } else {
                                        employeeId6.add(employee);
                                    }
                                }
                            }

                            groupedItem.setEmployeeId0(employeeId0);
                            groupedItem.setEmployeeId1(employeeId1);
                            groupedItem.setEmployeeId2(employeeId2);
                            groupedItem.setEmployeeId3(employeeId3);
                            groupedItem.setEmployeeId4(employeeId4);
                            groupedItem.setEmployeeId5(employeeId5);
                            groupedItem.setEmployeeId6(employeeId6);

                            return groupedItem;
                        })
                ))
                .values()
                .stream()
                .collect(Collectors.toList());
    }

    @Override
    public ResponseEntity<Object> exportData(DutySchedulesRequest.ReportForm dto) throws Exception {
        String pathTemplate = "template/export/BM_Xuat_lich_truc.xlsx";
        ExportExcel dynamicExport = utilsService.initExportExcel("LICH_TRUC_THEO_DON_VI", pathTemplate, 5);
        List<String> viTriTrucs = new ArrayList<>();
        int col = 2;
        List<DutySchedulesResponse> list = dutySchedulesRepository.getListExportTotal(dto, false);
        Map<String, List<String>> mapEmps = new HashMap<>();
        list.forEach(item -> {
            if (!viTriTrucs.contains(item.getDutyPositionName())) {
                viTriTrucs.add(item.getDutyPositionName());
            }
            String key = item.getDutyPositionName() + "-" + Utils.formatDate(item.getDateTimekeeping());
            if (!mapEmps.containsKey(key)) {
                mapEmps.put(key, new ArrayList<>());
            }
            mapEmps.get(key).add(item.getEmployeeName());
        });

        for (String chucDanh : viTriTrucs) {
            dynamicExport.setText(chucDanh, col++, 3);
        }
        dynamicExport.insertRow(Utils.daysBetween(dto.getFromDate(), dto.getToDate()));

        Date pDate = dto.getFromDate();
        Calendar calendar = Calendar.getInstance();
        while (!pDate.after(dto.getToDate())) {
            col = 0;
            calendar.setTime(pDate);
            dynamicExport.setText(DAY_OF_WEEKS[calendar.get(Calendar.DAY_OF_WEEK)], col++);
            dynamicExport.setText(Utils.formatDate(pDate, "dd/MM"), col++);
            for (String chucDanh : viTriTrucs) {
                String key = chucDanh + "-" + Utils.formatDate(pDate);
                dynamicExport.setText(Utils.join(" - ", mapEmps.get(key)), col++);
            }
            pDate = DateUtils.addDays(pDate, 1);
            dynamicExport.increaseRow();
        }

        dynamicExport.setCellFormat(3, 0, 3 + Utils.daysBetween(dto.getFromDate(), dto.getToDate()) + 1, col - 1, ExportExcel.BORDER_FORMAT);

        return ResponseUtils.ok(dynamicExport, "Lich_truc_theo_don_vi.xlsx");
    }

    @Override
    public List<DutySchedulesResponse.ListBoxBean> getWeeks() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MONTH, 1);
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        List<DutySchedulesResponse.ListBoxBean> result = new ArrayList();
        for (int idx = 0; idx < 20; idx++) {
            DutySchedulesResponse.ListBoxBean bean = new DutySchedulesResponse.ListBoxBean();
            bean.setValue(Utils.formatDate(cal.getTime()));
            bean.setName(Utils.formatDate(cal.getTime()) + " - " + Utils.formatDate(DateUtils.addDays(cal.getTime(), 6)));
            cal.add(Calendar.DATE, -7);
            result.add(bean);
        }
        return result;
    }

    @Override
    public ResponseEntity<Object> exportDataTotal(DutySchedulesRequest.ReportForm dto) throws Exception {
        //ham xuat bao cao tong hop
        String pathTemplate = "template/export/BM-Bao-cao-danh-sach-truc-tong.xlsx";
        ExportExcel dynamicExport = utilsService.initExportExcel("DANH_SACH_TRUC_TONG", pathTemplate, 7);
        //xuat title
        Map params = new HashMap();
        params.put("tu_ngay", Utils.formatDate(dto.getFromDate()));
        params.put("den_ngay", Utils.formatDate(dto.getToDate()));
        params.put("ngay_bao_cao", Utils.formatDate(new Date(), "dd"));
        params.put("thang_bao_cao", Utils.formatDate(new Date(), "MM"));
        params.put("nam_bao_cao", Utils.formatDate(new Date(), "yyyy"));
        dynamicExport.replaceKeys(params);

        Date pDate = dto.getFromDate();
        int col = 1;

        Calendar calendar = Calendar.getInstance();
        while (!pDate.after(dto.getToDate())) {
            calendar.setTime(pDate);
            //neu la t7, chu nhat thi set mau đỏ
            if (calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY
                    || calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY) {
                dynamicExport.setFontColor(5, col, 5, col, Color.red);
            }
            dynamicExport.setText(DAY_OF_WEEKS[calendar.get(Calendar.DAY_OF_WEEK)] + "\r\n" + Utils.formatDate(pDate, "dd/MM"), col++, 5);
            pDate = DateUtils.addDays(pDate, 1);
        }
        //Lay du lieu dang ky truc
        List<DutySchedulesResponse> list = dutySchedulesRepository.getListExportTotal(dto, true);

        Map<String, List<String>> mapOrg = new HashMap<>();
        List<String> organizationIds = new ArrayList<>();
        Map<String, List<String>> mapEmps = new HashMap<>();

        int row = 0;

        for (DutySchedulesResponse item : list) {
            if (!mapOrg.containsKey(item.getOrganizationName())) {
                mapOrg.put(item.getOrganizationName(), new ArrayList<>());
                mapOrg.get(item.getOrganizationName()).add(item.getDutyPositionName());
                row++;
            }
            if (!mapOrg.get(item.getOrganizationName()).contains(item.getDutyPositionName())) {
                mapOrg.get(item.getOrganizationName()).add(item.getDutyPositionName());
                row++;
            }
            if (!organizationIds.contains(item.getOrganizationName())) {
                organizationIds.add(item.getOrganizationName());
            }
            String key = item.getOrganizationName() + "-" + item.getDutyPositionName()
                    + "-" + Utils.formatDate(item.getDateTimekeeping());
            if (!mapEmps.containsKey(key)) {
                mapEmps.put(key, new ArrayList<>());
            }
            mapEmps.get(key).add(item.getEmployeeName());
        }
        //clone don
        dynamicExport.insertRow(row);

        for (String organizationName : organizationIds) {
            List<String> positions = mapOrg.get(organizationName);
            dynamicExport.setText(organizationName, 0);
            dynamicExport.setCellFormat(0, 0, ExportExcel.BOLD_FORMAT);
            if (positions.size() > 1) {
                dynamicExport.increaseRow();
            }
            for (String positionName : positions) {
                if (positions.size() > 1) {
                    dynamicExport.setText(positionName, 0);
                }
                pDate = dto.getFromDate();
                col = 1;
                while (!pDate.after(dto.getToDate())) {
                    String key = organizationName + "-" + positionName + "-" + Utils.formatDate(pDate);
                    dynamicExport.setText(
                            Utils.join(" - ", mapEmps.get(key)), col++);
                    pDate = DateUtils.addDays(pDate, 1);
                }
                dynamicExport.increaseRow();
            }
        }
        dynamicExport.setCellFormat(6, 0, 5 + row + 1, col - 1, ExportExcel.BORDER_FORMAT);

        return ResponseUtils.ok(dynamicExport, "Danh-sach-truc-tong.xlsx");
    }

}
