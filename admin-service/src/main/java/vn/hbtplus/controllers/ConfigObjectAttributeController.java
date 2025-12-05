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
import vn.hbtplus.models.AttributeConfigDto;
import vn.hbtplus.models.request.ConfigObjectAttributeRequest;
import vn.hbtplus.models.response.BaseResponseEntity;
import vn.hbtplus.models.response.ConfigObjectAttributeResponse;
import vn.hbtplus.models.response.ListResponseEntity;
import vn.hbtplus.models.response.TableResponseEntity;
import vn.hbtplus.services.ConfigObjectAttributeService;
import vn.hbtplus.utils.ResponseUtils;

import javax.validation.Valid;

@RestController
@RequestMapping("")
@RequiredArgsConstructor
@Resource(value = Constant.RESOURCES.CONFIG_OBJECT_ATTRIBUTE)
public class ConfigObjectAttributeController {

    private final ConfigObjectAttributeService configObjectAttributeService;

    @GetMapping(value = "/v1/config-object-attribute", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public TableResponseEntity<ConfigObjectAttributeResponse.SearchResult> searchData(ConfigObjectAttributeRequest.SearchForm dto) {
        return ResponseUtils.ok(configObjectAttributeService.searchData(dto));
    }

    @PostMapping(value = "/v1/config-object-attribute", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.CREATE)
    public ResponseEntity saveData(@Valid @RequestBody ConfigObjectAttributeRequest.SubmitForm dto) throws BaseAppException {
        return ResponseUtils.ok(configObjectAttributeService.saveData(dto, null));
    }

    @PutMapping(value = "/v1/config-object-attribute/{id}", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.UPDATE)
    public ResponseEntity update(@Valid @RequestBody ConfigObjectAttributeRequest.SubmitForm dto, @PathVariable Long id) throws BaseAppException {
        return ResponseUtils.ok(configObjectAttributeService.saveData(dto, id));
    }

    @DeleteMapping(value = "/v1/config-object-attribute/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.DELETE)
    public ResponseEntity deleteData(@PathVariable Long id) throws BaseAppException {
        return ResponseUtils.ok(configObjectAttributeService.deleteData(id));
    }

    @GetMapping(value = "/v1/config-object-attribute/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public BaseResponseEntity<ConfigObjectAttributeResponse.DetailBean> getDataById(@PathVariable Long id) throws BaseAppException {
        return ResponseUtils.ok(configObjectAttributeService.getDataById(id));
    }

    @GetMapping(value = "/v1/config-object-attribute/get-by-table-name", produces = MediaType.APPLICATION_JSON_VALUE)
    public ListResponseEntity<AttributeConfigDto> getAttributes(@RequestParam String  tableName, @RequestParam(required = false) String functionCode) throws BaseAppException {
        return ResponseUtils.ok(configObjectAttributeService.getAttributes(tableName, functionCode));
    }

    @GetMapping(value = "/v1/config-object-attribute/list-table-data", produces = MediaType.APPLICATION_JSON_VALUE)
    public ListResponseEntity<ConfigObjectAttributeResponse.ListTableName> getListTableData() {
        return ResponseUtils.ok(configObjectAttributeService.getListTableData());
    }
}
