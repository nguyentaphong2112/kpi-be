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
 * Lop entity ung voi bang crm_order_details
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Data
@Entity
@Table(name = "crm_order_details")
public class OrderDetailsEntity extends BaseEntity {

    @Id    @GeneratedValue(strategy = GenerationType.IDENTITY)    @Basic(optional = false)    @Column(name = "order_detail_id")    private Long orderDetailId;    @Column(name = "order_id")    private Long orderId;    @Column(name = "product_id")    private Long productId;    @Column(name = "quantity")    private Double quantity;    @Column(name = "discount")    private Double discount;    @Column(name = "discount_type")    private String discountType;    @Column(name = "unit_price")    private Double unitPrice;    @Column(name = "total_price")    private Double totalPrice;

}
