package vn.hbtplus.tax.personal.services.impl;

import lombok.RequiredArgsConstructor;
import org.apache.commons.beanutils.BeanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.exceptions.BaseAppException;
import vn.hbtplus.tax.personal.constants.Constant;
import vn.hbtplus.tax.personal.repositories.impl.EmployeeRepositoryImpl;
import vn.hbtplus.tax.personal.services.CommonUtilsService;
import vn.hbtplus.utils.ExportExcel;
import vn.hbtplus.utils.ResponseUtils;
import vn.hbtplus.utils.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CommonUtilsServiceImpl implements CommonUtilsService {
    private final EmployeeRepositoryImpl employeeRepository;
    private final Logger LOGGER = LoggerFactory.getLogger(CommonUtilsServiceImpl.class);

    @Override
    public List<Integer> getTaxValidStatusUpdate(boolean isAdmin) {
        List<Integer> result = new ArrayList<>();
        result.add(Constant.TAX_STATUS.DRAFT);
        result.add(Constant.TAX_STATUS.ACCOUNTANT_REJECT);
        result.add(Constant.TAX_STATUS.TAX_REJECT);
        result.add(Constant.TAX_STATUS.REGISTERED);
        result.add(Constant.TAX_STATUS.CONFIRMED);
        if (isAdmin) {
            result.add(Constant.TAX_STATUS.WAITING_APPROVAL);
            result.add(Constant.TAX_STATUS.ACCOUNTANT_PROCESSING);
            result.add(Constant.TAX_STATUS.ACCOUNTANT_RECEIVED);
        }
        return result;
    }

    @Override
    public List<Integer> getTaxValidStatusDelete() {
        List<Integer> result = new ArrayList<>();
        result.add(Constant.TAX_STATUS.DRAFT);
        result.add(Constant.TAX_STATUS.ACCOUNTANT_REJECT);
        result.add(Constant.TAX_STATUS.TAX_REJECT);
        result.add(Constant.TAX_STATUS.REGISTERED);
        result.add(Constant.TAX_STATUS.CONFIRMED);
        return result;
    }

    @Override
    public List<Integer> getValidStatusImportResult() {
        List<Integer> result = new ArrayList<>();
        result.add(Constant.TAX_STATUS.ACCOUNTANT_PROCESSING);
        result.add(Constant.TAX_STATUS.ACCOUNTANT_RECEIVED);
        return result;
    }

    @Override
    public List<Integer> getTaxStatusProcess() {
        List<Integer> result = new ArrayList<>();
        result.add(Constant.TAX_STATUS.DRAFT);
        result.add(Constant.TAX_STATUS.WAITING_APPROVAL);
        result.add(Constant.TAX_STATUS.ACCOUNTANT_RECEIVED);
        result.add(Constant.TAX_STATUS.ACCOUNTANT_PROCESSING);
        result.add(Constant.TAX_STATUS.REGISTERED);
        result.add(Constant.TAX_STATUS.CONFIRMED);
        return result;
    }


    @Override
    public Long getEmpIdLogin() {
        return employeeRepository.getEmployeeIdLogin();
    }

    @Override
    public Object validateWorkFlow(Class className, Long id, Integer statusInput, boolean isAdmin) {
        // validate statusInput duoc phep truyen vao
        if ((isAdmin && !this.getTaxValidStatusAdminWorkFlow().contains(statusInput))
                || (!isAdmin && !this.getTaxValidStatusPersonalWorkFlow().contains(statusInput))) {
            throw new BaseAppException("BAD_REQUEST");
        }

        Integer status = null;
        String isDeleted = null;
        Long employeeId = null;
        Object obj = employeeRepository.get(className, id);
        try {
            status = Integer.valueOf(BeanUtils.getProperty(obj, "status"));
            isDeleted = Utils.NVL(BeanUtils.getProperty(obj, "isDeleted"), BaseConstants.STATUS.NOT_DELETED);
            employeeId = Long.valueOf(BeanUtils.getProperty(obj, "employeeId"));
        } catch (Exception ex) {
            LOGGER.error("validateWorkFlow obj is null:{}", ex.getMessage());
        }
        if (status == null || employeeId == null || BaseConstants.STATUS.NOT_DELETED.equals(isDeleted)
                || (!getEmpIdLogin().equals(employeeId) && !isAdmin)) {
            throw new BaseAppException("BAD_REQUEST");
        }

        // validate trang thai hop le cua ban ghi
        if ((statusInput.equals(Constant.TAX_STATUS.WAITING_APPROVAL)
                && !status.equals(Constant.TAX_STATUS.DRAFT))//gui duyet thi trang thai ban ghi phai la khoi tao
                || (statusInput.equals(Constant.TAX_STATUS.DRAFT)
                && !status.equals(Constant.TAX_STATUS.WAITING_APPROVAL))// huy yeu cau thi trang thai phai la cho phe duyet
                || ((statusInput.equals(Constant.TAX_STATUS.ACCOUNTANT_RECEIVED) || statusInput.equals(Constant.TAX_STATUS.ACCOUNTANT_REJECT))
                && !status.equals(Constant.TAX_STATUS.WAITING_APPROVAL))// neu la phe duyet hoac tu choi thì ban ghi hien tai phai la cho phe duyet
        ) {
            throw new BaseAppException("BAD_REQUEST");
        }
        return obj;
    }


    public ResponseEntity<Object> processExport(ExportExcel dynamicExport, String fileName, List<Map<String, Object>> listData) {
        if (listData.isEmpty()) {
            return ResponseUtils.getResponseDataNotFound();
        }

        try {
            dynamicExport.replaceKeys(listData);
            String fileNameResponse = Utils.getFilePathExport(fileName);
            dynamicExport.exportFile(fileNameResponse);
            return ResponseUtils.getResponseFileEntity(fileNameResponse,false);
        } catch (Exception ex) {
            LOGGER.error("[exportData] has error {}", ex);
            return ResponseUtils.getResponseDataNotFound();
        }
    }

    private List<Integer> getTaxValidStatusAdminWorkFlow() {
        List<Integer> result = new ArrayList<>();
        result.add(Constant.TAX_STATUS.ACCOUNTANT_RECEIVED);
        result.add(Constant.TAX_STATUS.ACCOUNTANT_REJECT);
        return result;
    }

    private List<Integer> getTaxValidStatusPersonalWorkFlow() {
        List<Integer> result = new ArrayList<>();
        result.add(Constant.TAX_STATUS.DRAFT);
        result.add(Constant.TAX_STATUS.WAITING_APPROVAL);
        return result;
    }

}
