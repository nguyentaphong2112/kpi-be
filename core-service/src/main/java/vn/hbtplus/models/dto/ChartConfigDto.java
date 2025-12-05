package vn.hbtplus.models.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ChartConfigDto {
    private String code;
    private String name;
    private String type;
    private String sqlQuery;

    @Data
    public static class ChartData {
        @JsonIgnore
        private String category;
        private String label;
        private Double value;
        private String color;
    }

    @Data
    public static class BarChartData {
        private String category;
        private List<ChartData> series = new ArrayList<>();
    }

    public interface CHART_TYPE {
        String  PIE  = "PIE";
        String  BAR  = "BAR";
        String  COLUMN  = "COLUMN";
    }
}
