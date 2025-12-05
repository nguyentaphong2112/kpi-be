package vn.hbtplus.tax.personal.services.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.hbtplus.models.response.TableResponseEntity;
import vn.hbtplus.tax.personal.models.request.AdminSearchDTO;
import vn.hbtplus.tax.personal.models.response.EmployeeInfoResponse;
import vn.hbtplus.tax.personal.repositories.impl.EmployeeRepositoryImpl;
import vn.hbtplus.tax.personal.repositories.impl.EmployeeTaxRepositoryImpl;
import vn.hbtplus.tax.personal.repositories.impl.LockRegistrationsRepositoryImpl;
import vn.hbtplus.tax.personal.repositories.impl.TaxNumberRegistersRepositoryImpl;
import vn.hbtplus.tax.personal.services.CommonUtilsService;
import vn.hbtplus.tax.personal.services.EmployeeTaxService;
import vn.hbtplus.utils.ExportExcel;
import vn.hbtplus.utils.ResponseUtils;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmployeeTaxServiceImpl implements EmployeeTaxService {
    private final EmployeeTaxRepositoryImpl employeeTaxRepository;
    private final CommonUtilsService commonUtilsService;
    private final EmployeeRepositoryImpl employeeRepository;
    private final LockRegistrationsRepositoryImpl lockRegistrationsRepository;
    private final TaxNumberRegistersRepositoryImpl taxNumberRegistersRepository;

    @Override
    @Transactional(readOnly = true)
    public TableResponseEntity<EmployeeInfoResponse> searchData(AdminSearchDTO dto) {
        return ResponseUtils.ok(employeeTaxRepository.searchData(dto));
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<Object> exportListEmployeeTax(AdminSearchDTO dto) {
        try {
            List<Map<String, Object>> listEmployee = employeeTaxRepository.getEmployeeTaxList(dto);
            if (listEmployee == null || listEmployee.isEmpty()) {
                return ResponseUtils.getResponseDataNotFound();
            }

            String pathTemplate = "template/export/taxNumber/BM_Xuat_DS_MST.xlsx";
            ExportExcel dynamicExport = new ExportExcel(pathTemplate, 2, true);
            dynamicExport.replaceKeys(listEmployee);
            return ResponseUtils.ok(dynamicExport,"BM_Xuat_DS_MST.xlsx");
        } catch (Exception ex) {
            log.error("[EmployeeTaxServiceImpl exportListEmployeeTax] has error {}", ex);
            return ResponseUtils.getResponseDataNotFound();
        }
    }
}
