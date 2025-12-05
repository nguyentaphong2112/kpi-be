package vn.hbtplus.tax.income.services;

import org.springframework.http.ResponseEntity;
import vn.hbtplus.tax.income.models.request.RIncomeTaxRequest;

public interface RIncomeTaxService {

    ResponseEntity exportPersonalIncome(RIncomeTaxRequest.ReportByIdForm reportForm) throws Exception;

    ResponseEntity exportOrgIncome(RIncomeTaxRequest.ReportByIdForm reportForm) throws Exception;

    ResponseEntity exportIncomeItem(RIncomeTaxRequest.ReportByIdForm reportForm) throws Exception;
    ResponseEntity exportIncomeItemAccounting(RIncomeTaxRequest.ReportByIdForm reportForm) throws Exception;

    ResponseEntity exportIncomeItemAndOrg(RIncomeTaxRequest.ReportByIdForm reportForm) throws Exception;

    ResponseEntity exportDependentPersons(RIncomeTaxRequest.ReportByIdForm reportForm) throws Exception;

    ResponseEntity exportPersonalIncomeByIncomeItem(RIncomeTaxRequest.ReportByIdForm reportForm) throws Exception;

    ResponseEntity exportIncomeItemForChecking(RIncomeTaxRequest.ReportByIdForm reportForm) throws Exception;
}
