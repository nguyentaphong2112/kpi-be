package vn.hbtplus.tax.income.repositories.entity;

import lombok.Data;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import vn.hbtplus.repositories.entity.BaseEntity;


import javax.persistence.*;
import java.util.Date;

@Data
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "pit_tax_commitments")
public class TaxCommitmentEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tax_commitment_id")
    private Long taxCommitmentId;

    @Column(name = "employee_id")
    private Long employeeId;

    @Column(name = "income_amount")
    private Long incomeAmount;

    @Column(name = "start_date")
    @Temporal(TemporalType.DATE)
    private Date startDate;

    @Column(name = "end_date")
    @Temporal(TemporalType.DATE)
    private Date endDate;

    @Column(name = "last_update_time")
    private Date lastUpdateTime;

    @Column(name = "description")
    private String description;
}
