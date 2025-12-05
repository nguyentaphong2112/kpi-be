package vn.hbtplus.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import vn.hbtplus.annotations.HasPermission;
import vn.hbtplus.annotations.Resource;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.constants.Constant;
import vn.hbtplus.constants.Scope;
import vn.hbtplus.exceptions.BaseAppException;
import vn.hbtplus.models.BaseRejectRequest;
import vn.hbtplus.models.dto.WorkCalendarDetailsDTO;
import vn.hbtplus.models.request.WorkCalendarsRequest;
import vn.hbtplus.models.response.BaseResponseEntity;
import vn.hbtplus.services.WorkCalendarDetailsService;
import vn.hbtplus.utils.ResponseUtils;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

@RestController
@RequestMapping(BaseConstants.REQUEST_MAPPING_PREFIX)
@RequiredArgsConstructor
@Resource(value = Constant.RESOURCES.ABS_CALENDAR)
public class WorkCalendarDetailsController {

    @Autowired
    private WorkCalendarDetailsService workCalendarDetailsService;

    /**
     * API Tìm kiếm danh sách lịch đi làm
     * @param absWorkCalendarDetailsDTO
     * @return
     * @throws Exception
     */
    @GetMapping(value = "/v1/work-calendar-details", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public BaseResponseEntity<Object> search(WorkCalendarDetailsDTO absWorkCalendarDetailsDTO) {
        Object resultObj = workCalendarDetailsService.getSearchRequests(absWorkCalendarDetailsDTO);
        return ResponseUtils.ok(resultObj);
    }

    @PostMapping(value = "/v1/work-calendar-details/import", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.IMPORT)
    public ResponseEntity<Object> importWorkScheduleDetail(@RequestPart(value = "file") MultipartFile file,
                                                           @RequestPart(value = "workCalendarId") String workCalendarId) throws Exception {
        return workCalendarDetailsService.importWorkScheduleWorkScheduleDetail(file, Long.valueOf(workCalendarId));
    }


    @RequestMapping(value = "/v1/work-calendar-details/download-template", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity<Object> getTemplateImportWorkScheduleDetail() throws Exception {
        return ResponseUtils.getResponseFileEntity(workCalendarDetailsService.getTemplateImportWorkScheduleDetail() , true);
    }

    @PutMapping(value = "/v1/work-calendar-details/{id}", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.UPDATE)
    public BaseResponseEntity saveData(@RequestBody @Valid WorkCalendarDetailsDTO dto,
                                             @PathVariable Long id) throws BaseAppException {
        return workCalendarDetailsService.saveData(dto, id);
    }
}
