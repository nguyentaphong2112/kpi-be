
package vn.kpi.utils;


import lombok.extern.slf4j.Slf4j;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Đọc file Excel (.xls hoặc .xlsx) bằng Apache POI 5.x (Java 17 compatible)
 */
@Slf4j
public class ReadExelFile {

    public static List<String[]> readExcelFile(InputStream inputStream, int length) throws IOException {
        try {
            return readWorkbook(new XSSFWorkbook(inputStream), length);
        } catch (Exception e) {
            return readWorkbook(new HSSFWorkbook(inputStream), length);
        }
    }

    /**
     * Tự động xác định loại file (xls/xlsx) và đọc toàn bộ sheet đầu tiên
     */
    public static List<String[]> readExcelFile(File file, int numOfColumns) throws IOException {
        try (InputStream inputStream = new FileInputStream(file)) {
            if (file.getName().toLowerCase().endsWith(".xlsx")) {
                return readWorkbook(new XSSFWorkbook(inputStream), numOfColumns);
            } else if (file.getName().toLowerCase().endsWith(".xls")) {
                return readWorkbook(new HSSFWorkbook(inputStream), numOfColumns);
            } else {
                throw new IllegalArgumentException("Định dạng file không hợp lệ (chỉ hỗ trợ .xls hoặc .xlsx): " + file.getName());
            }
        }
    }

    /**
     * Hàm đọc dữ liệu từ Workbook (Sheet đầu tiên)
     */
    private static List<String[]> readWorkbook(Workbook workbook, int numOfColumns) {
        List<String[]> result = new ArrayList<>();
        Sheet sheet = workbook.getSheetAt(0);
        int currentRowIndex = 0;

        for (Row row : sheet) {
            // nếu có dòng trống giữa chừng thì vẫn thêm mảng rỗng
            while (currentRowIndex < row.getRowNum()) {
                result.add(new String[numOfColumns]);
                currentRowIndex++;
            }

            String[] rowData = new String[numOfColumns];
            for (int col = 0; col < numOfColumns; col++) {
                Cell cell = row.getCell(col, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
                if (cell != null) {
                    rowData[col] = getCellValue(cell);
                } else {
                    rowData[col] = "";
                }
            }
            result.add(rowData);
            currentRowIndex++;
        }

        // Nếu dòng cuối của file không đủ rows, bạn có thể thêm logic bổ sung tại đây
        return result;
    }

    /**
     * Chuyển cell về String (POI 5.x dùng CellType thay vì HSSFCell constants)
     */
    private static String getCellValue(Cell cell) {
        String value = "";
        try {
            switch (cell.getCellType()) {
                case STRING:
                    value = Utils.convertCp1258ToUTF8(cell.getStringCellValue().trim());
                    break;

                case BOOLEAN:
                    value = Boolean.toString(cell.getBooleanCellValue());
                    break;

                case NUMERIC:
                    if (DateUtil.isCellDateFormatted(cell)) {
                        value = Utils.formatDate(cell.getDateCellValue());
                    } else {
                        if (cell.getCellStyle() != null
                            && cell.getCellStyle().getDataFormatString() != null
                            && cell.getCellStyle().getDataFormatString().contains("%")) {
                            value = String.format("%.2f%%", cell.getNumericCellValue() * 100);
                        } else {
                            value = Utils.formatNumber(cell.getNumericCellValue(), "######.#############");
                        }
                    }
                    break;

                case FORMULA:
                    value = handleFormulaCell(cell);
                    break;

                case BLANK:
                    value = "";
                    break;

                case ERROR:
                    value = "#ERROR";
                    break;

                default:
                    value = "";
                    break;
            }
        } catch (Exception ex) {
            log.warn("Lỗi đọc ô [{}-{}]: {}", cell.getRowIndex(), cell.getColumnIndex(), ex.getMessage());
        }
        return value;
    }

    /**
     * Xử lý công thức, lấy giá trị cache theo kiểu kết quả
     */
    private static String handleFormulaCell(Cell cell) {
        FormulaEvaluator evaluator = cell.getSheet().getWorkbook().getCreationHelper().createFormulaEvaluator();
        CellValue evaluatedValue = evaluator.evaluate(cell);
        if (evaluatedValue == null) return "";

        switch (evaluatedValue.getCellType()) {
            case STRING:
                return evaluatedValue.getStringValue().trim();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return Utils.formatDate(cell.getDateCellValue());
                }
                return Utils.formatNumber(evaluatedValue.getNumberValue(), "######.#############");
            case BOOLEAN:
                return Boolean.toString(evaluatedValue.getBooleanValue());
            case BLANK:
                return "";
            default:
                return "";
        }
    }


}
