package vn.hbtplus.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import vn.hbtplus.exceptions.BaseAppException;
import vn.hbtplus.feigns.WarningConfigFeignClient;
import vn.hbtplus.models.BaseSearchRequest;
import vn.hbtplus.models.WarningDto;
import vn.hbtplus.models.dto.BaseCategoryDto;
import vn.hbtplus.models.dto.WarningConfigDto;
import vn.hbtplus.models.response.ObjectAttributesResponse;
import vn.hbtplus.models.response.TableResponseEntity;
import vn.hbtplus.repositories.impl.UtilsRepository;
import vn.hbtplus.services.WarningService;
import vn.hbtplus.utils.ExportExcel;
import vn.hbtplus.utils.I18n;
import vn.hbtplus.utils.ResponseUtils;
import vn.hbtplus.utils.Utils;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Service
@RequiredArgsConstructor
public class WarningServiceImpl implements WarningService {

    private final WarningConfigFeignClient warningConfigFeignClient;
    private final HttpServletRequest request;
    private final UtilsRepository utilsRepository;

    @Override
    public WarningDto getWarning(Long id) {
        WarningDto.Config config = warningConfigFeignClient.getConfig(Utils.getRequestHeader(request), id).getData();
        Map map = getParameter();
        Integer total = utilsRepository.queryForObject(config.getSqlQuery(), map, Integer.class);
        WarningDto warningDto = new WarningDto();
        warningDto.setId(id);
        warningDto.setTotal(total);
        warningDto.setTitle(config.getTitle());
        warningDto.setShow(!"Y".equalsIgnoreCase(config.getIsMustPositive()) || total > 0);
        return warningDto;
    }

    @Override
    public TableResponseEntity<Object> searchDataPopUp(BaseSearchRequest dto) {
        List<ObjectAttributesResponse> objectAttributesResponseList =
                utilsRepository.getListAttributes(dto.getId(), "sys_warning_configs");
        String queryTableValue = objectAttributesResponseList.stream()
                .filter(attr -> "QUERY_TABLE".equals(attr.getAttributeCode()))
                .map(ObjectAttributesResponse::getAttributeValue)
                .findFirst()
                .orElse(null);
        if (!Utils.isNullOrEmpty(queryTableValue)) {
            Map map = new HashMap<>();
            return ResponseUtils.ok(utilsRepository.getListPagination(queryTableValue, map, dto, TreeMap.class));
        }
        return null;
    }

    @Override
    public ResponseEntity<Object> exportData(Long id, WarningConfigDto dto) throws Exception {
        String pathTemplate = "template/BM_Xuat_DS_du_lieu.xlsx";
        ExportExcel dynamicExport = new ExportExcel(pathTemplate, 3, true);
        List<ObjectAttributesResponse> objectAttributesResponseList =
                utilsRepository.getListAttributes(id, "sys_warning_configs");
        String queryTableValue = objectAttributesResponseList.stream()
                .filter(attr -> "QUERY_TABLE".equals(attr.getAttributeCode()))
                .map(ObjectAttributesResponse::getAttributeValue)
                .findFirst()
                .orElse(null);
        String configTableValue = objectAttributesResponseList.stream()
                .filter(attr -> "CONFIG_TABLE".equals(attr.getAttributeCode()))
                .map(ObjectAttributesResponse::getAttributeValue)
                .findFirst()
                .orElse("");
        List<Map<String, Object>> listDataExport = utilsRepository.getListData(queryTableValue, new HashMap<>());
        if (Utils.isNullOrEmpty(listDataExport)) {
            throw new BaseAppException(I18n.getMessage("global.notFound"));
        }
        if (!Utils.isNullOrEmpty(configTableValue)) {
            List<BaseCategoryDto> listColumnTable = Utils.fromJsonList(configTableValue, BaseCategoryDto.class);
            dynamicExport.setText("Danh sách " + dto.getTitle(), 0, 0);
            dynamicExport.mergeCell(0, 0, 0, listColumnTable.size());
            dynamicExport.setCellFormat(0, 0, 0, 0, ExportExcel.CENTER_FORMAT);
            dynamicExport.setCellFormat(0, 0, 0, 0, ExportExcel.BOLD_FORMAT);
            dynamicExport.setText("STT", 0);
            dynamicExport.setText("${stt}", 0, 3);
            dynamicExport.setCellFormat(3, 0, 3, 0, ExportExcel.CENTER_FORMAT);
            dynamicExport.setColumnWidth(0, 8);
            dynamicExport.setCellFormat(0, listColumnTable.size(), ExportExcel.CENTER_FORMAT);
            dynamicExport.setCellFormat(0, listColumnTable.size(), ExportExcel.BOLD_FORMAT);
            dynamicExport.setCellFormat(2, 0, 3, listColumnTable.size(), ExportExcel.BORDER_FORMAT);
            int col = 1;
            for (BaseCategoryDto column : listColumnTable) {
                dynamicExport.setText(column.getLabel(), col);
                dynamicExport.setText("${" + column.getValue() + "}", col, 3);
                dynamicExport.setColumnWidth(col, 20);
                col++;
            }
        }
        dynamicExport.replaceKeys(listDataExport);
        return ResponseUtils.ok(dynamicExport, "BM_Xuat_DS_chi_so_KPI.xlsx");
    }

    private Map getParameter() {
        Map map = new HashMap<>();
        map.put("userName", Utils.getUserNameLogin());
        return map;
    }
}
