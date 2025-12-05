package vn.hbtplus.models.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RequestApproversDTO {
    private Long requestApproverId;

    private Long requestId;

    private Long employeeId;

    private Long empApproverId;

    private Long orderNumber;

    private String comment;

}
