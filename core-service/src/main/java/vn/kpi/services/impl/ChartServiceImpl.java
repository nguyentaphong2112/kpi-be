package vn.kpi.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import vn.kpi.feigns.ChartFeignClient;
import vn.kpi.models.dto.ChartConfigDto;
import vn.kpi.repositories.impl.UtilsRepository;
import vn.kpi.services.ChartService;
import vn.kpi.utils.Utils;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ChartServiceImpl implements ChartService {
    private final ChartFeignClient chartFeignClient;
    private final HttpServletRequest request;
    private final UtilsRepository utilsRepository;

    @Override
    public Object getChartData(Long id) {
        //Lay thong tin cua chart
        ChartConfigDto chartConfigDto = chartFeignClient.getReportConfig(Utils.getRequestHeader(request), id).getData();
        if (chartConfigDto != null && chartConfigDto.getSqlQuery() != null) {
            if (ChartConfigDto.CHART_TYPE.PIE.equals(chartConfigDto.getType())) {
                Map<String, Object> params = new HashMap<>();
                params.put("userName", Utils.getUserNameLogin());
                return utilsRepository.getListData(chartConfigDto.getSqlQuery(), params, ChartConfigDto.ChartData.class);
            } else {
                Map<String, Object> params = new HashMap<>();
                params.put("userName", Utils.getUserNameLogin());
                List<ChartConfigDto.ChartData> datas = utilsRepository.getListData(chartConfigDto.getSqlQuery(), params, ChartConfigDto.ChartData.class);
                List<ChartConfigDto.BarChartData> results = new ArrayList<>();
                Map<String, ChartConfigDto.BarChartData> mapValues = new HashMap<>();
                for (ChartConfigDto.ChartData data : datas) {
                    if (mapValues.get(data.getCategory()) == null) {
                        ChartConfigDto.BarChartData barChartData = new ChartConfigDto.BarChartData();
                        barChartData.setCategory(data.getCategory());
                        mapValues.put(data.getCategory(), barChartData);
                        results.add(barChartData);
                    }
                    mapValues.get(data.getCategory()).getSeries().add(data);
                }
                return results;
            }
        }
        return null;
    }
}
