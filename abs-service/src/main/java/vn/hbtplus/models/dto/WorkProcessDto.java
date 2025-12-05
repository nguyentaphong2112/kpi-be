package vn.hbtplus.models.dto;

import lombok.Data;

import java.util.Date;

@Data
public class WorkProcessDto {
    private Long employeeId;
    private Date startDate;
    private Date endDate;
}
