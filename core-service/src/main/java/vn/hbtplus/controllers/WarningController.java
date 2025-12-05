package vn.hbtplus.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.hbtplus.annotations.HasPermission;
import vn.hbtplus.constants.Scope;
import vn.hbtplus.models.BaseSearchRequest;
import vn.hbtplus.models.dto.WarningConfigDto;
import vn.hbtplus.models.response.TableResponseEntity;
import vn.hbtplus.services.WarningService;
import vn.hbtplus.utils.ResponseUtils;

@RestController
@RequestMapping("")
@RequiredArgsConstructor
@Slf4j
public class WarningController {
    private final WarningService warningService;


    @GetMapping(value = "/v1/warning/count/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity getWarning(@PathVariable Long id) {
        return ResponseUtils.ok(warningService.getWarning(id));
    }

    @GetMapping(value = "/v1/warning-configs/pop-up", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public TableResponseEntity<Object> searchDataPopUp(BaseSearchRequest dto) {
        return warningService.searchDataPopUp(dto);
    }

    @GetMapping(value = "/v1/warning/export/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity<Object> exportData(@PathVariable Long id, WarningConfigDto dto) throws Exception {
        return warningService.exportData(id, dto);
    }
}
