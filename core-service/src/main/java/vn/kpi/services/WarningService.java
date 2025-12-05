package vn.kpi.services;

import org.springframework.http.ResponseEntity;
import vn.kpi.models.BaseSearchRequest;
import vn.kpi.models.WarningDto;
import vn.kpi.models.dto.WarningConfigDto;
import vn.kpi.models.response.TableResponseEntity;

public interface WarningService {

    WarningDto getWarning(Long id);

    TableResponseEntity<Object> searchDataPopUp(BaseSearchRequest dto);

    ResponseEntity<Object> exportData(Long id, WarningConfigDto dto) throws Exception;
}
