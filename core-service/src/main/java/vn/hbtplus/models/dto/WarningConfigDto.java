package vn.hbtplus.models.dto;


import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WarningConfigDto {
    private String code;
    private String name;
    private String orgName;
    private String title;
}
