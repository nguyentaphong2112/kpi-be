/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.kpi.services.impl;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import vn.kpi.configs.MdcForkJoinPool;
import vn.kpi.constants.BaseConstants;
import vn.kpi.constants.Constant;
import vn.kpi.exceptions.BaseAppException;
import vn.kpi.exceptions.ErrorImportException;
import vn.kpi.exceptions.RecordNotExistsException;
import vn.kpi.models.BaseDataTableDto;
import vn.kpi.models.dto.EmployeeDto;
import vn.kpi.models.dto.OrganizationDto;
import vn.kpi.models.request.OrganizationsRequest;
import vn.kpi.models.response.BaseResponseEntity;
import vn.kpi.models.response.EmployeesResponse;
import vn.kpi.models.response.OrgHierarchyResponse;
import vn.kpi.models.response.OrganizationsResponse;
import vn.kpi.repositories.entity.CategoryEntity;
import vn.kpi.repositories.entity.EmpTypesEntity;
import vn.kpi.repositories.entity.JobsEntity;
import vn.kpi.repositories.entity.OrganizationsEntity;
import vn.kpi.repositories.impl.JobsRepository;
import vn.kpi.repositories.impl.OrganizationsRepository;
import vn.kpi.repositories.jpa.OrganizationsRepositoryJPA;
import vn.kpi.services.ObjectAttributesService;
import vn.kpi.services.OrganizationsService;
import vn.kpi.utils.ExportExcel;
import vn.kpi.utils.I18n;
import vn.kpi.utils.ImportExcel;
import vn.kpi.utils.ResponseUtils;
import vn.kpi.utils.Utils;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Lop impl service ung voi bang hr_organizations
 *
 * @author tudd
 * @version 1.0
 * @since 1.0
 */

@Service
@RequiredArgsConstructor
public class OrganizationsServiceImpl implements OrganizationsService {

    private final OrganizationsRepository organizationsRepository;
    private final ObjectAttributesService objectAttributesService;
    private final OrganizationsRepositoryJPA organizationsRepositoryJPA;
    private final MdcForkJoinPool forkJoinPool;
    private final JobsRepository jobsRepository;

    @Override
    @Transactional(readOnly = true)
    public BaseDataTableDto<OrganizationsResponse.SearchResult> searchData(OrganizationsRequest.SearchForm dto) {
        return organizationsRepository.searchData(dto);
    }

    @Override
    @Transactional
    public BaseResponseEntity<Long> saveData(OrganizationsRequest.SubmitForm dto, Long organizationId) throws BaseAppException {
        OrganizationsEntity entity;

        // validate duplicate code
        boolean isDuplicateCode = organizationsRepository.isDuplicateOrgCode(organizationId, dto);
        if (isDuplicateCode) {
            throw new BaseAppException("ERROR_ORG_DUPLICATE_CODE", I18n.getMessage("error.organization.orgCode.used"));
        }

        // chek ngay hieu luc voi don vi cha
        OrganizationsEntity parentEntity;
        if (dto.getParentId() != null && dto.getParentId() > 0L) {
            parentEntity = organizationsRepository.get(OrganizationsEntity.class, dto.getParentId());
            if (parentEntity == null || BaseConstants.STATUS.DELETED.equals(parentEntity.getIsDeleted())) {
                throw new RecordNotExistsException(dto.getParentId(), OrganizationsEntity.class);
            }
            Date maxDate = Utils.getMaxDate();
            Date parentToDate = Utils.NVL(parentEntity.getEndDate(), maxDate);
            Date childToDate = Utils.NVL(dto.getEndDate(), maxDate);
            if ((parentEntity.getStartDate() != null && parentEntity.getStartDate().after(dto.getStartDate())) || parentToDate.before(childToDate)) {
                throw new BaseAppException("ERROR_ORG_INVALID_TIME", I18n.getMessage("error.organization.parent.effective"));
            }
        }

        //check ngay hieu luc voi don vi con
        boolean isUpdate = false;
        if (organizationId != null && organizationId > 0L) {
            List<OrganizationsEntity> listChildOrg = organizationsRepository.findByProperties(OrganizationsEntity.class, "parentId", organizationId);
            listChildOrg = listChildOrg.stream().filter(item -> (item.getStartDate() != null && item.getStartDate().before(dto.getStartDate()))).toList();
            if (!Utils.isNullOrEmpty(listChildOrg)) {
                throw new BaseAppException("ERROR_ORG_INVALID_TIME", I18n.getMessage("error.organization.child.effective"));
            }
            isUpdate = true;
        }

        Date curDate = new Date();
        String userName = Utils.getUserNameLogin();
        if (isUpdate) {
            entity = organizationsRepositoryJPA.getById(organizationId);
            entity.setModifiedTime(curDate);
            entity.setModifiedBy(userName);
        } else {
            entity = new OrganizationsEntity();
            entity.setCreatedTime(curDate);
            entity.setCreatedBy(userName);
        }
        Long oldParentId = Utils.NVL(entity.getParentId());
        Utils.copyProperties(dto, entity);

        entity.setIsDeleted(BaseConstants.STATUS.NOT_DELETED);

        organizationsRepositoryJPA.save(entity);
        organizationsRepositoryJPA.flush();

        // don vi lien quan
//        organizationsRepository.deleteRelatedOrg(entity.getOrganizationId());
//        if (!Utils.isNullOrEmpty(dto.getListConstraintOrgIds())) {
//            List<RelatedOrganizationsEntity> listRelatedOrganizations = new ArrayList<>();
//            for (Long constraintOrgId : dto.getListConstraintOrgIds()) {
//                RelatedOrganizationsEntity relatedOrganizationsEntity = new RelatedOrganizationsEntity();
//                relatedOrganizationsEntity.setCreatedTime(curDate);
//                relatedOrganizationsEntity.setCreatedBy(userName);
//                relatedOrganizationsEntity.setOrganizationId(entity.getOrganizationId());
//                relatedOrganizationsEntity.setConstraintOrgId(constraintOrgId);
//                relatedOrganizationsEntity.setIsDeleted(BaseConstants.STATUS.NOT_DELETED);
//                listRelatedOrganizations.add(relatedOrganizationsEntity);
//            }
//            organizationsRepository.insertBatch(RelatedOrganizationsEntity.class, listRelatedOrganizations, userName);
//        }

        // attribute
//        objectAttributesService.saveObjectAttributes(entity.getOrganizationId(), dto.getListAttributes(), OrganizationsEntity.class, null);

        // update org_path
        if (!oldParentId.equals(dto.getParentId()) || !isUpdate) {
            organizationsRepository.updateOrgPath(entity.getOrganizationId());
        }
        // update org_order
//        organizationsRepository.updateOrgOrder();

        return ResponseUtils.ok(entity.getOrganizationId());
    }

    @Override
    @Transactional
    public BaseResponseEntity<Long> deleteData(Long id) throws BaseAppException {
        OrganizationsEntity organizationsEntity = organizationsRepository.get(OrganizationsEntity.class, id);
        if (organizationsEntity == null || BaseConstants.STATUS.DELETED.equals(organizationsEntity.getIsDeleted())) {
            throw new RecordNotExistsException(id, OrganizationsEntity.class);
        }

        if (organizationsRepository.checkUsedOrgById(id)) { // check org da duoc su dung
            throw new BaseAppException("ERROR_CATEGORY_VALUE_DUPLICATE", I18n.getMessage("error.organization.used"));
        }
        organizationsRepository.deActiveObject(OrganizationsEntity.class, id);
        return ResponseUtils.ok(id);
    }

    @Override
    @Transactional(readOnly = true)
    public BaseResponseEntity<OrganizationsResponse.DetailBean> getDataById(Long organizationId) throws RecordNotExistsException {
        OrganizationsResponse.DetailBean response = organizationsRepository.getOrganizationById(organizationId);
        if (response == null) {
            throw new RecordNotExistsException(organizationId, OrganizationsEntity.class);
        }

        //Lay thong tin attributes
//        response.setListAttributes(objectAttributesService.getAttributes(organizationId, organizationsRepository.getSQLTableName(OrganizationsEntity.class)));
//
//        //Lay thong tin don vi lien quan
//        response.setListRelatedOrg(organizationsRepository.getListRelatedOrg(organizationId));
        return ResponseUtils.ok(response);
    }

    @Override
    public ResponseEntity<Object> exportData(OrganizationsRequest.SearchForm dto) throws Exception {
        String pathTemplate = "template/export/BM_Xuat_DS_tuyen_dung.xlsx";
        ExportExcel dynamicExport = new ExportExcel(pathTemplate, 2, true);
        List<Map<String, Object>> listDataExport = organizationsRepository.getListExport(dto);

        dynamicExport.replaceKeys(listDataExport);
        return ResponseUtils.ok(dynamicExport, "BM_Xuat_DS_tuyen_dung.xlsx");
    }

    @Override
    public BaseDataTableDto<EmployeesResponse.SearchResult> searchListPayroll(OrganizationsRequest.SearchForm dto) {
        return organizationsRepository.searchListPayroll(dto);
    }

    @Override
    public List getHierarchy(Long orgId) {
        //thong ke danh sach nhan vien
        List<OrgHierarchyResponse> results = new ArrayList<>();
        List<EmployeeDto> employeeDtos = organizationsRepository.getEmployeeForHierarchy(orgId);
        List<OrganizationDto> organizationDtos = organizationsRepository.getOrgForHierarchy(orgId);
        if (organizationDtos.isEmpty()) {
            results.addAll(summaryEmp(employeeDtos, true));
        } else {
            results.addAll(summaryEmp(employeeDtos, false));
            results.addAll(summaryOrg(organizationDtos));
        }
        return results;
    }

    @Override
    public BaseResponseEntity<Object> getChart(String chartType, Long organizationId) throws ExecutionException, InterruptedException {
        List<OrganizationsResponse.ChartDto> result = null;
        if ("GENDER".equalsIgnoreCase(chartType)) {
            result = organizationsRepository.getChartByGender(organizationId);
        } else if ("EDUCATION_LEVEL".equalsIgnoreCase(chartType)) {
            result = organizationsRepository.getChartByEducationLevel(organizationId);
        } else if ("EMP_TYPE".equalsIgnoreCase(chartType)) {
            result = organizationsRepository.getChartByEmpType(organizationId);
        } else if ("ORG".equalsIgnoreCase(chartType)) {
            List<CompletableFuture<Object>> completableFutures = new ArrayList<>();
            Date currentDate = new Date();
            int durationMonth = Utils.getMonthByDate(currentDate) + 12;
            for (int i = durationMonth - 1; i >= 0; i--) {
                Date toDate = Utils.getLastDayOfMonth(DateUtils.addMonths(currentDate, -i));
                Supplier<Object> getReportByOrg = () -> organizationsRepository.getChartByOrg(organizationId, toDate);
                completableFutures.add(CompletableFuture.supplyAsync(getReportByOrg, forkJoinPool));
            }
            CompletableFuture<Void> allReturn = CompletableFuture.allOf(completableFutures.toArray(new CompletableFuture[completableFutures.size()]));
            CompletableFuture<Object> allFutures = allReturn.thenApply(v -> completableFutures.stream().map(CompletableFuture::join).collect(Collectors.toList()));
            return ResponseUtils.ok(allFutures.get());
        }
        return ResponseUtils.ok((Object) result);
    }

    @Override
    public BaseResponseEntity<Object> getChartLaborStructure(Long organizationId) {
        Date currentDate = Utils.getLastDayOfMonth(new Date());
        List<String> categories = new ArrayList<>();
        List<EmpTypesEntity> listEmpType = organizationsRepository.findByProperties(EmpTypesEntity.class);
        Map<String, List<Double>> mapDatas = new HashMap<>();
        for (int i = 11; i >= 0; i--) {
            Date dateReport = Utils.getLastDayOfMonth(DateUtils.addMonths(currentDate, -i));
            categories.add(Utils.formatDate(dateReport, BaseConstants.SHORT_DATE_FORMAT));
            List<OrganizationsResponse.ChartDto> listData = organizationsRepository.getChartLaborStructure(organizationId, dateReport);
            for (EmpTypesEntity empType : listEmpType) {
                Double count = 0D;
                for (OrganizationsResponse.ChartDto dashboardResponse : listData) {
                    if (dashboardResponse.getName().equalsIgnoreCase(empType.getName())) {
                        count = dashboardResponse.getTotal();
                        break;
                    }
                }
                List<Double> listByMap = mapDatas.get(empType.getName());
                if (listByMap == null) {
                    listByMap = new ArrayList<>();
                }
                listByMap.add(count);
                mapDatas.put(empType.getName(), listByMap);
            }
        }

        Map<String, Object> data = new HashMap<>();
        List<OrganizationsResponse.ChartLaborStructureDto> series = new ArrayList<>();
        for (EmpTypesEntity empTypeEntity : listEmpType) {
            series.add(new OrganizationsResponse.ChartLaborStructureDto(empTypeEntity.getName(), mapDatas.get(empTypeEntity.getName())));
        }
        data.put("categories", categories);
        data.put("series", series);

        return ResponseUtils.ok(data);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseEntity<Object> processImport(MultipartFile file, boolean isForceUpdate) throws IOException {
        ImportExcel importExcel = new ImportExcel("template/import/BM_import_khoi_tao_don_vi.xml");
        List<Object[]> dataList = new ArrayList<>();
        String userName = Utils.getUserNameLogin();
        //Lay danh sach nhom don vi
        List<CategoryEntity> orgGroups = organizationsRepository.findByProperties(CategoryEntity.class, "categoryType", Constant.CATEGORY_CODES.HR_LOAI_HINH_DON_VI);
        //Lay danh muc chuc danh
        List<JobsEntity> jobChucVu = jobsRepository.findByProperties(JobsEntity.class, "jobType",JobsEntity.JOB_TYPES.CHUC_VU);
        List<JobsEntity> jobChucDanhCongViec = jobsRepository.findByProperties(JobsEntity.class, "jobType",JobsEntity.JOB_TYPES.CONG_VIEC);
        List<JobsEntity> jobViTriViecLam = jobsRepository.findByProperties(JobsEntity.class, "jobType",JobsEntity.JOB_TYPES.VI_TRI_VIEC_LAM);
        Map<String, JobsEntity> mapChucVu = jobChucVu.stream().collect(Collectors.toMap(item -> item.getName().toLowerCase(), job -> job));
        Map<String, JobsEntity> mapCongViec = jobChucDanhCongViec.stream().collect(Collectors.toMap(item -> item.getName().toLowerCase(), job -> job));
        Map<String, JobsEntity> mapVitriVieclam = jobViTriViecLam.stream().collect(Collectors.toMap(item -> item.getName().toLowerCase(), job -> job));
        Map<String, CategoryEntity> mapNhomDonVi = orgGroups.stream().collect(Collectors.toMap(item -> item.getName().toLowerCase(), group -> group));
        if (importExcel.validateCommon(file.getInputStream(), dataList)) {

        } else {
            throw new ErrorImportException(file, importExcel);
        }

        return ResponseUtils.ok();
    }

    private List summaryEmp(List<EmployeeDto> employeeDtos, boolean isGroupByJob) {

        if (Utils.isNullOrEmpty(employeeDtos)) {
            return new ArrayList<>();
        }
        Map<String, List<EmployeeDto>> mapTemps = new HashMap<>();
        List<String> jobNames = new ArrayList<>();
        employeeDtos.forEach(item -> {
            String key = isGroupByJob ? item.getJobName() : item.getOrganizationName();
            if (mapTemps.get(key) == null) {
                mapTemps.put(key, new ArrayList<>());
                jobNames.add(key);
            }
            mapTemps.get(key).add(item);
        });


        List<OrgHierarchyResponse> results = new ArrayList<>();
        jobNames.forEach(jobName -> {
            OrgHierarchyResponse response = new OrgHierarchyResponse();
            response.setEmployee(true);
            response.setHierarchyName(jobName);
            response.addDetails(mapTemps.get(jobName), isGroupByJob);
            results.add(response);
        });
        return results;
    }

    private List summaryOrg(List<OrganizationDto> employeeDtos) {
        if (Utils.isNullOrEmpty(employeeDtos)) {
            return new ArrayList<>();
        }
        Map<String, List<OrganizationDto>> mapTemps = new HashMap<>();
        List<String> jobNames = new ArrayList<>();
        employeeDtos.forEach(item -> {
            if (mapTemps.get(item.getOrgTypeName()) == null) {
                mapTemps.put(item.getOrgTypeName(), new ArrayList<>());
                jobNames.add(item.getOrgTypeName());
            }
            mapTemps.get(item.getOrgTypeName()).add(item);
        });
        List<OrgHierarchyResponse> results = new ArrayList<>();
        jobNames.forEach(jobName -> {
            OrgHierarchyResponse response = new OrgHierarchyResponse();
            response.setEmployee(false);
            response.setHierarchyName(jobName);
            response.addOrgDetails(mapTemps.get(jobName));
            results.add(response);
        });
        return results;
    }

}
