package vn.hbtplus.utils;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Import tu file Excel.
 *
 * @author Biennv1
 * @version 1.0
 * @since 1.0
 */
public class ImportExcel {

    private static final Logger LOGGER = LoggerFactory.getLogger(ImportExcel.class);

    /**
     * Bieu thuc chinh quy
     */
    private static final String DOUBLE_REGEX = "(-)?(\\d+|(\\d|\\d\\d|\\d\\d\\d)(,\\d\\d\\d)*)(\\.\\d+)?"; // so thuc
    private static final String LONG_REGEX = "(-)?(\\d+|(\\d|\\d\\d|\\d\\d\\d)(,\\d\\d\\d)*)(\\.0+)?"; // so nguyen
    /**
     * Cac loai du lieu
     */
    private static final String[] TYPE_NAMES = new String[]{
            "import.integerType",
            "import.floatType",
            "import.stringType",
            "import.dateType",
            "import.boolean",
            "import.mmyyyyType",
            "import.dateTimeType"
    };
    /**
     * Kieu du lieu (xau)
     */
    public static final Long LONG = 0L; // Kieu so nguyen

    /**
     *
     */
    public static final Long DOUBLE = 1L; // Kieu so thuc

    /**
     *
     */
    public static final Long STRING = 2L; // Kieu xau

    /**
     *
     */
    public static final Long DATE = 3L; // Kieu ngay thang

    /**
     *
     */
    public static final Long BOOLEAN = 4L; // Kieu true false

    /**
     *
     */
    public static final Long MMYYYY_DATE = 5L; // Kieu ngay thang mmYYYY

    /**
     *
     */
    public static final Long DATETIME_DATE = 6L; // Kieu ngay thang ddMMMyyyy hh:mm:ss
    /**
     * Kieu du lieu (xau)
     */
    private static final String STR_LONG = "long"; // Kieu so nguyen
    private static final String STR_DOUBLE = "double"; // Kieu so thuc
    private static final String STR_STRING = "string"; // Kieu xau
    private static final String STR_DATE = "date"; // Kieu ngay thang
    private static final String STR_BOOLEAN = "boolean"; // Kieu boolean
    private static final String STR_MMYYYY_DATE = "mmyyyy"; // Kieu ngay thang mmYYYY
    private static final String STR_DATETIME = "ddMMyyyy hhmmss"; // Kieu ngay thang ddMMMyyyy hh:mm:ss
    /**
     * So dong toi da, neu khong gioi han thi nho hon 0
     */
    private int maxNumberOfRecord;
    /**
     * Dong du lieu dau tien
     */
    private int firstDataRow;
    /**
     * Cau hinh
     */
    private List<ImportConfigBean> columnConfig = new ArrayList<>();

    /**
     * Danh sach loi
     */
    private List<ImportErrorBean> errorList = new LinkedList();
    /**
     * So loi toi da
     */
    private static final int MAX_ERROR_NUM = 1000;
    /**
     * Danh sach cac dong co du lieu
     */
    private List<Integer> rowList = new LinkedList();
    /**
     * Format so
     */
    private DecimalFormat decimalFormat = new DecimalFormat("###,###.###");
    /**
     * check duplicate
     */
    private List[] duplicateList;

    private Integer sizeData;
    private IMPORT_RESULT importResult;
    private boolean isXlsx;

    /**
     * Doc cau hinh tu file.
     *
     * @param template Duong dan cua file
     */
    public ImportExcel(String template) {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(template);
        this.initFromXMLFile(inputStream);
    }

    /**
     * Ham khoi tao day du. Chung ta khong khoi tao tu mot file cau hinh, ma tu
     * truyen vao. VD chung ta luu trong CSDL, va buoc thanh lau cac cau hinh
     * chung ta tu code truoc.
     *
     * @param columnConfig      Mang cac cau hinh cho cot
     * @param maxNumberOfRecord So ban ghi toi da cua file Excel
     * @param firstDataRow      Dong bat dau du lieu
     */
    public ImportExcel(ImportConfigBean[] columnConfig, int maxNumberOfRecord, int firstDataRow) {
        this.duplicateList = new List[columnConfig.length];
        for (int i = 0; i < columnConfig.length; i++) {
            duplicateList[i] = new ArrayList();
        }

        this.firstDataRow = firstDataRow;
        this.maxNumberOfRecord = maxNumberOfRecord;
        this.columnConfig = List.of(columnConfig);
    }

    public void addColumnConfig(ImportConfigBean columnConfig) {
        this.columnConfig.add(columnConfig);
    }

    /**
     * Validate nhung loi chung sau: Khong du bo nho Khong dung dinh dang Khong
     * co du lieu Du lieu loi (kieu du lieu, do dai, min, max, bat buoc hay
     * khong, trung lap,...)
     *
     * @param dataList Danh sach du lieu
     * @return
     */
    public boolean validateCommon(InputStream inputStream, List<Object[]> dataList) {
        try {
            List<String[]> excellDatas = new ArrayList<>();
            importResult = IMPORT_RESULT.NO_ERROR;
            try {
                excellDatas = ReadExelFile.readExcelFile(inputStream, columnConfig.size());
                isXlsx = true;
            } catch (IOException ex) {
                LOGGER.error("FILE IS NOT EXCEL FILE", ex);
                importResult = IMPORT_RESULT.FORMAT_ERROR;
            }
            // Luu file upload, lay sheet dau tien
            if (importResult == IMPORT_RESULT.NO_ERROR) {
                int rowNum = excellDatas.size();
                if (rowNum < firstDataRow) {
                    importResult = IMPORT_RESULT.FIRST_DATA_ROW_ERROR;
                } else {
                    //<editor-fold defaultstate="collapsed" desc="Doc tung dong du lieu, cho vao danh sach">
                    for (int row = firstDataRow; row < rowNum; row++) {
                        String[] cells = excellDatas.get(row);
                        if (cells.length >= 1) {
                            boolean emptyRow = true;
                            for (int col = 0; col < columnConfig.size(); col++) {
                                if (col < cells.length) {
                                    String content = cells[col];
                                    if (!Utils.isNullOrEmpty(content)) {
                                        emptyRow = false;
                                        break;
                                    }
                                } else {
                                    break;
                                }
                            }
                            if (!emptyRow) {
                                Object[] a = new Object[columnConfig.size()];
                                for (int col = 0; col < columnConfig.size(); col++) {
                                    ImportConfigBean  columnConfig = this.columnConfig.get(col);
                                    // Lay du lieu cua o
                                    String content = cells[col];
                                    if (!columnConfig.getIgnore()) {
                                        if (col < cells.length) {
                                            if (Utils.isNullOrEmpty(content)) {
                                                // Kiem tra NULL
                                                if (!columnConfig.getNullable()) {
                                                    if (errorList.size() < MAX_ERROR_NUM) {
                                                        errorList.add(new ImportErrorBean(row, col, "<b>" + columnConfig.getExcelColumn() + "</b> " + I18n.getMessage("import.isRequired"), null));
                                                    }
                                                }
                                                if (columnConfig.getCheckDuplicate()) {
                                                    duplicateList[col].add(null);
                                                }
                                            } else {
                                                // Kiem tra do dai du lieu
                                                if (columnConfig.getLength() < content.length()) {
                                                    if (errorList.size() < MAX_ERROR_NUM) {
                                                        errorList.add(new ImportErrorBean(row, col, "<b>" + columnConfig.getExcelColumn() + "</b> " + I18n.getMessage("import.exceedMaxLength", columnConfig.getLength()), content));
                                                    }
                                                } else {
                                                    // Kiem tra kieu du lieu
                                                    a[col] = checkDataType(columnConfig, content, row, col);
                                                }

                                                // Kiem tra trung
                                                if (columnConfig.getCheckDuplicate()) {
                                                    if (duplicateList[col].contains(content.toUpperCase())) {
                                                        if (errorList.size() < MAX_ERROR_NUM) {
                                                            errorList.add(new ImportErrorBean(row, col, columnConfig.getExcelColumn() + " trùng với dòng " + (duplicateList[col].indexOf(content.toUpperCase()) + firstDataRow + 1), content));
                                                        }
                                                    }
                                                    duplicateList[col].add(content.toUpperCase());
                                                }

                                                // Kiem tra validation
                                                if (columnConfig.getDataValidationType() != null) {
                                                    Integer dataValidationType = columnConfig.getDataValidationType().intValue();
                                                    switch (dataValidationType) {
                                                        case 1: {
                                                            // Neu la 1 thi validate List
                                                            List<String> textValueList =columnConfig.getTextValueList();
                                                            if (!StringUtils.containsIgnoreCase(textValueList.toString(), content)) {
                                                                errorList.add(new ImportErrorBean(row, col, "<b>" + columnConfig.getExcelColumn()
                                                                                                            + "</b> chỉ được nhập " + textValueList, content));
                                                            } else {
                                                                a[col] = content;
                                                            }
                                                            break;
                                                        }
                                                        case 2:
                                                            // Neu la 2 thi validate theo regex
                                                            String regularExpression = columnConfig.getRegularExpression();
                                                            if (!content.toLowerCase().matches(regularExpression)) {
                                                                errorList.add(new ImportErrorBean(row, col, "<b>" + columnConfig.getExcelColumn()
                                                                                                            + "</b> không đúng định dạng " + regularExpression, content));
                                                            }
                                                            break;
                                                        case 3:
                                                            // Neu la 3 la toUpperCase hoac toLowerCase
                                                            String actionType = columnConfig.getActionType();
                                                            if ("uppercase".equalsIgnoreCase(actionType)) {
                                                                content = content.toUpperCase();
                                                            } else if ("lowercase".equalsIgnoreCase(actionType)) {
                                                                content = content.toLowerCase();
                                                            }
                                                            a[col] = content;
                                                            break;
                                                        case 4: {
                                                            // Neu la 1 thi validate List
                                                            List<String> textValueList = columnConfig.getTextValueList();
                                                            if (!textValueList.contains(content)) {
                                                                errorList.add(new ImportErrorBean(row, col, "<b>" + columnConfig.getExcelColumn()
                                                                                                            + "</b> chỉ được nhập " + textValueList + " ( phân biệt hoa thường)", content));
                                                            } else {
                                                                a[col] = content;
                                                            }
                                                            break;
                                                        }
                                                        default:
                                                            break;
                                                    }
                                                }
                                            }
                                        } else if (!columnConfig.getNullable()) {
                                            if (errorList.size() < MAX_ERROR_NUM) {
                                                errorList.add(new ImportErrorBean(row, col, "<b>" + columnConfig.getExcelColumn() + "</b> " + I18n.getMessage("import.isRequired"), null));
                                            }
                                        }
                                    }
                                }
                                dataList.add(a); // chen vao danh sach du lieu
                                rowList.add(row);
                            }
                        }
                    }
                    //</editor-fold>

                    if (dataList.isEmpty()) {
                        importResult = IMPORT_RESULT.NO_DATA_ERROR;
                    } else if ((maxNumberOfRecord > 0) && (dataList.size() > maxNumberOfRecord)) {
                        importResult = IMPORT_RESULT.EXCEED_MAX_NUMBER_OF_RECORD_ERROR;
                    } else {
                        if (!errorList.isEmpty()) {
                            importResult = IMPORT_RESULT.DATA_CONTENT_ERROR;
                            sizeData = dataList.size();
                        }
                    }
                }
            }
            return (importResult == IMPORT_RESULT.NO_ERROR);
        } catch (Exception ex) {
            LOGGER.error("Loi validateCommon", ex);
            return false;
        } finally {
            try {
                inputStream.close();
            } catch (Exception e) {
                LOGGER.error("Not close stream", e);
            }
        }
    }

    /**
     * Them loi.
     *
     * @param row
     * @param col
     * @param errorMessage
     * @param content
     */
    public void addError(int row, int col, String errorMessage, String content) {
        if (errorList.size() < MAX_ERROR_NUM) {
            errorList.add(new ImportErrorBean(rowList.get(row), col, errorMessage, content));
        }
    }

    /**
     * Kiem tra co loi hay khong.
     *
     * @return
     */
    public boolean hasError() {
        return !errorList.isEmpty();
    }

    /**
     * Kiem tra xem du lieu tai cell cho truoc co loi hay khong
     *
     * @param row : so dong
     * @param col : so cot
     * @return
     */
    public boolean hasError(int row, int col) {
        for (ImportErrorBean error : errorList) {
            if ((error.getColumn() - 1 == col) && (error.getRow() - 1 == rowList.get(row))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Tra ve danh sach loi.
     *
     * @return
     */
    public List<ImportErrorBean> getErrorList() {
        return errorList;
    }

    public IMPORT_RESULT getImportResult() {
        return importResult;
    }

    public void setImportResult(IMPORT_RESULT importResult) {
        this.importResult = importResult;
    }

    /**
     * @param a
     * @return
     */
    public Map<String, Object> getPropertyMap(Object[] a) {
        Map<String, Object> propertyMap = new HashMap<>();
        for (int i = 0; i < columnConfig.size(); i++) {
            ImportConfigBean columnConfig = this.columnConfig.get(i);
            if (!Utils.isNullOrEmpty(columnConfig.getDatabaseColumn())) {
                if (columnConfig.getType() != null && columnConfig.getType().equals(DATE)) {
                    propertyMap.put(this.propertyName(columnConfig.getDatabaseColumn()), Utils.formatDate((Date) a[i]));
                } else if (columnConfig.getType() != null && columnConfig.getType().equals(DATETIME_DATE)) {
                    propertyMap.put(this.propertyName(columnConfig.getDatabaseColumn()), Utils.formatDate((Date) a[i], "dd/MM/yyyy HH:mm:ss"));
                } else {
                    propertyMap.put(this.propertyName(columnConfig.getDatabaseColumn()), a[i]);
                }
            }
        }
        return propertyMap;
    }

    // =================
    private String propertyName(String input) {
        StringBuilder output = new StringBuilder("");
        int i = 0;
        while (i < input.length()) {
            if (input.charAt(i) == '_') {
                i++;
                output.append(Character.toUpperCase(input.charAt(i)));
            } else {
                output.append(Character.toLowerCase(input.charAt(i)));
            }
            i++;
        }
        return output.toString();
    }

    /**
     * Doc cau hinh tu file XML.
     *
     * @param inputStream Duong dan toi file
     */
    private void initFromXMLFile(InputStream inputStream) {
        try {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document doc = builder.parse(inputStream);
            firstDataRow = Integer.parseInt(doc.getElementsByTagName("firstDataRow").item(0).getTextContent()) - 1;
            maxNumberOfRecord = Integer.parseInt(doc.getElementsByTagName("maxNumberOfRecord").item(0).getTextContent());
            NodeList elementList = doc.getElementsByTagName("col");
            duplicateList = new List[elementList.getLength()];
            for (int i = 0; i < elementList.getLength(); i++) {
                duplicateList[i] = new ArrayList();
            }
            int totalColumn = elementList.getLength();
            for (int i = 0; i < totalColumn; i++) {
                ImportConfigBean columnConfigBean = new ImportConfigBean();
                this.columnConfig.add(columnConfigBean);
                Element element = (Element) elementList.item(i);
                int numOfChildNodes = element.getAttributes().getLength();
                if (numOfChildNodes < 2) {
                    columnConfigBean.setValues();
                } else if (numOfChildNodes == 2) {
                    String databaseColumn = element.getAttribute("dbCol");
                    columnConfigBean.setValues(databaseColumn);
                } else if (numOfChildNodes == 3) {
                    String databaseColumn = element.getAttribute("dbCol");
                    String strDataType = element.getAttribute("type");
                    Long dataType = parseDataType(strDataType);
                    columnConfigBean.setValues(databaseColumn, dataType);
                } else {
                    String excelColumn;
                    try {
                        excelColumn = I18n.getMessage(element.getAttribute("title"));
                    } catch (Exception e) {
                        excelColumn = element.getAttribute("title");
                        LOGGER.debug("##### Chua Da Ngon Ngu ##### " + excelColumn);
                    }
                    String databaseColumn = element.getAttribute("dbCol");
                    Long dataType = parseDataType(element.getAttribute("type"));
                    Integer length = Integer.valueOf(element.getAttribute("length"));
                    Boolean nullable = !"false".equalsIgnoreCase(element.getAttribute("nullable"));
                    Boolean checkDuplicate = "false".equalsIgnoreCase(element.getAttribute("duplicate"));

                    Double min = parseDoubleAttribute(element, "min");
                    Double greater = parseDoubleAttribute(element, "greater");
                    Double max = parseDoubleAttribute(element, "max");
                    Double less = parseDoubleAttribute(element, "less");

                    boolean containsMinValue = (min != null);
                    boolean containsMaxValue = (max != null);

                    // Ưu tiên 'greater' và 'less' nếu có
                    if (greater != null) {
                        min = greater;
                        containsMinValue = false;
                    }
                    if (less != null) {
                        max = less;
                        containsMaxValue = false;
                    }

                    String textValueList = element.getAttribute("textValueList");
                    if (Utils.isNullOrEmpty(textValueList)) {
                        columnConfigBean.setValues(excelColumn, dataType, nullable, length, checkDuplicate, min, max, databaseColumn, containsMinValue, containsMaxValue);
                    } else {
                        Long dataValidationType;
                        String attribute = element.getAttribute("validationType");
                        if (Utils.isNullOrEmpty(attribute)) {
                            dataValidationType = 1L;
                        } else {
                            dataValidationType = Long.parseLong(attribute);
                        }
                        columnConfigBean.setValues(excelColumn, dataType, nullable, length, checkDuplicate, min, max, databaseColumn, containsMinValue, containsMaxValue, dataValidationType, textValueList);
                    }

                }
            }
        } catch (ParserConfigurationException | SAXException | IOException | DOMException | NumberFormatException ex) {
            LOGGER.error("Loi khoi tao file cau hinh import", ex);
        }
    }

    private Long parseDataType(String strDataType) {
        switch (strDataType) {
            case STR_LONG: return LONG;
            case STR_DOUBLE: return DOUBLE;
            case STR_STRING: return STRING;
            case STR_DATE: return DATE;
            case STR_BOOLEAN: return BOOLEAN;
            case STR_MMYYYY_DATE: return MMYYYY_DATE;
            case STR_DATETIME: return DATETIME_DATE;
            default: return null;
        }
    }

    private Double parseDoubleAttribute(Element e, String attr) {
        String v = e.getAttribute(attr);
        return v.isEmpty() ? null : Double.parseDouble(v);
    }



    /**
     * Check kieu du lieu.
     *
     * @param columnConfig Cau hinh cot Excel
     * @param content      Noi dung
     * @param row          Hang
     * @param col          Cot
     * @return Gia tri
     */
    private Object checkDataType(ImportConfigBean columnConfig, String content, int row, int col) {
        Object temp = null;
        String error = null;
        try {
            if (columnConfig.getType().equals(LONG)) {
                if (!content.matches(LONG_REGEX)) {
                    errorList.add(new ImportErrorBean(row, col, I18n.getMessage("import.must.integer"), content));
                }
                int index = content.indexOf(".");
                if (index >= 0) {
                    content = content.substring(0, index);
                }
                temp = Long.parseLong(content.replace(",", ""));
                if ((columnConfig.getMaxValue() != null) && (columnConfig.getMinValue() != null)) {
                    throw new NumberFormatException();
                }
            } else if (columnConfig.getType().equals(DOUBLE)) {
                if (!content.matches(DOUBLE_REGEX)) {
                    throw new Exception();
                }
                temp = Double.parseDouble(content.replace(",", ""));
                if ((columnConfig.getMaxValue() != null) && (columnConfig.getMinValue() != null)) {
                    throw new NumberFormatException();
                }
            } else if (columnConfig.getType().equals(DATE)) {
                temp = Utils.stringToDate(content);
                if ((content != null) && !content.isEmpty() && (temp == null)) {
                    errorList.add(new ImportErrorBean(row, col, I18n.getMessage("import.must.date"), content));
                }
            } else if (columnConfig.getType().equals(STRING)) {
                temp = content;
            } else if (columnConfig.getType().equals(MMYYYY_DATE)) {
                temp = Utils.stringToDate(content, "MM/yyyy");
                if ((content != null) && !content.isEmpty() && (temp == null)) {
                    errorList.add(new ImportErrorBean(row, col, I18n.getMessage("import.must.date"), content));
                }
            } else if (columnConfig.getType().equals(DATETIME_DATE)) {
                temp = Utils.stringToDate(content, "dd/MM/yyyy HH:mm:ss");
                if ((content != null) && !content.isEmpty() && (temp == null)) {
                    // throw new Exception();
                    errorList.add(new ImportErrorBean(row, col, I18n.getMessage("import.must.date"), content));
                }
            }
        } catch (NumberFormatException ex) {
            Double val;
            if ((temp instanceof Long)) {
                val = ((Long) temp) * 1.0D;
            } else {
                val = (Double) temp;
            }
            if (val != null) {
                if ((!columnConfig.getContainsMaxValue() && val >= columnConfig.getMaxValue())) {
                    error = "<b>" + columnConfig.getExcelColumn() + "</b> " + I18n.getMessage("import.must") + " &lt; " + decimalFormat.format(columnConfig.getMaxValue());
                } else if ((columnConfig.getContainsMaxValue() && val > columnConfig.getMaxValue())) {
                    error = "<b>" + columnConfig.getExcelColumn() + "</b> " + I18n.getMessage("import.must") + " &le; " + decimalFormat.format(columnConfig.getMaxValue());
                } else if ((!columnConfig.getContainsMinValue() && val <= columnConfig.getMinValue())) {
                    error = "<b>" + columnConfig.getExcelColumn() + "</b> " + I18n.getMessage("import.must") + " &gt; " + decimalFormat.format(columnConfig.getMinValue());
                } else if ((columnConfig.getContainsMinValue() && val < columnConfig.getMinValue())) {
                    error = "<b>" + columnConfig.getExcelColumn() + "</b> " + I18n.getMessage("import.must") + " &ge; " + decimalFormat.format(columnConfig.getMinValue());
                }
            }
        } catch (Exception ex) {
            LOGGER.debug("debug", ex);
            error = "<b>" + columnConfig.getExcelColumn() + "</b> "
                    + I18n.getMessage("import.invalidType") + " ("
                    + I18n.getMessage(TYPE_NAMES[columnConfig.getType().intValue()]) + ")";
        }
        if (error != null) {
            if (errorList.size() < MAX_ERROR_NUM) {
                errorList.add(new ImportErrorBean(row, col, error, content));
            }
        }
        return temp;
    }

    /**
     * Tra ve so dong.
     *
     * @param dataIndex
     * @return
     */
    public int getRowNumber(int dataIndex) {
        return rowList.get(dataIndex) + 1;
    }

    private File getImportFile(byte[] bytes, String uploadPath) {
        try {
            Path path = Paths.get(uploadPath);
            Files.write(path, bytes);
            return new File(uploadPath);
        } catch (IOException e) {
            LOGGER.error("Loi khi luu file", e);
            return null;
        }

    }

    public String getFileErrorDescription(MultipartFile files, String exportFolder) throws Exception {
        if (this.errorList != null && !this.errorList.isEmpty()) {
            ExportExcel dynamicExport = new ExportExcel(files.getInputStream(), this.firstDataRow, isXlsx);

            int columnLength = this.columnConfig.size();
            dynamicExport.setText(I18n.getMessage("import.error.columnName"), columnLength);
            dynamicExport.setCellFormat(columnLength, columnLength, ExportExcel.CENTER_FORMAT);

            Map<Integer, StringBuilder> mapError = new HashMap<>();
            for (ImportExcel.ImportErrorBean bean : this.errorList) {
                StringBuilder description = mapError.get(bean.getRow());
                if (description == null) {
                    description = new StringBuilder(bean.getDescription());
                    mapError.put(bean.getRow(), description);
                } else {
                    description.append(", ").append(bean.getDescription());
                    mapError.put(bean.getRow(), description);
                }
            }
            for (ImportExcel.ImportErrorBean bean : this.errorList) {
                String description = mapError.get(bean.getRow()).toString();
                dynamicExport.setText(Utils.removeHtml(description), columnLength, bean.getRow() - 1);
                try {
                    dynamicExport.setCellFormat(bean.getRow() - 1, 0, bean.getRow() - 1, columnLength, ExportExcel.CELL_COLOR_YELLOW);
                } catch (Exception e) {
                    LOGGER.error("error", Utils.toJson(bean) + " : ## : " + columnLength);
                }
            }

            int rowNumber = this.sizeData == null ? 1 : this.sizeData;
            try {
                dynamicExport.setCellFormat(this.firstDataRow - 1, 0, rowNumber + this.firstDataRow, columnLength, ExportExcel.BORDER_FORMAT);
            } catch (Exception e) {
                LOGGER.error("error", rowNumber + this.firstDataRow);
            }

            String userName = Utils.getUserNameLogin();
            String fileName = Utils.formatDate(new Date(), "ddMMyyyyHHmmss") + "_" + userName + "_Noidung_file_loi.xlsx";
            dynamicExport.exportFile(exportFolder + fileName);
            return fileName;
        } else {
            return null;
        }
    }

    /**
     * Hien thi du lieu dong loi.
     *
     * @param a Mang du lieu
     */
//    private void printErrorData(Object[] a) {
//        for (int col = 0; col < columnConfig.length; col++) {
//            if (columnConfig[col].getDatabaseColumn() != null && !columnConfig[col].getDatabaseColumn().isEmpty() && (a[col] != null)) {
//                LOGGER.error(columnConfig[col].getDatabaseColumn() + ": " + a[col]);
//            }
//        }
//    }

    // ====================

    /**
     * Cau hinh import file Excel.
     *
     * @author Biennv1
     * @version 1.0
     * @since 1.0
     */
    @Data
    public static class ImportConfigBean {

        /**
         * Ten cot trong file Excel
         */
        private String excelColumn;
        /**
         * 0: NUMBER (long), 1: NUMBER (double), 2: VARCHAR2, 3: DATE
         */
        private Long type;
        /**
         * Co the null hay khong
         */
        private Boolean nullable = true;
        /**
         * Do dai du lieu
         */
        private int length;
        /**
         * Bo qua, khong day vao CSDL
         */
        private Boolean ignore;
        /**
         * Kiem tra trung du lieu
         */
        private Boolean checkDuplicate = false;
        /**
         * Gia tri nho nhat voi truong hop kieu du lieu la so thuc
         */
        private Double minValue;
        /**
         * Gia tri lon nhat voi truong hop kieu du lieu la so thuc
         */
        private Double maxValue;
        /**
         * Cot day vao CSDL
         */
        private String databaseColumn;
        /**
         * Co the lay gia tri nho nhat
         */
        private Boolean containsMinValue;
        /**
         * Co the lay gia tri lon nhat
         */
        private Boolean containsMaxValue;
        /**
         * Loai validate
         */
        private Long dataValidationType; // 1 la trong list, 2 la bieu thuc chinh quy, 3 la upperCase - lowerCase,...
        /**
         * Danh sach list
         */
        private List<String> textValueList;
        /**
         * Bieu thuc chinh quy
         */
        private String regularExpression;
        /**
         * Hanh dong
         */
        private String actionType;

        /**
         *
         */
        public ImportConfigBean() {
        }

        public ImportConfigBean(String excelColumn, Long type, Boolean nullable, Integer length,
                                Boolean checkDuplicate) {
            this.ignore = false;
            this.excelColumn = excelColumn;
            this.type = type;
            this.nullable = nullable;
            this.length = length;
            this.checkDuplicate = checkDuplicate;
        }

        /**
         *
         */
        public void setValues() {
            this.ignore = true;
            this.databaseColumn = "";
        }

        /**
         * @param databaseColumn
         */
        public void setValues(String databaseColumn) {
            this.ignore = true;
            this.databaseColumn = databaseColumn;
        }

        /**
         * @param databaseColumn
         * @param type
         */
        public void setValues(String databaseColumn, Long type) {
            this.ignore = true;
            this.databaseColumn = databaseColumn;
            this.type = type;
        }

        /**
         * @param excelColumn
         * @param type
         * @param nullable
         * @param length
         * @param checkDuplicate
         * @param minValue
         * @param maxValue
         * @param databaseColumn
         * @param containsMinValue
         * @param containsMaxValue
         */
        public void setValues(String excelColumn, Long type, Boolean nullable, int length,
                              Boolean checkDuplicate, Double minValue, Double maxValue, String databaseColumn,
                              Boolean containsMinValue, Boolean containsMaxValue) {
            this.ignore = false;
            this.databaseColumn = databaseColumn;
            this.excelColumn = excelColumn;
            this.type = type;
            this.nullable = nullable;
            this.length = length;
            this.checkDuplicate = checkDuplicate;
            this.minValue = minValue;
            this.maxValue = maxValue;
            this.containsMinValue = containsMinValue;
            this.containsMaxValue = containsMaxValue;
        }

        /**
         * @param excelColumn
         * @param type
         * @param nullable
         * @param length
         * @param checkDuplicate
         * @param minValue
         * @param maxValue
         * @param databaseColumn
         * @param containsMinValue
         * @param containsMaxValue
         * @param dataValidationType
         * @param dataValidationText
         */
        public void setValues(String excelColumn, Long type, Boolean nullable, int length,
                              Boolean checkDuplicate, Double minValue, Double maxValue, String databaseColumn,
                              Boolean containsMinValue, Boolean containsMaxValue, Long dataValidationType,
                              String dataValidationText) {
            this.ignore = false;
            this.databaseColumn = databaseColumn;
            this.excelColumn = excelColumn;
            this.type = type;
            this.nullable = nullable;
            this.length = length;
            this.checkDuplicate = checkDuplicate;
            this.minValue = minValue;
            this.maxValue = maxValue;
            this.containsMinValue = containsMinValue;
            this.containsMaxValue = containsMaxValue;
            this.dataValidationType = dataValidationType;
            if (this.dataValidationType != null) {
                if (dataValidationType.equals(1L) || dataValidationType.equals(4L)) {
                    String[] a = dataValidationText.split(",");
                    textValueList = new ArrayList(a.length);
                    for (String s : a) {
                        textValueList.add(s.trim());
                    }
                } else if (dataValidationType.equals(2L)) {
                    this.regularExpression = dataValidationText;
                }
            }
        }
    }

    /**
     * Doi tuong loi khi thuc hien nghiep vu Import Excel.
     *
     * @author Biennv1
     * @version 1.0
     * @since 1.0
     */
    @Data
    public class ImportErrorBean {

        /**
         * Dong
         */
        private int row;
        /**
         * Cot
         */
        private int column;
        /**
         * Nhan cua cot
         */
        private String columnLabel;
        /**
         * Mo ta
         */
        private String description;
        /**
         * Noi dung file Excel
         */
        private String content;

        /**
         * Ham tao mac dinh.
         */
        public ImportErrorBean() {
        }

        /**
         * Ham tao.
         *
         * @param row         Hang
         * @param icolumn     Cot
         * @param description Mo ta
         * @param content     Noi dung
         */
        public ImportErrorBean(int row, int icolumn, String description, String content) {
            final int ALPHABET_NUMBER = 26; // so chu cai
            this.row = row + 1;
            this.column = icolumn + 1;
            this.description = description;
            this.content = content;

            //this.columnLabel = Character.forDigit(Character.digit('A', 10), 10);
            if (icolumn < ALPHABET_NUMBER) {
                this.columnLabel = String.valueOf((char) ('A' + icolumn));
            } else {
                int temp = icolumn / ALPHABET_NUMBER;
                icolumn -= ALPHABET_NUMBER * temp;
                this.columnLabel = String.valueOf((char) ('A' + temp - 1)) + String.valueOf((char) ('A' + icolumn));
            }
        }
    }

    public enum IMPORT_RESULT {
        NO_ERROR(0, "error.import.noError"),
        FIRST_DATA_ROW_ERROR(1, "error.import.firstDataRowError"),
        NO_DATA_ERROR(2, "error.import.noDataError"),
        DATA_CONTENT_ERROR(3, "error.import.dataContentError"),
        FORMAT_ERROR(4, "error.import.formatError"),
        EXCEED_MAX_NUMBER_OF_RECORD_ERROR(8, "error.import.maximumRow");

        IMPORT_RESULT(int result, String messageKey) {
            this.result = result;
            this.messageKey = messageKey;
        }

        private final int result;
        private final String messageKey;

        public int getResult() {
            return result;
        }

        public String getMessageKey() {
            return messageKey;
        }
    }
}

