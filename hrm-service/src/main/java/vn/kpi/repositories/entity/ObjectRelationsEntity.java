/*
 * Copyright (C) 2022 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.kpi.repositories.entity;

import lombok.Data;

import javax.persistence.*;


/**
 * Lop entity ung voi bang hr_salary_grades
 *
 * @author tudd
 * @version 1.0
 * @since 1.0
 */

@Data
@Entity
@Table(name = "hr_object_relations")
public class ObjectRelationsEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "object_relation_id")
    private Long objectRelationId;

    @Column(name = "table_name")
    private String tableName;
    @Column(name = "refer_table_name")
    private String referTableName;
    @Column(name = "object_id")
    private String objectId;
    @Column(name = "refer_object_id")
    private String referObjectId;
    @Column(name = "function_code")
    private String functionCode;

    public interface TABLE_NAMES {
        String SALARY_RANKS = "hr_salary_ranks";
        String ORGANIZATION = "hr_organizations";
        String JOB = "hr_jobs";
    }
    public interface FUNCTION_CODES {
        String GAN_CHUC_DANH_HUONG_LUONG = "GAN_CHUC_DANH_HUONG_LUONG";
    }
}
