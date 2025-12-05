package vn.hbtplus.tax.income.repositories.entity;

import lombok.Data;
import vn.hbtplus.repositories.entity.BaseEntity;

import javax.persistence.*;
import java.util.Date;

@Data
@Entity
@Table(name = "sys_parameters")
public class ParameterEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "parameter_id")
    private Long parameterId;
    @Column(name = "config_group")
    private String configGroup;
    @Column(name = "config_code")
    private String configCode;
    @Column(name = "config_name")
    private String configName;
    @Column(name = "config_value")
    private String configValue;

    @Column(name = "data_type")
    private String dataType;

    @Column(name = "start_date")
    @Temporal(TemporalType.DATE)
    private Date startDate;

    @Column(name = "end_date")
    @Temporal(TemporalType.DATE)
    private Date endDate;


}
