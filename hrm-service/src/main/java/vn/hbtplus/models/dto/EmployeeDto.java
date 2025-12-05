package vn.hbtplus.models.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class EmployeeDto {
    private String employeeCode;
    private String fullName;
    private String jobName;
    private String organizationName;
    private String eduPromotionName; //học hàm
    private String eduMajorLevelName; //trình độ

    private Long employeeId;
}
