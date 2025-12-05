package vn.hbtplus.models.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ParticiDTO {
    private Long employeeId;
    private String roleId;
}
