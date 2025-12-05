/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.hbtplus.models.dto.WorkProcessDto;
import vn.hbtplus.models.request.AnnualLeavesRequest;
import vn.hbtplus.models.response.*;
import vn.hbtplus.repositories.entity.AnnualLeavesEntity;
import vn.hbtplus.repositories.impl.AnnualLeavesRepository;
import vn.hbtplus.repositories.jpa.AnnualLeavesRepositoryJPA;
import vn.hbtplus.services.AnnualLeavesService;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.utils.ResponseUtils;
import vn.hbtplus.utils.ExportExcel;
import vn.hbtplus.exceptions.BaseAppException;
import vn.hbtplus.exceptions.RecordNotExistsException;
import vn.hbtplus.utils.Utils;

import java.time.Month;
import java.util.*;

/**
 * Lop impl service ung voi bang abs_annual_leaves
 *
 * @author tudd
 * @version 1.0
 * @since 1.0
 */

@Service
@RequiredArgsConstructor
public class AnnualLeavesServiceImpl implements AnnualLeavesService {

    private final AnnualLeavesRepository annualLeavesRepository;
    private final AnnualLeavesRepositoryJPA annualLeavesRepositoryJPA;

    @Override
    @Transactional(readOnly = true)
    public TableResponseEntity<AnnualLeavesResponse.SearchResult> searchData(AnnualLeavesRequest.SearchForm dto) {
        return ResponseUtils.ok(annualLeavesRepository.searchData(dto));
    }

    @Override
    @Transactional
    public ResponseEntity saveData(AnnualLeavesRequest.SubmitForm dto) throws BaseAppException {
        AnnualLeavesEntity entity;
        if (dto.getAnnualLeaveId() != null && dto.getAnnualLeaveId() > 0L) {
            entity = annualLeavesRepositoryJPA.getById(dto.getAnnualLeaveId());
            entity.setModifiedTime(new Date());
            entity.setModifiedBy(Utils.getUserNameLogin());
        } else {
            entity = new AnnualLeavesEntity();
            entity.setCreatedTime(new Date());
            entity.setCreatedBy(Utils.getUserNameLogin());
        }
        Utils.copyProperties(dto, entity);
        entity.setIsDeleted(BaseConstants.STATUS.NOT_DELETED);
        annualLeavesRepositoryJPA.save(entity);
        return ResponseUtils.ok(entity.getAnnualLeaveId());
    }

    @Override
    @Transactional
    public ResponseEntity deleteData(Long id) throws RecordNotExistsException {
        Optional<AnnualLeavesEntity> optional = annualLeavesRepositoryJPA.findById(id);
        if (!optional.isPresent() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new RecordNotExistsException(id, AnnualLeavesEntity.class);
        }
        annualLeavesRepository.deActiveObject(AnnualLeavesEntity.class, id);
        return ResponseUtils.ok(id);
    }

    @Override
    @Transactional(readOnly = true)
    public BaseResponseEntity<AnnualLeavesResponse> getDataById(Long id) throws RecordNotExistsException {
        Optional<AnnualLeavesEntity> optional = annualLeavesRepositoryJPA.findById(id);
        if (!optional.isPresent() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new RecordNotExistsException(id, AnnualLeavesEntity.class);
        }
        AnnualLeavesResponse dto = new AnnualLeavesResponse();
        Utils.copyProperties(optional.get(), dto);
        return ResponseUtils.ok(dto);
    }

    @Override
    public ResponseEntity<Object> exportData(AnnualLeavesRequest.SearchForm dto) throws Exception {
        String pathTemplate = "template/export/BM_Xuat_DS_theo_doi_ngay_phep.xlsx";
        ExportExcel dynamicExport = new ExportExcel(pathTemplate, 2, true);
        List<Map<String, Object>> listDataExport = annualLeavesRepository.getListExport(dto);

        List<String> months = Arrays.asList(
                "january", "february", "march", "april", "may", "june",
                "july", "august", "september", "october", "november", "december"
        );

        for (Map<String, Object> employeeData : listDataExport) {
            String employeeId = employeeData.get("employee_id").toString();

            List<Map<String, Object>> usedDays = annualLeavesRepository.getUsedDaysByEmp(employeeId);

            Map<String, Integer> monthCountMap = new HashMap<>();
            for (String month : months) {
                monthCountMap.put(month, 0);
            }

            for (Map<String, Object> usedDay : usedDays) {
                Object dateObj = usedDay.get("date_timekeeping");
                if (dateObj == null) continue;

                Date date = Utils.stringToDate(dateObj.toString(), "yyyy-MM-dd");
                int year = Utils.getYearByDate(date);
                if (year != dto.getYear()) continue;

                int month = Utils.getMonthByDate(date);
                String monthName = this.getMonthName(month);
                int currentCount = monthCountMap.getOrDefault(monthName, 0);
                monthCountMap.put(monthName, currentCount + 1);
            }

            for (String month : months) {
                String key = "used_" + month + "_days";
                employeeData.put(key, monthCountMap.get(month));
            }
        }

        dynamicExport.replaceKeys(listDataExport);
        return ResponseUtils.ok(dynamicExport, "BM_Xuat_DS_theo_doi_ngay_phep.xlsx");
    }

    @Override
    public ResponseEntity calculate(Integer year, List<Long> empIds) {
        //Lay danh sach nhan vien can tinh phep
        List<WorkProcessDto> listWorkProcess = annualLeavesRepository.getListEmployee(year, empIds);
        List<WorkProcessDto> joinProcess = new ArrayList<>();
        for (WorkProcessDto workProcessDto : listWorkProcess) {
            if (!joinProcess.isEmpty()) {
                WorkProcessDto lastProcess = joinProcess.get(joinProcess.size() - 1);
                if (lastProcess.getEmployeeId().equals(workProcessDto.getEmployeeId())
                    && Utils.daysBetween(lastProcess.getEndDate(), workProcessDto.getStartDate()) == 1) {
                    lastProcess.setEndDate(workProcessDto.getEndDate());
                    continue;
                }
            }
            joinProcess.add(workProcessDto);
        }
        Map<String, List<Long>> mapEmployeeIds = new HashMap<>();
        for (WorkProcessDto workProcessDto : joinProcess) {
            String key = Utils.formatDate(workProcessDto.getStartDate()) + "-" + Utils.formatDate(workProcessDto.getEndDate());
            List<Long> employeeIds = mapEmployeeIds.getOrDefault(key, new ArrayList<>());
            employeeIds.add(workProcessDto.getEmployeeId());
            mapEmployeeIds.put(key, employeeIds);
        }

        List<AnnualLeavesEntity> listSaveEntity = new ArrayList<>();
        for (Map.Entry<String, List<Long>> entry : mapEmployeeIds.entrySet()) {
            String key = entry.getKey();
            Date startDate = Utils.stringToDate(key.split("-")[0]);
            Date endDate = Utils.stringToDate(key.split("-")[1]);
            listSaveEntity.addAll(calculateAnnualLeave(entry.getValue(), startDate, endDate));
        }
        listSaveEntity.forEach(entity -> {
            Integer soThangLamviec = Utils.NVL(entity.getWorkingMonths());
            Integer soThangNghiKhongLuong = Utils.NVL(entity.getUnpaidMonths());
            Integer soThangNghiOm = Utils.NVL(entity.getSicknessMonths());
            Integer soThangNghiTainan = Utils.NVL(entity.getAccidentMonths());
            Integer soPhepDuocNghi = soThangLamviec.intValue();
            Integer phepThamNien = Utils.NVL(entity.getSeniority()) / 12 / 5;
            if (soThangLamviec < (soThangNghiKhongLuong + soThangNghiOm + soThangNghiTainan)) {
                soPhepDuocNghi = 0;
                phepThamNien = 0;
            } else {
                if (soThangNghiKhongLuong > 1) {
                    //thoi gian nghi khong luong khong qua 1 thang
                    soPhepDuocNghi = soPhepDuocNghi - soThangNghiKhongLuong + 1;
                }
                if (soThangNghiOm > 2) {
                    //thoi gian nghi om khong qua 2 thang
                    soPhepDuocNghi = soPhepDuocNghi - soThangNghiOm + 2;
                }
                if (soThangNghiTainan > 6) {
                    //thoi gian nghi om tai nang lao dong khong qua 6 thang
                    soPhepDuocNghi = soPhepDuocNghi - soThangNghiTainan + 6;
                }
            }
            entity.setSeniority(Utils.NVL(entity.getSeniority()) / 12);
            entity.setSeniorityDays(phepThamNien);
            entity.setAnnualDays(soPhepDuocNghi);
            entity.setTotalAnnualDays(soPhepDuocNghi + Utils.NVL(entity.getSeniorityDays()));
        });
        //Lay danh sach du lieu cu
        Map<Long, List<AnnualLeavesEntity>> mapNewEntity = new HashMap<>();
        for (AnnualLeavesEntity entity : listSaveEntity) {
            mapNewEntity.computeIfAbsent(entity.getEmployeeId(), k -> new ArrayList<>()).add(entity);
        }
        List<AnnualLeavesEntity> listOldData = annualLeavesRepository.getListAnnualLeave(year, empIds);
        Map<Long, List<AnnualLeavesEntity>> mapOldEntity = new HashMap<>();
        listOldData.forEach(entity -> {
            mapOldEntity.computeIfAbsent(entity.getEmployeeId(), k -> new ArrayList<>()).add(entity);
        });

        Set<Long> allEmpIds = new HashSet<>(mapOldEntity.keySet());
        allEmpIds.addAll(mapNewEntity.keySet());
        List<Long> empIdChange = new ArrayList<>();
        List<AnnualLeavesEntity> listInsert = new ArrayList<>();
        allEmpIds.forEach(empId -> {
            List<AnnualLeavesEntity> oldEntity = mapOldEntity.get(empId);
            List<AnnualLeavesEntity> newEntity = mapNewEntity.get(empId);
            if (isChange(oldEntity, newEntity)) {
                empIdChange.add(empId);
                if (newEntity != null) {
                    listInsert.addAll(newEntity);
                }
            }
        });
        if (!empIdChange.isEmpty()) {
            annualLeavesRepository.inactiveOldData(empIdChange, year);
            annualLeavesRepository.insertBatch(AnnualLeavesEntity.class, listInsert, Utils.getUserNameLogin());
        }
        //cap nhat thong tin phep da nghi
        annualLeavesRepository.updateLeaveDays(year, empIds);


        return ResponseUtils.ok();
    }

    private boolean isChange(List<AnnualLeavesEntity> oldEntities, List<AnnualLeavesEntity> newEntities) {
        if (oldEntities == null || newEntities == null) {
            return true;
        }
        if (oldEntities.size() != newEntities.size()) {
            return true;
        }
        for (AnnualLeavesEntity oldEntity : oldEntities) {
            boolean contains = false;
            for (AnnualLeavesEntity newEntity : newEntities) {
                if (oldEntity.equals(newEntity)) {
                    contains = true;
                    break;
                }
            }
            if (!contains) {
                return true;
            }
        }
        return false;
    }

    private List<AnnualLeavesEntity> calculateAnnualLeave(List<Long> empIds, Date startDate, Date endDate) {
        //Lay thong tin
        List<AnnualLeavesEntity> listSaveEntity = annualLeavesRepository.getListWorkingMonths(empIds, startDate, endDate);
        List<AnnualLeavesEntity> listSeniority = annualLeavesRepository.getListSeniority(empIds, startDate, endDate);
        List<AnnualLeavesEntity> listLeaveMonths = annualLeavesRepository.getListTotalLeaveMonth(empIds, startDate, endDate);
        Map<Long, AnnualLeavesEntity> mapSaveEntity = new HashMap<>();
        listSaveEntity.forEach(entity -> mapSaveEntity.put(entity.getEmployeeId(), entity));
        listSeniority.forEach(entity -> {
            AnnualLeavesEntity saveEntity = mapSaveEntity.get(entity.getEmployeeId());
            if (saveEntity != null) {
                saveEntity.setSeniority(entity.getSeniority());
            }
        });
        listLeaveMonths.forEach(entity -> {
            AnnualLeavesEntity saveEntity = mapSaveEntity.get(entity.getEmployeeId());
            if (saveEntity != null) {
                saveEntity.setAccidentMonths(entity.getAccidentMonths());
                saveEntity.setSicknessMonths(entity.getSicknessMonths());
                saveEntity.setUnpaidMonths(entity.getUnpaidMonths());
            }
        });
        return listSaveEntity;
    }

    private String getMonthName(int month) {
        if (month < 1 || month > 12) {
            throw new IllegalArgumentException("Month must be between 1 and 12");
        }
        return Month.of(month).name().toLowerCase();
    }


}
