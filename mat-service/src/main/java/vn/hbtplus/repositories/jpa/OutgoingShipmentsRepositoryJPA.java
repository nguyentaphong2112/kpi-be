/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.repositories.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.hbtplus.repositories.entity.OutgoingShipmentsEntity;

import java.util.Collection;
import java.util.List;

/**
 * Lop repository JPA ung voi bang stk_outgoing_shipments
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Repository
public interface OutgoingShipmentsRepositoryJPA extends JpaRepository<OutgoingShipmentsEntity, Long> {

    List<OutgoingShipmentsEntity> findByOutgoingShipmentIdIn(Collection<Long> outgoingShipmentIds);
}
