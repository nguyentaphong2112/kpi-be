/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.services.impl;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.constants.Constant;
import vn.hbtplus.exceptions.BaseAppException;
import vn.hbtplus.exceptions.ErrorImportException;
import vn.hbtplus.exceptions.RecordNotExistsException;
import vn.hbtplus.feigns.FileStorageFeignClient;
import vn.hbtplus.feigns.ReportFeignClient;
import vn.hbtplus.models.BaseResponse;
import vn.hbtplus.models.ReportConfigDto;
import vn.hbtplus.models.dto.SalaryReviewPeriodDto;
import vn.hbtplus.models.request.SalaryReviewsRequest;
import vn.hbtplus.models.response.BaseResponseEntity;
import vn.hbtplus.models.response.SalaryReviewsResponse;
import vn.hbtplus.models.response.TableResponseEntity;
import vn.hbtplus.repositories.entity.InsuranceSalaryProcessEntity;
import vn.hbtplus.repositories.entity.SalaryReviewsEntity;
import vn.hbtplus.repositories.impl.SalaryReviewsRepository;
import vn.hbtplus.repositories.jpa.SalaryReviewsRepositoryJPA;
import vn.hbtplus.services.FileService;
import vn.hbtplus.services.ReportService;
import vn.hbtplus.services.SalaryReviewsService;
import vn.hbtplus.services.UtilsService;
import vn.hbtplus.utils.ExportExcel;
import vn.hbtplus.utils.ExportWorld;
import vn.hbtplus.utils.ImportExcel;
import vn.hbtplus.utils.ResponseUtils;
import vn.hbtplus.utils.Utils;
import vn.hbtplus.utils.ZipFileUtils;

import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Lop impl service ung voi bang hr_salary_reviews
 *
 * @author tudd
 * @version 1.0
 * @since 1.0
 */

@Service
@RequiredArgsConstructor
public class SalaryReviewsServiceImpl implements SalaryReviewsService {

    private final SalaryReviewsRepository salaryReviewsRepository;
    private final SalaryReviewsRepositoryJPA salaryReviewsRepositoryJPA;
    private final FileService fileService;
    private final HttpServletRequest request;
    private final ReportFeignClient reportFeignClient;
    private final FileStorageFeignClient fileStorageFeignClient;
    private final ReportService reportService;
    private final UtilsService utilsService;


    @Override
    @Transactional(readOnly = true)
    public TableResponseEntity<SalaryReviewsResponse.SearchResult> searchData(SalaryReviewsRequest.SearchForm dto) {
        return ResponseUtils.ok(salaryReviewsRepository.searchData(dto));
    }

    @Override
    @Transactional
    public ResponseEntity saveData(SalaryReviewsRequest.SubmitForm dto, Long id) throws BaseAppException {
        SalaryReviewsEntity entity;
        if (id != null && id > 0L) {
            entity = salaryReviewsRepositoryJPA.getById(id);
            entity.setModifiedTime(new Date());
            entity.setModifiedBy(Utils.getUserNameLogin());
        } else {
            entity = new SalaryReviewsEntity();
            entity.setCreatedTime(new Date());
            entity.setCreatedBy(Utils.getUserNameLogin());
        }
        Utils.copyProperties(dto, entity);
        entity.setIsDeleted(BaseConstants.STATUS.NOT_DELETED);
        salaryReviewsRepositoryJPA.save(entity);
        return ResponseUtils.ok(entity.getSalaryReviewId());
    }

    @Override
    @Transactional
    public ResponseEntity deleteData(Long id) throws RecordNotExistsException {
        Optional<SalaryReviewsEntity> optional = salaryReviewsRepositoryJPA.findById(id);
        if (!optional.isPresent() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new RecordNotExistsException(id, SalaryReviewsEntity.class);
        }
        salaryReviewsRepository.deActiveObject(SalaryReviewsEntity.class, id);
        return ResponseUtils.ok(id);
    }

    @Override
    @Transactional(readOnly = true)
    public BaseResponseEntity<SalaryReviewsResponse.SearchResult> getDataById(Long id) throws RecordNotExistsException {
        SalaryReviewsRequest.SearchForm dtoSearch = new SalaryReviewsRequest.SearchForm();
        dtoSearch.setSalaryReviewId(id);
        SalaryReviewsResponse.SearchResult dto = salaryReviewsRepository.findById(dtoSearch);
        if (dto == null || BaseConstants.STATUS.DELETED.equals(dto.getIsDeleted())) {
            throw new RecordNotExistsException(id, SalaryReviewsEntity.class);
        }
        return ResponseUtils.ok(dto);
    }

    @Override
    public ResponseEntity<Object> exportData(SalaryReviewsRequest.SearchForm dto) throws Exception {

        BaseResponse<ReportConfigDto> response = reportFeignClient.getReportConfig(
                Utils.getRequestHeader(request),
                "DANH_SACH_DE_XUAT_NANG_LUONG");
        ReportConfigDto reportConfigDto = response.getData();
        ExportExcel dynamicExport = utilsService.initExportExcelByFileId(reportConfigDto.getAttachmentFileList().get(0).getFileId(), 7);

        List<Map<String, Object>> listDataExport1 = salaryReviewsRepository.getListExport(reportConfigDto.getQueryResponseList().get(0).getSqlQuery(), dto, Arrays.asList(SalaryReviewsEntity.TYPES.TRUOC_HAN));
        dynamicExport.replaceKeys(listDataExport1);
        List<Map<String, Object>> listDataExport2 = salaryReviewsRepository.getListExport(reportConfigDto.getQueryResponseList().get(0).getSqlQuery(), dto, Arrays.asList(SalaryReviewsEntity.TYPES.THUONG_XUYEN));
        dynamicExport.replaceKeys(listDataExport2);
        List<Map<String, Object>> listDataExport3 = salaryReviewsRepository.getListExport(reportConfigDto.getQueryResponseList().get(0).getSqlQuery(), dto, Arrays.asList(SalaryReviewsEntity.TYPES.NANG_TNVK));
        dynamicExport.replaceKeys(listDataExport3);

        return ResponseUtils.ok(dynamicExport, "Danh sach de xuat nang luong.xlsx");
    }

    @Override
    public boolean makeList(String periodId) throws IllegalAccessException {
        SalaryReviewPeriodDto periodEntity = salaryReviewsRepository.getCategory(Constant.CATEGORY_CODES.HR_KY_NANG_LUONG, periodId);
        List<SalaryReviewsEntity> entities = salaryReviewsRepository.getListProposed(periodEntity, SalaryReviewsEntity.TYPES.THUONG_XUYEN);
        entities.addAll(salaryReviewsRepository.getListProposed(periodEntity, SalaryReviewsEntity.TYPES.TRUOC_HAN));
        entities.addAll(salaryReviewsRepository.getListProposed(periodEntity, SalaryReviewsEntity.TYPES.NANG_TNVK));
        List<SalaryReviewsEntity> listOldData = salaryReviewsRepository.findAllByProperties(SalaryReviewsEntity.class, "periodId", periodId);
        Map<Long, SalaryReviewsEntity> mapOldData = new HashMap();
        listOldData.forEach(item -> {
            if (item.isDeleted() ||
                !Arrays.asList(SalaryReviewsEntity.STATUS.DA_PHE_DUYET, SalaryReviewsEntity.STATUS.DA_KY).contains(item.getStatusId())) {
                mapOldData.put(item.getEmployeeId(), item);
            }
        });
        String userName = Utils.getUserNameLogin();
        entities.forEach(entity -> {
            SalaryReviewsEntity newEntity = mapOldData.get(entity.getEmployeeId());
            if (newEntity == null) {
                newEntity = new SalaryReviewsEntity();
                newEntity.setCreatedBy(userName);
                newEntity.setCreatedTime(new Date());
            } else {
                newEntity.setModifiedBy(userName);
                newEntity.setModifiedTime(new Date());
                mapOldData.remove(entity.getEmployeeId());
            }
            newEntity.setIsDeleted(BaseConstants.STATUS.NOT_DELETED);
            newEntity.setPeriodId(periodId);
            newEntity.setStatusId(SalaryReviewsEntity.STATUS.DU_THAO);
            newEntity.setType(entity.getType());
            newEntity.setApplyDate(entity.getApplyDate());
            newEntity.setAwardInfos(entity.getAwardInfos());
            newEntity.setPunishmentInfos(entity.getPunishmentInfos());
            newEntity.setIncrementDate(entity.getIncrementDate());
            newEntity.setApplyDate(entity.getApplyDate());
            newEntity.setProposedApplyDate(entity.getProposedApplyDate());
            newEntity.setOrganizationId(entity.getOrganizationId());
            newEntity.setJobId(entity.getJobId());
            newEntity.setPositionTitle(entity.getPositionTitle());
            newEntity.setReviewStatusId(SalaryReviewsEntity.REVIEW_STATUS.OK);
            newEntity.setEmployeeId(entity.getEmployeeId());
            newEntity.setSalaryRankId(entity.getSalaryRankId());
            newEntity.setSalaryGradeId(entity.getSalaryGradeId());
            newEntity.setProposedSalaryGradeId(entity.getProposedSalaryGradeId());

            salaryReviewsRepositoryJPA.save(newEntity);
        });

        //thuc hien inactive du lieu cu
        mapOldData.values().forEach(entity -> {
            if (!entity.isDeleted()) {
                entity.setModifiedBy(userName);
                entity.setModifiedTime(new Date());
                entity.setIsDeleted(BaseConstants.STATUS.DELETED);
                salaryReviewsRepositoryJPA.save(entity);
            }
        });

        return true;
    }

    @Override
    public boolean importData(String periodId, MultipartFile fileImport, List<MultipartFile> fileExtends) throws IOException {
        String fileConfigName = "BM_import_quyet_dinh_nang_luong.xml";
        ImportExcel importExcel = new ImportExcel("template/import/" + fileConfigName);
        List<Object[]> dataList = new ArrayList<>();
        if (importExcel.validateCommon(fileImport.getInputStream(), dataList)) {
            //Lay danh sach ma nhan vien
            List<String> empCodes = new ArrayList<>();
            List<Long> empIds = new ArrayList<>();
            dataList.forEach(objs -> {
                empCodes.add((String) objs[1]);
            });
            //Lay danh sach de xuat nang luong theo ma nhan vien
            List<SalaryReviewsEntity> listEntities = salaryReviewsRepository.getListSalaryReviewsEntityByEmpCode(periodId, empCodes);
            Map<String, SalaryReviewsEntity> mapEmps = new HashMap<>();
            listEntities.forEach(item -> {
                mapEmps.put(item.getEmployeeCode(), item);
                empIds.add(item.getEmployeeId());
            });

            List<InsuranceSalaryProcessEntity> listLastProcess = salaryReviewsRepository.getLastInsuranceSalaryProcess(empIds);
            Map<Long, InsuranceSalaryProcessEntity> mapLastProcess = new HashMap<>();
            listLastProcess.forEach(item -> {
                mapLastProcess.put(item.getEmployeeId(), item);
            });
            List<InsuranceSalaryProcessEntity> listNewProcess = new ArrayList<>();


            int row = 0;
            String userName = Utils.getUserNameLogin();
            List<Long> ids = new ArrayList<>();
            for (Object[] obj : dataList) {
                int col = 1;
                String empCode = (String) obj[col];
                if (mapEmps.get(empCode.toLowerCase()) == null) {
                    importExcel.addError(row, col, "Không có đề xuất nâng lương với mã nhân viên tương ứng", empCode);
                    continue;
                }
                SalaryReviewsEntity entity = mapEmps.get(empCode.toLowerCase());
                empIds.add(entity.getEmployeeId());
                if (!"OK".equalsIgnoreCase(entity.getReviewStatusId())) {
                    importExcel.addError(row, col, "Nhân viên không được đề xuất nâng lương", empCode);
                } else if (SalaryReviewsEntity.STATUS.DA_KY.equalsIgnoreCase(entity.getStatusId())) {
                    importExcel.addError(row, col, "Nhân viên đã import quyết định nâng lương", empCode);
                } else {
                    entity.setModifiedTime(new Date());
                    entity.setModifiedBy(userName);
                    entity.setStatusId(SalaryReviewsEntity.STATUS.DA_KY);
                    ids.add(entity.getSalaryReviewId());
                }
                InsuranceSalaryProcessEntity lastProcessEntity = mapLastProcess.get(entity.getEmployeeId());
                if (lastProcessEntity != null && lastProcessEntity.getStartDate().after(entity.getProposedApplyDate())) {
                    //bao loi da co du lieu ton tai
                    importExcel.addError(row, col, String.format("Tồn tại quá trình hệ số lương từ ngày %s sau ngày áp dụng của đề xuất", Utils.formatDate(lastProcessEntity.getStartDate())), empCode);
                }
                //Sinh them qua trinh he so luong moi
                if (lastProcessEntity == null || lastProcessEntity.getStartDate().before(entity.getProposedApplyDate())) {
                    InsuranceSalaryProcessEntity newProcessEntity = new InsuranceSalaryProcessEntity();
                    newProcessEntity.setStartDate(entity.getProposedApplyDate());
                    newProcessEntity.setEmployeeId(entity.getEmployeeId());
                    newProcessEntity.setIncrementDate(entity.getProposedApplyDate());
                    newProcessEntity.setSalaryRankId(entity.getSalaryRankId());
                    newProcessEntity.setSalaryGradeId(entity.getProposedSalaryGradeId());
                    newProcessEntity.setPercent(100l);
                    newProcessEntity.setEmpTypeId(lastProcessEntity.getEmpTypeId());
                    newProcessEntity.setDocumentNo((String) obj[3]);
                    newProcessEntity.setDocumentSignedDate((Date) obj[4]);
                    newProcessEntity.setCreatedBy(userName);
                    newProcessEntity.setCreatedTime(new Date());
                    listNewProcess.add(newProcessEntity);
                    if (lastProcessEntity != null) {
                        lastProcessEntity.setEndDate(DateUtils.addDays(entity.getProposedApplyDate(), -1));
                        lastProcessEntity.setModifiedTime(new Date());
                        lastProcessEntity.setModifiedBy(userName);
                    }
                } else {
                    if (lastProcessEntity.getStartDate().equals(entity.getProposedApplyDate())) {
                        lastProcessEntity.setDocumentNo((String) obj[3]);
                        lastProcessEntity.setDocumentSignedDate((Date) obj[4]);
                        lastProcessEntity.setModifiedBy(userName);
                        lastProcessEntity.setModifiedTime(new Date());
                    }
                }
            }

            if (importExcel.hasError()) {
                throw new ErrorImportException(fileImport, importExcel);
            } else {
                Map<String, List<MultipartFile>> mapFiles = ZipFileUtils.splitFileByEmpCode(fileExtends);

                //Thuc hien them moi qua trinh he so luong
                salaryReviewsRepository.updateBatch(InsuranceSalaryProcessEntity.class, listLastProcess, true);
                salaryReviewsRepository.insertBatch(InsuranceSalaryProcessEntity.class, listNewProcess, userName);

                //Thuc hien update du lieu bang de xuat nang luong
                salaryReviewsRepository.updateBatch(SalaryReviewsEntity.class, listEntities, true);

                //upload file
                listEntities.forEach(entity -> {
                    fileService.uploadFiles(mapFiles.get(entity.getEmployeeCode()), entity.getSalaryReviewId(), Constant.ATTACHMENT.TABLE_NAMES.HR_SALARY_REVIEWS, Constant.ATTACHMENT.FILE_TYPES.SALARY_REVIEW_DOCUMENT_SIGNED, Constant.ATTACHMENT.MODULE);
                    fileService.uploadFiles(mapFiles.get("all"), entity.getSalaryReviewId(), Constant.ATTACHMENT.TABLE_NAMES.HR_SALARY_REVIEWS, Constant.ATTACHMENT.FILE_TYPES.SALARY_REVIEW_DOCUMENT_SIGNED, Constant.ATTACHMENT.MODULE);
                });
                //dinh kem file vao qua trinh luong
                salaryReviewsRepository.insertAttachmentForProcess(ids);

            }
        } else {
            throw new ErrorImportException(fileImport, importExcel);
        }

        return true;
    }

    @Override
    public ExportWorld exportDataById(Long id) throws Exception {
        Map mapParams = new HashMap();
        mapParams.put("reviewId", id);
        SalaryReviewsEntity salaryReviewsEntity = salaryReviewsRepositoryJPA.getById(id);
        if (SalaryReviewsEntity.TYPES.TRUOC_HAN.equalsIgnoreCase(salaryReviewsEntity.getType())) {
            return reportService.exportWord("QUYET_DINH_NANG_LUONG_TRUOC_HAN", mapParams);
        } else if (SalaryReviewsEntity.TYPES.THUONG_XUYEN.equalsIgnoreCase(salaryReviewsEntity.getType())) {
            return reportService.exportWord("QUYET_DINH_NANG_LUONG_THUONG_XUYEN", mapParams);
        } else {
            return reportService.exportWord("QUYET_DINH_NANG_TNVK", mapParams);
        }
    }

    @Override
    public ResponseEntity<Object> downloadTemplate(String periodId) throws Exception {
        String pathTemplate = "template/import/BM_Import_Quyet_dinh_nang_luong.xlsx";
        ExportExcel dynamicExport = new ExportExcel(pathTemplate, 3, true);
        List<Map<String, Object>> listDataExport = salaryReviewsRepository.getListForImport(periodId);

        dynamicExport.replaceKeys(listDataExport);
        return ResponseUtils.ok(dynamicExport, "BM_Import_Quyet_dinh_nang_luong.xlsx", false);
    }

}
