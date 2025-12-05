/*
 * Copyright (C) 2022 EcoIT. All rights reserved.
 * EcoIT. Use is subject to license terms.
 */
package vn.hbtplus.services;

import org.springframework.web.multipart.MultipartFile;
import vn.hbtplus.models.dto.ConfigSeqDetailsDTO;
import vn.hbtplus.models.response.BaseResponseEntity;
import vn.hbtplus.models.response.ConfigSeqDetailsResponse;
import vn.hbtplus.models.response.TableResponseEntity;

import java.util.List;

/**
 * Lop interface service ung voi bang PNS_CONFIG_SEQ_DETAILS
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

public interface ConfigSeqDetailsService {

    TableResponseEntity<ConfigSeqDetailsResponse> searchData(ConfigSeqDetailsDTO dto);

    BaseResponseEntity<Object> saveData(ConfigSeqDetailsDTO dto, List<MultipartFile> files);

    BaseResponseEntity<Object> deleteData(Long id);

    BaseResponseEntity<Object> getDataById(Long id);

}
