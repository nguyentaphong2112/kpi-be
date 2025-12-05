package vn.hbtplus.models.dto;

import lombok.Data;

import java.util.Map;

@Data
public class WorkdayTypeVariableDTO {
    Map<Long, String> mapWorkdayTypes;
    Long congNgayLe;
    Long congNghiBu;
    Long congDilam;
    Long congDilamThu7;
    Long congDihoc;
    Long congCongTac;
    Long congNghiKhongLuong;
    boolean isLog;
}
