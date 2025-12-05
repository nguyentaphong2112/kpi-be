package vn.hbtplus.services;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.multipart.MultipartFile;
import vn.hbtplus.exceptions.BaseAppException;
import vn.hbtplus.models.dto.WorkCalendarDetailsDTO;
import vn.hbtplus.models.request.TimekeepingsRequest;
import vn.hbtplus.models.response.BaseResponseEntity;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author ecoIt
 * @since 11/05/2022
 * @version 1.0
 */
public interface WorkCalendarDetailsService {

    List<WorkCalendarDetailsDTO> getSearchRequests(WorkCalendarDetailsDTO absWorkCalendarDetailsDTO);

    ResponseEntity<Object> importWorkScheduleWorkScheduleDetail(MultipartFile file, Long workCalendarId) throws Exception;

    BaseResponseEntity saveData(WorkCalendarDetailsDTO dto, Long id) throws BaseAppException;

    String getTemplateImportWorkScheduleDetail() throws Exception;

}
