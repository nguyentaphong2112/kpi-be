package vn.hbtplus.models.dto;

import com.aspose.cells.DateTime;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AbsOvertimeRecordDTO {

    private Long overtimeRecordId;

    private String overtimeTypeId;

    private String startTime;

    private String endTime;

    private String content;
}
