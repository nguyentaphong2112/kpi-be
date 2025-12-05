package vn.kpi.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import vn.kpi.utils.ImportExcel;

import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ImportResultDTO {

    private String errorFile;
    private List<ImportExcel.ImportErrorBean> errorList;
}
