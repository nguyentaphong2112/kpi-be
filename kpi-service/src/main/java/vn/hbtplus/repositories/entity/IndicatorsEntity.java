/*
 * Copyright (C) 2022 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.repositories.entity;

import javax.persistence.*;

import lombok.Data;

import java.util.List;


/**
 * Lop entity ung voi bang kpi_indicators
 *
 * @author tudd
 * @version 1.0
 * @since 1.0
 */

@Data
@Entity
@Table(name = "kpi_indicators")
public class IndicatorsEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "indicator_id")
    private Long indicatorId;

    @Column(name = "name")
    private String name;

    @Column(name = "unit_id")
    private String unitId;

    @Column(name = "period_type")
    private String periodType;

    @Column(name = "significance")
    private String significance;

    @Column(name = "measurement")
    private String measurement;

    @Column(name = "system_info")
    private String systemInfo;

    @Column(name = "type")
    private String type;

    @Column(name = "note")
    private String note;

    @Column(name = "organization_id")
    private Long organizationId;
    @Column(name = "rating_type")
    private String ratingType;
    @Column(name = "list_values")
    private String listValues;

    @Transient
    private List<Long> orgIds;

    public interface RATING_TYPES {
        String NUMBER = "NUMBER";
        String SELECT = "SELECT";
    }
    public static Long ID_KHCTCN = 1l;

}
