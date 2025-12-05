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
import javax.persistence.Temporal;


/**
 * Lop entity ung voi bang crm_orders
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Data
@Entity
@Table(name = "crm_orders")
public class OrdersEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "order_id")
    private Long orderId;

    @Column(name = "order_no")
    private String orderNo;

    @Column(name = "customer_id")
    private Long customerId;

    @Column(name = "order_date")
    @Temporal(javax.persistence.TemporalType.DATE)
    private Date orderDate;

    @Column(name = "total_amount")
    private Long totalAmount;

    @Column(name = "discount_amount")
    private Long discountAmount;

    @Column(name = "discount_code")
    private String discountCode;

    @Column(name = "final_amount")
    private Long finalAmount;

    @Column(name = "tax_amount")
    private Long taxAmount;

    @Column(name = "tax_rate")
    private Long taxRate;

    @Column(name = "sale_staff_id")
    private Long saleStaffId;

    @Column(name = "province_id")
    private String provinceId;

    @Column(name = "district_id")
    private String districtId;

    @Column(name = "ward_id")
    private String wardId;

    @Column(name = "village_address")
    private String villageAddress;

    @Column(name = "introducer_id")
    private Long introducerId;

    @Column(name = "caregiver_id")
    private Long caregiverId;


}
