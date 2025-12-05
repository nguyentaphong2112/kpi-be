/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.insurance.services.impl;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.exceptions.BaseAppException;
import vn.hbtplus.exceptions.RecordNotExistsException;
import vn.hbtplus.insurance.models.InsuranceContributionsDto;
import vn.hbtplus.insurance.models.request.InsuranceRetractionsRequest;
import vn.hbtplus.insurance.models.response.InsuranceRetractionsResponse;
import vn.hbtplus.insurance.repositories.entity.InsuranceContributionsEntity;
import vn.hbtplus.insurance.repositories.entity.InsuranceRetractionsEntity;
import vn.hbtplus.insurance.repositories.impl.InsuranceRetractionsRepository;
import vn.hbtplus.insurance.repositories.jpa.InsuranceRetractionsRepositoryJPA;
import vn.hbtplus.insurance.services.InsuranceContributionsService;
import vn.hbtplus.insurance.services.InsuranceRetractionsService;
import vn.hbtplus.models.response.BaseResponseEntity;
import vn.hbtplus.models.response.TableResponseEntity;
import vn.hbtplus.utils.ExportExcel;
import vn.hbtplus.utils.I18n;
import vn.hbtplus.utils.ResponseUtils;
import vn.hbtplus.utils.Utils;

import java.util.*;

/**
 * Lop impl service ung voi bang icn_insurance_retractions
 *
 * @author tudd
 * @version 1.0
 * @since 1.0
 */

@Service
@RequiredArgsConstructor
public class InsuranceRetractionsServiceImpl implements InsuranceRetractionsService {

    private final InsuranceRetractionsRepository insuranceRetractionsRepository;
    private final InsuranceRetractionsRepositoryJPA insuranceRetractionsRepositoryJPA;
    private final InsuranceContributionsService insuranceContributionsService;

    @Override
    @Transactional(readOnly = true)
    public TableResponseEntity<InsuranceRetractionsResponse> searchData(InsuranceRetractionsRequest.SearchForm dto) {
        return ResponseUtils.ok(insuranceRetractionsRepository.searchData(dto));
    }

    /**
     * tim kiem tai popup truy thu truy linh
     *
     * @param dto tham so tim kiem
     * @return danh sach
     */
    @Override
    @Transactional(readOnly = true)
    public TableResponseEntity<InsuranceRetractionsResponse> searchDataPopup(InsuranceRetractionsRequest.SearchForm dto) {
        return ResponseUtils.ok(insuranceRetractionsRepository.searchDataPopup(dto));
    }

    @Override
    @Transactional
    public ResponseEntity saveData(InsuranceRetractionsRequest.SubmitForm dto) throws BaseAppException {
        InsuranceRetractionsEntity entity;
        if (dto.getInsuranceRetractionId() != null && dto.getInsuranceRetractionId() > 0L) {
            entity = insuranceRetractionsRepositoryJPA.getById(dto.getInsuranceRetractionId());
            entity.setModifiedTime(new Date());
            entity.setModifiedBy(Utils.getUserNameLogin());
        } else {
            entity = new InsuranceRetractionsEntity();
            entity.setCreatedTime(new Date());
            entity.setCreatedBy(Utils.getUserNameLogin());
        }
        Utils.copyProperties(dto, entity);
        entity.setIsDeleted(BaseConstants.STATUS.NOT_DELETED);
        insuranceRetractionsRepositoryJPA.save(entity);
        return ResponseUtils.ok(entity.getInsuranceRetractionId());
    }

    @Override
    @Transactional
    public ResponseEntity deleteData(InsuranceRetractionsRequest.SearchForm dto) throws BaseAppException {

        int record = insuranceRetractionsRepository.deleteByForm(dto);
        if (record == 0) {
            throw new BaseAppException(I18n.getMessage("global.notFoundDelete"));
        }
        return ResponseUtils.ok(record);
    }

    @Override
    @Transactional(readOnly = true)
    public BaseResponseEntity<InsuranceRetractionsResponse> getDataById(Long id) throws RecordNotExistsException {
        Optional<InsuranceRetractionsEntity> optional = insuranceRetractionsRepositoryJPA.findById(id);
        if (!optional.isPresent() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new RecordNotExistsException(id, InsuranceRetractionsEntity.class);
        }
        InsuranceRetractionsResponse dto = new InsuranceRetractionsResponse();
        Utils.copyProperties(optional.get(), dto);
        return ResponseUtils.ok(dto);
    }

    @Override
    public ResponseEntity<Object> exportData(InsuranceRetractionsRequest.SearchForm dto) throws Exception {
        String pathTemplate = "template/export/insurance/BM_BC_Danh_sach_truy_thu_linh.xlsx";
        ExportExcel dynamicExport = new ExportExcel(pathTemplate, 2, true);
        List<Map<String, Object>> listDataExport = insuranceRetractionsRepository.getListExport(dto);
        dynamicExport.replaceKeys(listDataExport);
        return ResponseUtils.ok(dynamicExport, "Danh_sach_truy_thu_linh.xlsx");
    }

    @Override
    @Transactional(rollbackFor = {Exception.class})
    public int calculate(List<String> empCodes, Date periodDate, boolean isScheduled) throws Exception {

        //Khong duoc lap danh sach truy thu, truy linh cua ky van chua phe duyet
        if (insuranceRetractionsRepository.existsNotApproved(periodDate)) {
            throw new BaseAppException(String.format("Tồn tại dữ liệu của kỳ tháng %s chưa được phê duyệt!", Utils.formatDate(periodDate, "MM/yyyy")));
        }
        if (!insuranceRetractionsRepository.existsApproved(periodDate)) {
            throw new BaseAppException(String.format("Chưa tồn tại dữ liệu được phê duyệt của kỳ tháng %s!", Utils.formatDate(periodDate, "MM/yyyy")));
        }

        List<InsuranceContributionsDto> contributionsDtos = insuranceContributionsService.calculateContributions(empCodes, periodDate, true);
        Map<Long, InsuranceContributionsDto> mapPhaiThu = new HashMap<>();
        contributionsDtos.forEach(item -> {
            mapPhaiThu.put(item.getEmployeeId(), item);
        });


        //Lay thong tin da dong cua nhan vien
        List<InsuranceContributionsDto> listExists = insuranceRetractionsRepository.getExistsContributions(empCodes, periodDate);
        //Lay thong tin cua ban ghi truy thu/truy linh da duoc thuc hien
        List<InsuranceContributionsDto> listRetro = insuranceRetractionsRepository.getExistsRetro(empCodes, periodDate);
        listExists.addAll(listRetro);
        //Lay danh sach da duoc tinh toan
        Map<Long, InsuranceContributionsDto> mapOldDatas = new HashMap<>();
        if (!isScheduled) {
            List<InsuranceContributionsDto> listOldData = insuranceRetractionsRepository.getListOldRetro(empCodes, periodDate);
            listOldData.stream().forEach(item -> {
                mapOldDatas.put(item.getEmployeeId(), item);
            });
        }
        List<Long> keepEmpIds = new ArrayList<>();

        Map<Long, InsuranceContributionsDto> mapDaThu = new HashMap<>();
        listExists.forEach(item -> {
            if (mapDaThu.get(item.getEmployeeId()) == null) {
                mapDaThu.put(item.getEmployeeId(), item);
            } else {
                mapDaThu.get(item.getEmployeeId()).add(item);
            }
        });
        Set<Long> empIds = new HashSet<>(mapPhaiThu.keySet());
        empIds.addAll(mapDaThu.keySet());
        List<InsuranceContributionsDto> listResults = new ArrayList<>();
        empIds.stream().forEach(empId -> {
            InsuranceContributionsDto dto = compareContribution(mapPhaiThu.get(empId), mapDaThu.get(empId));

            if (dto != null && !Utils.NVL(dto.getTotalAmount(), 0L).equals(0L)) {
                if (mapOldDatas.get(empId) == null
                    || !mapOldDatas.get(empId).getTotalAmount().equals(dto.getTotalAmount())) {
                    dto.setSoDaThuDto(mapDaThu.get(empId));
                    dto.setSoPhaiThuDto(mapPhaiThu.get(empId));
                    listResults.add(dto);
                } else {
                    keepEmpIds.add(dto.getEmployeeId());
                }
            }
        });

        insuranceRetractionsRepository.deleteOldData(empCodes, periodDate, keepEmpIds);

        insuranceRetractionsRepository.saveAll(listResults, periodDate);

        return listResults.size();
    }

    @Override
    @Transactional(rollbackFor = {Exception.class})
    public int calculateByListPeriod(List<String> empCodes, List<String> listPeriodDate) throws Exception {
        int total = 0;
        for (String date : listPeriodDate) {
            total += calculate(empCodes, Utils.getLastDay(Utils.stringToDate(date)), false);
        }
        return total;
    }

    /**
     * hàm lấy kỳ để thực hiện tính truy thu truy lĩnh tự động
     */
    @Override
    public Date getPeriodForSchedule() {
        return Utils.getLastDay(DateUtils.addMonths(new Date(), -1));
    }

    private InsuranceContributionsDto compareContribution(InsuranceContributionsDto afterDto, InsuranceContributionsDto beforeDto) {

        if (afterDto == null || afterDto.getTotalAmount().equals(0L)) {
            if (beforeDto != null && !Utils.NVL(beforeDto.getTotalAmount()).equals(0L)) {
                InsuranceContributionsDto dto = Utils.copyProperties(beforeDto, new InsuranceContributionsDto());
                dto.setType(InsuranceContributionsEntity.TYPES.TRUY_LINH);
                dto.retroactive();
                return dto;
            }
        } else {
            if (beforeDto == null || beforeDto.getTotalAmount().equals(0L)) {
                afterDto.setType(InsuranceContributionsEntity.TYPES.TRUY_THU);
            } else {
                InsuranceContributionsDto dto = Utils.copyProperties(afterDto, new InsuranceContributionsDto());
                dto.retroactive(beforeDto);
                if (dto.getTotalAmount() < 0) {
                    dto.setType(InsuranceContributionsEntity.TYPES.TRUY_LINH);
                } else {
                    dto.setType(InsuranceContributionsEntity.TYPES.TRUY_THU);
                }
                return dto;
            }
            return afterDto;
        }
        return null;
    }

}
