package vn.kpi.services.impl;


import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.kpi.constants.BaseConstants;
import vn.kpi.exceptions.BaseAppException;
import vn.kpi.exceptions.RecordNotExistsException;
import vn.kpi.models.BaseDataTableDto;
import vn.kpi.models.dto.ConfigObjectAttributeDto;
import vn.kpi.models.request.ConfigObjectAttributeRequest;
import vn.kpi.models.response.ConfigObjectAttributeResponse;
import vn.kpi.repositories.ParameterRepository;
import vn.kpi.repositories.entity.ConfigObjectAttributeEntity;
import vn.kpi.repositories.impl.ConfigObjectAttributeRepository;
import vn.kpi.repositories.impl.UtilsRepository;
import vn.kpi.repositories.jpa.ConfigObjectAttributeRepositoryJPA;
import vn.kpi.services.ConfigObjectAttributeService;
import vn.kpi.utils.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@AllArgsConstructor
public class ConfigObjectAttributeServiceImpl implements ConfigObjectAttributeService {
    private final ConfigObjectAttributeRepository configObjectAttributeRepository;
    private final UtilsRepository utilsRepository;
    private final ParameterRepository parameterRepository;
    private final ConfigObjectAttributeRepositoryJPA configObjectAttributeRepositoryJPA;

    @Override
    @Transactional(readOnly = true)
    public BaseDataTableDto searchData(ConfigObjectAttributeRequest.SearchForm dto) {
        return configObjectAttributeRepository.searchData(dto);
    }

    @Override
    public ResponseEntity deleteData(Long id) throws RecordNotExistsException {
        Optional<ConfigObjectAttributeEntity> optional = configObjectAttributeRepositoryJPA.findById(id);
        if (optional.isEmpty() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new RecordNotExistsException(id, ConfigObjectAttributeEntity.class);
        }
        configObjectAttributeRepository.deActiveObject(ConfigObjectAttributeEntity.class, id);
        return ResponseEntity.ok(id);
    }

    @Override
    public ResponseEntity saveData(ConfigObjectAttributeRequest.SubmitForm dto, Long configObjectAttributeId) throws BaseAppException {
        ConfigObjectAttributeEntity entity;
        Date curDate = new Date();
        String userName = Utils.getUserNameLogin();
        if (configObjectAttributeId != null && configObjectAttributeId > 0L) {
            entity = configObjectAttributeRepositoryJPA.getById(configObjectAttributeId);
            entity.setModifiedTime(curDate);
            entity.setModifiedBy(userName);
        } else {
            entity = new ConfigObjectAttributeEntity();
            entity.setCreatedTime(curDate);
            entity.setCreatedBy(userName);
        }
        Utils.copyProperties(dto, entity);
        entity.setAttributes(Utils.toJson(dto.getAttributes()));
        configObjectAttributeRepositoryJPA.save(entity);
        return ResponseEntity.ok(entity.getConfigObjectAttributeId());
    }

    @Override
    public ConfigObjectAttributeResponse.DetailBean getDataById(Long id) throws RecordNotExistsException {
        Optional<ConfigObjectAttributeEntity> optional = configObjectAttributeRepositoryJPA.findById(id);
        if (optional.isEmpty() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new RecordNotExistsException(id, ConfigObjectAttributeEntity.class);
        }
        ConfigObjectAttributeResponse.DetailBean result = new ConfigObjectAttributeResponse.DetailBean();
        Utils.copyProperties(optional.get(), result);
        result.setAttributes(Utils.fromJsonList(optional.get().getAttributes(), ConfigObjectAttributeDto.AttributeDto.class));
        return result;
    }

    @Override
    public List<ConfigObjectAttributeResponse.ListTableName> getListTableData() {
        String tableConfigs = parameterRepository.getConfigValue("LIST_TABLE_CONFIG_ATTRIBUTE", new Date(), String.class);
        String[] tables;
        if(Utils.isNullOrEmpty(tableConfigs)){
            tables = new String[] {
                    "hr_allowance_process", "hr_attachments", "hr_award_process", "hr_bank_accounts", "hr_concurrent_process", "hr_contact_addresses",
                    "hr_contract_process", "hr_contract_types", "hr_discipline_process", "hr_document_types", "hr_education_certificates", "hr_education_degrees",
                    "hr_education_process", "hr_education_promotions", "hr_employees", "hr_emp_types", "hr_family_relationships", "hr_insurance_salary_process",
                    "hr_jobs", "hr_object_attributes", "hr_object_relations", "hr_organizations", "hr_personal_identities", "hr_planning_assignments", "hr_positions",
                    "hr_position_salary_process", "hr_related_organizations", "hr_salary_grades", "hr_salary_ranks", "hr_worked_histories", "hr_work_process",
                    "sys_categories", "sys_category_attributes", "sys_config_object_attributes","sys_config_pages", "kpi_evaluation_periods", "med_mentoring_trainees", "med_mentoring_trainers",
                    "crm_employees", "hr_political_participations", "crm_training_programs", "crm_orders", "med_research_projects", "lms_internship_sessions","crm_products","crm_family_relationships",
                    "kpi_org_configs", "hr_health_records", "sys_config_charts", "sys_warning_configs"
            };
        } else {
            tables = tableConfigs.replace(" ", "").split(",");
        }
        List<ConfigObjectAttributeResponse.ListTableName> tableNames = new ArrayList<>();
        Arrays.stream(tables).forEach(tableName -> {
            tableNames.add(new ConfigObjectAttributeResponse.ListTableName(tableName));
        });
        return tableNames;
    }

    @Override
    public ConfigObjectAttributeResponse.SearchByTableName getByTableName(String tableName, String functionCode) {
        return configObjectAttributeRepository.searchDataByTableName(tableName, functionCode);
    }

    @Override
    public List getAttributes(String tableName, String functionCode) {
        return utilsRepository.getAttributes(tableName, functionCode);
    }

    @Override
    public List<ConfigObjectAttributeResponse> getListConfigByCodes(String attributeValue) {
        return configObjectAttributeRepository.getListConfigByCodes(List.of(attributeValue.replace(" ", "").split(",")));
    }
}
