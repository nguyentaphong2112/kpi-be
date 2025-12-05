package vn.hbtplus.services.impl;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.hbtplus.exceptions.BaseAppException;
import vn.hbtplus.exceptions.RecordConflictException;
import vn.hbtplus.exceptions.RecordNotExistsException;
import vn.hbtplus.models.BaseDataTableDto;
import vn.hbtplus.models.request.ConfigParameterRequest;
import vn.hbtplus.models.request.ParameterRequest;
import vn.hbtplus.models.response.ConfigParameterResponse;
import vn.hbtplus.models.response.ParameterResponse;
import vn.hbtplus.repositories.entity.ConfigParameterEntity;
import vn.hbtplus.repositories.entity.ParameterEntity;
import vn.hbtplus.repositories.impl.ConfigParameterRepository;
import vn.hbtplus.repositories.jpa.ConfigParameterRepositoryJPA;
import vn.hbtplus.repositories.jpa.ParameterRepositoryJPA;
import vn.hbtplus.services.ConfigParameterService;
import vn.hbtplus.utils.I18n;
import vn.hbtplus.utils.ResponseUtils;
import vn.hbtplus.utils.Utils;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@AllArgsConstructor
public class ConfigParameterServiceImpl implements ConfigParameterService {
    private final ConfigParameterRepository configParameterRepository;
    private final ConfigParameterRepositoryJPA configParameterRepositoryJPA;
    private final ParameterRepositoryJPA parameterRepositoryJPA;


    @Override
    public BaseDataTableDto search(ParameterRequest.SearchForm request, String configGroup) {
        BaseDataTableDto<ParameterResponse.SearchResult> results = configParameterRepository.search(request, configGroup);
        if (!results.isEmpty()) {
            results.getListData().stream().forEach(item -> {
                List<ParameterEntity> parameterEntities = configParameterRepository.findByProperties(ParameterEntity.class, "configGroup", configGroup, "startDate", item.getStartDate());

                ConfigParameterEntity configParametersEntity = configParameterRepository.getByConfigGroup(configGroup);
                List<ParameterResponse.ColumnResponse> columnConfigList = Utils.fromJsonList(
                        configParametersEntity.getConfigColumns(),
                        ParameterResponse.ColumnResponse.class
                );

                Map<String, ParameterResponse.ColumnResponse> columnConfigMap = columnConfigList.stream()
                        .collect(Collectors.toMap(ParameterResponse.ColumnResponse::getConfigCode, c -> c));

                parameterEntities.forEach(parameter -> {
                    ParameterResponse.ColumnResponse response = new ParameterResponse.ColumnResponse();
                    response.setConfigCode(parameter.getConfigCode());
                    response.setConfigValue(parameter.getConfigValue());

                    ParameterResponse.ColumnResponse columnConfig = columnConfigMap.get(parameter.getConfigCode());
                    if (columnConfig != null) {
                        response.setDataType(columnConfig.getDataType());
                        response.setUrlLoadData(columnConfig.getUrlLoadData());
                    }
                    item.getColumns().add(response);
                });
            });
        }
        return results;
    }

    @Override
    public Object exportData(ParameterRequest.SearchForm request, String configGroup) {
        return null;
    }

    @Override
    @Transactional
    public Object saveData(ParameterRequest.SubmitForm request, String configGroup, Date startDate) throws BaseAppException {
        //Lay config cua group
        ConfigParameterResponse configParameterResponse = configParameterRepository.getConfigGroup(configGroup);
        Map<String, ConfigParameterResponse.ConfigColumn> mapColumns = new HashMap();
        configParameterResponse.getColumns().stream().forEach(item -> {
            mapColumns.put(item.getConfigCode(), item);
        });
        if(ConfigParameterEntity.CONFIG_PERIOD_TYPES.MONTH.equalsIgnoreCase(configParameterResponse.getConfigPeriodType())){
            request.setStartDate(Utils.getFirstDay(request.getStartDate()));
            request.setEndDate(Utils.getLastDay(request.getEndDate()));
        } else if(ConfigParameterEntity.CONFIG_PERIOD_TYPES.ONLY_MONTH.equalsIgnoreCase(configParameterResponse.getConfigPeriodType())){
            request.setStartDate(Utils.getFirstDay(request.getStartDate()));
            request.setEndDate(Utils.getLastDay(request.getStartDate()));
        }

        if (request.getColumns() == null || request.getColumns().isEmpty()) {
            //tra ve noi dung loi
            throw new BaseAppException("request columns must not be empty!");
        }
        //validate conflict
        validateConflictProcess(request, configGroup, startDate);

        Map<String, ParameterEntity> mapEntities = new HashMap<>();
        if (startDate != null) {
            List<ParameterEntity> parameterEntities = parameterRepositoryJPA.getByStartDate(configGroup, startDate);
            mapEntities.putAll(parameterEntities.stream()
                    .collect(Collectors.toMap(ParameterEntity::getConfigCode, item -> item)));
        }

        request.getColumns().stream().forEach(item -> {
            ParameterEntity parameterEntity = mapEntities.get(item.getConfigCode());
            if (parameterEntity == null) {
                parameterEntity = new ParameterEntity();
                parameterEntity.setCreatedBy(Utils.getUserNameLogin());
                parameterEntity.setCreatedTime(new Date());

            } else {
                parameterEntity.setModifiedBy(Utils.getUserNameLogin());
                parameterEntity.setModifiedTime(new Date());
            }
            parameterEntity.setStartDate(request.getStartDate());
            parameterEntity.setEndDate(request.getEndDate());
            parameterEntity.setConfigGroup(configGroup);
            parameterEntity.setConfigCode(item.getConfigCode());
            parameterEntity.setConfigValue(item.getConfigValue());
            parameterEntity.setConfigName(mapColumns.get(item.getConfigCode()).getConfigName());
            parameterEntity.setDataType(mapColumns.get(item.getConfigCode()).getDataType());
            parameterRepositoryJPA.save(parameterEntity);
        });
        //update ngay het hieu luc cua ban ghi cu

        parameterRepositoryJPA.updatePreConfigs(configGroup, request.getStartDate());

        return null;
    }

    private void validateConflictProcess(ParameterRequest.SubmitForm request, String configGroup, Date startDate) throws RecordConflictException {
        ParameterEntity parameterEntity = configParameterRepository.getConflict(request, configGroup, startDate);
        if (parameterEntity != null) {
            throw new RecordConflictException("error.configParameter.conflict", Utils.formatDate(parameterEntity.getStartDate()), Utils.formatDate(parameterEntity.getEndDate()));
        }
    }

    @Override
    public Object getById(Long id) {
        return null;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConfigParameterResponse> getConfigGroups(String moduleCode) {
        List<ConfigParameterEntity> configParameterEntities = configParameterRepository.getConfigGroups(moduleCode);
        List<ConfigParameterResponse> results = new ArrayList<>();
        configParameterEntities.stream().forEach(item -> {
            ConfigParameterResponse response = new ConfigParameterResponse();
            Utils.copyProperties(item, response);
            results.add(response);
        });
        return results;
    }

    @Override
    @Transactional
    public ResponseEntity updateConfigGroup(ConfigParameterRequest.SubmitForm config) throws BaseAppException {
        boolean isDuplicate = configParameterRepository.duplicate(ConfigParameterEntity.class, config.getConfigParameterId(), "configGroup", config.getConfigGroup(), "moduleCode", config.getModuleCode());
        if (isDuplicate) {
            throw new BaseAppException("ERROR_CONFIG_PARAMETER_DUPLICATE", I18n.getMessage("error.configParameter.configGroup.duplicate"));
        }
        ConfigParameterEntity configParameterEntity;
        if (config.getConfigParameterId() != null && config.getConfigParameterId() > 0) {
            configParameterEntity = configParameterRepository.get(ConfigParameterEntity.class, "configParameterId", config.getConfigParameterId());
            configParameterEntity.setModifiedBy(Utils.getUserNameLogin());
            configParameterEntity.setModifiedTime(new Date());
        } else {
            configParameterEntity = new ConfigParameterEntity();
            configParameterEntity.setCreatedBy(Utils.getUserNameLogin());
            configParameterEntity.setCreatedTime(new Date());
        }
        configParameterEntity.setConfigGroup(config.getConfigGroup());
        configParameterEntity.setModuleCode(config.getModuleCode());
        configParameterEntity.setConfigGroupName(config.getConfigGroupName());
        configParameterEntity.setConfigPeriodType(config.getConfigPeriodType());
        configParameterEntity.setConfigColumns(Utils.toJson(config.getConfigColumns()));
        configParameterRepositoryJPA.save(configParameterEntity);
        return ResponseEntity.ok(configParameterEntity.getConfigParameterId());
    }

    @Override
    @Transactional
    public Object deleteById(String configGroup, Date startDate) {
        parameterRepositoryJPA.deleteParameter(configGroup, startDate, Utils.getUserNameLogin());
        return null;
    }

    @Override
    public ParameterResponse.SearchResult getById(String configGroup, Date startDate) throws RecordNotExistsException {
        List<ParameterEntity> parameterEntities = configParameterRepository.findByProperties(ParameterEntity.class, "configGroup", configGroup, "startDate", startDate);
        if(parameterEntities.isEmpty()){
            throw new RecordNotExistsException("record not exists");
        }
        //Lay config cua group
        ConfigParameterResponse configParameterResponse = configParameterRepository.getConfigGroup(configGroup);
        Map<String, ConfigParameterResponse.ConfigColumn> mapColumns = new HashMap();
        configParameterResponse.getColumns().stream().forEach(item -> {
            mapColumns.put(item.getConfigCode(), item);
        });

        ParameterResponse.SearchResult response = new ParameterResponse.SearchResult();
        response.setConfigGroup(configGroup);
        response.setStartDate(parameterEntities.get(0).getStartDate());
        response.setEndDate(parameterEntities.get(0).getEndDate());
        response.setConfigGroupName(configParameterResponse.getConfigGroupName());
        response.setConfigPeriodType(configParameterResponse.getConfigPeriodType());
        parameterEntities.stream().forEach(parameter -> {
            ParameterResponse.ColumnResponse column = new ParameterResponse.ColumnResponse();
            column.setConfigCode(parameter.getConfigCode());
            column.setConfigValue(parameter.getConfigValue());
            if(mapColumns.get(parameter.getConfigCode()) != null){
                column.setDataType(mapColumns.get(parameter.getConfigCode()).getDataType());
                column.setConfigName(mapColumns.get(parameter.getConfigCode()).getConfigName());
            }
            response.getColumns().add(column);
        });
        return response;
    }

    @Override
    public Map<String, String> getParameters(List<String> configCodes) {
        return configParameterRepository.getParameters(configCodes);
    }

    @Override
    public List<ConfigParameterResponse> getListConfigByCodes(String attributeValue) {
        return configParameterRepository.getListConfigByCodes(List.of(attributeValue.replace(" ", "").split(",")));

    }
}
