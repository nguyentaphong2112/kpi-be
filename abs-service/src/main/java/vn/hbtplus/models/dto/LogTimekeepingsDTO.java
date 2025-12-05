package vn.hbtplus.models.dto;

import lombok.Data;

import java.util.Date;

@Data
public class LogTimekeepingsDTO {
    private Long employeeId;
    private String dataBefore;
    private String dataAfter;
    private Date dateTimekeeping;

    public LogTimekeepingsDTO() {
    }

    public LogTimekeepingsDTO(Long employeeId, String dataBefore, String dataAfter, Date dateTimekeeping) {
        this.employeeId = employeeId;
        this.dataBefore = dataBefore;
        this.dataAfter = dataAfter;
        this.dateTimekeeping = dateTimekeeping;
    }
}
