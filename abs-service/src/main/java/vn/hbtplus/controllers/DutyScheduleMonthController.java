package vn.hbtplus.controllers;


import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.hbtplus.annotations.HasPermission;
import vn.hbtplus.annotations.Resource;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.constants.Constant;
import vn.hbtplus.constants.Scope;
import vn.hbtplus.exceptions.BaseAppException;
import vn.hbtplus.models.request.DutySchedulesRequest;
import vn.hbtplus.models.response.DutySchedulesResponse;
import vn.hbtplus.models.response.TableResponseEntity;
import vn.hbtplus.services.DutySchedulesService;

import javax.validation.Valid;

@RestController
@RequestMapping(BaseConstants.REQUEST_MAPPING_PREFIX)
@RequiredArgsConstructor
@Resource(value = Constant.RESOURCES.ABS_DUTY_SCHEDULE_MONTH)
public class DutyScheduleMonthController {
    private final DutySchedulesService dutySchedulesService;

    @GetMapping(value = "/v1/duty-schedule-month", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public TableResponseEntity<DutySchedulesResponse.SearchResultMonth> searchData(DutySchedulesRequest.SearchForm dto) {
        return dutySchedulesService.searchDataMonth(dto);
    }

    @PostMapping(value = "/v1/duty-schedule-month", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.UPDATE)
    public ResponseEntity saveData(@Valid @RequestBody DutySchedulesRequest.SubmitFormMonth dto) throws BaseAppException {
        return dutySchedulesService.saveDataMonth(dto);
    }

}
