package vn.hbtplus.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.hbtplus.annotations.Resource;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.constants.Constant;
import vn.hbtplus.models.request.ShudRequest;
import vn.hbtplus.models.response.BaseResponseEntity;
import vn.hbtplus.services.ShudService;

@RestController
@RequestMapping(BaseConstants.REQUEST_MAPPING_PREFIX)
@RequiredArgsConstructor
@Resource(value = Constant.RESOURCES.EMPLOYEE)
public class ShudController {

    private final ShudService shudService;

    @PostMapping(value = "/v1/shud/export-adult", produces = MediaType.APPLICATION_JSON_VALUE)
    public BaseResponseEntity<String> exportData(@RequestBody ShudRequest.ExportForm dto) throws Exception {
        return shudService.exportData(dto);
    }
}
