package vn.hbtplus.models.dto;


import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.hbtplus.utils.Utils;

@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EmployeeDto {
    private Long employeeId;
    private String employeeCode;
    private String employeeName;
    private String identityNo;

}
