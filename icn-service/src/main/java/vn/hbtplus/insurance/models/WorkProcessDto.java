package vn.hbtplus.insurance.models;

import lombok.Data;

import java.util.Date;

@Data
public class WorkProcessDto {
    private Long employeeId;
    private Date endDate;
    private Date startDate;
}
