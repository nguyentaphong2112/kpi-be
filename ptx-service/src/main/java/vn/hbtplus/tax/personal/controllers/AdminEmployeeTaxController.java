package vn.hbtplus.tax.personal.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vn.hbtplus.annotations.HasPermission;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.constants.Scope;
import vn.hbtplus.tax.personal.constants.Constant;
import vn.hbtplus.tax.personal.models.request.AdminSearchDTO;
import vn.hbtplus.tax.personal.models.response.EmployeeInfoResponse;
import vn.hbtplus.models.response.TableResponseEntity;
import vn.hbtplus.tax.personal.services.DeclarationRegistersService;
import vn.hbtplus.tax.personal.services.EmployeeTaxService;
import vn.hbtplus.tax.personal.services.InvoiceRequestsService;
import vn.hbtplus.utils.ResponseUtils;

@RestController
@RequestMapping(Constant.REQ_ADMIN_MAPPING_PREFIX)
@RequiredArgsConstructor
public class AdminEmployeeTaxController {

    private final EmployeeTaxService employeeTaxService;
    private final DeclarationRegistersService declarationRegistersService;
    private final InvoiceRequestsService invoiceRequestsService;

    @GetMapping(value = "/v1/admin/tax/search-tax-no", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public TableResponseEntity<EmployeeInfoResponse> searchData(AdminSearchDTO dto) {
        return employeeTaxService.searchData(dto);
    }

    @GetMapping(value = "/v1/admin/tax/export-employee", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity<Object> exportNewRegister(AdminSearchDTO dto) {
        return employeeTaxService.exportListEmployeeTax(dto);
    }

    @GetMapping(value = "/v1/admin/tax/job", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity<Object> runJob(@RequestParam(value = "type") String type) {
//        if ("REMIND_TAX".equalsIgnoreCase(type)) {
//            employeeTaxService.sendMailRemindTaxNumberRegister(true);
//        } else if ("REMIND_DECLARE".equalsIgnoreCase(type)) {
//            employeeTaxService.sendMailRemindDeclarationRegister();
//        } else if ("AUTO_REGISTER_DECLARE".equalsIgnoreCase(type)) {
//            declarationRegistersService.autoRegister();
//        } else if ("AUTO_REGISTER_INVOICE".equalsIgnoreCase(type)) {
//            invoiceRequestsService.autoRegister();
//        } else if ("SCAN_INVOICE_STATUS".equalsIgnoreCase(type)) {
//            invoiceRequestsService.scanInvoiceStatus();
//        }
        return ResponseUtils.ok();
    }
}
