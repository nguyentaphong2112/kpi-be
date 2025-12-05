/*
 * Copyright (C) 2010 HRPlus. All rights reserved.
 * HRPLUS PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package vn.hbtplus.utils;

import com.jxcell.CellException;
import com.jxcell.CellFormat;
import com.jxcell.FindReplaceInfo;
import com.jxcell.View;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Bao cao dong cot.
 *
 * @author tudd
 * @version 1.0
 * @since 1.0
 */
public class ExportExcel {

    /**
     * Format cho header
     */
    public static final int HEADER_FORMAT = 0;
    /**
     * Format tao border
     */
    public static final int BORDER_FORMAT = 1;
    /**
     * Tieu de
     */
    public static final int TITLE = 2;

    /* Format tao border voi vien Den.*/
    /**
     *
     */
    public static final int NORMAL_ITALIC = 23;
    /**
     *
     */
    /**
     *
     */
    public static final int CENTER_FORMAT = 26;

    /**
     *
     */
    public static final int BOLD_FORMAT = 28;
    public static final int CELL_COLOR_YELLOW = 35;
    public static final String TIMES_NEW_ROMAN = "Times New Roman";
    // Left alignment
    /**
     *
     */
    public static final int ALIGN_LEFT = 34;
    // Cell color

    /**
     *
     */
    public static final int ALIGN_RIGHT = 36;
    // No wrap text
    /**
     *
     */
    public static final int NO_WRAP_TEXT = 37;
    /**
     *
     */
    public static final int MERGE_CELL = 41;
    /**
     * bôi đậm và căn lề trái
     */
    public static final int BOLD_FORMAT_LEFT = 42;
    /**
     * Doi tuong de tuong tac voi file Excel
     */
    private final View view;
    /**
     * Dong du lieu cuoi cung, moi nhat, hien tai
     */
    private int lastRow;
    private final boolean isXLSX;
    private static final Logger LOGGER = LoggerFactory.getLogger(ExportExcel.class);

    /**
     * @param templateFile
     * @param startDataRow
     * @param isXLSX
     */
    public ExportExcel(String templateFile, int startDataRow, boolean isXLSX) {

        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(templateFile);
        this.lastRow = startDataRow - 1;
        view = new View();
        this.isXLSX = isXLSX;
        if (isXLSX) {
            try {
                view.readXLSX(inputStream);
            } catch (Exception ex) {
                LOGGER.error(ex.getMessage());
            }

        } else {
            try {
                view.read(inputStream);
            } catch (CellException | IOException ex) {
                LOGGER.error(ex.getMessage());
            }
        }

    }

    public ExportExcel(InputStream templateFile, int startDataRow, boolean isXLSX) {
        this.lastRow = startDataRow - 1;
        view = new View();
        this.isXLSX = isXLSX;
        try (templateFile) {
            if (isXLSX) {
                view.readXLSX(templateFile);
            } else {
                view.read(templateFile);
            }
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage());
        }
    }

    /**
     * Ghi ra file Excel.
     *
     * @param exportFile File Excel xuat ra
     * @throws Exception Exception
     */
    public void exportFile(String exportFile) throws Exception {
        view.setPrintGridLines(false);
        if (isXLSX) {
            view.writeXLSX(exportFile);
        } else {
            view.write(exportFile);
        }
    }

    public void deleteSheets(int start, int numOfSheet) throws CellException {
        view.deleteSheets(start, numOfSheet);
    }

    public void copySheet(int sheetIndex) throws Exception {
        view.copySheet(sheetIndex);
    }

    public void copySheetFromBook(int srcSheetIndex, int destSheetIndex, ExportExcel srcView) throws Exception {
        view.CopySheetFromBook(srcView.view, srcSheetIndex, destSheetIndex);
    }

    public void insertSheets(int index, int numOfSheet) throws Exception {
        view.insertSheets(index, numOfSheet);
    }

    /**
     * Xuat file pdf
     *
     * @param exportFile
     * @throws Exception
     */
    public void exportPdfFile(String exportFile)
            throws Exception {
        view.setPrintGridLines(false);
        view.exportPDF(exportFile);
    }

    /**
     * Them vao anh
     *
     * @param col1
     * @param row1
     * @param col2
     * @param row2
     * @param img
     * @throws CellException
     */
    public void addPicture(int col1, int row1, int col2, int row2, String img) throws CellException {
        view.addPicture(col1, row1, col2, row2, img);
    }

    /**
     * Chuyen den dong moi.
     */
    public void increaseRow() {
        lastRow++;
    }

    /**
     * Lay dong hien tai, dong cuoi cung.
     *
     * @return Dong cuoi cung
     */
    public int getLastRow() {
        return lastRow;
    }

    /**
     * Thiet lap chi so dong cuoi cung.
     *
     * @param row
     */
    public void setLastRow(int row) {
        lastRow = row;
    }

    /**
     * @param r1
     * @param r2
     * @param value
     * @throws Exception
     */
    public void setRowHeight(int r1, int r2, Long value) throws Exception {
        if (value.equals(0L)) {
            value = 500L;
        }
        for (int i = r1; i <= r2; i++) {
            view.setRowHeight(i, value.intValue());
        }
    }

    /**
     * Set wraptext
     *
     * @param r1
     * @param c1
     * @param r2
     * @param c2
     * @throws Exception
     */
    public void setWrapText(int r1, int c1, int r2, int c2) throws Exception {
        for (int i = r1; i <= r2; i++) {
            CellFormat format = view.getCellFormat(i, c1, i, c2);
            format.setWordWrap(true);
        }
    }

    /**
     * @param col
     * @param width in inch
     * @throws Exception
     */
    public void setColumnWidth(int col, int width) throws Exception {
        view.setColWidth(col, width * 254);
    }

    /**
     * @param col1
     * @param col2
     * @param width in inch
     * @throws Exception
     */
    public void setColumnWidth(int col1, int col2, int width) throws Exception {
        view.setColWidth(col1, col2, width * 254, true);
    }

    /**
     * @param row
     * @param height
     * @throws Exception
     */
    public void setRowHeight(int row, int height) throws Exception {
        view.setRowHeight(row, height);
    }

    /**
     * @param row1
     * @param row2
     * @param height
     * @throws Exception
     */
    public void setRowHeight(int row1, int row2, int height) throws Exception {
        view.setColWidth(row1, row2, height, true);
    }

    /**
     * @param r1
     * @param c1
     * @param r2
     * @param c2
     * @param cellColor
     * @throws CellException
     */
    public void setCellColor(int r1, int c1, int r2, int c2, Color cellColor) throws CellException {
        CellFormat format = view.getCellFormat(r1, c1, r2, c2);
        format.setPattern((short) 1);
        format.setPatternFG(cellColor);
        view.setCellFormat(format, r1, c1, r2, c2);
    }

    public void setCellColor(String text, Color cellColor) throws CellException {
        FindReplaceInfo fi = view.findFirst(text, 1);
        int deepth = 1;

        while (fi != null) {
            CellFormat format = view.getCellFormat(fi.getRow(), fi.getCol(), fi.getRow(), fi.getCol());
            format.setPattern((short) 1);
            format.setFontColor(cellColor);
            view.setCellFormat(format, fi.getRow(), fi.getCol(), fi.getRow(), fi.getCol());
            fi = view.findNext(text, 1, fi);
            deepth++;
            if (deepth > 50) {
                return;
            }
        }

    }

    /**
     * @param r1
     * @param c1
     * @param r2
     * @param c2
     * @param red
     * @param green
     * @param blue
     * @throws CellException
     */
    public void setCellColor(int r1, int c1, int r2, int c2, int red, int green, int blue) throws CellException {
        CellFormat format = view.getCellFormat(r1, c1, r2, c2);
        format.setPattern((short) 1);
        format.setPatternFG(new Color(red, green, blue));
        view.setCellFormat(format, r1, c1, r2, c2);
    }

    /**
     * @param r1
     * @param c1
     * @param r2
     * @param c2
     * @param size
     * @throws CellException
     */
    public void setFontSize(int r1, int c1, int r2, int c2, Double size) throws CellException {
        CellFormat format = view.getCellFormat(r1, c1, r2, c2);
        format.setFontSize(size);
        view.setCellFormat(format, r1, c1, r2, c2);
    }

    public void setFontName(int r1, int c1, int r2, int c2, String font) throws CellException {
        if (r1 <= r2 && c1 <= c2) {
            CellFormat format = view.getCellFormat(r1, c1, r2, c2);
            format.setFontName(font);
            view.setCellFormat(format, r1, c1, r2, c2);
        }
    }

    public void setFontColor(int r1, int c1, int r2, int c2, Color font) throws CellException {
        if (r1 <= r2 && c1 <= c2) {
            CellFormat format = view.getCellFormat(r1, c1, r2, c2);
            format.setFontColor(font);
            view.setCellFormat(format, r1, c1, r2, c2);
        }
    }

    /**
     * Format cell
     *
     * @param r1     Top
     * @param c1     Left
     * @param r2     Bottom
     * @param c2     Right
     * @param format
     * @throws com.jxcell.CellException
     */
    public void setCellFormat(int r1, int c1, int r2, int c2, CellFormat format)
            throws CellException {
        view.setCellFormat(format, r1, c1, r2, c2);
    }

    /**
     * @param i1
     * @param i2
     * @param format
     * @throws CellException
     * @throws Exception
     */
    public void setTextFormat(int i1, int i2, CellFormat format)
            throws CellException, Exception {
        view.setTextSelection(i1, i2);
        view.setCellFormat(format);
    }

    /**
     * @param r1
     * @param c1
     * @param i1
     * @param i2
     * @param formatType
     * @throws CellException
     * @throws Exception
     */
    public void setTextFormat(int r1, int c1, int i1, int i2, int formatType)
            throws CellException, Exception {
        view.setSelection(r1, c1, r1, c1);
        view.setTextSelection(i1, i2);
        CellFormat format = view.getCellFormat();
        if (formatType == BOLD_FORMAT) {
            format.setFontBold(true);
        }
        view.setCellFormat(format);
    }

    /**
     * @param c1
     * @param i1
     * @param i2
     * @param formatType
     * @throws CellException
     * @throws Exception
     */
    public void setTextFormat(int c1, int i1, int i2, int formatType)
            throws CellException, Exception {
        setTextFormat(lastRow, c1, i1, i2, formatType);
    }


    /**
     * @param r1
     * @param c1
     * @param r2
     * @param c2
     * @param formatType
     * @throws CellException
     */
    public void setCellFormat(int r1, int c1, int r2, int c2, int formatType)
            throws CellException {
        if (r1 <= r2 && c1 <= c2) {
            CellFormat format = view.getCellFormat(r1, c1, r2, c2);
            switch (formatType) {
                case HEADER_FORMAT: {
                    //<editor-fold defaultstate="collapsed" desc="Header cua bang">
                    short border = CellFormat.BorderThin;
                    format.setLeftBorder(border);
                    format.setRightBorder(border);
                    format.setTopBorder(border);
                    format.setBottomBorder(border);
                    format.setHorizontalInsideBorder(border);
                    format.setVerticalInsideBorder(border);
                    Color borderColor = Color.GREEN.darker();
                    format.setLeftBorderColor(borderColor);
                    format.setRightBorderColor(borderColor);
                    format.setTopBorderColor(borderColor);
                    format.setBottomBorderColor(borderColor);
                    format.setFontBold(true);
                    format.setPattern((short) 1);
                    format.setPatternFG(new Color(254, 252, 172));
                    format.setWordWrap(true);
                    format.setVerticalAlignment(CellFormat.VerticalAlignmentCenter);
                    format.setHorizontalAlignment(CellFormat.HorizontalAlignmentCenter);
                    //</editor-fold>
                    break;
                }
                case BORDER_FORMAT: {
                    //<editor-fold defaultstate="collapsed" desc="Border cho du lieu binh thuong">
                    short border = CellFormat.BorderThin;
                    format.setLeftBorder(border);
                    format.setRightBorder(border);
                    format.setTopBorder(border);
                    format.setBottomBorder(border);
                    format.setHorizontalInsideBorder(border);
                    format.setVerticalInsideBorder(border);
                    Color borderColor = Color.BLACK.darker();
                    format.setLeftBorderColor(borderColor);
                    format.setRightBorderColor(borderColor);
                    format.setTopBorderColor(borderColor);
                    format.setBottomBorderColor(borderColor);
                    format.setWordWrap(true);
                    //</editor-fold>
                    break;
                }
                case TITLE:
                    format.setFontBold(true);
                    format.setPattern((short) 1);
                    format.setPatternFG(Color.GREEN);
                    format.setHorizontalAlignment(CellFormat.HorizontalAlignmentCenter);
                    break;
                case CENTER_FORMAT:
                    format.setHorizontalAlignment(CellFormat.HorizontalAlignmentCenter);
                    break;
                case BOLD_FORMAT:
                    format.setFontBold(true);
                    break;
                case NORMAL_ITALIC:
                    format.setFontItalic(true);
                    break;
                case ALIGN_LEFT:
                    format.setHorizontalAlignment(CellFormat.HorizontalAlignmentLeft);
                    break;
                case ALIGN_RIGHT:
                    format.setHorizontalAlignment(CellFormat.HorizontalAlignmentRight);
                    break;
                case NO_WRAP_TEXT:
                    format.setWordWrap(false);
                    break;
                case BOLD_FORMAT_LEFT:
                    format.setFontBold(true);
                    format.setHorizontalAlignment(CellFormat.HorizontalAlignmentLeft);
                    break;
                case MERGE_CELL:
                    format.setMergeCells(true);
                    break;
                case CELL_COLOR_YELLOW:
                    format.setPattern((short) 1);
                    format.setPatternFG(Color.YELLOW);
                    break;
                default:
                    break;
            }
            view.setCellFormat(format, r1, c1, r2, c2);
        }
        //#042 End
    }


    /**
     * Format cell
     *
     * @param c1         Left
     * @param c2         Right
     * @param formatType Loai cell
     * @throws com.jxcell.CellException
     */
    public void setCellFormat(int c1, int c2, int formatType) throws CellException {
        setCellFormat(lastRow, c1, lastRow, c2, formatType);
    }

    /**
     * @param c1
     * @param c2
     * @param formatType
     * @throws CellException
     */
    public void setCellFormat(int c1, int c2, CellFormat formatType) throws CellException {
        setCellFormat(lastRow, c1, lastRow, c2, formatType);
    }

    /**
     * @param r1
     * @param c1
     * @param r2
     * @param c2
     * @return
     * @throws CellException
     */
    public CellFormat getCellFormat(int r1, int c1, int r2, int c2) throws CellException {
        return view.getCellFormat(r1, c1, r2, c2);
    }

    /**
     * @param r1 dest
     * @param c1
     * @param r2
     * @param c2
     * @param r3 origin
     * @param c3
     * @param r4
     * @param c4
     * @throws CellException
     */
    public void copyRange(int r1, int c1, int r2, int c2, int r3, int c3, int r4, int c4) throws CellException {
        view.copyRange(r1, c1, r2, c2, view, r3, c3, r4, c4, View.CopyAll);
    }

    public int getColWidth(int col) throws CellException {
        return view.getColWidth(col);
    }

    public void setColWidth(int col, int width) throws CellException {
        view.setColWidth(col, width);
    }

    /**
     * Merge cell.
     *
     * @param r1 Top
     * @param c1 Left
     * @param r2 Bottom
     * @param c2 Right
     * @throws CellException CellException
     */
    public void mergeCell(int r1, int c1, int r2, int c2)
            throws CellException {
        try {
            setCellFormat(r1, c1, r2, c2, MERGE_CELL);
        } catch (CellException e) {
            LOGGER.debug("debug", e);
            view.setSelection(r1, c1, r2, c2);
            CellFormat format = view.getCellFormat();
            format.setMergeCells(true);
            view.setCellFormat(format);
        }
    }

    /**
     * Merge cell.
     *
     * @param c1 Left
     * @param c2 Right
     * @throws CellException CellException
     */
    public void mergeCell(int c1, int c2)
            throws CellException {
        try {
            mergeCell(lastRow, c1, lastRow, c2);
        } catch (CellException e) {
            LOGGER.debug("debug", e);
            view.setSelection(lastRow, c1, lastRow, c2);
            CellFormat format = view.getCellFormat();
            format.setMergeCells(true);
            view.setCellFormat(format);
        }
    }


    /**
     * Thiet lap gia tri cho cell o dong hien tai.
     *
     * @param text   Gia tri
     * @param column Cot
     * @throws CellException CellException
     */
    public void setEntry(String text, int column)
            throws CellException {
        if (!Utils.isNullOrEmpty(text)) {
            view.setTextAsValue(lastRow, column, text);
        }
    }

    /**
     * Thiet lap gia tri cho cell o dong hien tai.
     *
     * @param obj
     * @param column Cot
     * @throws CellException CellException
     */
    public void setEntry(Object obj, int column)
            throws CellException {
        if (obj != null) {
            view.setTextAsValue(lastRow, column, obj.toString());
        }
    }

    /**
     * Thiet lap gia tri cho cell o dong row.
     *
     * @param text   Gia tri
     * @param column Cot
     * @param row    Dong
     * @throws CellException CellException
     */
    public void setEntry(String text, int column, int row)
            throws CellException {
        if (!Utils.isNullOrEmpty(text)) {
            view.setTextAsValue(row, column, text);
        }
    }

    /**
     * Thiet lap gia tri cho cell o dong row.
     *
     * @param text   Gia tri
     * @param column Cot
     * @throws CellException CellException
     */
    public void setCenterAlignmentEntry(String text, int column)
            throws CellException {
        view.setTextAsValue(this.getLastRow(), column, text);
        CellFormat format = view.getCellFormat(this.getLastRow(), column, this.getLastRow(), column);
        format.setHorizontalAlignment(CellFormat.HorizontalAlignmentCenter);
        view.setCellFormat(format, this.getLastRow(), column, this.getLastRow(), column);
    }

    /**
     * Thiet lap gia tri cho cell o dong row.
     *
     * @param text             Gia tri
     * @param column           Cot
     * @param row              Dong
     * @param alignHorizontal: can giua theo chieu rong 0 : can trai 1 : can
     *                         giua 2 : can phai
     * @param alignVertical    : can giua theo chieu cao 0 : can tren 1 : can giua
     *                         2 : can duoi
     * @throws CellException CellException
     */
    public void setAlignmentEntry(String text, int column, int row, int alignHorizontal, int alignVertical)
            throws CellException {
        view.setText(row, column, text);
        CellFormat format = view.getCellFormat(row, column, row, column);
        switch (alignHorizontal) {
            case 0:
                format.setHorizontalAlignment(CellFormat.HorizontalAlignmentLeft);
                break;
            case 1:
                format.setHorizontalAlignment(CellFormat.HorizontalAlignmentCenter);
                break;
            case 2:
                format.setHorizontalAlignment(CellFormat.HorizontalAlignmentRight);
                break;
            default:
                break;
        }

        switch (alignVertical) {
            case 0:
                format.setVerticalAlignment(CellFormat.VerticalAlignmentTop);
                break;
            case 1:
                format.setVerticalAlignment(CellFormat.VerticalAlignmentCenter);
                break;
            case 2:
                format.setVerticalAlignment(CellFormat.VerticalAlignmentBottom);
                break;
            default:
                break;
        }
        view.setCellFormat(format, row, column, row, column);
    }

    /**
     * @param text             Gia tri
     * @param column           Cot
     * @param alignHorizontal: can giua theo chieu rong 0 : can trai 1 : can
     *                         giua 2 : can phai
     * @param alignVertical    : can giua theo chieu cao 0 : can tren 1 : can giua
     *                         2 : can duoi
     * @throws CellException CellException
     */
    public void setAlignmentEntry(String text, int column, int alignHorizontal, int alignVertical)
            throws CellException {
        int row = this.getLastRow();
        view.setText(row, column, text);
        CellFormat format = view.getCellFormat(row, column, row, column);
        switch (alignHorizontal) {
            case 0:
                format.setHorizontalAlignment(CellFormat.HorizontalAlignmentLeft);
                break;
            case 1:
                format.setHorizontalAlignment(CellFormat.HorizontalAlignmentCenter);
                break;
            case 2:
                format.setHorizontalAlignment(CellFormat.HorizontalAlignmentRight);
                break;
            default:
                break;
        }

        switch (alignVertical) {
            case 0:
                format.setVerticalAlignment(CellFormat.VerticalAlignmentTop);
                break;
            case 1:
                format.setVerticalAlignment(CellFormat.VerticalAlignmentCenter);
                break;
            case 2:
                format.setVerticalAlignment(CellFormat.VerticalAlignmentBottom);
                break;
            default:
                break;
        }
        view.setCellFormat(format, row, column, row, column);
    }

    /**
     * Thiet lap gia tri cho cell o dong row.
     *
     * @param text                Gia tri
     * @param column              Cot
     * @param row                 Dong
     * @param horizontalAlignment
     * @throws CellException CellException
     */
    public void setHorizontalAlignmentEntry(String text, int column, int row, short horizontalAlignment)
            throws CellException {
        view.setTextAsValue(row, column, text);
        CellFormat format = view.getCellFormat(row, column, row, column);
        format.setHorizontalAlignment(horizontalAlignment);
        view.setCellFormat(format, row, column, row, column);
    }

    /**
     * Thiet lap gia tri cho cell o dong hien tai.
     *
     * @param text   Gia tri
     * @param column Cot
     * @throws CellException CellException
     */
    public void setText(String text, int column)
            throws CellException {
        view.setText(lastRow, column, Utils.isNullOrEmpty(text) ? "" : text);
    }

    /**
     * Thiet lap gia tri cho cell o dong row.
     *
     * @param text   Gia tri
     * @param column Cot
     * @param row    Dong
     * @throws CellException CellException
     */
    public void setText(String text, int column, int row)
            throws CellException {
        view.setText(row, column, Utils.isNullOrEmpty(text) ? "" : text);
    }

    public void setNumber(Long number, int column)
            throws CellException {
        CellFormat format = view.getCellFormat(lastRow, column, lastRow, column);
        format.setCustomFormat("#,###");
        view.setNumber(lastRow, column, Utils.NVL(number));
        view.setCellFormat(format, lastRow, column, lastRow, column);
    }

    public void setNumber(Long number, int column, int row)
            throws CellException {
        CellFormat format = view.getCellFormat(row, column, row, column);
        format.setCustomFormat("#,##0");
        view.setNumber(row, column, Utils.NVL(number));
        view.setCellFormat(format, row, column, row, column);
    }

    public void setNumber(Integer number, int column)
            throws CellException {
        CellFormat format = view.getCellFormat(lastRow, column, lastRow, column);
        format.setCustomFormat("#,###");
        view.setNumber(lastRow, column, Utils.NVL(number));
        view.setCellFormat(format, lastRow, column, lastRow, column);
    }

    /**
     * Thiet lap gia tri so cho cell o dong row, cot column
     *
     * @param num:   so
     * @param column
     * @throws com.jxcell.CellException
     */
    public void setNumber(Double num, int column) throws CellException {
        if (num == null) {
            num = 0D;
        }
        CellFormat format = view.getCellFormat(lastRow, column, lastRow, column);
        format.setCustomFormat("#,###");
        view.setNumber(lastRow, column, num);
        view.setCellFormat(format, lastRow, column, lastRow, column);
    }

    /**
     * @param num
     * @param column
     * @throws CellException
     */
    public void setNumberNoFormat(Double num, int column) throws CellException {
        if (num == null) {
            num = 0D;
        }
        view.setNumber(lastRow, column, num);
    }

    /**
     * Thiet lap gia tri so cho cell o dong row, cot column
     *
     * @param num:   so
     * @param column
     * @param row
     * @throws com.jxcell.CellException
     */
    public void setNumber(Double num, int column, int row) throws CellException {
        if (num == null) {
            num = 0D;
        }
        CellFormat format = view.getCellFormat(row, column, row, column);
        format.setCustomFormat("#,##0");
        view.setNumber(row, column, num);
        view.setCellFormat(format, row, column, row, column);
    }

    /**
     * @param num
     * @param fmt
     * @param column
     * @param row
     * @throws CellException
     */
    public void setNumberFormat(Double num, String fmt, int column, int row) throws CellException {
        if (num == null) {
            num = 0D;
        }
        CellFormat format = view.getCellFormat(row, column, row, column);
        format.setCustomFormat(fmt);
        view.setNumber(row, column, num);
        view.setCellFormat(format, row, column, row, column);
    }

    /**
     * Thiet lap gia tri so cho cell o cot column
     *
     * @param num:   so
     * @param column
     * @throws com.jxcell.CellException
     */
    public void setNumberD(Double num, int column) throws CellException {
        if (num == null) {
            num = 0D;
        }
        CellFormat format = view.getCellFormat(lastRow, column, lastRow, column);
        format.setCustomFormat("#,##0.00");
        view.setNumber(lastRow, column, num);
        view.setCellFormat(format, lastRow, column, lastRow, column);
    }

    /**
     * Thiet lap gia tri so cho cell o dong row, cot column
     *
     * @param num:   so
     * @param column
     * @param row
     * @throws com.jxcell.CellException
     */
    public void setNumberD(Double num, int column, int row) throws CellException {
        if (num == null) {
            num = 0D;
        }
        CellFormat format = view.getCellFormat(row, column, row, column);
        format.setCustomFormat("#,##0.00");
        view.setNumber(row, column, num);
        view.setCellFormat(format, row, column, row, column);
    }

    /**
     * Thiet lap cong thuc cho cell.
     *
     * @param text   Gia tri
     * @param column Cot
     * @param row    Dong
     * @throws CellException CellException
     */
    public void setFormula(String text, int column, int row)
            throws CellException {
        view.setFormula(row, column, text);
        //view.recalc();
    }

    /**
     * @throws CellException
     */
    public void recalculateFormula()
            throws CellException {
        view.recalc();
    }

    public void setFormula(String text, int column)
            throws CellException {
        view.setFormula(lastRow, column, text);
        //view.recalc();
    }

    /**
     * Chuyen chi so cot (0, 1, 2, 3,..) thanh nhan (A, B, C,...).
     *
     * @param column Chi so cot
     * @return Nhan tuong ung
     */
    public String convertColumnIndexToLabel(int column) {
        final int ALPHABET_NUMBER = 26; // so chu cai
        if (column < ALPHABET_NUMBER) {
            return String.valueOf((char) ('A' + column));
        } else {
            int temp = column / ALPHABET_NUMBER;
            column -= ALPHABET_NUMBER * temp;
            String result = String.valueOf((char) ('A' + temp - 1));
            return result + ((char) ('A' + column));
        }
    }

    /**
     * @param sheetIndex
     * @throws Exception
     */
    public void setActiveSheet(int sheetIndex) throws Exception {
        view.setSheet(sheetIndex);
        view.setSheetSelected(sheetIndex, true);
    }

    public void setSheetName(String sheetName) throws Exception {
        view.setSheetName(view.getSheet(), sheetName);
    }

    /**
     * @param sheetIndex
     * @throws Exception
     */
    public void setInactiveSheet(int sheetIndex) throws Exception {
        view.setSheet(sheetIndex);
        view.setSheetSelected(sheetIndex, false);
    }

    /**
     * set cot an
     *
     * @param col
     * @throws Exception
     */
    public void hideColumn(int col) throws Exception {
        view.setColHidden(col, true);

    }

    /**
     * an dong
     *
     * @param row
     * @throws Exception
     */
    public void hideRow(int row) throws Exception {
        view.setRowHidden(row, true);
    }

    /**
     * an dong co chua ky tu truyen vao
     *
     * @param text chuoi ky tu truyen vao
     * @throws Exception
     */
    public void hideRow(String text) throws Exception {
        FindReplaceInfo rowHidden = findPosition(text);
        while (rowHidden != null) {
            rowHidden = findPosition(text);
            replaceText(text, "", 1);
            hideRow(rowHidden.getRow());
            rowHidden = findPosition(text);
        }

    }

    public void addText(String str, int row, int col) throws CellException {
        String text = view.getText(row, col);
        view.setText(row, col, Utils.NVL(text) + Utils.NVL(str));
    }


    /**
     * clearCell
     *
     * @param row
     * @param col
     * @throws CellException
     */
    public void clearCell(int row, int col) throws CellException {
        view.setText(row, col, "");
    }

    /**
     * getCellText
     *
     * @param row
     * @param col
     * @return
     * @throws CellException
     */
    public String getCellText(int row, int col) throws CellException {
        return view.getEntry(row, col);
    }

    public FindReplaceInfo findPosition(String text) throws CellException {
        return view.findFirst(text, 1);
    }

    //#042 #End
    public boolean replaceText(String origin, String dest) throws CellException {
        FindReplaceInfo fi = view.findFirst(origin, 1);
        int deepth = 1;
        boolean result = fi != null;
        while (fi != null) {
            view.replace(origin, dest, 1, view.findFirst(origin, 1));
            fi = view.findFirst(origin, 1);
            deepth++;
            if (deepth > 50) {
                return true;
            }
        }
        return result;
    }

    public boolean replaceText(String origin, String dest, int counter) throws CellException {
        FindReplaceInfo fi = view.findFirst(origin, 1);
        int deepth = 1;
        boolean result = fi != null;
        while (fi != null) {
            view.replace(origin, dest, 1, view.findFirst(origin, 1));
            fi = view.findFirst(origin, 1);
            deepth++;
            if (deepth > counter) {
                return true;
            }
        }
        return result;
    }

    public void replaceKeys(Map<String, Object> map) throws CellException {
        FindReplaceInfo fi = null;
        for (String str : map.keySet()) {
            if (!"stt".equalsIgnoreCase(str)) {
                fi = view.findFirst("${" + str + "}", 1);
                if (fi != null) {
                    break;
                }
            }
        }
        if (fi != null) {
            fi = view.findFirst(fi.getRow(), 0, fi.getRow(), 200, "{stt}", 1);
            if (fi != null) {
                view.replace("${stt}", map.get("stt") == null ? "" : map.get("stt").toString(), 1, fi);
            }
        }

        for (String key : map.keySet()) {
            if (!"stt".equalsIgnoreCase(key)) {
                Object object = map.get(key);
                if (object != null) {
                    replaceText("${" + key + "}", objectToString(map.get(key)));
                } else {
                    replaceText("${" + key + "}", "");
                }
            }
        }
    }

    public void replaceKeys(Map<String, Object> map, int row) throws CellException {
        FindReplaceInfo fi;
        for (String origin : map.keySet()) {
            String str = "${" + origin + "}";
            fi = view.findFirst(row, 0, row, 200, str, 1);
            int loop = 1;
            while (fi != null) {
                view.replace(str, objectToString(map.get(origin)), 1, fi);
                fi = view.findFirst(row, 0, row, 200, str, 1);
                loop++;
                if (loop == 20) {
                    break;
                }
            }
        }
    }

    public void setPrintScaleFitToPage(boolean fitTopage) {
        view.setPrintScaleFitToPage(fitTopage);
    }

    public void deleteRange(int row1, int col1, int row2, int col2) throws CellException {
        view.deleteRange(row1, col1, row2, col2, (short) 1);
    }

    public void insertRange(int row1, int col1, int row2, int col2) throws CellException {
        view.insertRange(row1, col1, row2, col2, (short) 3);
    }

    public void insertRow(int totalRow) throws CellException {
        view.insertRange(lastRow + 1, 0, lastRow + totalRow, 100, (short) 3);
    }

    public void replaceKeys(List<Map<String, Object>> maps) throws CellException {
        FindReplaceInfo fi = null;
        boolean isHasFormula = view.findFirst("$FUNCTION", 1) != null;
        if (maps == null || maps.isEmpty()) {
            return;
        }
        //them stt vao neu chua co
        if (!maps.get(0).containsKey("stt")) {
            int STT = 1;
            for (Map<String, Object> map : maps) {
                map.put("stt", String.valueOf(STT++));
            }
        }
        Map<String, Integer> mapColumns = new HashMap();
        for (String str : maps.get(0).keySet()) {
            if (!"stt".equalsIgnoreCase(str)) {
                if (fi == null) {
                    fi = view.findFirst("${" + str + "}", 1);
                }
            }
            FindReplaceInfo fiCol = view.findFirst("${" + str + "}", 1);
            if (fiCol != null) {
                mapColumns.put(str, fiCol.getCol());
            }
        }
        if (fi == null) {
            return;
        }
        int row = fi.getRow();
        if (maps.size() > 1) {
            view.insertRange(row + 1, 0, row + maps.size() - 1, maps.get(0).keySet().size() + 1, (short) 3);
            copyRange(row + 1, 0, row + maps.size() - 1, maps.get(0).keySet().size() + 1, row, 0, row, maps.get(0).keySet().size() + 1);
        }
        for (Map<String, Object> map : maps) {
            for (String origin : map.keySet()) {
                if (mapColumns.get(origin) != null) {
                    String value = objectToString(map.get(origin));
                    if (Utils.isNullOrEmpty(value)) {
                        view.setTextAsValue(row, mapColumns.get(origin), "");
                    } else {
                        view.setTextAsValue(row, mapColumns.get(origin), value);
                    }
                }
            }
            if (isHasFormula) {
                do {
                    fi = view.findFirst(row, 0, row, 200, "$FUNCTION", 1);
                    if (fi != null) {
                        String formula = view.getText(fi.getRow(), fi.getCol());
                        try {
                            setFormula(formula.substring(10, formula.length() - 1), fi.getCol(), fi.getRow());
                        } catch (Exception e) {
                            LOGGER.error(formula + " ; col: " + fi.getCol());
                            throw e;
                        }
                    }
                } while (fi != null);
            }
            row++;
        }
    }

    private String objectToString(Object obj) {
        String dest;
        if (obj == null) {
            dest = "";
        } else if (obj instanceof String) {
            dest = (String) obj;
        } else if (obj instanceof BigDecimal) {
            dest = Utils.formatNumber(((BigDecimal) obj).doubleValue(), "###,###.###");
        } else if (obj instanceof Double) {
            dest = Utils.formatNumber(((Double) obj), "###,###.###");
        } else if (obj instanceof Long) {
            dest = Utils.formatNumber(((Long) obj), "###,###");
        } else if (obj instanceof Integer) {
            dest = Utils.formatNumber(((Integer) obj), "###,###");
        } else if (obj instanceof Date) {
            dest = Utils.formatDate((Date) obj);
        } else if (obj instanceof java.sql.Date) {
            dest = Utils.formatDate((java.sql.Date) obj);
        } else {
            dest = obj.toString();
        }
        return dest;
    }
}
