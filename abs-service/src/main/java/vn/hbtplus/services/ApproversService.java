package vn.hbtplus.services;

import org.springframework.http.ResponseEntity;
import vn.hbtplus.models.dto.RejectRequestDTO;
import vn.hbtplus.models.request.RequestsRequest;

import java.util.List;

/**
 * Lop interface service ung voi bang abs_request_approvers
 * @author tudd
 * @since 1.0
 * @version 1.0
 */
public interface ApproversService {
    ResponseEntity<Object> approveRequests(Long listId);
    ResponseEntity<Object> rejectRequests(RejectRequestDTO rejectRequestDTO);
    ResponseEntity approveAll();
}
