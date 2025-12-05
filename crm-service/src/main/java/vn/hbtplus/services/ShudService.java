package vn.hbtplus.services;

import vn.hbtplus.models.request.ShudRequest;
import vn.hbtplus.models.response.BaseResponseEntity;

public interface ShudService {
    BaseResponseEntity<String> exportData(ShudRequest.ExportForm dto);
}
