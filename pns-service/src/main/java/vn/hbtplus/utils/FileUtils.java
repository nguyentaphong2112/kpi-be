/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vn.hbtplus.utils;

import com.aspose.words.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author tudd
 */
public class FileUtils {

    private static Object[] getRowTable(TableCollection tables, List<String> listParams) {
        Row rowBase = null;
        Table tableBase = null;
        for (Table table : tables) {
            RowCollection rows = table.getRows();
            for (Row row : rows) {
                CellCollection cells = row.getCells();
                for (Cell cell : cells) {
                    for (String param : listParams) {
                        if (cell.getText().toUpperCase().contains("${" + param.toUpperCase() + "}")) {
                            if (cell.getTables().getCount() > 0) {
                                Object[] a = getRowTable(cell.getTables(), listParams);
                                if (a != null) {
                                    return a;
                                }
                            }
                            rowBase = row;
                            break;
                        }
                    }
                    if (rowBase != null) {
                        break;
                    }
                }
                if (rowBase != null) {
                    break;
                }
            }
            if (rowBase != null) {
                tableBase = table;
                break;
            }
        }
        if (rowBase != null) {
            return new Object[]{rowBase, tableBase};
        } else {
            return null;
        }
    }

    /**
     * Ham thuc hien replace bang trong file doc
     *
     * @param doc    document
     * @param params danh sach du lieu
     * @throws Exception
     */
    public static void aposeReplaceKeys(Document doc, List<Map<String, Object>> params)
            throws Exception {
        if (Utils.isNullOrEmpty(params)) {
            return;
        }
        TableCollection tables = doc.getFirstSection().getBody().getTables();
        List<String> listParams = new ArrayList<>(params.get(0).keySet());
        Object[] rowTable = getRowTable(tables, listParams);
        if (rowTable != null) {
            Row rowBase = (Row) rowTable[0];
            Table tableBase = (Table) rowTable[1];
            for (Map<String, Object> mapParams : params) {
                Row newRow = (Row) rowBase.deepClone(true);
                for (String keySet : mapParams.keySet()) {
                    aposeReplaceText(doc, newRow, "${" + keySet + "}", mapParams.get(keySet));
                }
                tableBase.insertBefore(newRow, rowBase);
            }
            rowBase.remove();
        }
    }

    public static void aposeReplaceKeys(Document doc, Map<String, Object> mapParams)
            throws Exception {
        if (mapParams == null) {
            return;
        }
        for (String keySet : mapParams.keySet()) {
            String placeHolder;
            Object object = mapParams.get(keySet);
            if (object == null) {
                placeHolder = "";
            } else {
                if (object instanceof BigDecimal) {
                    placeHolder = Utils.formatNumber(((BigDecimal) object).doubleValue());
                } else if (object instanceof Double) {
                    placeHolder = Utils.formatNumber(((Double) object));
                } else if (object instanceof Long) {
                    placeHolder = Utils.formatNumber(((Long) object));
                } else {
                    placeHolder = String.valueOf(object);
                    placeHolder = placeHolder.replaceAll("\r\n", ControlChar.LINE_BREAK);
                }
            }
            doc.getRange().replace("${" + keySet + "}", placeHolder, false, false);
        }
    }

    public static void aposeReplaceText(Document doc, Row row, String key, Object object) throws Exception {
        String placeHolder;
        if (object == null) {
            placeHolder = "";
        } else {
            if (object instanceof BigDecimal) {
                placeHolder = Utils.formatNumber(((BigDecimal) object).doubleValue());
            } else if (object instanceof Double) {
                placeHolder = Utils.formatNumber(((Double) object));
            } else if (object instanceof Long) {
                placeHolder = Utils.formatNumber(((Long) object));
            } else {
                placeHolder = String.valueOf(object);
                placeHolder = placeHolder.replaceAll("\r\n", ControlChar.LINE_BREAK);
            }
        }
        if (row.getRange().replace(key, placeHolder, false, false) == 0) {
            doc.getRange().replace(key, placeHolder, false, false);
        }
    }

}
