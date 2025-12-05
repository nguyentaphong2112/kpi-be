package vn.hbtplus.models.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ContractTypeResponse {
    private Long contractTypeId;
    private String code;
    private String name;
    private String classifyCode;
}
