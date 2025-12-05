package vn.hbtplus.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import vn.hbtplus.utils.Utils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Data
public class PermissionDataDto implements Serializable {
    private List<Long> orgIds = new ArrayList<>();
    private List<Long> empTypeIds = new ArrayList<>();

    @JsonIgnore
    public String getKey() {
        String key = "";
        if (Utils.isNullOrEmpty(orgIds)) {
            key = "null";
        } else {
            Collections.sort(orgIds);
            key = orgIds.toString();
        }
        key = key + "-";
        if (Utils.isNullOrEmpty(empTypeIds)) {
            key = key + "null";
        } else {
            Collections.sort(empTypeIds);
            key = key + empTypeIds.toString();
        }
        return key;
    }

}
