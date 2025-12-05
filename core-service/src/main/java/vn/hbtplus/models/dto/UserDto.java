package vn.hbtplus.models.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import vn.hbtplus.models.UserTokenDto;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserDto implements UserTokenDto {

    private String loginName;
    private Long userId;
    private String email;
    private String mobileNumber;
    @JsonIgnore
    private String password;
    private String fullName;
    private String status;
    private String employeeCode;
    private List<String> roleCodeList;
    @Override
    public String getId() {
        return loginName;
    }

    @Override
    public String getIssuer() {
        return null;
    }

    @Override
    public String getSubject() {
        return null;
    }

    @Override
    public String getAudience() {
        return null;
    }

    @Override
    public List<String> getRoleCodeList() {
        return roleCodeList;
    }
}
