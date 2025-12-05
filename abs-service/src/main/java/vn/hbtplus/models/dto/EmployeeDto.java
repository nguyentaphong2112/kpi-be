package vn.hbtplus.models.dto;

import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class EmployeeDto {
    private Long employeeId;
    private String employeeCode;
    private String fullName;
    private Long organizationId;
    private Long positionId;
    private Long jobId;
    private Long empTypeId;
    private Date startDate;
    private Date endDate;
    private Date dateOfBirth;
    private String orgName;
    private String positionName;
    private String empTypeName;
}
