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
import lombok.NoArgsConstructor;
import vn.hbtplus.models.dto.OrderDetailDto;
import vn.hbtplus.utils.Utils;

import javax.validation.constraints.NotNull;
import java.util.Date;
import javax.persistence.Temporal;


/**
 * Lop entity ung voi bang crm_order_payables
 *
 * @author tudd
 * @version 1.0
 * @since 1.0
 */

@Data
@Entity
@Table(name = "crm_order_payables")
@NoArgsConstructor
public class OrderPayablesEntity extends BaseEntity {

    public OrderPayablesEntity(Date periodDate, OrderDetailDto dto) {
        this.customerId = dto.getCustomerId();
        this.orderId = dto.getOrderId();
        this.productId = dto.getProductId();
        this.periodDate = periodDate;
        this.paymentDate = dto.getPaymentDate();
        this.orderAmount = dto.getOrderAmount();
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "order_payable_id")
    private Long orderPayableId;

    @Column(name = "order_id")
    private Long orderId;

    @Column(name = "product_id")
    private Long productId;

    @Column(name = "customer_id")
    private Long customerId;

    @Column(name = "period_date")
    @Temporal(javax.persistence.TemporalType.DATE)
    private Date periodDate;

    @Column(name = "referral_fee")
    private Long referralFee;

    @Column(name = "care_fee")
    private Long careFee;

    @Column(name = "order_amount")
    private Long orderAmount;

    @Column(name = "welfare_fee")
    private Long welfareFee;

    @Column(name = "status_id")
    private String statusId;

    @Column(name = "approved_note")
    private String approvedNote;

    @Column(name = "receiver_id")
    private Long receiverId;
    @Column(name = "payment_date")
    private Date paymentDate;

    public boolean existFee() {
        if(Utils.NVL(careFee) < 0){
            careFee = 0l;
        }
        if(Utils.NVL(referralFee) < 0){
            referralFee = 0l;
        }
        if(Utils.NVL(welfareFee) < 0){
            welfareFee = 0l;
        }
        return Utils.NVL(careFee) > 0 ||
               Utils.NVL(referralFee) > 0 ||
               Utils.NVL(welfareFee) > 0;
    }


    public interface STATUS {
        String PHE_DUYET = "PHE_DUYET";
        String CHO_PHE_DUYET = "CHO_PHE_DUYET";
        String TU_CHOI = "TU_CHOI";
    }
}
