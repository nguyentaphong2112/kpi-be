package vn.kpi.models.beans;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceHeaderBean {
    private String servicePath;
    private String httpMethod;
    private String clientMessageId;
    private String transactionId;

    private Date messageTimeStamp;
    private String sourceAppId;
    private String sourceAppIp;
    private String destAppIp;
    private int destAppPort;
    private String httpPath;
    private String serviceMessageId;
    private String authUser;
    @JsonIgnore
    private String authorization;
    private Object authenticationUser;


    public String getServiceMessageId() {
        return (sourceAppId != null || clientMessageId != null)
                ? String.format("%s-%s", sourceAppId, clientMessageId)
                : null;
    }
}
