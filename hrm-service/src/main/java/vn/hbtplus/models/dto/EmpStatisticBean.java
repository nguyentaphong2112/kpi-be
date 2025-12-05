package vn.hbtplus.models.dto;

import lombok.Data;

@Data
public class EmpStatisticBean {
    private Long organizationId;
    private Integer total;
    private String type;
}
