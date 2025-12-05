package vn.hbtplus.tax.income.services.impl;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.tax.income.constants.Constant;
import vn.hbtplus.exceptions.BaseAppException;
import vn.hbtplus.exceptions.ErrorImportException;
import vn.hbtplus.exceptions.RecordNotExistsException;
import vn.hbtplus.feigns.FileStorageFeignClient;
import vn.hbtplus.models.AttachmentFileDto;
import vn.hbtplus.models.BaseDataTableDto;
import vn.hbtplus.tax.income.models.EmployeeDto;
import vn.hbtplus.tax.income.models.dto.ContractProcessDTO;
import vn.hbtplus.tax.income.models.request.TaxCommitmentRequest;
import vn.hbtplus.tax.income.models.response.AttachmentFileResponse;
import vn.hbtplus.tax.income.models.response.TaxCommitmentResponse;
import vn.hbtplus.tax.income.repositories.entity.TaxCommitmentEntity;
import vn.hbtplus.tax.income.repositories.impl.EmployeeRepository;
import vn.hbtplus.tax.income.repositories.impl.IncomeItemMastersRepository;
import vn.hbtplus.tax.income.repositories.impl.TaxCommitmentRepository;
import vn.hbtplus.tax.income.repositories.jpa.TaxCommitmentRepositoryJPA;
import vn.hbtplus.services.AttachmentService;
import vn.hbtplus.tax.income.services.FileService;
import vn.hbtplus.tax.income.services.TaxCommitmentService;
import vn.hbtplus.utils.*;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaxCommitmentServiceImpl implements TaxCommitmentService {

    private final TaxCommitmentRepository taxCommitmentRepository;
    private final TaxCommitmentRepositoryJPA taxCommitmentRepositoryJPA;
    private final EmployeeRepository employeeRepository;
    private final FileStorageFeignClient storageFeignClient;
    private final AttachmentService attachmentService;
    private final HttpServletRequest request;
    private final FileService fileService;
    private final IncomeItemMastersRepository incomeItemMastersRepository;


    static final String FORMAT_DATE = "MM/yyyy";

    @Override
    @Transactional(readOnly = true)
    public BaseDataTableDto<TaxCommitmentResponse> searchData(TaxCommitmentRequest.SearchForm dto) {
        BaseDataTableDto<TaxCommitmentResponse> resultList = taxCommitmentRepository.searchData(dto);

        List<Long> taxCommitmentIds = resultList.getListData().stream().map(TaxCommitmentResponse::getTaxCommitmentId).collect(Collectors.toList());
        if (!Utils.isNullOrEmpty(taxCommitmentIds.toString())) {
            Map<Long, List<AttachmentFileResponse>> mapAttachmentFile = getMapFileAttachment(taxCommitmentIds, Constant.FILE_TYPE.FILE_TAX_COMMITMENT);
            resultList.getListData().forEach(item -> item.setAttachmentList(mapAttachmentFile.get(item.getTaxCommitmentId())));
        }

        return resultList;
    }

    private Map<Long, List<AttachmentFileResponse>> getMapFileAttachment(List<Long> taxCommentIds, Long type) {
        List<AttachmentFileDto> dtoList = attachmentService.getAttachmentList("", type.toString(), taxCommentIds);

        if (dtoList == null || dtoList.isEmpty()) {
            return Collections.emptyMap();
        }
        List<AttachmentFileResponse> responseList = dtoList.stream()
                .map(dto -> {
                    AttachmentFileResponse response = new AttachmentFileResponse();
                    Utils.copyProperties(dto, response);
                    return response;
                })
                .collect(Collectors.toList());

        return responseList.stream().collect(Collectors.groupingBy(AttachmentFileResponse::getObjectId));
    }

    @Override
    @Transactional
    public Long deleteById(Long id) throws Exception {
        Optional<TaxCommitmentEntity> optional = taxCommitmentRepositoryJPA.findById(id);
        if (!optional.isPresent() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new RecordNotExistsException(id, TaxCommitmentEntity.class);
        }
        taxCommitmentRepository.deActiveObject(TaxCommitmentEntity.class, id);

//        attachmentService.deleteFileByObjIdAndType(Utils.getHeader(), id, Constant.FILE_TYPE.FILE_TAX_COMMITMENT);
        return id;
    }

    @Override
    @Transactional
    public Long saveTaxCommitment(TaxCommitmentRequest.UpdateForm form) throws Exception {
        TaxCommitmentEntity entity;
        if (form.getTaxCommitmentId() == null) {
            entity = new TaxCommitmentEntity();
        } else {
            Optional<TaxCommitmentEntity> optional = taxCommitmentRepositoryJPA.findById(form.getTaxCommitmentId());
            if (optional.isEmpty()) {
                throw new RecordNotExistsException(form.getTaxCommitmentId(), TaxCommitmentEntity.class);
            }
            entity = optional.get();
            if (entity.isDeleted()) {
                throw new RecordNotExistsException(form.getTaxCommitmentId(), TaxCommitmentEntity.class);
            }

        }

//        ContractProcessDTO contractProcessDTO = incomeItemMastersRepository.getEmployeeByCode(item.getEmpCode());

        if (!Utils.isNullObject(form.getEndDate())){
            int totalMonth = Utils.calculateMonthsBetween(form.getStartDate(), form.getEndDate());
            Long incomeAmount = form.getIncomeAmount();

            Long incomePerMonth = 0L;
            if (totalMonth > 0 && incomeAmount != null) {
                incomePerMonth = incomeAmount / totalMonth;
            }
            if(incomePerMonth >= 11000000L){
                throw new BaseAppException("Số tiền cam kết thu nhập mỗi tháng phải nhỏ hơn hoặc bằng 11 triệu đồng");
            }
        }

        List<EmployeeDto> listSeniority = taxCommitmentRepository.getListSeniority(List.of(form.getEmployeeId()));
        if (!Utils.isNullObject(listSeniority) && listSeniority.get(0).getSeniority() >= 12D) {
            throw new BaseAppException("Nhân viên đã có hợp đồng lao động hơn 12 tháng không thể đăng ký cam kết thu nhập");
        }

        List<TaxCommitmentResponse> list = taxCommitmentRepository.getEmployeeConflictDate(form);
        if (!Utils.isNullOrEmpty(list)) {
            String messageEndDate = Utils.formatDate(form.getEndDate(), FORMAT_DATE) == null ? "hiện tại" : Utils.formatDate(form.getEndDate(), FORMAT_DATE);
            throw new BaseAppException(String.format("Nhân viên đã tồn tại cam kết thu nhập trong khoảng thời gian từ kỳ %s đến kỳ %s",
                    Utils.formatDate(form.getStartDate(), FORMAT_DATE), messageEndDate));
        }
        Utils.copyProperties(form, entity);
        taxCommitmentRepositoryJPA.save(entity);
        fileService.deActiveFileByAttachmentId(form.getAttachmentDeleteIds(), Constant.ATTACHMENT.TABLE_NAMES.PIT_TAX_COMMITMENTS, Constant.ATTACHMENT.FILE_TYPES.PIT_TAX_COMMITMENTS_FILE_TEMPLATE);
        fileService.uploadFiles(form.getFileList(), entity.getTaxCommitmentId(), Constant.ATTACHMENT.TABLE_NAMES.PIT_TAX_COMMITMENTS, Constant.ATTACHMENT.FILE_TYPES.PIT_TAX_COMMITMENTS_FILE_TEMPLATE, Constant.ATTACHMENT.MODULE);
        return entity.getTaxCommitmentId();
    }

    @Override
    @Transactional(readOnly = true)
    public TaxCommitmentResponse getTaxCommitmentById(Long id) throws Exception {
        Optional<TaxCommitmentEntity> optional = taxCommitmentRepositoryJPA.findById(id);
        if (optional.isEmpty()) {
            throw new RecordNotExistsException(id, TaxCommitmentEntity.class);
        }

        TaxCommitmentEntity entity = optional.get();
        if (entity.isDeleted()) {
            throw new RecordNotExistsException(id, TaxCommitmentEntity.class);
        }
        TaxCommitmentResponse response = new TaxCommitmentResponse();
        Utils.copyProperties(entity, response);
        response.setAttachFileList(attachmentService.getAttachmentEntities(Constant.ATTACHMENT.TABLE_NAMES.PIT_TAX_COMMITMENTS, Constant.ATTACHMENT.FILE_TYPES.PIT_TAX_COMMITMENTS_FILE_TEMPLATE, id));
        return response;
    }

    @Override
    @Transactional
    public void importTaxCommitment(MultipartFile fileImport) throws Exception {
        ImportExcel importExcel = new ImportExcel("template/import/tax/BM_Import_cam_ket_thu_nhap.xml");
        List<Object[]> dataList = new ArrayList<>();
        List<TaxCommitmentEntity> entityList = new ArrayList<>();
        String createdBy = Utils.getUserNameLogin();
        if (importExcel.validateCommon(fileImport.getInputStream(), dataList)) {
            List<String> empCodes = getEmpCodes(dataList);
            List<EmployeeDto> employeeList = employeeRepository.getEmployeeDtos(empCodes);
            Map<String, EmployeeDto> mapEmployee = new HashMap<>();
            employeeList.forEach(item -> mapEmployee.put(item.getEmployeeCode(), item));

            Map<String, List<TaxCommitmentResponse>> mapTaxCommitment = taxCommitmentRepository.getTaxCommitmentByEmpCodes(empCodes);
            int row = 0;
            int col;
            for (Object[] obj : dataList) {
                col = 1;
                String empCode = (String) obj[col];
                empCode = StringUtils.trimToEmpty(empCode);

                if (mapEmployee.get(empCode.toUpperCase()) == null) {
                    importExcel.addError(row, col, I18n.getMessage("global.validate.employeeCode"), empCode);
                } else {
                    if (mapEmployee.get(empCode.toUpperCase()).getSeniority() >= 12D) {
                        importExcel.addError(row, col, I18n.getMessage("Nhân viên đã có hợp đồng lao động hơn 12 tháng không thể đăng ký cam kết thu nhập"), empCode);
                    }
                }
                col++;

                String fullName = (String) obj[col];
                fullName = StringUtils.trimToEmpty(fullName);

                if (mapEmployee.get(empCode.toUpperCase()) != null && !StringUtils.equalsIgnoreCase(fullName, mapEmployee.get(empCode.toUpperCase()).getFullName())) {
                    importExcel.addError(row, col, I18n.getMessage("global.validate.fullName"), fullName);
                }
                col++;

                TaxCommitmentEntity entity = new TaxCommitmentEntity();
                EmployeeDto employeeDto = mapEmployee.get(empCode.toUpperCase());
                if (employeeDto != null) {
                    entity.setEmployeeId(employeeDto.getEmployeeId());
                }

                Long incomeAmount = (Long) obj[col];
                if (incomeAmount < 0L) {
                    importExcel.addError(row, col, I18n.getMessage("global.validate.commit.income.amount"), String.valueOf(incomeAmount));
                }
                entity.setIncomeAmount(incomeAmount);
                col++;

                String inputStartDate = (String) obj[col];
                inputStartDate = "01/" + inputStartDate;

                Date startDate = Utils.stringToDate(inputStartDate);
                if (startDate == null) {
                    importExcel.addError(row, col, I18n.getMessage("global.validate.startDate"), (String) obj[col]);
                }
                entity.setStartDate(startDate);
                col++;

                String inputEndDate = (String) obj[col];
                if (StringUtils.isNotBlank(inputEndDate)) {
                    Date endDate = Utils.stringToDate("01/" + inputEndDate);
                    if (endDate == null) {
                        importExcel.addError(row, col, I18n.getMessage("global.validate.endDate"), inputEndDate);
                    } else if (startDate.after(endDate)) {
                        importExcel.addError(row, col, I18n.getMessage("global.validate.startDate.greater.than.endDate"), inputEndDate);
                    } else {
                        int totalMonth = Utils.calculateMonthsBetween(startDate, endDate);

                        Long incomePerMonth = 0L;
                        if (totalMonth > 0) {
                            incomePerMonth = incomeAmount / totalMonth;
                        }
                        if(incomePerMonth >= 11000000L){
                            throw new BaseAppException("Số tiền cam kết thu nhập mỗi tháng phải nhỏ hơn hoặc bằng 11 triệu đồng");
                        }
                    }

                    entity.setEndDate(endDate);
                }

                col++;
                entity.setDescription((String) obj[col]);

                List<TaxCommitmentResponse> taxCommitmentList = mapTaxCommitment.get(StringUtils.lowerCase(empCode));
                if (isConflict(taxCommitmentList, startDate, entity.getEndDate())) {
                    importExcel.addError(row, 1, String.format("Nhân viên đã tồn tại cam kết thu nhập trong khoảng thời gian từ kỳ %s đến %s",
                            Utils.formatDate(startDate, FORMAT_DATE), StringUtils.isBlank(inputEndDate) ? "hiện tại" : "kỳ " + inputEndDate), empCode);
                }

                entity.setCreatedBy(Utils.getUserNameLogin());
                entity.setCreatedTime(new Date());
                entityList.add(entity);
                row++;
            }

            if (importExcel.hasError()) {
                throw new ErrorImportException(fileImport, importExcel);
            } else {
                taxCommitmentRepository.insertBatch(TaxCommitmentEntity.class, entityList, createdBy);
            }
        } else {
            throw new ErrorImportException(fileImport, importExcel);
        }
    }

    private boolean isConflict(List<TaxCommitmentResponse> taxCommitmentList, Date startDate, Date endDate) {
        if (Utils.isNullOrEmpty(taxCommitmentList)) {
            return false;
        }

        for (TaxCommitmentResponse tax : taxCommitmentList) {
            if (Utils.compareDateLessThanOrEqual(startDate, tax.getEndDate(), true)
                    && Utils.compareDateLessThanOrEqual(tax.getStartDate(), endDate, true)) {
                return true;
            }
        }

        return false;
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<Object> exportData(TaxCommitmentRequest.SearchForm dto) throws Exception {
        String pathTemplate = "template/export/tax/danh-sach-cam-ket-thu-nhap.xlsx";
        ExportExcel dynamicExport = new ExportExcel(pathTemplate, 2, true);
        List<Map<String, Object>> listDataExport = taxCommitmentRepository.getDataExportByCondition(dto);
        dynamicExport.replaceKeys(listDataExport);
        return ResponseUtils.ok(dynamicExport, "danh-sach-cam-ket-thu-nhap.xlsx");
    }

    @Override
    public ResponseEntity<Object> downloadTemplate() throws Exception {
        String pathTemplate = "template/import/tax/BM_Import_cam_ket_thu_nhap.xlsx";
        ExportExcel dynamicExport = new ExportExcel(pathTemplate, 2, true);
        return ResponseUtils.ok(dynamicExport, "BM_Import_cam_ket_thu_nhap.xlsx", false);
    }

    private List<String> getEmpCodes(List<Object[]> dataList) {
        List<String> empCodes = new ArrayList<>();
        for (Object[] obj : dataList) {
            String empCode = (String) obj[1];
            if (!Utils.isNullOrEmpty(empCode)) {
                if (!empCodes.contains(empCode.toUpperCase())) {
                    empCodes.add(empCode.trim().toUpperCase());
                }
            }
        }
        return empCodes;
    }
}
