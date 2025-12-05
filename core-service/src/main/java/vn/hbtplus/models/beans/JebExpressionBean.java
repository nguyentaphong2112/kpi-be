package vn.hbtplus.models.beans;

import lombok.Data;
import lombok.NoArgsConstructor;
import vn.hbtplus.utils.Utils;

@Data
@NoArgsConstructor
public class JebExpressionBean {
    private String variable;
    private String expression;
    private Object value;
    private Object defaultValue;
    private String errorInfo;

    public boolean hasError(){
        return !Utils.isNullOrEmpty(errorInfo);
    }

    public JebExpressionBean(String variable, Object value) {
        this.variable = variable;
        this.value = value;
    }
}
