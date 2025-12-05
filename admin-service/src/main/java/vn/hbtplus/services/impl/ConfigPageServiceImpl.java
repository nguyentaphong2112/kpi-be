package vn.hbtplus.services.impl;

import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.function.EntityResponse;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.constants.Constant;
import vn.hbtplus.exceptions.BaseAppException;
import vn.hbtplus.exceptions.RecordNotExistsException;
import vn.hbtplus.models.ReportConfigDto;
import vn.hbtplus.models.request.ConfigPageRequest;
import vn.hbtplus.models.response.BaseResponseEntity;
import vn.hbtplus.models.response.ConfigPageResponse;
import vn.hbtplus.models.response.ObjectAttributesResponse;
import vn.hbtplus.models.response.TableResponseEntity;
import vn.hbtplus.repositories.entity.ConfigPageEntity;
import vn.hbtplus.repositories.impl.ConfigPageRepository;
import vn.hbtplus.repositories.jpa.ConfigPageRepositoryJPA;
import vn.hbtplus.services.AttachmentService;
import vn.hbtplus.services.ConfigMappingsService;
import vn.hbtplus.services.ConfigObjectAttributeService;
import vn.hbtplus.services.ConfigPageService;
import vn.hbtplus.services.ConfigParameterService;
import vn.hbtplus.services.DynamicReportsService;
import vn.hbtplus.services.ObjectAttributesService;
import vn.hbtplus.utils.ExportExcel;
import vn.hbtplus.utils.I18n;
import vn.hbtplus.utils.ResponseUtils;
import vn.hbtplus.utils.Utils;

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
