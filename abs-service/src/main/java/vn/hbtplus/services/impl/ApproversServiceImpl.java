package vn.hbtplus.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import vn.hbtplus.constants.Constant;
import vn.hbtplus.exceptions.BaseAppException;
import vn.hbtplus.feigns.AdminFeignClient;
import vn.hbtplus.models.dto.AdmUsersDTO;
import vn.hbtplus.models.dto.RejectRequestDTO;
import vn.hbtplus.models.request.RequestsRequest;
import vn.hbtplus.models.response.AdmUsersResponse;
import vn.hbtplus.repositories.entity.RequestApproversEntity;
import vn.hbtplus.repositories.entity.RequestsEntity;
import vn.hbtplus.repositories.impl.RequestsApproversRepository;
import vn.hbtplus.repositories.impl.RequestsRepository;
import vn.hbtplus.repositories.jpa.RequestsApproversJPA;
import vn.hbtplus.repositories.jpa.RequestsRepositoryJPA;
import vn.hbtplus.services.ApproversService;
import vn.hbtplus.utils.I18n;
import vn.hbtplus.utils.ResponseUtils;
import vn.hbtplus.utils.Utils;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ApproversServiceImpl implements ApproversService {

    private final RequestsRepositoryJPA requestsRepositoryJPA;
    private final RequestsApproversJPA requestsApproversJPA;
    private final RequestsApproversRepository requestsApproversRepository;
    private final RequestsRepository requestsRepository;
    private final AdminFeignClient adminFeignClient;
    private final HttpServletRequest request;

    @Override
    public ResponseEntity<Object> approveRequests(Long listId) {
        RequestsEntity entity = requestsRepositoryJPA.findById(listId)
                .orElseThrow(() -> new BaseAppException(I18n.getMessage("requests.notFound")));

        if (!Constant.REQUEST_STATUS.WAIT_APPROVE.equals(entity.getStatus())) {
            throw new BaseAppException(I18n.getMessage("requests.status.notAllowed"));
        }
        entity.setStatus(Constant.REQUEST_STATUS.APPROVED);
        entity.setModifiedTime(new Date());
        entity.setModifiedBy(Utils.getUserNameLogin());
        requestsRepositoryJPA.save(entity);

        return ResponseUtils.ok();
    }


    @Override
    public ResponseEntity<Object> rejectRequests(RejectRequestDTO rejectRequestDTO) {
        if (Utils.isNullOrEmpty(rejectRequestDTO.getListId())) {
            throw new BaseAppException(I18n.getMessage("listId.empty"));
        }
        String employeeCode = getEmployeeCodeByUserLogin(Utils.getUserNameLogin());
        Long empApproverId = requestsApproversRepository.getEmployeeIdByEmpCode(employeeCode);
        List<RequestsEntity> requestsToUpdate = new ArrayList<>();
        List<RequestApproversEntity> approversToUpdate = new ArrayList<>();

        for (Long requestId : rejectRequestDTO.getListId()) {
            RequestsEntity entity = requestsRepositoryJPA.findById(requestId)
                    .orElseThrow(() -> new BaseAppException(I18n.getMessage("requests.notFound")));
            if (!Constant.REQUEST_STATUS.WAIT_APPROVE.equals(entity.getStatus())) {
                throw new BaseAppException(I18n.getMessage("requests.status.notAllowed"));
            }
            RequestApproversEntity approver = requestsApproversJPA.findByRequestId(entity.getRequestId());
            if (Utils.isNullObject(approver)) {
                throw new BaseAppException(I18n.getMessage("requestsApprover.notFound"));
            }
            if (!approver.getEmpApproverId().equals(empApproverId)) {
                throw new BaseAppException(I18n.getMessage("employee.requests.approver"));
            }
            approver.setStatus(Constant.REQUEST_APPROVE_STATUS.REJECT);
            approversToUpdate.add(approver);
            entity.setStatus(Constant.REQUEST_STATUS.REJECT);
            requestsToUpdate.add(entity);
        }
        requestsApproversJPA.saveAll(approversToUpdate);
        requestsRepositoryJPA.saveAll(requestsToUpdate);

        return ResponseUtils.ok();
    }

    @Override
    public ResponseEntity approveAll() {
        List<RequestsEntity> listEntity =  requestsRepository.getListData();
        if (Utils.isNullOrEmpty(listEntity)) {
            throw new BaseAppException(I18n.getMessage("global.notFound"));
        }
        List<Long> ids = new ArrayList<>();
        for (RequestsEntity entity : listEntity) {
            this.approveRequests(entity.getRequestId());
            ids.add(entity.getRequestId());
        }

        return ResponseUtils.ok(ids);


    }


    public String getEmployeeCodeByUserLogin(String userName) {
        AdmUsersDTO dataResponse = adminFeignClient.getUserInfo(Utils.getRequestHeader(request), userName);
        AdmUsersDTO usersDTO = dataResponse;
        if (usersDTO == null) {
            throw new BaseAppException("USER_NOT_EXISTS");
        }
        return usersDTO.getEmployeeCode();
    }

}
