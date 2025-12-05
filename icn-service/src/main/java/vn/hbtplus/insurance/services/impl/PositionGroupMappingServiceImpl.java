package vn.hbtplus.insurance.services.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import vn.hbtplus.insurance.constants.Constant;
import vn.hbtplus.exceptions.ErrorImportException;
import vn.hbtplus.insurance.models.response.CategoryResponse;
import vn.hbtplus.insurance.repositories.entity.PositionGroupMappingEntity;
import vn.hbtplus.insurance.repositories.impl.CategoryRepository;
import vn.hbtplus.insurance.repositories.impl.JobRepositoryImpl;
import vn.hbtplus.insurance.repositories.impl.OrganizationRepositoryImpl;
import vn.hbtplus.insurance.repositories.impl.PositionGroupMappingRepositoryImpl;
import vn.hbtplus.insurance.services.PositionGroupMappingService;
import vn.hbtplus.utils.ExportExcel;
import vn.hbtplus.utils.ImportExcel;
import vn.hbtplus.utils.ResponseUtils;
import vn.hbtplus.utils.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class PositionGroupMappingServiceImpl implements PositionGroupMappingService {

    private final PositionGroupMappingRepositoryImpl positionGroupMappingRepositoryImpl;
    private final JobRepositoryImpl jobRepositoryImpl;
    private final CategoryRepository categoryRepository;
    private final OrganizationRepositoryImpl organizationRepositoryImpl;

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<Object> downloadTemplate() throws Exception {
        String pathTemplate = "template/import/insurance/BM_import_nhom_chuc_danh.xlsx";
        ExportExcel dynamicExport = new ExportExcel(pathTemplate, 5, true);
        dynamicExport.setActiveSheet(1);
        List<Map<String, Object>> mapJob = jobRepositoryImpl.getAllJobs();
        dynamicExport.replaceKeys(mapJob);

        dynamicExport.setActiveSheet(2);
        List<Map<String, Object>> mapCategoryName = positionGroupMappingRepositoryImpl.getAllCategoryByType(Constant.CATEGORY_TYPE.PHAN_NHOM_CHUC_DANH);
        dynamicExport.replaceKeys(mapCategoryName);

        dynamicExport.setActiveSheet(0);

        return ResponseUtils.ok(dynamicExport, "BM_import_nhom_chuc_danh.xlsx", false);
    }

    @Override
    @Transactional
    public void processImport(MultipartFile fileImport) throws Exception {
        ImportExcel importExcel = new ImportExcel("template/import/insurance/BM_import_nhom_chuc_danh.xml");
        List<Object[]> dataList = new ArrayList<>();
        if (importExcel.validateCommon(fileImport.getInputStream(), dataList)) {
            List<String> orgNameList = new ArrayList<>();

            int col = 2;
            int row = 0;
            for (Object[] obj : dataList) {
                String orgNameLevel1 = (String) obj[col++];
                String orgNameLevel2 = (String) obj[col++];
                String orgNameLevel3 = (String) obj[col++];
                String orgNameLevel4 = (String) obj[col++];
                String orgNameLevel5 = (String) obj[col++];

                if (StringUtils.isNotBlank(orgNameLevel5)) {
                    if (Utils.isNullAnyString(orgNameLevel4, orgNameLevel3, orgNameLevel2, orgNameLevel1)) {
                        importExcel.addError(row, 2, "Bạn phải nhập đơn vị cấp 1 đến cấp 4", orgNameLevel1);
                    }
                } else if (StringUtils.isNotBlank(orgNameLevel4)) {
                    if (Utils.isNullAnyString(orgNameLevel3, orgNameLevel2, orgNameLevel1)) {
                        importExcel.addError(row, 2, "Bạn phải nhập đơn vị cấp 1 đến cấp 3", orgNameLevel1);
                    }
                } else if (StringUtils.isNotBlank(orgNameLevel3)) {
                    if (Utils.isNullAnyString(orgNameLevel2, orgNameLevel1)) {
                        importExcel.addError(row, 2, "Bạn phải nhập đơn vị cấp 1 đến cấp 2", orgNameLevel1);
                    }
                } else if (StringUtils.isNotBlank(orgNameLevel2)) {
                    if (Utils.isNullAnyString(orgNameLevel1)) {
                        importExcel.addError(row, 2, "Bạn phải nhập đơn vị cấp 1", orgNameLevel1);
                    }
                } else if (StringUtils.isBlank(orgNameLevel1)) {
                    importExcel.addError(row, 2, "Bạn phải nhập thông tin đơn vị", orgNameLevel1);
                }

                List<String> orgNameLevelList = new ArrayList<>();
                if (StringUtils.isNotEmpty(orgNameLevel1)) {
                    orgNameLevelList.add(StringUtils.lowerCase(orgNameLevel1));
                }

                if (StringUtils.isNotEmpty(orgNameLevel2)) {
                    orgNameLevelList.add(StringUtils.lowerCase(orgNameLevel2));
                }

                if (StringUtils.isNotEmpty(orgNameLevel3)) {
                    orgNameLevelList.add(StringUtils.lowerCase(orgNameLevel3));
                }

                if (StringUtils.isNotEmpty(orgNameLevel4)) {
                    orgNameLevelList.add(StringUtils.lowerCase(orgNameLevel4));
                }

                if (StringUtils.isNotEmpty(orgNameLevel5)) {
                    orgNameLevelList.add(StringUtils.lowerCase(orgNameLevel5));
                }

                if (!Utils.isNullOrEmpty(orgNameLevelList)) {
                    orgNameList.add(String.join(StringUtils.EMPTY, orgNameLevelList));
                }
                row++;
            }

            Map<String, Long> mapJob = jobRepositoryImpl.createMapJob();
            Map<String, CategoryResponse> mapCategory = categoryRepository.createCategoryMap(Constant.CATEGORY_TYPE.PHAN_NHOM_CHUC_DANH);
            Map<String, Long> mapOrg = organizationRepositoryImpl.createOrgMap(orgNameList);

            List<Long> orgIds = mapOrg.entrySet().stream().map(item -> item.getValue()).collect(Collectors.toList());
            Map<String, PositionGroupMappingEntity> positionGroupMap = positionGroupMappingRepositoryImpl.createMapPositionGroup(orgIds);
            List<PositionGroupMappingEntity> entityList = new ArrayList<>();
            col = 1;
            row = 0;
            String formatKey = "jobId%s#orgId%s#groupType%s";
            for (Object[] obj : dataList) {
                PositionGroupMappingEntity entity = new PositionGroupMappingEntity();

                String jobCode = (String) obj[col];
                if (mapJob.get(StringUtils.trim(jobCode).toLowerCase()) == null) {
                    importExcel.addError(row, col, "Chức danh không tồn tại", jobCode);
                } else {
                    entity.setJobId(mapJob.get(StringUtils.trim(jobCode).toLowerCase()));
                }
                col++;

                String orgNameLevel1 = (String) obj[col++];
                String orgNameLevel2 = (String) obj[col++];
                String orgNameLevel3 = (String) obj[col++];
                String orgNameLevel4 = (String) obj[col++];
                String orgNameLevel5 = (String) obj[col];
                List<String> orgNameLevelList = new ArrayList<>();
                if (StringUtils.isNotEmpty(orgNameLevel1)) {
                    orgNameLevelList.add(StringUtils.lowerCase(orgNameLevel1));
                }

                if (StringUtils.isNotEmpty(orgNameLevel2)) {
                    orgNameLevelList.add(StringUtils.lowerCase(orgNameLevel2));
                }

                if (StringUtils.isNotEmpty(orgNameLevel3)) {
                    orgNameLevelList.add(StringUtils.lowerCase(orgNameLevel3));
                }

                if (StringUtils.isNotEmpty(orgNameLevel4)) {
                    orgNameLevelList.add(StringUtils.lowerCase(orgNameLevel4));
                }

                if (StringUtils.isNotEmpty(orgNameLevel5)) {
                    orgNameLevelList.add(StringUtils.lowerCase(orgNameLevel5));
                }

                if (mapOrg.get(String.join(StringUtils.EMPTY, orgNameLevelList)) == null) {
                    importExcel.addError(row, col, "Đơn vị không tồn tại", String.join(" ", orgNameLevelList));
                } else {
                    entity.setOrgId(mapOrg.get(String.join(StringUtils.EMPTY, orgNameLevelList)));
                }
                col++;

                String categoryName = (String) obj[col];
                if (mapCategory.get(StringUtils.trimToEmpty(categoryName).toLowerCase()) == null) {
                    importExcel.addError(row, col, "Loại nhóm không tồn tại", categoryName);
                } else {
                    entity.setGroupType(mapCategory.get(StringUtils.trimToEmpty(categoryName).toLowerCase()).getValue());
                }
                col++;
                String value = (String) obj[col];
                entity.setValue(StringUtils.trim(value));

                String key = String.format(formatKey, entity.getJobId(), entity.getOrgId(), entity.getGroupType());
                if (positionGroupMap.get(StringUtils.lowerCase(key)) != null) {
                    importExcel.addError(row, col, "Đã tồn tại cấu hình mapping", jobCode);
                }
                row++;
                entityList.add(entity);
            }

            if (importExcel.hasError()) {
                throw new ErrorImportException(fileImport, importExcel);
            } else {
                positionGroupMappingRepositoryImpl.insertBatch(PositionGroupMappingEntity.class, entityList, Utils.getUserNameLogin());
            }

        }
    }
}
