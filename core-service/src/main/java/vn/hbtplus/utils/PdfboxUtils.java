package vn.hbtplus.utils;

import org.apache.commons.lang3.StringUtils;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.destination.PDPageDestination;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.destination.PDPageFitDestination;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDDocumentOutline;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDOutlineItem;

import java.awt.*;
import java.io.File;
import java.io.IOException;

public class PdfboxUtils {

    public void mergePdfFile(String file1, String file2) throws IOException {
        String outputPath = "C:\\Users\\bienn\\project\\vt\\vcc\\hồ sơ\\bbnt\\BHXH\\merged.pdf";
        try (PDDocument doc1 = Loader.loadPDF(new File(file1));
             PDDocument doc2 = Loader.loadPDF(new File(file2));
             PDDocument mergedDoc = new PDDocument()) {

            int totalPagesFile1 = doc1.getNumberOfPages();
            int totalPagesFile2 = doc2.getNumberOfPages();

            int maxPairs = Math.max(totalPagesFile1, totalPagesFile2);

            for (int i = 0; i < maxPairs; i++) {
                // Trang xuôi từ file1: i = 0 → page 0, i = 1 → page 1, ...
                if (i < totalPagesFile1) {
                    PDPage pageFromFile1 = doc1.getPage(i);
                    mergedDoc.addPage(pageFromFile1);
                }

                // Trang ngược từ file2: i = 0 → last page, i = 1 → last - 1, ...
                int reverseIndex = totalPagesFile2 - 1 - i;
                if (reverseIndex >= 0) {
                    PDPage pageFromFile2 = doc2.getPage(reverseIndex);
                    mergedDoc.addPage(pageFromFile2);
                }
            }

            mergedDoc.save(outputPath);
            System.out.println("✅ Đã gộp file thành công: " + outputPath);

        } catch (IOException e) {
            System.err.println("❌ Lỗi khi xử lý PDF: " + e.getMessage());
        }

    }

    public static void main(String[] args) {
        try {
            PdfboxUtils pdfboxUtils = new PdfboxUtils();
            String file1 = "C:\\Users\\bienn\\project\\vt\\vcc\\hồ sơ\\bbnt\\BHXH\\file-hop-dong-1.pdf";
            String file2 = "C:\\Users\\bienn\\project\\vt\\vcc\\hồ sơ\\bbnt\\BHXH\\file-hop-dong-2.pdf";
            pdfboxUtils.mergePdfFile(file1, file2);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void addImageFullPage(String path, PDPage page, PDDocument document) throws IOException {
        PDImageXObject image = PDImageXObject.createFromFile(path, document);
        PDPageContentStream contentStream = new PDPageContentStream(document, page);
        PDRectangle mediaBox = page.getMediaBox();
        contentStream.drawImage(image, 0, 0, mediaBox.getWidth(), mediaBox.getHeight());
        contentStream.close();
    }

    public static void addImageToPage(String path, PDPage page, PDDocument document, float x, float y, float width, float height) throws IOException {
        PDImageXObject image = PDImageXObject.createFromFile(path, document);
        PDPageContentStream contentStream = new PDPageContentStream(document, page, PDPageContentStream.AppendMode.APPEND, true, true);
        contentStream.drawImage(image, x, y, width, height);
        contentStream.close();
    }

    public static void fillTextToPage(Float x, Float y, PDFont font, PDPage page, PDDocument document,
                                      String text, int fontSize, Color color) throws IOException {
        PDPageContentStream contentStream = new PDPageContentStream(document, page, PDPageContentStream.AppendMode.APPEND, true, true);
        if (color != null) {
            contentStream.setNonStrokingColor(color);
        }
        contentStream.setFont(font, fontSize);
        contentStream.beginText();
        contentStream.newLineAtOffset(x, y);
        contentStream.showText(text);
        contentStream.endText();
        contentStream.close();
    }

    public static void fillTextToPage(Float x, Float y, PDFont font, PDPage page, PDDocument document,
                                      String text, int fontSize, Color color, String align) throws IOException {
        PDPageContentStream contentStream = new PDPageContentStream(document, page, PDPageContentStream.AppendMode.APPEND, true, true);
        if (color != null) {
            contentStream.setNonStrokingColor(color);
        }
        float textWidth = (font.getStringWidth(text) / 1000) * fontSize;
        if (StringUtils.equalsIgnoreCase(align, "C")) {
            x = x - textWidth / 2;
        }
        contentStream.setFont(font, fontSize);
        contentStream.beginText();
        contentStream.newLineAtOffset(x, y);
        contentStream.showText(text);
        contentStream.endText();
        contentStream.close();
    }

    public static void writeTextWithWrapping(PDPageContentStream contentStream, PDFont font,
                                              String text, float startX, float startY, float width, float fontSize) throws IOException {
        // Tạo mảng chứa từng từ của văn bản
        String[] words = text.split(" ");
        StringBuilder line = new StringBuilder();
        float leading = 1.5f * fontSize; // Khoảng cách giữa các dòng
        float currentY = startY;

        contentStream.beginText();
        contentStream.newLineAtOffset(startX, currentY);
        contentStream.setFont(font, fontSize);

        for (String word : words) {
            word = word.replace("\t", "    ");
            String tempLine = line + word + " ";

            float textWidth = (font.getStringWidth(tempLine) / 1000) * fontSize;
            if (textWidth > width) {
                contentStream.showText(line.toString());
                contentStream.newLineAtOffset(0, - leading);
                line = new StringBuilder();
            }
            line.append(word).append(" ");
        }
        if (line.length() > 0) {
            contentStream.showText(line.toString());
        }

        contentStream.endText();
    }

    public static void addBookmark(String title, PDDocumentOutline outline, PDPage page, PDOutlineItem parent, Color color) {
        PDOutlineItem item = new PDOutlineItem();
        item.setTitle(title);
        item.setBold(true);
        if (color != null) {
            item.setTextColor(color);
        }
        PDPageDestination destination = new PDPageFitDestination();
        destination.setPage(page);
        item.setDestination(destination);
        if (parent != null) {
            parent.addLast(item);
        } else {
            outline.addLast(item);
        }
    }

    public static void addBookmark(String title, PDDocumentOutline outline, PDPage page, PDOutlineItem parent, Color color, boolean isFirst) {
        PDOutlineItem item = new PDOutlineItem();
        item.setTitle(title);
        item.setBold(true);
        if (color != null) {
            item.setTextColor(color);
        }
        PDPageDestination destination = new PDPageFitDestination();
        destination.setPage(page);
        item.setDestination(destination);
        if (isFirst) {
            if (parent != null) {
                parent.addFirst(item);
            } else {
                outline.addFirst(item);
            }
        } else {
            if (parent != null) {
                parent.addLast(item);
            } else {
                outline.addLast(item);
            }
        }
    }

    public static void addBookmarkParent(String title, PDDocumentOutline outline, PDPage page, PDOutlineItem item, Color color) {
        item.setTitle(title);
        item.setBold(true);
        if (color != null) {
            item.setTextColor(color);
        }
        PDPageDestination destination = new PDPageFitDestination();
        destination.setPage(page);
        item.setDestination(destination);
        outline.addLast(item);
    }
}
