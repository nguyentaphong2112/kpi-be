/*
 * Copyright (C) 2022 EcoIT. All rights reserved.
 * EcoIT. Use is subject to license terms.
 */
package vn.hbtplus.repositories.entity;

import lombok.Data;

import javax.persistence.*;
import java.util.Date;


/**
 * Lop entity ung voi bang HR_CONTRACT_PROCESS
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Data
@Entity
@Table(name = "hr_contract_process")
public class HrContractProcessEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "contract_process_id")
    private Long contractProcessId;

    @Column(name = "employee_id")
    private Long employeeId;

    @Column(name = "emp_type_id")
    private Long empTypeId;

    @Column(name = "contract_type_id")
    private Long contractTypeId;

    @Column(name = "contract_number")
    private String contractNumber;

    @Column(name = "from_date")
    @Temporal(TemporalType.DATE)
    private Date fromDate;

    @Column(name = "to_date")
    @Temporal(TemporalType.DATE)
    private Date toDate;

    @Column(name = "signed_date")
    @Temporal(TemporalType.DATE)
    private Date signedDate;

    @Column(name = "note")
    private String note;

    @Column(name = "classify_code")
    private String classifyCode;

    @Column(name = "signer_id")
    private Long signerId;

    @Column(name = "signer_position")
    private String signerPosition;

}
