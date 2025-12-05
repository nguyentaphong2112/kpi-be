package vn.hbtplus.models.request;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UserRequest {
    private String loginName;
    private String fullName;
}
