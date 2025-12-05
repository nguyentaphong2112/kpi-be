/*
 * Copyright (C) 2022 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.repositories.entity;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.GenerationType;
import javax.persistence.Table;
import lombok.Data;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.persistence.Temporal;


/**
 * Lop entity ung voi bang hr_bank_accounts
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Data
@Entity
@Table(name = "hr_bank_accounts")
public class BankAccountsEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "bank_account_id")
    private Long bankAccountId;

    @Column(name = "employee_id")
    private Long employeeId;

    @Column(name = "account_no")
    private String accountNo;

    @Column(name = "bank_id")
    private String bankId;

    @Column(name = "bank_branch")
    private String bankBranch;

    @Column(name = "is_main")
    private String isMain;

    @Column(name = "account_type_id")
    private String accountTypeId;



    public static final Map<String, String> IS_MAIN_MAP = new HashMap<>() {{
        put("Có", "Y");
        put("Không", "N");
    }};



}
