package vn.hbtplus.insurance.models;

import lombok.Data;

@Data
public class TimekeepingDto {
    private Long employeeId;
    private Double numOfDays;
    private Long workdayTypeId;
    private String workdayTypeName;
}
