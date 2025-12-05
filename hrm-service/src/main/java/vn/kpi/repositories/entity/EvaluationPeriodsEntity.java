package vn.kpi.repositories.entity;

import lombok.Data;

import javax.persistence.*;
import java.util.Date;

@Data
@Entity
@Table(name = "kpi_evaluation_periods")
public class EvaluationPeriodsEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "evaluation_period_id")
    private Long evaluationPeriodId;

    @Column(name = "year")
    private Integer year;

    @Column(name = "name")
    private String name;

    @Column(name = "evaluation_type")
    private String evaluationType;

    @Column(name = "start_date")
    @Temporal(javax.persistence.TemporalType.DATE)
    private Date startDate;

    @Column(name = "end_date")
    @Temporal(javax.persistence.TemporalType.DATE)
    private Date endDate;


}
