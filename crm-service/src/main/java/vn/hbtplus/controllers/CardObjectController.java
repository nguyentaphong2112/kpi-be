package vn.hbtplus.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vn.hbtplus.annotations.HasPermission;
import vn.hbtplus.annotations.Resource;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.constants.Constant;
import vn.hbtplus.constants.Scope;
import vn.hbtplus.models.request.CardObjectRequest;
import vn.hbtplus.models.request.PartnersRequest;
import vn.hbtplus.models.response.CardObjectResponse;
import vn.hbtplus.models.response.ListResponseEntity;
import vn.hbtplus.models.response.TableResponseEntity;
import vn.hbtplus.services.CardObjectService;
import vn.hbtplus.utils.ResponseUtils;

@RestController
@RequestMapping(BaseConstants.REQUEST_MAPPING_PREFIX)
@RequiredArgsConstructor
@Resource(value = Constant.RESOURCES.CRM_CUSTOMERS)
public class CardObjectController {
    private final CardObjectService cardObjectService;

    @GetMapping(value = "/v1/card-objects", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public TableResponseEntity<CardObjectResponse.SearchResult> searchData(CardObjectRequest.SearchForm dto) {
        return cardObjectService.searchData(dto);
    }

    @GetMapping(value = "/v1/card-objects/export", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity<Object> exportData(CardObjectRequest.SearchForm dto) throws Exception {
        return cardObjectService.exportData(dto);
    }

    @GetMapping(value = "/v1/card-objects/list", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ListResponseEntity<CardObjectResponse.DetailBean> getListObject(@RequestParam(required = false) String objType,
                                                                           @RequestParam(required = false) Long objId) {
        return ResponseUtils.ok(cardObjectService.getListObject(objType, objId));
    }

    @PostMapping(value = "/v1/card-objects/export-card", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity<Object> exportCard(@RequestBody PartnersRequest.PrintCard dto) throws Exception {
        return cardObjectService.exportCard(dto);
    }
}
