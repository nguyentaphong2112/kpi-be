package vn.hbtplus.tax.income.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.hbtplus.annotations.HasPermission;
import vn.hbtplus.annotations.Resource;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.constants.Scope;
import vn.hbtplus.models.response.ListResponseEntity;
import vn.hbtplus.tax.income.constants.Constant;
import vn.hbtplus.tax.income.models.response.IncomeTemplatesResponse;
import vn.hbtplus.tax.income.services.IncomeTemplateService;
import vn.hbtplus.utils.ResponseUtils;

@RestController
@RequestMapping(Constant.REQUEST_MAPPING_PREFIX)
@RequiredArgsConstructor
@Resource(value = Constant.RESOURCE.PIT_INCOME_ITEM_TEMPLATE)
public class IncomeTemplateController {

    private final IncomeTemplateService incomeTemplateService;

    @GetMapping(value = "/v1/income-template/get-all", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ListResponseEntity<IncomeTemplatesResponse> getAll() {
        return ResponseUtils.ok(incomeTemplateService.getAll());
    }
}
