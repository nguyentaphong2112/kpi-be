package vn.hbtplus.utils;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class PdfUtils {

    public static byte[] convertToImage(byte[] pdfBytes) throws Exception {
        // Đọc tài liệu PDF từ mảng byte
        try (PDDocument document = PDDocument.load(pdfBytes)) {
            // Tạo đối tượng PDFRenderer từ PDDocument
            PDFRenderer pdfRenderer = new PDFRenderer(document);

            // Render trang đầu tiên thành ảnh BufferedImage
            BufferedImage bim = pdfRenderer.renderImageWithDPI(0, 300); // 300 DPI

            // Chuyển đổi BufferedImage thành mảng byte
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(bim, "png", baos);
            baos.flush();
            byte[] imageBytes = baos.toByteArray();
            baos.close();
            return imageBytes;
        }
    }

    public static byte[] getFistPage(InputStream inputStream) throws IOException {
        PDDocument document = PDDocument.load(inputStream);
        // Lấy trang đầu tiên từ tài liệu
        PDPage firstPage = document.getPage(0);
        return firstPage.getContents().readAllBytes();
    }

    public static MultipartFile convertBytesToMultipartFile(byte[] content, String originalFilename, String contentType) {
        MultipartFile multipartFile = new CustomMultipartFile("file", originalFilename, contentType, content);
        return multipartFile;
    }
}
