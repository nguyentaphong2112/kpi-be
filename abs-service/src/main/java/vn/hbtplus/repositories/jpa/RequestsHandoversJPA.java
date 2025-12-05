/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.repositories.jpa;

import feign.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import vn.hbtplus.repositories.entity.RequestHandoversEntity;
import vn.hbtplus.repositories.entity.RequestsEntity;

import java.util.List;

/**
 * Lop repository JPA ung voi bang abs_requests
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Repository
public interface RequestsHandoversJPA extends JpaRepository<RequestHandoversEntity, Long> {
    @Query("SELECT h FROM RequestHandoversEntity h " +
            "JOIN RequestsEntity r ON h.requestId = r.requestId " +
            "WHERE r.requestNo = (SELECT req.requestNo FROM RequestsEntity req WHERE req.requestId = :requestId)")
    List<RequestHandoversEntity> findByRequestNo(@Param("requestId") Long requestId);

    List<RequestHandoversEntity> findByRequestId(Long requestId);

}
