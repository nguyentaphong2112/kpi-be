
package vn.hbtplus.tax.personal.services;

import org.springframework.http.ResponseEntity;
import vn.hbtplus.utils.ExportExcel;

import java.util.List;
import java.util.Map;

/**
 *
 * @author tudd
 */
public interface CommonUtilsService {
    List<Integer> getTaxValidStatusUpdate(boolean isAdmin);

    List<Integer> getTaxValidStatusDelete();

    List<Integer> getValidStatusImportResult();

    List<Integer> getTaxStatusProcess();

    Long getEmpIdLogin();

    Object validateWorkFlow(Class className, Long id, Integer statusInput, boolean isAdmin);

    ResponseEntity<Object> processExport(ExportExcel dynamicExport, String fileName, List<Map<String, Object>> listData);
}
