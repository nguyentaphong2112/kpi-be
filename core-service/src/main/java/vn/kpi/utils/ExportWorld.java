package vn.kpi.utils;


import com.aspose.words.Cell;
import com.aspose.words.CellCollection;
import com.aspose.words.ControlChar;
import com.aspose.words.Document;
import com.aspose.words.LayoutCollector;
import com.aspose.words.Node;
import com.aspose.words.DocumentBuilder;
import com.aspose.words.NodeCollection;
import com.aspose.words.NodeType;
import com.aspose.words.Paragraph;
import com.aspose.words.RelativeHorizontalPosition;
import com.aspose.words.RelativeVerticalPosition;
import com.aspose.words.Row;
import com.aspose.words.RowCollection;
import com.aspose.words.SaveFormat;
import com.aspose.words.Shape;
import com.aspose.words.ShapeType;
import com.aspose.words.Table;
import com.aspose.words.TableCollection;
import com.aspose.words.WrapType;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author tudd
 */
@Slf4j

public class ExportWorld {
    private Document doc;

    public Document getDocument() {
        return doc;
    }

    public ExportWorld(String fileTemplate) throws Exception {
        this.doc = new Document(this.getClass().getClassLoader().getResourceAsStream(fileTemplate));
    }

    public ExportWorld(InputStream is) throws Exception {
        try (is) {
            this.doc = new Document(is);
        } catch (Exception ex) {
            log.error("[ExportWorld] error cause ", ex);
        }
    }

    public void updateFields() throws Exception {
        doc.updateFields();
    }

    public boolean replaceByDocument(String keySearch, Document replaceDoc) throws Exception {
        // Tìm vị trí của placeholder trong tài liệu chính
        try {
            // Tải tài liệu nguồn
            // Lặp qua các đoạn văn trong tài liệu nguồn
            NodeCollection allParagraphs = doc.getChildNodes(NodeType.PARAGRAPH, true);
            for (int i = allParagraphs.getCount() - 1; i >= 0; i--) {
                Paragraph paragraph = (Paragraph) allParagraphs.get(i);
                if (paragraph.toString(SaveFormat.TEXT).contains(keySearch)) {
                    // Thay thế nội dung của đoạn văn bằng nội dung từ tài liệu thay thế
                    replaceParagraph(paragraph, replaceDoc);
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private static void replaceParagraph(Paragraph targetParagraph, Document replacementDoc) throws Exception {
        // Xóa nội dung hiện tại
        targetParagraph.removeAllChildren();

        // Thêm nội dung từ tài liệu thay thế
        for (Node replacementNode : replacementDoc.getChildNodes(NodeType.ANY, true).toArray()) {
            // Nhân bản node từ tài liệu thay thế
            Node importedNode = targetParagraph.getDocument().importNode(replacementNode, true);

            // Kiểm tra loại node và vị trí chèn
            if (importedNode.getNodeType() == NodeType.PARAGRAPH) {
                targetParagraph.appendChild(importedNode); // Đối với đoạn văn
            } else {
                // Nếu node không phải là đoạn văn, bạn cần tìm vị trí chèn phù hợp
                // Ví dụ: bạn có thể thêm nó vào một đoạn văn mới
//                Paragraph newParagraph = new Paragraph(targetParagraph.getDocument());
//                newParagraph.appendChild(importedNode);
//                targetParagraph.getParentNode().insertAfter(newParagraph, targetParagraph);
            }
        }
    }


    private static Object[] getRowTable(TableCollection tables, List<String> listParams) {
        Row rowBase = null;
        Table tableBase = null;
        for (Table table : tables) {
            RowCollection rows = table.getRows();
            for (Row row : rows) {
                CellCollection cells = row.getCells();
                for (Cell cell : cells) {
                    for (String param : listParams) {
                        if (param.equalsIgnoreCase("stt")) {
                            continue;
                        }
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

    private static Object[] getRowTable(Table table, List<String> listParams) {
        Row rowBase = null;
        Table tableBase = null;
            RowCollection rows = table.getRows();
            for (Row row : rows) {
                CellCollection cells = row.getCells();
                for (Cell cell : cells) {
                    for (String param : listParams) {
                        if (param.equalsIgnoreCase("stt")) {
                            continue;
                        }
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
     * @param params danh sach du lieu
     * @throws Exception
     */
    public void replaceKeys(List<Map<String, Object>> params)
            throws Exception {
        if (Utils.isNullOrEmpty(params)) {
            return;
        }
        if (!params.get(0).keySet().contains("stt")) {
            //add stt vao
            for (int i = 0; i < params.size(); i++) {
                params.get(i).put("stt", i + 1);
            }
        }

        TableCollection tables = doc.getFirstSection().getBody().getTables();
        List<String> listParams = new ArrayList();
        listParams.addAll(params.get(0).keySet());
        Object[] rowTable = getRowTable(tables, listParams);
        if (rowTable != null) {
            Row rowBase = (Row) rowTable[0];
            Table tableBase = (Table) rowTable[1];
            for (Map<String, Object> mapParams : params) {
                Row newRow = (Row) rowBase.deepClone(true);
                for (String keySet : mapParams.keySet()) {
                    replaceText(newRow, "${" + keySet + "}", mapParams.get(keySet));
                }
                tableBase.insertBefore(newRow, rowBase);
            }
            rowBase.remove();
        } else if (params.size() == 1) {
            replaceKeys(params.get(0));
        }
    }

    public Document replaceKeysV2(List<Map<String, Object>> params) throws Exception {
        if (Utils.isNullOrEmpty(params)) {
            return doc;
        }
        if (!params.get(0).keySet().contains("stt")) {
            // Add stt vào params
            for (int i = 0; i < params.size(); i++) {
                params.get(i).put("stt", i + 1);
            }
        }

        // Lấy các bảng từ phần body chính của tài liệu
        NodeCollection bodyTables = doc.getFirstSection().getBody().getTables();

        // Gọi hàm để xử lý các bảng trong hộp văn bản
        List<Table> shapeTables = getTablesFromShapes(doc);

        // Kết hợp cả bảng trong body và trong các hộp văn bản
        List<Table> allTables = new ArrayList<>();
        for (Table table : (Iterable<Table>) bodyTables) {
            allTables.add(table);
        }
        allTables.addAll(shapeTables);

        // Duyệt qua tất cả các bảng để tìm và thay thế key
        List<String> listParams = new ArrayList<>();
        listParams.addAll(params.get(0).keySet());
        for (Table table : allTables) {
            Object[] rowTable = getRowTable(table, listParams);
            if (rowTable != null) {
                Row rowBase = (Row) rowTable[0];
                Table tableBase = (Table) rowTable[1];
                for (Map<String, Object> mapParams : params) {
                    Row newRow = (Row) rowBase.deepClone(true);
                    for (String keySet : mapParams.keySet()) {
                        replaceText(newRow, "${" + keySet + "}", mapParams.get(keySet));
                    }
                    tableBase.insertBefore(newRow, rowBase);
                }
                rowBase.remove();
            }
        }

        // Nếu params chỉ có một bản ghi
        if (params.size() == 1) {
            replaceKeys(params.get(0));
        }

        return doc;
    }

    // Phương thức này sẽ lấy các bảng từ các Shape (Text Box) trong tài liệu
    private List<Table> getTablesFromShapes(Document doc) throws Exception {
        List<Table> shapeTables = new ArrayList<>();
        NodeCollection shapes = doc.getChildNodes(NodeType.SHAPE, true);
        for (Shape shape : (Iterable<Shape>) shapes) {
            if (shape.hasChildNodes()) {
                NodeCollection childTables = shape.getChildNodes(NodeType.TABLE, true);
                for (Table table : (Iterable<Table>) childTables) {
                    shapeTables.add(table);
                }
            }
        }
        return shapeTables;
    }


    public void removePageContainKey(String keyword) throws Exception {
        NodeCollection paragraphs = doc.getChildNodes(NodeType.PARAGRAPH, true);
        for (Paragraph paragraph : (Iterable<Paragraph>) paragraphs) {
            if (paragraph.getRange().getText().contains(keyword)) {
                // Lấy số trang của đoạn văn
                LayoutCollector layoutCollector = new LayoutCollector(doc);
                int pageNumber = layoutCollector.getStartPageIndex(paragraph);
                // Xóa nội dung trên trang đó
                removePageContent(pageNumber);
                break; // Chỉ xóa trang đầu tiên tìm thấy
            }
        }
    }

    private void removePageContent(int pageNumber) throws Exception {
        NodeCollection allParagraphs = doc.getChildNodes(NodeType.PARAGRAPH, true);
        for (int i = allParagraphs.getCount() - 1; i >= 0; i--) {
            Paragraph paragraph = (Paragraph) allParagraphs.get(i);
            LayoutCollector layoutCollector = new LayoutCollector(doc);
            int currentPage = layoutCollector.getStartPageIndex(paragraph);

            // Nếu đoạn văn nằm trên trang xác định, xóa nó
            if (currentPage == pageNumber) {
                paragraph.remove();
            }
        }
    }

    public void replaceKeys(Map<String, Object> mapParams)
            throws Exception {
        if (mapParams == null) {
            return;
        }
        for (String keySet : mapParams.keySet()) {
            if (keySet.equalsIgnoreCase("stt")) {
                continue;
            }
            String placeHolder;
            boolean isNumber = false;
            Object object = mapParams.get(keySet);
            if (object == null) {
                placeHolder = "";
            } else {
                if (object instanceof BigDecimal) {
                    placeHolder = Utils.formatNumber(((BigDecimal) object).doubleValue());
                    isNumber = true;
                } else if (object instanceof Double) {
                    placeHolder = Utils.formatNumber(((Double) object));
                    isNumber = true;
                } else if (object instanceof Long) {
                    placeHolder = Utils.formatNumber(((Long) object));
                    isNumber = true;
                } else if (object instanceof Integer) {
                    placeHolder = Utils.formatNumber((Integer) object);
                    isNumber = true;
                } else if (object instanceof Date) {
                    placeHolder = Utils.formatDate((Date) object);
                } else if (object instanceof java.sql.Date) {
                    placeHolder = Utils.formatDate((java.sql.Date) object);
                } else {
                    placeHolder = String.valueOf(object);
                    placeHolder = placeHolder.replaceAll("\r\n", ControlChar.LINE_BREAK);
                }
                if (isNumber) {
                    placeHolder = placeHolder.replace(",", "#");
                    placeHolder = placeHolder.replace(".", ",");
                    placeHolder = placeHolder.replace("#", ".");
                }
            }
            doc.getRange().replace("${" + keySet + "}", placeHolder, false, false);
        }
    }

    public void replaceText(Row row, String key, Object object) throws Exception {
        if (key.equalsIgnoreCase("${stt}")) {
            System.out.println("catch here");
        }
        boolean isNumber = false;
        String placeHolder;
        if (object == null) {
            placeHolder = "";
        } else {
            if (object instanceof BigDecimal) {
                placeHolder = Utils.formatNumber(((BigDecimal) object).doubleValue());
                isNumber = true;
            } else if (object instanceof Double) {
                placeHolder = Utils.formatNumber(((Double) object));
                isNumber = true;
            } else if (object instanceof Long) {
                placeHolder = Utils.formatNumber(((Long) object));
                isNumber = true;
            } else if (object instanceof Integer) {
                placeHolder = Utils.formatNumber((Integer) object);
                isNumber = true;
            } else if (object instanceof Date) {
                placeHolder = Utils.formatDate((Date) object);
            } else if (object instanceof java.sql.Date) {
                placeHolder = Utils.formatDate((java.sql.Date) object);
            } else {
                placeHolder = String.valueOf(object);
                placeHolder = placeHolder.replaceAll("\r\n", ControlChar.LINE_BREAK);
            }
            if (isNumber) {
                placeHolder = placeHolder.replace(",", "#");
                placeHolder = placeHolder.replace(".", ",");
                placeHolder = placeHolder.replace("#", ".");
            }
        }
        if (row.getRange().replace(key, placeHolder, false, false) == 0) {
            if (!key.equalsIgnoreCase("${stt}")) {
                doc.getRange().replace(key, placeHolder, false, false);
            }
        }
    }

    public void insertImageDoc(byte[] imageData) throws Exception {
        NodeCollection<?> shapes = doc.getChildNodes(NodeType.SHAPE, true);
        Shape lastImage = null;
        Iterator<?> iterator = shapes.iterator();
        while (iterator.hasNext()) {
            Shape shape = (Shape) iterator.next();
            // Check if the shape is an image
            if (shape.getShapeType() == ShapeType.IMAGE) {
                lastImage = shape;
            }
        }
        if (lastImage != null) {
            lastImage.getImageData().setImage(imageData);
        }
    }

    public void saveFile(String fileName) throws Exception {
        saveFile(fileName, SaveFormat.DOCX);
    }

    public void saveFile(String fileName, int format) throws Exception {
        doc.save(fileName, format);
    }
    public void addImageToWord(byte[] fileImage, float width, float height, float x, float y) throws Exception {
        DocumentBuilder builder = new DocumentBuilder(this.doc);
        builder.moveToDocumentStart();

        try (ByteArrayInputStream byteInputStream = new ByteArrayInputStream(fileImage)) {
            com.aspose.words.Shape imageShape = builder.insertImage(byteInputStream, width, height);
            imageShape.setWrapType(WrapType.NONE);
            imageShape.setRelativeHorizontalPosition(RelativeHorizontalPosition.PAGE);
            imageShape.setRelativeVerticalPosition(RelativeVerticalPosition.PAGE);
            imageShape.setTop(x);
            imageShape.setLeft(y);
            imageShape.setBehindText(true);
        }
    }

    public void removeRow(int indexTable, int indexRow) {
        TableCollection tables = doc.getFirstSection().getBody().getTables();
        Table table = tables.get(indexTable);
        RowCollection rows = table.getRows();
        Row row = rows.get(indexRow);
        if (row != null) {
            row.remove();
        }
    }
}
