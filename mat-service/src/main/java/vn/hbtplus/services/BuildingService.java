package vn.hbtplus.services;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

public interface BuildingService {
    List<Map<String, Object>> createListMapBuilding(HttpServletRequest request);
    Map<String, Long> createMapBuildingAndId(HttpServletRequest request);
}
