package vn.kpi.models;

import java.util.List;

public interface UserTokenDto {
    String getLoginName();

    String getId();

    String getIssuer();

    String getSubject();

    String getAudience();
    String getEmployeeCode();

    List<String> getRoleCodeList();

    Long getUserId();
}
