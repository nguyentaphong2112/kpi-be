/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.services;

import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import vn.hbtplus.exceptions.BaseAppException;
import vn.hbtplus.exceptions.RecordNotExistsException;
import vn.hbtplus.models.request.EquipmentsRequest;
import vn.hbtplus.models.response.BaseResponseEntity;
import vn.hbtplus.models.response.EquipmentsResponse;
import vn.hbtplus.models.response.TableResponseEntity;

import java.util.List;

/**
 * Lop interface service ung voi bang fpn_equipments
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

public interface EquipmentsService {

    TableResponseEntity<EquipmentsResponse> searchData(EquipmentsRequest.SearchForm dto);

    TableResponseEntity<EquipmentsResponse> searchListData(EquipmentsRequest.SearchForm dto);

    ResponseEntity saveData(EquipmentsRequest.SubmitForm dto) throws BaseAppException;

    ResponseEntity deleteData(Long id) throws RecordNotExistsException;

    BaseResponseEntity<EquipmentsResponse> getDataById(Long id) throws RecordNotExistsException;

    ResponseEntity<Object> exportData(EquipmentsRequest.SearchForm dto) throws Exception;

    List<EquipmentsResponse> getAllEquipment();

    List<EquipmentsResponse> getListEquipment(EquipmentsRequest.SearchForm dto);

    ResponseEntity<Object> downloadTemplate() throws Exception;

    Object processImport(MultipartFile file) throws Exception;

    List<EquipmentsResponse> getEquipmentByNames(List<String> equipmentNames);

    List<EquipmentsResponse> getEquipmentByIds(List<Long> equipmentIds);

    List<EquipmentsResponse> getListByType(Long equipmentTypeId);
}
