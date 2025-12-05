package vn.hbtplus.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vn.hbtplus.annotations.HasPermission;
import vn.hbtplus.annotations.Resource;
import vn.hbtplus.constants.Constant;
import vn.hbtplus.constants.Scope;
import vn.hbtplus.exceptions.BaseAppException;
import vn.hbtplus.models.request.ConfigParameterRequest;
import vn.hbtplus.models.request.ParameterRequest;
import vn.hbtplus.models.response.BaseResponseEntity;
import vn.hbtplus.models.response.ConfigParameterResponse;
import vn.hbtplus.models.response.ListResponseEntity;
import vn.hbtplus.models.response.ParameterResponse;
import vn.hbtplus.models.response.TableResponseEntity;
import vn.hbtplus.services.ConfigParameterService;
import vn.hbtplus.utils.ResponseUtils;
import vn.hbtplus.utils.Utils;

import javax.validation.Valid;
import java.util.Date;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("")
@RequiredArgsConstructor
@Resource(value = Constant.RESOURCES.CONFIG_PARAMETER)
public class ConfigParameterController {

    private final ConfigParameterService configParameterService;

    @GetMapping(value = "/v1/config-parameter", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ListResponseEntity<ConfigParameterResponse> getAllConfigGroups(@RequestParam(required = false) String moduleCode) {
        return ResponseUtils.ok(configParameterService.getConfigGroups(moduleCode));
    }

    @PostMapping(value = "/v1/config-parameter", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.CREATE)
    public ResponseEntity saveConfigGroup(@RequestBody ConfigParameterRequest.SubmitForm config) throws BaseAppException {
        configParameterService.updateConfigGroup(config);
        return ResponseUtils.ok();
    }


    @GetMapping(value = "/v1/config-parameter/{configGroup}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public TableResponseEntity<ParameterResponse.SearchResult> search(ParameterRequest.SearchForm request, @PathVariable String configGroup) {
        return ResponseUtils.ok(configParameterService.search(request, configGroup));
    }

    @GetMapping(value = "/v1/config-parameter/{configGroup}/export/excel", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.EXPORT)
    public ResponseEntity exportData(ParameterRequest.SearchForm request, @PathVariable String configGroup) {
        return ResponseUtils.ok(configParameterService.exportData(request, configGroup));
    }

    @PostMapping(value = "/v1/config-parameter/{configGroup}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.CREATE)
    public ResponseEntity save(@Valid @RequestBody ParameterRequest.SubmitForm request, @PathVariable String configGroup) throws BaseAppException {
        return ResponseUtils.ok(configParameterService.saveData(request, configGroup, null));
    }

    @PutMapping(value = "/v1/config-parameter/{configGroup}/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.UPDATE)
    public ResponseEntity update(@Valid @RequestBody ParameterRequest.SubmitForm request, @PathVariable String configGroup, @PathVariable String id) throws BaseAppException {
        Date startDate = Utils.stringToDate(id.substring(0, 8), "ddMMyyyy");
        return ResponseUtils.ok(configParameterService.saveData(request, configGroup, startDate));
    }

    @DeleteMapping(value = "/v1/config-parameter/{configGroup}/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.DELETE)
    public ResponseEntity deleteById(@PathVariable String configGroup, @PathVariable String id) throws BaseAppException {
        Date startDate = Utils.stringToDate(id.substring(0, 8), "ddMMyyyy");
        return ResponseUtils.ok(configParameterService.deleteById(configGroup, startDate));
    }

    @GetMapping(value = "/v1/config-parameter/{configGroup}/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public BaseResponseEntity<ParameterResponse.SearchResult> getById(@PathVariable String configGroup, @PathVariable String id) throws BaseAppException {
        Date startDate = Utils.stringToDate(id.substring(0, 8), "ddMMyyyy");
        return ResponseUtils.ok(configParameterService.getById(configGroup, startDate));
    }

    @GetMapping(value = "/v1/parameter", produces = MediaType.APPLICATION_JSON_VALUE)
    public BaseResponseEntity<Map<String, String>> getParameters(@RequestParam List<String> configCodes) {
        return ResponseUtils.ok(configParameterService.getParameters(configCodes));
    }
}