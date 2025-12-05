package vn.kpi.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.kpi.annotations.HasPermission;
import vn.kpi.annotations.Resource;
import vn.kpi.constants.Constant;
import vn.kpi.constants.Scope;
import vn.kpi.exceptions.BaseAppException;
import vn.kpi.exceptions.RecordNotExistsException;
import vn.kpi.models.request.ConfigPageRequest;
import vn.kpi.models.response.BaseResponseEntity;
import vn.kpi.models.response.ConfigPageResponse;
import vn.kpi.models.response.TableResponseEntity;
import vn.kpi.services.ConfigPageService;
import vn.kpi.utils.ResponseUtils;

import javax.validation.Valid;

@RestController
@RequestMapping("")
@RequiredArgsConstructor
@Resource(value = Constant.RESOURCES.CONFIG_PAGE)
public class ConfigPageController {
    private final ConfigPageService configPageService;

    @GetMapping(value = "/v1/config-pages/config-by-url", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public BaseResponseEntity<ConfigPageResponse.ExtractBean> getConfigByUrl(
            @RequestParam String url) throws BaseAppException {
        return ResponseUtils.ok(configPageService.getConfigByUrl(url));
    }

    @GetMapping(value = "/v1/config-pages", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public TableResponseEntity<ConfigPageResponse.SearchResult> searchData(ConfigPageRequest.SearchForm dto) {
        return configPageService.searchData(dto);
    }

    @PostMapping(value = "/v1/config-pages", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.CREATE)
    public ResponseEntity saveData(@Valid @RequestBody ConfigPageRequest.SubmitForm dto) throws BaseAppException {
        return configPageService.saveData(dto,null);
    }

    @PutMapping(value = "/v1/config-pages/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.UPDATE)
    public ResponseEntity updateData(@Valid @RequestBody ConfigPageRequest.SubmitForm dto, @PathVariable Long id) throws BaseAppException {
        return configPageService.saveData(dto,id);
    }

    @DeleteMapping(value = "/v1/config-pages/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.DELETE)
    public ResponseEntity deleteData(@PathVariable Long id) throws RecordNotExistsException {
        return configPageService.deleteData(id);
    }

    @GetMapping(value = "/v1/config-pages/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public BaseResponseEntity<ConfigPageResponse.Detail> getDataById(@PathVariable Long id) throws RecordNotExistsException {
        return configPageService.getDataById(id);
    }

    @GetMapping(value = "/v1/config-pages/export", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity<Object> exportData(ConfigPageRequest.SearchForm dto) throws Exception {
        return configPageService.exportData(dto);
    }


}
