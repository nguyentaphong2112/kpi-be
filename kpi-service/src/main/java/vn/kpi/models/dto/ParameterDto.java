package vn.kpi.models.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ParameterDto {
    private List<String> listHeadId;
    private List<String> listProbationaryId;
    private List<String> listJobFreeId;
    private List<String> listJobFreeByOrgId;
    private List<String> listOrgId;
}
