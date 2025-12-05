package vn.kpi.controllers;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.kpi.annotations.HasPermission;
import vn.kpi.annotations.Resource;
import vn.kpi.constants.BaseConstants;
import vn.kpi.constants.Constant;
import vn.kpi.constants.Scope;
import vn.kpi.exceptions.BaseAppException;
import vn.kpi.exceptions.RecordNotExistsException;
import vn.kpi.models.request.EmployeeEvaluationsRequest;
import vn.kpi.models.request.EmployeeWorkPlanningsRequest;
import vn.kpi.models.response.BaseResponseEntity;
import vn.kpi.models.response.EmployeeEvaluationsResponse;
import vn.kpi.models.response.TableResponseEntity;
import vn.kpi.repositories.entity.EmployeeEvaluationsEntity;
import vn.kpi.repositories.jpa.EmployeeEvaluationsRepositoryJPA;
import vn.kpi.repositories.jpa.EmployeesRepositoryJPA;
import vn.kpi.services.EmployeeEvaluationsService;
import vn.kpi.utils.ResponseUtils;
import vn.kpi.utils.Utils;

import javax.validation.Valid;

@RestController
@RequestMapping(BaseConstants.REQUEST_MAPPING_PREFIX)
@RequiredArgsConstructor
@Resource(value = Constant.RESOURCES.PERSONAL_EVALUATIONS)
public class PersonalEvaluationsController {
    private final EmployeeEvaluationsService employeeEvaluationsService;
    private final EmployeesRepositoryJPA employeesRepositoryJPA;
    private final EmployeeEvaluationsRepositoryJPA employeeEvaluationsRepositoryJPA;

    @GetMapping(value = "/v1/personal-evaluations", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public TableResponseEntity<EmployeeEvaluationsResponse.SearchResult> searchDataPersonal(EmployeeEvaluationsRequest.SearchForm dto) {
        Long employeeId = employeesRepositoryJPA.getIdByEmployeeCode(Utils.getUserEmpCode());
        dto.setEmployeeId(employeeId == null ? -1L : employeeId);
        return employeeEvaluationsService.searchData(dto);
    }


    @PostMapping(value = "/v1/personal-evaluations", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.UPDATE)
    public ResponseEntity saveDataPersonal(@Valid EmployeeEvaluationsRequest.SubmitForm dto) throws BaseAppException {
        EmployeeEvaluationsEntity entity = employeeEvaluationsRepositoryJPA.getById(dto.getEmployeeEvaluationId());
        Long employeeId = employeesRepositoryJPA.getIdByEmployeeCode(Utils.getUserEmpCode());
        if (!entity.getEmployeeId().equals(employeeId)) {
            throw new BaseAppException("invalid permission");
        }
        employeeEvaluationsService.validatePermissionUpdate(dto.getEmployeeEvaluationId());
        return employeeEvaluationsService.saveData(dto);
    }

    @GetMapping(value = "/v1/personal-evaluations/get-validate/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity getValidate(@PathVariable Long id) throws BaseAppException {
        //validate không được cập nhật khi hết thời han

        return ResponseUtils.ok(employeeEvaluationsService.validatePermissionUpdate(id));
    }

    @GetMapping(value = "/v1/personal-evaluations/get-data-emp/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity getDataEmp(@PathVariable Long id) throws RecordNotExistsException {
        return employeeEvaluationsService.getEmpData(id);
    }

    @GetMapping(value = "/v1/personal-evaluations/get-current-job/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity getCurrentJob(@PathVariable Long id) throws BaseAppException {
        //validate không được cập nhật khi hết thời han

        return ResponseUtils.ok(employeeEvaluationsService.getCurrentJob(id));
    }

    @PostMapping(value = "/v1/personal-evaluations/evaluate", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.UPDATE, resource = Constant.RESOURCES.PERSONAL_EVALUATE)
    public ResponseEntity evaluate(@Valid EmployeeEvaluationsRequest.SubmitForm dto) throws BaseAppException {
        return employeeEvaluationsService.saveData(dto);
    }


    @PostMapping(value = "/v1/personal-evaluations/evaluate-manage", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.APPROVE, resource = Constant.RESOURCES.PERSONAL_EVALUATE)
    public ResponseEntity evaluateManage(@Valid EmployeeEvaluationsRequest.SubmitForm dto) throws BaseAppException {
        return employeeEvaluationsService.saveData(dto);
    }


    @DeleteMapping(value = "/v1/personal-evaluations/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.DELETE)
    public ResponseEntity deleteDataPersonal(@PathVariable Long id) throws RecordNotExistsException {
        return employeeEvaluationsService.deleteData(id);
    }

    @GetMapping(value = "/v1/personal-evaluations/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public BaseResponseEntity<EmployeeEvaluationsResponse.SearchResult> getDataByIdPersonal(@PathVariable Long id) throws RecordNotExistsException {
        return employeeEvaluationsService.getDataById(id);
    }

    @GetMapping(value = "/v1/personal-evaluations/export", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity<Object> exportDataPersonal(EmployeeEvaluationsRequest.SearchForm dto) throws Exception {
        return employeeEvaluationsService.exportData(dto);
    }

    @GetMapping(value = "/v1/personal-evaluations/export/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity<Object> exportDataByIdPersonal(@PathVariable Long id) throws Exception {
        return employeeEvaluationsService.exportDataById(id);
    }

    @GetMapping(value = "/v1/personal-evaluations/export-evaluate/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity<Object> exportEvaluationsById(@PathVariable Long id) throws Exception {
        return employeeEvaluationsService.exportEvaluationsById(id);
    }

    @GetMapping(value = "/v1/personal-evaluations/indicator/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity getIndicatorByIdPersonal(@PathVariable Long id) throws BaseAppException {
        return employeeEvaluationsService.getIndicatorById(id, true);
    }

    @PutMapping(value = "/v1/personal-evaluations/indicator/{id}", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.UPDATE)
    public ResponseEntity saveIndicatorDataPersonal(@Valid EmployeeEvaluationsRequest.IndicatorSubmitForm dto, @PathVariable Long id) throws BaseAppException {
        return employeeEvaluationsService.saveIndicatorData(dto, id);
    }

    @GetMapping(value = "/v1/personal-evaluations/work-planning/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity getWorkPlanningByIdPersonal(@PathVariable Long id) throws BaseAppException {
        return employeeEvaluationsService.getWorkPlanningById(id);
    }

    @PostMapping(value = "/v1/personal-evaluations/work-planning", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.UPDATE)
    public ResponseEntity saveWorkPlanningDataPersonal(@Valid EmployeeWorkPlanningsRequest.SubmitForm dto) throws BaseAppException {
        return employeeEvaluationsService.saveWorkPlanningData(dto);
    }

    @PutMapping(value = "/v1/personal-evaluations/status/{employeeEvaluationId}", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.UPDATE)
    public ResponseEntity updateStatus(@RequestBody @Valid EmployeeWorkPlanningsRequest.Status dto, @PathVariable Long employeeEvaluationId) throws BaseAppException {
        return employeeEvaluationsService.updateStatusById(dto, employeeEvaluationId);
    }

    @PutMapping(value = "/v1/personal-evaluations/status-approved/{employeeEvaluationId}", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.APPROVE)
    public ResponseEntity updateAdjustEvaluate(@RequestBody @Valid EmployeeWorkPlanningsRequest.Status dto, @PathVariable Long employeeEvaluationId) throws BaseAppException {
        return employeeEvaluationsService.updateStatusById(dto, employeeEvaluationId);
    }

    @PutMapping(value = "/v1/personal-evaluations/send-for-approval/{id}", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.UPDATE)
    public ResponseEntity sendForApproval(@PathVariable Long id) throws BaseAppException {
        //check lay ra employee_id cua ban ghi so sanh voi employee_id cua user dang nhap
        //khac nhau thi thong bao loi
        EmployeeEvaluationsEntity entity = employeeEvaluationsRepositoryJPA.getById(id);
        Long employeeId = employeesRepositoryJPA.getIdByEmployeeCode(Utils.getUserEmpCode());
        if (!entity.getEmployeeId().equals(employeeId)) {
            throw new BaseAppException("invalid permission");
        }
        return ResponseUtils.ok(employeeEvaluationsService.sendForApproval(id, true));
    }

    @PostMapping(value = "/v1/personal-evaluations/review/{type}", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.REVIEW)
    public ResponseEntity actionReview(@PathVariable String type, @RequestBody EmployeeEvaluationsRequest.Review reviewRequest) throws BaseAppException {
        if (!StringUtils.equalsAnyIgnoreCase(type, "ok", "not-ok")) {
            throw new BaseAppException("type is invalid");
        }
        return ResponseUtils.ok(employeeEvaluationsService.review(type, reviewRequest));
    }

    @PostMapping(value = "/v1/personal-evaluations/approve/{type}", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.APPROVE)
    public ResponseEntity actionApprove(@PathVariable String type, @RequestBody EmployeeEvaluationsRequest.Review reviewRequest) throws BaseAppException {
        if (!StringUtils.equalsAnyIgnoreCase(type, "ok", "not-ok")) {
            throw new BaseAppException("type is invalid");
        }
        return ResponseUtils.ok(employeeEvaluationsService.approve(type, reviewRequest));
    }
}
