package vn.hbtplus.tax.personal.services;

import org.springframework.http.ResponseEntity;
import vn.hbtplus.tax.personal.models.request.AdminSearchDTO;
import vn.hbtplus.tax.personal.models.response.EmployeeInfoResponse;
import vn.hbtplus.models.response.TableResponseEntity;

public interface EmployeeTaxService {
    TableResponseEntity<EmployeeInfoResponse> searchData(AdminSearchDTO dto);
    ResponseEntity<Object> exportListEmployeeTax(AdminSearchDTO dto);
}
