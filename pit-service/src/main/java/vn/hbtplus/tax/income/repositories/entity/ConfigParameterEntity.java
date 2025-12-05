package vn.hbtplus.tax.income.repositories.entity;

import lombok.Data;
import vn.hbtplus.repositories.entity.BaseEntity;

import javax.persistence.*;

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
    public interface CONFIG_PERIOD_TYPES {
        String DATE = "DATE";
        String MONTH = "MONTH";
        String ONLY_MONTH = "ONLY_MONTH";
    }

}
