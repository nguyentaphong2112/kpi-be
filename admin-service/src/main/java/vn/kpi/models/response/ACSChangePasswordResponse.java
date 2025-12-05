package vn.kpi.models.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
public class ACSChangePasswordResponse {
    @JsonProperty("Success")
    private Boolean isSuccess;

    @JsonProperty("Param")
    private ParamResponse paramResponse;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ParamResponse {
        @JsonProperty("Messages")
        private List<String> messages;
    }
}
