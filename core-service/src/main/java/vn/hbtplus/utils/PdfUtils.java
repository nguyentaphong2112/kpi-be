package vn.hbtplus.utils;

import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Image;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;

public class PdfUtils {
    public static void appendPdfFile(PdfDocument pdfDoc, String pathFile) throws IOException {
        PdfDocument srcDoc = new PdfDocument(new PdfReader(pathFile));
        srcDoc.copyPagesTo(1, srcDoc.getNumberOfPages(), pdfDoc);
        srcDoc.close(); // Đóng file PDF nguồn
    }

    public static void appendPdfFile(PdfDocument pdfDoc, InputStream pathFile) throws IOException {
        PdfDocument srcDoc = new PdfDocument(new PdfReader(pathFile));
        srcDoc.copyPagesTo(1, srcDoc.getNumberOfPages(), pdfDoc);
        srcDoc.close(); // Đóng file PDF nguồn
    }

    public static void addImageToPdf(PdfDocument pdfDoc, String imagePath) throws MalformedURLException {
        pdfDoc.addNewPage(PageSize.A4);
        Document document = new Document(pdfDoc);

        Image img = new Image(ImageDataFactory.create(imagePath));
        img.setWidth(PageSize.A4.getWidth());
        img.setHeight(PageSize.A4.getHeight());
        document.add(img);
        // Đóng tài liệu PDF
        document.close();
    }

    public static void addImageToPdf(PdfDocument pdfDoc, InputStream imagePath) throws IOException {
        pdfDoc.addNewPage(PageSize.A4);
        Document document = new Document(pdfDoc);

        Image img = new Image(ImageDataFactory.create(imagePath.readAllBytes()));
        img.setWidth(PageSize.A4.getWidth());
        img.setHeight(PageSize.A4.getHeight());
        document.add(img);
        // Đóng tài liệu PDF
        document.close();
    }



}
