package vn.hbtplus.models.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ACSLoginResponse {
    @JsonProperty("Data")
    private DataResponse data;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DataResponse{
        @JsonProperty("User")
        private UserResponse user;

        @JsonProperty("TokenCode")
        private String tokenCode;
    }
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserResponse {
        @JsonProperty("LoginName")
        private String loginName;
        @JsonProperty("UserName")
        private String fullName;
        @JsonProperty("Mobile")
        private String mobileNumber;
        @JsonProperty("Email")
        private String email;
    }
}
