/*
 * Copyright (C) 2022 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.kpi.repositories.entity;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.GenerationType;
import javax.persistence.Table;
import lombok.Data;


/**
 * Lop entity ung voi bang hr_contact_addresses
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Data
@Entity
@Table(name = "hr_contact_addresses")
public class ContactAddressesEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "contact_address_id")
    private Long contactAddressId;

    @Column(name = "employee_id")
    private Long employeeId;

    @Column(name = "province_id")
    private String provinceId;

    @Column(name = "district_id")
    private String districtId;

    @Column(name = "ward_id")
    private String wardId;

    @Column(name = "village_address")
    private String villageAddress;

    @Column(name = "address_type")
    private String addressType;


}
