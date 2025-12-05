package vn.hbtplus.models;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
public class WarningDto {
    private Long id;
    private String title;
    private Integer total;
    private boolean isShow = true;
    @Data
    @NoArgsConstructor
    public static class Config {
        private Long warningConfigId;
        private String resource;
        private String sqlQuery;
        private String title;
        private String isMustPositive;
    }
}
