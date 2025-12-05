package vn.hbtplus.tax.income.controllers;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.hbtplus.annotations.HasPermission;
import vn.hbtplus.annotations.Resource;
import vn.hbtplus.constants.Scope;
import vn.hbtplus.tax.income.constants.Constant;
import vn.hbtplus.tax.income.models.request.RIncomeTaxRequest;
import vn.hbtplus.tax.income.repositories.entity.TaxDeclareMastersEntity;
import vn.hbtplus.tax.income.services.RIncomeTaxService;
import vn.hbtplus.tax.income.services.TaxDeclareMastersService;
import vn.hbtplus.utils.ResponseUtils;
import vn.hbtplus.utils.Utils;

@RestController
@RequestMapping(Constant.REQUEST_MAPPING_PREFIX)
@RequiredArgsConstructor
@Resource(value = Constant.RESOURCE.PIT_TAX_COMMITMENTS)
public class RIncomeTaxController {
    private final RIncomeTaxService rIncomeTaxService;
    private final TaxDeclareMastersService taxDeclareMastersService;

    @GetMapping(value = "/v1/tax-reports/detail/{reportType}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity reportByPeriod(
            RIncomeTaxRequest.ReportByIdForm reportForm,
            @Schema(description = "Type of the tax report", allowableValues = "BC_DS_GTGC,BC_THTN_THEO_NV, BC_THTN_THEO_DV, BC_THTN_THEO_KHOAN_TN,BC_THTN_THEO_KHOAN_TAI_CHINH")
            @PathVariable String reportType) throws Exception {
        if ("BC_DS_GTGC".equalsIgnoreCase(reportType)) {
            return rIncomeTaxService.exportDependentPersons(reportForm);
        } else if ("BC_THTN_THEO_NV_KHOAN_TN".equalsIgnoreCase(reportType)) {
            return rIncomeTaxService.exportPersonalIncomeByIncomeItem(reportForm);
        } else if ("BC_THTN_THEO_NV".equalsIgnoreCase(reportType)) {
            return rIncomeTaxService.exportPersonalIncome(reportForm);
        } else if ("BC_THTN_THEO_DV".equalsIgnoreCase(reportType)) {
            return rIncomeTaxService.exportOrgIncome(reportForm);
        } else if ("BC_THTN_THEO_KHOAN_TN".equalsIgnoreCase(reportType)) {
            return rIncomeTaxService.exportIncomeItem(reportForm);
        } else if ("BC_THTN_THEO_KHOAN_TAI_CHINH".equalsIgnoreCase(reportType)) {
            return rIncomeTaxService.exportIncomeItemAccounting(reportForm);
        } else if ("BC_THTN_THEO_DV_KHOAN_TN".equalsIgnoreCase(reportType)) {
            return rIncomeTaxService.exportIncomeItemAndOrg(reportForm);
        } else if ("BC_DOI_CHIEU_TAI_CHINH".equalsIgnoreCase(reportType)) {
            return rIncomeTaxService.exportIncomeItemForChecking(reportForm);
        } else if ("BC_CHI_TIET_KK02".equalsIgnoreCase(reportType)
                || "BC_TO_KHAI_XML".equalsIgnoreCase(reportType)
                || "BC_PHAN_BO_THUE".equalsIgnoreCase(reportType)
        ) {
            TaxDeclareMastersEntity declareMaster = taxDeclareMastersService.getTaxDeclareMaster(Utils.getLastDay(reportForm.getStartDate()), reportForm.getInputType());
            if (declareMaster == null) {
                return ResponseUtils.getResponseDataNotFound();
            } else {
                if ("BC_CHI_TIET_KK02".equalsIgnoreCase(reportType)) {
                    return taxDeclareMastersService.exportDetailKK02(declareMaster.getTaxDeclareMasterId(), reportForm.getOrgIds());
                } else if ("BC_TO_KHAI_XML".equalsIgnoreCase(reportType)){
                    return taxDeclareMastersService.exportXml(declareMaster.getTaxDeclareMasterId(), reportForm.getOrgIds());
                } else {
                    return taxDeclareMastersService.exportTaxAllocation(declareMaster.getTaxDeclareMasterId(), reportForm.getOrgIds());
                }
            }
        }
        return null;
    }
}
