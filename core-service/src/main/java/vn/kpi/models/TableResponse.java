package vn.kpi.models;

import lombok.Data;
import vn.kpi.utils.I18n;

import java.util.Date;

@Data
public class TableResponse<T> {

    private Date timestamp;
    private String clientMessageId;
    private String message;
    private String code;
    private BaseDataTableDto<T> data;
    public int status = 1;

    public TableResponse(String clientMessageId, BaseDataTableDto obj) {
        this.setTimestamp(new Date());
        this.code = code == null ? "SUCCESS" : code;
        this.message = I18n.getMessage("global.success");
        this.setClientMessageId(clientMessageId);
        if(obj != null){
            this.data = obj;
        }
    }
}
