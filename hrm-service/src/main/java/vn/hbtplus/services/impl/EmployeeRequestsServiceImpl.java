package vn.hbtplus.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.exceptions.BaseAppException;
import vn.hbtplus.models.response.EmployeeRequestsResponse;
import vn.hbtplus.repositories.entity.*;
import vn.hbtplus.repositories.impl.EmployeeRequestsRepository;
import vn.hbtplus.repositories.impl.EmployeesRepository;
import vn.hbtplus.repositories.impl.WorkProcessRepository;
import vn.hbtplus.repositories.jpa.EmployeeRequestsRepositoryJPA;
import vn.hbtplus.services.EmployeeRequestsService;
import vn.hbtplus.utils.Utils;

import java.util.Date;

@Service
@RequiredArgsConstructor
public class EmployeeRequestsServiceImpl implements EmployeeRequestsService {
    private final EmployeeRequestsRepository employeeRequestsRepository;
    private final EmployeesRepository employeesRepository;
    private final WorkProcessRepository workProcessRepository;
    private final EmployeeRequestsRepositoryJPA employeeRequestsRepositoryJPA;

    @Override
    public EmployeeRequestsResponse getActiveRequest(String requestType, Long employeeId) {
        return employeeRequestsRepository.getActiveRequest(requestType, employeeId);
    }

    @Override
    @Transactional
    public boolean updateStatus(String requestType, Long employeeId, Long id) throws BaseAppException {
        EmployeeRequestsEntity entity = employeeRequestsRepositoryJPA.getById(id);
        if (!entity.getRequestType().equalsIgnoreCase(requestType)
            && !employeeId.equals(entity.getEmployeeId())
        ) {
            throw new BaseAppException("id invalid!");
        }
        //validate thong tin theo request type
        validate(requestType, employeeId, id);

        entity.setStatus("DA_XAC_NHAN");
        entity.setModifiedBy(Utils.getUserNameLogin());
        entity.setModifiedTime(new Date());
        employeeRequestsRepositoryJPA.save(entity);
        return true;
    }

    @Override
    public boolean validate(String requestType, Long employeeId, Long id) {
        String validate = employeeRequestsRepository.validate(requestType, employeeId, id);
        if (!Utils.isNullOrEmpty(validate)) {
            throw new BaseAppException("Bạn chưa cập nhật đầy đủ thông tin: " + validate);
        } else {
            return true;
        }
    }
}
