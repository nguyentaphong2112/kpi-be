package vn.kpi.models.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.kpi.constants.BaseConstants;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
public class UserResponse {
    @Data
    @NoArgsConstructor
    @Schema(name = "UserResponseTokenResponse")
    public static class TokenResponse {
        private String accessToken;
        private String refreshToken;

        private UserInfo userInfo;

    }

    @Data
    @NoArgsConstructor
    @Schema(name = "UserResponsePermissionResponse")
    public static class PermissionResponse {
        private String resourceCode;
        private List<String> scopes = new ArrayList<>();

        public PermissionResponse(String resourceCode, String scope) {
            this.resourceCode = resourceCode;
            scopes.add(scope);
        }
    }

    @Data
    @NoArgsConstructor
    @Schema(name = "UserResponseUserInfo")
    public static class UserInfo{
        private String loginName;
        private String employeeCode;
        private String fullName;
        private String email;
        private String mobileNumber;
    }

    @Data
    @NoArgsConstructor
    @Schema(name = "UserResponseSearchResult")
    public static class SearchResult {
        private Long userId;
        private String loginName;
        private String employeeCode;
        private String fullName;
        private String email;
        private String status;
        private String mobileNumber;
        private String createdBy;

        @JsonFormat(pattern = BaseConstants.COMMON_DATETIME_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
        private Date createdTime;
        private String modifiedBy;

        @JsonFormat(pattern = BaseConstants.COMMON_DATETIME_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
        private Date modifiedTime;
    }
    @Data
    @NoArgsConstructor
    @Schema(name = "UserResponseDetailBean")
    public static class DetailBean {
        private Long userId;
        private String loginName;
        private String employeeCode;
        private String fullName;
        private String email;
    }
}
