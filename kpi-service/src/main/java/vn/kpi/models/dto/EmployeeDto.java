package vn.kpi.models.dto;

import lombok.Data;

@Data
public class EmployeeDto {
    private String promotionRankName;
    private String jobName;
    private String jobNameManage;
    private String positionConcurrent;
    private Long employeeEvaluationId;
}
