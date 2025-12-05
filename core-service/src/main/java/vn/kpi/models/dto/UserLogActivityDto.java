package vn.kpi.models.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserLogActivityDto {
    private String loginName;
    private String method;
    private String data;
    private String uri;
    private String ipAddress;
}
