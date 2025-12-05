package vn.hbtplus.models;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;
import vn.hbtplus.utils.StrimDeSerializer;

import java.util.List;

@Data
public class BaseSearchRequest {
//    private Integer page = 0;
    private Integer startRecord = 0;
    private Integer pageSize = 10;

    private Long id;

    @JsonDeserialize(using = StrimDeSerializer.class)
    private String keySearch;
    private List<Long> selectedValue;
    private List<String> valueFilter;
    private List<String> listStatus;
    private List<String> listType;
    private List<String> listEmpTypeCode;
}
