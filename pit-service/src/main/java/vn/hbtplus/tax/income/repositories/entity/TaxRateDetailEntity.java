package vn.hbtplus.tax.income.repositories.entity;

import lombok.Data;
import vn.hbtplus.repositories.entity.BaseEntity;


import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Data
@Entity
@Table(name = "pit_tax_rate_details")
public class TaxRateDetailEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @NotNull
    @Column(name = "tax_rate_detail_id")
    private Long taxRateDetailId;

    @Column(name = "tax_rate_id")
    private Long taxRateId;
    @Column(name = "amount")
    private Long amount;

    @Column(name = "percent")
    private Double percent;
}
