/*
 * Copyright (C) 2022 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.repositories.entity;

import lombok.Data;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


/**
 * Lop entity ung voi bang stk_transferring_shipments
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Data
@Entity
@Table(name = "mat_transferring_shipments")
public class TransferringShipmentsEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "transferring_shipment_id")
    private Long transferringShipmentId;

    @Column(name = "warehouse_id")
    private Long warehouseId;

    @Column(name = "transferring_date")
    @Temporal(javax.persistence.TemporalType.DATE)
    private Date transferringDate;

    @Column(name = "received_warehouse_id")
    private Long receivedWarehouseId;

    @Column(name = "transferred_employee_id")
    private Long transferredEmployeeId;

    @Column(name = "approved_by")
    private String approvedBy;

    @Column(name = "approved_time")
    @Temporal(javax.persistence.TemporalType.DATE)
    private Date approvedTime;

    @Column(name = "picking_no")
    private String pickingNo;

    @Column(name = "status_id")
    private String statusId;

    @Column(name = "name")
    private String name;

    @Column(name = "received_employee_id")
    private Long receivedEmployeeId;

    @Column(name = "created_employee_id")
    private Long createdEmployeeId;

    @Column(name = "note")
    private String note;

    @Column(name = "approved_note")
    private String approvedNote;

    public interface STATUS {
        String DU_THAO = "DU_THAO";
        String CHO_DUYET = "CHO_DUYET";
        String PHE_DUYET = "PHE_DUYET";
        String TU_CHOI = "TU_CHOI";
    }

    public static final Map<String, String> listStatusMap = new HashMap<>();

    static {
        listStatusMap.put(STATUS.DU_THAO, "Dự thảo");
        listStatusMap.put(STATUS.CHO_DUYET, "Chờ phê duyệt");
        listStatusMap.put(STATUS.PHE_DUYET, "Đã phê duyệt");
        listStatusMap.put(STATUS.TU_CHOI, "Đã từ chối");
    }
}
