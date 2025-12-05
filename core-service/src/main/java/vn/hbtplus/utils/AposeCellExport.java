/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vn.hbtplus.utils;

import com.aspose.cells.AutoFitterOptions;
import com.aspose.cells.Chart;
import com.aspose.cells.ChartMarkerType;
import com.aspose.cells.DataLabels;
import com.aspose.cells.DataLablesSeparatorType;
import com.aspose.cells.HtmlSaveOptions;
import com.aspose.cells.LabelPositionType;
import com.aspose.cells.Line;
import com.aspose.cells.LineType;
import com.aspose.cells.PageSetup;
import com.aspose.cells.SaveOptions;
import com.aspose.cells.Series;
import com.aspose.cells.SeriesCollection;
import com.aspose.cells.WeightType;
import com.aspose.cells.Workbook;
import com.aspose.cells.Worksheet;

import javax.servlet.http.HttpServletRequest;
import java.io.InputStream;

/**
 *
 * @author Admin
 */
public class AposeCellExport {

    /**
     * Cot dau tien cua du lieu
     */
    private Workbook workbook;
    Worksheet worksheet;
    private int row;
    private Chart chart;

    public AposeCellExport(String templateFile, int startDataRow)
            throws Exception {
        row = startDataRow;
        workbook = new Workbook(templateFile);
        worksheet = workbook.getWorksheets().get(0);
    }

    public AposeCellExport(InputStream templateFile, int startDataRow)
            throws Exception {
        row = startDataRow;
        workbook = new Workbook(templateFile);
        worksheet = workbook.getWorksheets().get(0);
    }

    public AposeCellExport(String templateFile)
            throws Exception {
        row = 0;
        workbook = new Workbook(templateFile);
        worksheet = workbook.getWorksheets().get(0);
    }

    public void setActiveSheet(int sheet) {
        worksheet = workbook.getWorksheets().get(sheet);
    }

    public void setText(int row, int column, Object value) {
        worksheet.getCells().get(row, column).putValue(value);
    }

    public void setText(int column, Object value) {
        worksheet.getCells().get(row, column).putValue(value);
    }

    public void increaseRow() {
        row = row + 1;
    }

    /**
     * Ghi ra file Excel.
     *
     * @param fileName File Excel xuat ra
     * @param req
     * @return
     * @throws Exception Exception
     */
    public String exportFile(String fileName, HttpServletRequest req) throws Exception {
        workbook.save(fileName);
        req.setAttribute("fileName", fileName);
        req.setAttribute("filePath", fileName);
        return fileName;
    }

    public void saveFile(String fileName, SaveOptions saveOptions) throws Exception {
        AutoFitterOptions options = new AutoFitterOptions();
        options.setAutoFitMergedCells(true);
        worksheet.autoFitRows(options);
        workbook.calculateFormula();
        workbook.save(fileName, saveOptions);
    }

    public void saveHtmlFile(String fileName) throws Exception {
        HtmlSaveOptions options = new HtmlSaveOptions();
        options.setExportActiveWorksheetOnly(true);
        saveFile(fileName, options);

    }

    public void formatPieChartLabel(int chartIndex, boolean isShowSeriesName, boolean isShowValue, boolean isShowCategoryName, boolean isShowPercentage) throws Exception {
        chart = worksheet.getCharts().get(chartIndex);
        DataLabels dataLabels = chart.getNSeries().get(0).getDataLabels();
        dataLabels.setShowSeriesName(isShowSeriesName);
        dataLabels.setShowValue(isShowValue);
        dataLabels.setShowCategoryName(isShowCategoryName);
        dataLabels.setShowPercentage(isShowPercentage);
        dataLabels.setPosition(LabelPositionType.OUTSIDE_END);
        dataLabels.setSeparator(DataLablesSeparatorType.COMMA);
        chart.getNSeries().get(0).setHasLeaderLines(true);
        chart.calculate();
        int DELTA = 100;
        for (int i = 0; i < chart.getNSeries().get(0).getPoints().getCount(); i++) {
            int X = chart.getNSeries().get(0).getPoints().get(i).getDataLabels().getX();
            if (X > 2000) {
                chart.getNSeries().get(0).getPoints().get(i).getDataLabels().setX(X + DELTA);
            } else {
                chart.getNSeries().get(0).getPoints().get(i).getDataLabels().setX(X - DELTA);
            }
        }
    }

    public void setPieNSeries(int startRow, int startColumn, int endRow, int endColumn, int sheetIndex) {
        String startColumnName = this.getExcelColumnName(startColumn);
        String endColumnName = this.getExcelColumnName(endColumn);
        String area = startColumnName + startRow + ":" + endColumnName + endRow;
        chart.getNSeries().add("Sheet" + sheetIndex + "!" + area, true);

    }

    public void setPieCategoryData(int startRow, int startColumn, int endRow, int endColumn, int sheetIndex) {
        String startColumnName = this.getExcelColumnName(startColumn);
        String endColumnName = this.getExcelColumnName(endColumn);
        String area = startColumnName + startRow + ":" + endColumnName + endRow;
//        Sheet2!$A$1:$F$2
        chart.getNSeries().setCategoryData(sheetIndex + "!" + area);
    }

    public void formatStackColumnChart(int startRow, int startColumn, int endRow, int endColumn, int sheetIndex) throws Exception {
        chart = worksheet.getCharts().get(0);
        SeriesCollection nSeries = chart.getNSeries();
        String startColumnName = this.getExcelColumnName(startColumn);
        String endColumnName = this.getExcelColumnName(endColumn);
        String area = startColumnName + startRow + ":" + endColumnName + endRow;
        nSeries.add("Sheet" + sheetIndex + "!" + area, true);
//            nSeries.add("Sheet2!$A1:$E6", true);

        Series aSeries = nSeries.get(0);
        Line line = aSeries.getSeriesLines();
        line.setStyle(LineType.DOT);
        aSeries.getMarker().setMarkerStyle(ChartMarkerType.TRIANGLE);
        aSeries = nSeries.get(1);
        line = aSeries.getSeriesLines();
        line.setWeight(WeightType.MEDIUM_LINE);

        //Calculate chart
        chart.calculate();
    }

    public void setPrintTitles(String printTitles) {
        PageSetup pageSetup = workbook.getWorksheets().get(0).getPageSetup();
        pageSetup.setPrintTitleRows(printTitles);
    }

    private String getExcelColumnName(int number) {
        final StringBuilder sb = new StringBuilder();

        int num = number - 1;
        while (num >= 0) {
            int numChar = (num % 26) + 65;
            sb.append((char) numChar);
            num = (num / 26) - 1;
        }
        return sb.reverse().toString();
    }
}
