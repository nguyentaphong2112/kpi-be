package vn.kpi.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.util.List;

@Data
public class BaseDataTableDto<T> {
    public Integer pageIndex;
    public Integer pageSize;
    public Long total;
    List<T> listData;

    @JsonIgnore
    public boolean isEmpty() {
        return listData == null || listData.isEmpty();
    }
}
