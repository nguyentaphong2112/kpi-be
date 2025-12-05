package vn.hbtplus.services;

import java.util.List;
import java.util.Map;

public interface CommonUtilsService {
    String getFullAddress(String currentAddress, String wardId, String districtId, String provinceId);

    byte[] printCardPdf(List<String> listParams, List<Map<String, Object>> listMapParams, String fileId, String templatePath) throws Exception;
}
