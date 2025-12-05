package vn.hbtplus.services;

import org.springframework.http.ResponseEntity;
import vn.hbtplus.models.BaseSearchRequest;
import vn.hbtplus.models.WarningDto;
import vn.hbtplus.models.dto.WarningConfigDto;
import vn.hbtplus.models.response.TableResponseEntity;

public interface WarningService {

    WarningDto getWarning(Long id);

    TableResponseEntity<Object> searchDataPopUp(BaseSearchRequest dto);

    ResponseEntity<Object> exportData(Long id, WarningConfigDto dto) throws Exception;
}
