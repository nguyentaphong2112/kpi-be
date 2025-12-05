package vn.hbtplus.models.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RequestHandoversDTO {
    private Long requestHandoverId;

    private Long requestId;

    private Long employeeId;

    private Long empHandoverId;

    private Long orderNumber;

    private String content;

}
