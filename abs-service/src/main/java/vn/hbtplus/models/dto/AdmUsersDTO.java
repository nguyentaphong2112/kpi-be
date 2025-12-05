package vn.hbtplus.models.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AdmUsersDTO {
    private Long userId;
    private String loginName;
    private String fullName;
    private String email;
    private String mobileNumber;
    private String employeeCode;
}
