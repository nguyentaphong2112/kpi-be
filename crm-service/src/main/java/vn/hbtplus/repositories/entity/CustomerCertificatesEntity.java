/*
 * Copyright (C) 2022 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.repositories.entity;

import javax.persistence.*;

import lombok.Data;
import org.springframework.data.annotation.CreatedBy;

import javax.validation.constraints.NotNull;
import java.util.Date;


/**
 * Lop entity ung voi bang crm_customer_certificates
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Data
@Entity
@Table(name = "crm_customer_certificates")
public class CustomerCertificatesEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "customer_certificate_id")
    private Long customerCertificateId;

    @Column(name = "customer_id")
    private Long customerId;

    @Column(name = "certificate_id")
    private String certificateId;

    @Column(name = "issued_date")
    @Temporal(javax.persistence.TemporalType.DATE)
    private Date issuedDate;

    @Column(name = "approved_by")
    private String approvedBy;

    @Column(name = "approved_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date approvedDate;


    @Column(name = "status_id")
    private String statusId;

    @Column(name = "note")
    private String note;

    @Column(name = "approved_note")
    private String approvedNote;


    public interface STATUS {
        String PHE_DUYET = "PHE_DUYET";
        String CHO_PHE_DUYET = "CHO_PHE_DUYET";
        String DE_NGHI_XOA = "DE_NGHI_XOA";
        String TU_CHOI = "TU_CHOI";
    }


}
