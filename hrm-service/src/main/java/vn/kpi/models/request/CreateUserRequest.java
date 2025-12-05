package vn.kpi.models.request;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@Data
@NoArgsConstructor
public class CreateUserRequest {
    @NotBlank
    private String loginName;
    private String employeeCode;
    @NotBlank
    private String fullName;
    private String email;
    private String note;
    private String mobileNumber;
}
