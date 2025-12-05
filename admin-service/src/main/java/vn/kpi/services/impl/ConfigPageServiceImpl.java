package vn.kpi.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.kpi.constants.BaseConstants;
import vn.kpi.constants.Constant;
import vn.kpi.exceptions.BaseAppException;
import vn.kpi.exceptions.RecordNotExistsException;
import vn.kpi.models.ReportConfigDto;
import vn.kpi.models.request.ConfigPageRequest;
import vn.kpi.models.response.BaseResponseEntity;
import vn.kpi.models.response.ConfigPageResponse;
import vn.kpi.models.response.ObjectAttributesResponse;
import vn.kpi.models.response.TableResponseEntity;
import vn.kpi.repositories.entity.ConfigPageEntity;
import vn.kpi.repositories.impl.ConfigPageRepository;
import vn.kpi.repositories.jpa.ConfigPageRepositoryJPA;
import vn.kpi.services.AttachmentService;
import vn.kpi.services.ConfigMappingsService;
import vn.kpi.services.ConfigObjectAttributeService;
import vn.kpi.services.ConfigPageService;
import vn.kpi.services.ConfigParameterService;
import vn.kpi.services.DynamicReportsService;
import vn.kpi.services.ObjectAttributesService;
import vn.kpi.utils.ExportExcel;
import vn.kpi.utils.I18n;
import vn.kpi.utils.ResponseUtils;
import vn.kpi.utils.Utils;

import java.util.*;

@Service
@RequiredArgsConstructor
public class ConfigPageServiceImpl implements ConfigPageService {
    private final ConfigPageRepository configPageRepository;
    private final DynamicReportsService dynamicReportsService;
    private final AttachmentService attachmentService;
    private final ConfigPageRepositoryJPA configPageRepositoryJPA;
    private final ObjectAttributesService objectAttributesService;
    private final ConfigObjectAttributeService configObjectAttributeService;
    private final ConfigParameterService configParameterService;
    private final ConfigMappingsService configMappingsService;


    @Override
    public ConfigPageResponse.ExtractBean getConfigByUrl(String url) {
        ConfigPageEntity configPageEntity = configPageRepository.getEntityByUrl(url);
        ConfigPageResponse.ExtractBean configPageResponse = new ConfigPageResponse.ExtractBean();
        if (configPageEntity != null) {
            if (!Utils.isNullOrEmpty(configPageEntity.getReportCodes())) {
                List<String> codes = Arrays.asList(configPageEntity.getReportCodes().toUpperCase().replace(" ", "").split(","));
                List<ReportConfigDto> reportConfigDtos = dynamicReportsService.getListReportByCode(codes);
                reportConfigDtos.forEach(item -> {
                    item.setAttachmentFileList(attachmentService.getAttachmentList(
                            Constant.ATTACHMENT.TABLE_NAMES.DYNAMIC_REPORTS, Constant.ATTACHMENT.FILE_TYPES.DYNAMIC_REPORT_FILE_TEMPLATE, Utils.castToList(item.getDynamicReportId())));
                });

                configPageResponse.setReportConfigs(reportConfigDtos);
            }
            //Lay ra cac attributes
            List<ObjectAttributesResponse> objectAttributesResponses = objectAttributesService.getAttributes(configPageEntity.getConfigPageId(), configPageRepository.getSQLTableName(ConfigPageEntity.class));
            for (ObjectAttributesResponse bean : objectAttributesResponses) {
                if (!Utils.isNullOrEmpty(bean.getAttributeValue())) {
                    if ("TABLE_NAME_CFG".equalsIgnoreCase(bean.getAttributeCode())) {
                        configPageResponse.setConfigObjectAttributes(
                                configObjectAttributeService.getListConfigByCodes(bean.getAttributeValue())
                        );
                    } else if ("CONFIG_PARAMETER".equalsIgnoreCase(bean.getAttributeCode())) {
                        configPageResponse.setConfigParameters(
                                configParameterService.getListConfigByCodes(bean.getAttributeValue())
                        );
                    } else if ("MAPPING_CODE".equalsIgnoreCase(bean.getAttributeCode())) {
                        configPageResponse.setConfigMappings(
                                configMappingsService.getListConfigByCodes(bean.getAttributeValue())
                        );
                    }
                }
            }
        }

        return configPageResponse;
    }

    @Override
    @Transactional
    public TableResponseEntity<ConfigPageResponse.SearchResult> searchData(ConfigPageRequest.SearchForm dto) {
        return ResponseUtils.ok(configPageRepository.searchData(dto));
    }

    ;

    @Override
    @Transactional
    public ResponseEntity saveData(ConfigPageRequest.SubmitForm dto, Long id) {
        ConfigPageEntity entity;
        if (id != null && id > 0L) {
            entity = configPageRepositoryJPA.getById(id);
            entity.setModifiedTime(new Date());
            entity.setModifiedBy(Utils.getUserNameLogin());
        } else {
            entity = new ConfigPageEntity();
            entity.setCreatedTime(new Date());
            entity.setCreatedBy(Utils.getUserNameLogin());
        }
        Utils.copyProperties(dto, entity);
        entity.setIsDeleted(BaseConstants.STATUS.NOT_DELETED);
        configPageRepositoryJPA.save(entity);

        objectAttributesService.saveObjectAttributes(entity.getConfigPageId(), dto.getListAttributes(), ConfigPageEntity.class, dto.getType());

        return ResponseUtils.ok(entity.getConfigPageId());
    }

    @Override
    @Transactional
    public ResponseEntity deleteData(Long id) throws RecordNotExistsException {
        Optional<ConfigPageEntity> optional = configPageRepositoryJPA.findById(id);
        if (!optional.isPresent() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new RecordNotExistsException(id, ConfigPageEntity.class);
        }
        configPageRepository.deActiveObject(ConfigPageEntity.class, id);
        return ResponseUtils.ok(id);
    }

    @Override
    @Transactional
    public BaseResponseEntity<ConfigPageResponse.Detail> getDataById(Long id) throws RecordNotExistsException {
        Optional<ConfigPageEntity> optional = configPageRepositoryJPA.findById(id);
        if (!optional.isPresent() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new RecordNotExistsException(id, ConfigPageEntity.class);
        }
        ConfigPageResponse.Detail dto = new ConfigPageResponse.Detail();
        Utils.copyProperties(optional.get(), dto);
        dto.setListAttributes(objectAttributesService.getAttributes(id, Constant.ATTACHMENT.TABLE_NAMES.CONFIG_PAGES));

        return ResponseUtils.ok(dto);
    }

    @Override
    public ResponseEntity<Object> exportData(ConfigPageRequest.SearchForm dto) throws Exception {
        String pathTemplate = "template/export/BM_Xuat_cau_hinh_tham_so_theo_page.xlsx";
        ExportExcel dynamicExport = new ExportExcel(pathTemplate, 2, true);
        List<Map<String, Object>> listDataExport = configPageRepository.getListExport(dto);
        if (Utils.isNullOrEmpty(listDataExport)) {
            throw new BaseAppException(I18n.getMessage("global.notFound"));
        }
        dynamicExport.replaceKeys(listDataExport);
        return ResponseUtils.ok(dynamicExport, "BM_Xuat_cau_hinh_tham_so_theo_page.xlsx");
    }
}
