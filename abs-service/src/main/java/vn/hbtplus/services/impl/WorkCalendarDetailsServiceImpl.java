package vn.hbtplus.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.constants.Constant;
import vn.hbtplus.exceptions.BaseAppException;
import vn.hbtplus.models.dto.WorkCalendarDetailsDTO;
import vn.hbtplus.models.request.AttendanceHistoriesRequest;
import vn.hbtplus.models.response.BaseResponseEntity;
import vn.hbtplus.repositories.entity.AttendanceHistoriesEntity;
import vn.hbtplus.repositories.entity.WorkCalendarDetailsEntity;
import vn.hbtplus.repositories.entity.WorkCalendarsEntity;
import vn.hbtplus.repositories.impl.WorkCalendarDetailsRepository;
import vn.hbtplus.repositories.jpa.WorkCalendarDetailsRepositoryJPA;
import vn.hbtplus.repositories.jpa.WorkCalendarsRepositoryJPA;
import vn.hbtplus.services.WorkCalendarDetailsService;
import vn.hbtplus.utils.*;
import vn.hbtplus.services.UtilsService;

import java.util.*;

@Service
@RequiredArgsConstructor
public class WorkCalendarDetailsServiceImpl implements WorkCalendarDetailsService {

    private final WorkCalendarDetailsRepository workCalendarDetailsRepository;
    private final WorkCalendarsRepositoryJPA workCalendarsJPA;
    private final WorkCalendarDetailsRepositoryJPA workCalendarDetailsRepositoryJPA;
    private final UtilsService utilsService;
    @Override
    public List<WorkCalendarDetailsDTO> getSearchRequests(WorkCalendarDetailsDTO workCalendarDetailsDTO) {
        List<WorkCalendarDetailsDTO> lstResult = workCalendarDetailsRepository.getSearchRequests(workCalendarDetailsDTO);
        if (lstResult.isEmpty()) {
            Optional<WorkCalendarsEntity> optional = workCalendarsJPA.findById(workCalendarDetailsDTO.getWorkCalendarId());
            if (optional.isPresent()) {
                workCalendarDetailsRepository.reInsertWorkCalendarDetails(optional.get() , workCalendarDetailsDTO.getYear());
            } else {
                throw new BaseAppException("BAD_REQUEST");
            }
            lstResult = workCalendarDetailsRepository.getSearchRequests(workCalendarDetailsDTO);
        }
//        List<CateGo> lookupValuesDTOS = catalogsService.getListDataByType(BaseConstants.CATEGORY_CODES.LOAI_NGAY_NGHI, null);
//        lookupValuesDTOS.forEach(item -> mapWorkdayTypes.put(item.getValue(), item.getLabel()));
//

//        for (WorkCalendarDetailsDTO dto : lstResult){
//            dto.setDescription(mapWorkdayTypes.get(dto.getWorkdayType()) + ", " + Constants.WorkdayTime.valueOf(dto.getWorkdayTime()).getName());
//        }
        return lstResult;
    }



    @Override
    public ResponseEntity<Object> importWorkScheduleWorkScheduleDetail(MultipartFile file, Long workCalendarId) throws Exception {
        String fileConfigName = "BM_NhapMoi_LichLamViec.xml";
        ImportExcel importExcel = new ImportExcel("template/import/" + fileConfigName);
        List<Object[]> dataList = new ArrayList<>();
        ResponseEntity<Object> resultValidate = utilsService.validateFileImport(importExcel, file, dataList);
        if (resultValidate != null) {
            return resultValidate;
        }

        // map date_timekeeping co tren file import
        HashMap<String, Long> mapDateTimekeepingImport = new HashMap<>();

        // Tạo map thông tin những ngày công cũ
        List<WorkCalendarDetailsEntity> lstCurrentTimekeeping = workCalendarDetailsRepositoryJPA.findByWorkCalendarId(workCalendarId);
        HashMap<String, WorkCalendarDetailsEntity> mapCurrentTimekeeping = new HashMap<>();
        lstCurrentTimekeeping.forEach(x -> {
            mapCurrentTimekeeping.put(Utils.formatDate(x.getDateTimekeeping()), x);
        });

        List<WorkCalendarDetailsEntity> lstWorkCalendarDetailsEntityNew = new ArrayList<>();
        List<WorkCalendarDetailsEntity> lstWorkCalendarDetailsEntityOld = new ArrayList<>();

        Map<String, String> mapWorkdayTimes = workCalendarDetailsRepository.loadDataBySysCategory(true, BaseConstants.CATEGORY_CODES.LICH_LAM_VIEC);

        int row = -1;
        for (Object[] obj : dataList) {
            row++;
            int col = 1;
            Date dateTimekeeping = (Date) obj[col];
            String dateTimekeepingStr = Utils.formatDate(dateTimekeeping);

            // Check trùng trên file import
            if (mapDateTimekeepingImport.containsKey(dateTimekeepingStr)) {
                importExcel.addError(row, col, I18n.getMessage("workCalendar.validate.duplicateTimekeeping"), dateTimekeepingStr);
                continue;
            } else {
                mapDateTimekeepingImport.put(dateTimekeepingStr, null);
            }

            // Tìm kiếm ngày công cũ nếu tồn tại
            WorkCalendarDetailsEntity oldCalendarDetail = mapCurrentTimekeeping.get(dateTimekeepingStr);
            if (oldCalendarDetail != null) {
                lstWorkCalendarDetailsEntityOld.add(oldCalendarDetail);
            }
            col++;

            String workDayTimeStr = ((String) obj[col]).toLowerCase();
            String workDayTimeValue = mapWorkdayTimes.get(workDayTimeStr);
            if (workDayTimeValue == null) {
                // Lỗi loại thời gian không hợp lệ
                importExcel.addError(row, col, I18n.getMessage("workCalendar.validate.invalidDayTime"), workDayTimeStr);
                continue;
            }
            col++;

            String description = ((String) obj[col]);

            WorkCalendarDetailsEntity entity = new WorkCalendarDetailsEntity();
            entity.setDateTimekeeping(dateTimekeeping);
            entity.setWorkdayTimeId(workDayTimeValue);
            entity.setDescription(description);
            entity.setWorkCalendarId(workCalendarId);
            entity.setCreatedTime(new Date());
            entity.setCreatedBy(Utils.getUserNameLogin());
            lstWorkCalendarDetailsEntityNew.add(entity);
        }

        if (importExcel.hasError()) { // co loi xay ra
            return utilsService.responseErrorImportFile(importExcel, file);
        } else { // thuc hien insert vao DB
            this.workCalendarDetailsRepositoryJPA.deleteAll(lstWorkCalendarDetailsEntityOld);
            this.workCalendarDetailsRepositoryJPA.saveAll(lstWorkCalendarDetailsEntityNew);
        }
        return ResponseUtils.ok();
    }



    @Override
    public String getTemplateImportWorkScheduleDetail() throws Exception {
        String importTemplateName = "BM_NhapMoi_LichLamViec.xlsx";
        String pathTemplate = "template/import/" + importTemplateName;
        ExportExcel dynamicExport = new ExportExcel(pathTemplate, 2, true);
        int activeSheet = 1;
        dynamicExport.setActiveSheet(activeSheet);
        dynamicExport.setActiveSheet(0);
        String fileName = Utils.getFilePathExport(importTemplateName);
        dynamicExport.exportFile(fileName);
        return fileName;
    }

    @Override
    @Transactional
    public BaseResponseEntity saveData(WorkCalendarDetailsDTO dto, Long id) throws BaseAppException {
        WorkCalendarDetailsEntity entity;
        if (id != null && id > 0L) {
            entity = workCalendarDetailsRepositoryJPA.getById(id);
            entity.setModifiedTime(new Date());
            entity.setModifiedBy(Utils.getUserNameLogin());
        } else {
            entity = new WorkCalendarDetailsEntity();
            entity.setCreatedTime(new Date());
            entity.setCreatedBy(Utils.getUserNameLogin());
        }
        Utils.copyProperties(dto, entity);
        entity.setIsDeleted(BaseConstants.STATUS.NOT_DELETED);
        workCalendarDetailsRepositoryJPA.save(entity);
        return ResponseUtils.ok(entity.getWorkCalendarDetailId());
    }

}
