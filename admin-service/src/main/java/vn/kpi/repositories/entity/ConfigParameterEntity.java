package vn.kpi.repositories.entity;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Data
@Entity
@Table(name = "sys_config_parameters")
public class ConfigParameterEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "config_parameter_id")
    private Long configParameterId;
    @Column(name = "config_group")
    private String configGroup;
    @Column(name = "config_group_name")
    private String configGroupName;
    @Column(name = "config_period_type")
    private String configPeriodType;
    @Column(name = "config_columns")
    private String configColumns;
    @Column(name = "order_number")
    private Integer orderNumber;
    @Column(name = "module_code")
    private String moduleCode;
    public interface CONFIG_PERIOD_TYPES {
        String DATE = "DATE";
        String MONTH = "MONTH";
        String ONLY_MONTH = "ONLY_MONTH";
    }

}
