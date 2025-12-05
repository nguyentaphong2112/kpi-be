package vn.hbtplus.insurance.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import vn.hbtplus.annotations.HasPermission;
import vn.hbtplus.annotations.Resource;
import vn.hbtplus.insurance.constants.Constant;
import vn.hbtplus.constants.Scope;
import vn.hbtplus.insurance.services.PositionGroupMappingService;
import vn.hbtplus.utils.ResponseUtils;

@RestController
@RequestMapping(Constant.REQUEST_MAPPING_PREFIX)
@RequiredArgsConstructor
@Resource(value = Constant.RESOURCE.ICN_CONFIG_PARAMETER)
public class PositionGroupMappingController {
    private final PositionGroupMappingService service;

    @GetMapping(value = "/v1/position-group-mapping/download-template", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity<Object> downloadTemplate() throws Exception {
        return service.downloadTemplate();
    }

    @PostMapping(value = "/v1/position-group-mapping/import", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.IMPORT)
    public ResponseEntity processImport(@RequestPart MultipartFile fileImport) throws Exception {
        service.processImport(fileImport);
        return ResponseUtils.ok();
    }
}
