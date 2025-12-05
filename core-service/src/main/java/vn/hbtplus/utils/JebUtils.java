package vn.hbtplus.utils;

import org.nfunk.jep.JEP;
import vn.hbtplus.exceptions.BaseAppException;
import vn.hbtplus.models.beans.JebExpressionBean;
import vn.hbtplus.utils.formula.IsNull;
import vn.hbtplus.utils.formula.NVL;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JebUtils {
    private final JEP jep;
    private final List<JebExpressionBean> expressionList;

    public JebUtils() {
        this.expressionList = new ArrayList();
        this.jep = new JEP();
        jep.addStandardFunctions();
        jep.addFunction("isNull", new IsNull());
        jep.addFunction("nvl", new NVL());
        jep.setAllowUndeclared(false);
        jep.setAllowAssignment(false);
        jep.setImplicitMul(true);
    }

    public void clear() {
        expressionList.clear();
    }

    public void addVariables(Map<String, Object> mapVariables) {
        mapVariables.forEach((key, value) -> {
            expressionList.add(new JebExpressionBean(key, value));
        });
    }

    public void addVariable(String variable, Object value) {
        expressionList.add(new JebExpressionBean(variable, value));
    }

    public void addExpression(List<JebExpressionBean> el) {
        if (!Utils.isNullOrEmpty(el)) {
            expressionList.addAll(el);
        }
    }

    public void addExpression(JebExpressionBean el) {
        if (el != null) {
            expressionList.add(el);
        }
    }

    public void calculate() {
        for (JebExpressionBean e : expressionList) {
            if (Utils.isNullOrEmpty(e.getExpression())) {
                jep.addVariable(e.getVariable(), Utils.NVL(e.getValue(), e.getDefaultValue()));
            } else {
                jep.parseExpression(e.getExpression());
                if (jep.hasError()) {
                    throw new BaseAppException("JEB_FORMULA_ERROR", MessageFormat.format("ERROR when calculate variable {0}: {1}", e.getVariable(), jep.getErrorInfo()));
                }
                Object value = jep.getValueAsObject();
                e.setValue(value);
                jep.addVariable(e.getVariable(), value);
            }
        }
    }

    public void validateExpressions(List<String> primeVariables, List<JebExpressionBean> expressionList) {
        final Double doubleDefaultValue = 1.0;
        primeVariables.forEach(variable -> {
            jep.addVariable(variable, doubleDefaultValue);
        });
        expressionList.forEach(item -> {
            if (Utils.isNullOrEmpty(item.getExpression())) {
                jep.addVariable(item.getVariable(), Utils.NVL(item.getValue(), item.getDefaultValue()));
            } else {
                jep.parseExpression(item.getExpression());
                if (jep.hasError()) {
                    item.setErrorInfo(jep.getErrorInfo());
                }
                jep.addVariable(item.getVariable(), doubleDefaultValue);
            }
        });
    }

    public Object calculate(String expression) {
        calculate();
        jep.parseExpression(expression);
        return jep.getValueAsObject();
    }

    public static void main(String[] args) {
        JebUtils jebUtils = new JebUtils();
        Map map = new HashMap();
        map.put("he_so_luong", 2.34);
        map.put("luong_co_ban", 4400000);
        map.put("he_so_chuc_vu", 0.6);

        jebUtils.addVariables(map);
        System.out.println(Utils.formatNumber(
                jebUtils.calculate("if(he_so_luong >0, (he_so_luong + he_so_chuc_vu) * luong_co_ban, 0)"),"###,###")
        );
    }
}
