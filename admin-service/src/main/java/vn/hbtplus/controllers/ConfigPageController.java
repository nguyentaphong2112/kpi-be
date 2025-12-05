package vn.hbtplus.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.hbtplus.annotations.HasPermission;
import vn.hbtplus.annotations.Resource;
import vn.hbtplus.constants.Constant;
import vn.hbtplus.constants.Scope;
import vn.hbtplus.exceptions.BaseAppException;
import vn.hbtplus.exceptions.RecordNotExistsException;
import vn.hbtplus.models.TableResponse;
import vn.hbtplus.models.request.ConfigPageRequest;
import vn.hbtplus.models.response.BaseResponseEntity;
import vn.hbtplus.models.response.ConfigPageResponse;
import vn.hbtplus.models.response.TableResponseEntity;
import vn.hbtplus.services.ConfigPageService;
import vn.hbtplus.utils.ResponseUtils;

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
