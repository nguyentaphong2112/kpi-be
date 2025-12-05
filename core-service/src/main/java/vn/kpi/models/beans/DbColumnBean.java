package vn.kpi.models.beans;

import lombok.Data;

@Data
public class DbColumnBean {
    private String tableName;
    private String columnName;
    private String columnType;
    private String columnKey;
    private String columnComment;
    private String tableComment;
    private String modifyStatement;
    private String addStatement;

}
