/*
 * Copyright (C) 2022 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.tax.income.repositories.entity;

import lombok.Data;
import vn.hbtplus.repositories.entity.BaseEntity;


import javax.persistence.*;
import javax.validation.constraints.NotNull;


/**
 * Lop entity ung voi bang pit_income_templates
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Data
@Entity
@Table(name = "pit_income_templates")
public class IncomeTemplatesEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @NotNull
    @Column(name = "income_template_id")
    private Long incomeTemplateId;

    @Column(name = "code")
    private String code;

    @Column(name = "name")
    private String name;

    @Column(name = "emp_type")
    private String empType;

    @Column(name = "order_number")
    private Long orderNumber;

    @Column(name = "type")
    private String type;
    public interface TYPES {
        String LT = "LT"; //luong cbcnv
        String MN = "MN"; //thu lao may no
        String SXKDQ = "SXKDQ"; //luong sxkd quý
        String SXKDN = "SXKDN"; //luong sxkd năm
        String TNK = "TNK"; //Thu nhập khác
        String OT = "OT"; //Lương làm thêm
        String VL = "VL"; //Thu nhập đối tượng vãng lai
    }

    public interface EMP_TYPE {
        String STAFF = "STAFF";
        String ALL = "ALL";
        String NON_REST = "NON_REST";

    }

}
