package vn.hbtplus.insurance.repositories.entity;

import lombok.Data;
import vn.hbtplus.repositories.entity.BaseEntity;

import javax.persistence.*;
import java.util.Date;

@Data
@Entity
@Table(name = "ICN_CONTRIBUTION_RATES")
public class ContributionRateEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "contribution_rate_id")
    private Long contributionRateId;

    @Column(name = "emp_type_code")
    private String empTypeCode;
    @Column(name = "unit_social_percent")
    private Double unitSocialPercent;
    @Column(name = "per_social_percent")
    private Double perSocialPercent;
    @Column(name = "unit_medical_percent")
    private Double unitMedicalPercent;
    @Column(name = "per_medical_percent")
    private Double perMedicalPercent;
    @Column(name = "unit_unemp_percent")
    private Double unitUnempPercent;
    @Column(name = "per_unemp_percent")
    private Double perUnempPercent;
    @Column(name = "unit_union_percent")
    private Double unitUnionPercent;
    @Column(name = "per_union_percent")
    private Double perUnionPercent;

    @Column(name = "start_date")
    @Temporal(TemporalType.DATE)
    private Date startDate;
    @Column(name = "end_date")
    @Temporal(TemporalType.DATE)
    private Date endDate;


}
