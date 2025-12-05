package vn.hbtplus.models.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.hbtplus.models.BaseSearchRequest;

import javax.validation.constraints.NotBlank;
import java.util.List;

@Data
@NoArgsConstructor
public class UserRequest {
    @Data
    @NoArgsConstructor
    @Schema(name = "UserRequestSearchForm")
    public static class SearchForm extends BaseSearchRequest {
        private String loginName;
        private String fullName;
        private String keySearch;
    }

    @Data
    @NoArgsConstructor
    @Schema(name = "UserRequestLoginForm")
    public static class LoginForm {
        @NotBlank
        private String loginName;
        @NotBlank
        private String password;

    }

    @Data
    @NoArgsConstructor
    @Schema(name = "UserRequestRefreshTokenForm")
    public static class RefreshTokenForm {
        @NotBlank
        private String token;
    }

    @Data
    @NoArgsConstructor
    @Schema(name = "UserRequestChangePassForm")
    public static class ChangePassForm {
        @NotBlank
        private String loginName;
        @NotBlank
        private String password;
        @NotBlank
        private String captcha;
        @NotBlank
        private String retypePassword;
        @NotBlank
        private String oldPassword;

    }

    @Data
    @NoArgsConstructor
    @Schema(name = "UserRequestSubmitForm")
    public static class SubmitForm {
//        private Long userId;
        @NotBlank
        private String loginName;
        private String employeeCode;
        @NotBlank
        private String fullName;
        private String email;
        private String note;
        private String mobileNumber;
        private String password;
        private String idNo;

        private List<String> defaultRoles;

    }

    @Data
    @NoArgsConstructor
    @Schema(name = "UserRequestSubmitForm")
    public static class SubmitFormToSso {
        private Long userId;
        @NotBlank
        private String loginName;
        private String employeeCode;
        @NotBlank
        private String fullName;
        private String email;
        private String note;
        private String mobileNumber;
        private String password;

        private List<String> defaultRoles;

    }
}
