/*
 * Copyright (C) 2022 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.kpi.repositories.entity;

import lombok.Data;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;


/**
 * Lop entity ung voi bang kpi_indicator_conversions
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Data
@Entity
@Table(name = "kpi_indicator_conversion_details")
public class IndicatorConversionDetailEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "indicator_conversion_detail_id")
    private Long indicatorConversionDetailId;
    @Column(name = "indicator_conversion_id")
    private Long indicatorConversionId;
    @Column(name = "min_value")
    private String minValue;
    @Column(name = "max_value")
    private String maxValue;
    @Column(name = "min_comparison")
    private String minComparison;
    @Column(name = "max_comparison")
    private String maxComparison;

    @Column(name = "result_id")
    private String resultId;
    @Column(name = "note")
    private String note;

}
