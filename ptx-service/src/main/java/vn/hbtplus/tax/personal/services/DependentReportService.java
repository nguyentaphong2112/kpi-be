package vn.hbtplus.tax.personal.services;

import com.jxcell.CellException;
import org.springframework.http.ResponseEntity;
import vn.hbtplus.tax.personal.models.request.AdminSearchDTO;
import vn.hbtplus.tax.personal.models.response.DependentRegistersResponse;
import vn.hbtplus.models.response.TableResponseEntity;

public interface DependentReportService {

    TableResponseEntity<DependentRegistersResponse> searchByMonth(AdminSearchDTO dto);

    ResponseEntity<Object> exportByMonth(AdminSearchDTO dto) throws Exception;

    ResponseEntity<Object> exportGroupByMonth(AdminSearchDTO dto) throws CellException;
}
