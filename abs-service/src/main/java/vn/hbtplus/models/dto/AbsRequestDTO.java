package vn.hbtplus.models.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AbsRequestDTO {

    private Long requestId;

    private Long reasonTypeId;
    private Long workdayTypeId;

    private String startTime;

    private String endTime;

    private Long employeeId;

    private Long listId;

    private Double totalHours;
}
