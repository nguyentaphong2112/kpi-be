/*
 * Copyright (C) 2022 EcoIT. All rights reserved.
 * EcoIT. Use is subject to license terms.
 */
package vn.hbtplus.repositories.entity;

import lombok.Data;

import javax.persistence.*;
import java.util.Date;


/**
 * Lop entity ung voi bang PNS_CONFIG_SEQ_DETAILS
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Data
@Entity
@Table(name = "pns_config_seq_details")
public class ConfigSeqDetailsEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "config_seq_detail_id")
    private Long configSeqDetailId;

    @Column(name = "config_seq_contract_id")
    private Long configSeqContractId;

    @Column(name = "contract_type_id")
    private Long contractTypeId;

    @Column(name = "order_number")
    private Integer orderNumber;

}
