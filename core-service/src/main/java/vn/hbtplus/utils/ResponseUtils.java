package vn.hbtplus.utils;

import com.aspose.words.PdfCompliance;
import com.aspose.words.PdfSaveOptions;
import com.aspose.words.SaveFormat;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.ThreadContext;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import org.w3c.dom.Document;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.models.BaseDataTableDto;
import vn.hbtplus.models.BaseResponse;
import vn.hbtplus.models.TableResponse;
import vn.hbtplus.models.beans.ServiceHeaderBean;
import vn.hbtplus.models.response.BaseResponseEntity;
import vn.hbtplus.models.response.ListResponseEntity;
import vn.hbtplus.models.response.TableResponseEntity;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Component
public class ResponseUtils {
    private static long maxFileSizeToPreview;

    public static ResponseEntity<Object> getResponseDataNotFound() {
        return ResponseUtils.error(HttpStatus.BAD_REQUEST, "Không có dữ liệu xuất báo cáo!");
    }


    @Value("${app.maxFileSizeToPreview}")
    public void setMaxFileSizeToPreview(int maxFileSizeToPreview) {
        ResponseUtils.maxFileSizeToPreview = maxFileSizeToPreview;
    }

    public static <T> BaseResponseEntity<T> ok(T obj) {
        var serviceHeader = extractServiceHeader();
        return new BaseResponseEntity<>(new BaseResponse<T>(serviceHeader.getClientMessageId())
                .success(obj));
    }

    public static <T> ListResponseEntity<T> ok(List<T> obj) {
        var serviceHeader = extractServiceHeader();
        return new ListResponseEntity<>(new BaseResponse<List<T>>(serviceHeader.getClientMessageId())
                .success(obj));
    }

    public static ResponseEntity ok() {
        return ok("");
    }

    public static <T> TableResponseEntity<T> ok(BaseDataTableDto<T> obj) {
        var serviceHeader = extractServiceHeader();
        return new TableResponseEntity<>(new TableResponse(serviceHeader.getClientMessageId(), obj));
    }

    public static ResponseEntity ok(ExportExcel exportExcel, String fileName) throws Exception {
        return ok(exportExcel, fileName, true);
    }

    public static ResponseEntity ok(ExportExcel exportExcel, String fileName, boolean isPreView) throws Exception {
        //b1: luu file
        String pathExport = Utils.getFilePathExport(fileName);
        exportExcel.exportFile(pathExport);
        //b2: convert file to preview
        File fileExport = new File(pathExport);
        if (isPreView) {
            if (fileExport.length() < maxFileSizeToPreview * 1024) {
                try {
                    AposeCellExport aposeCellExport = new AposeCellExport(pathExport);
                    String htmlFileName = pathExport.replaceAll(".xlsx", ".html");
                    aposeCellExport.saveHtmlFile(htmlFileName);
                    ByteArrayResource byteArrayResource = new ByteArrayResource(Files.readAllBytes(Path.of(htmlFileName)));
                    return ResponseEntity.ok()
                            .headers(getHttpHeaders(pathExport, htmlFileName))
                            .contentLength(byteArrayResource.contentLength())
                            .contentType(MediaType.APPLICATION_OCTET_STREAM)
                            .body(byteArrayResource);
                } catch (Exception e) {
                    log.error("error when convert file to pdf", e);
                }
            }
            return getResponsePreviewError(pathExport);
        } else {
            return getResponseFileEntity(pathExport, false);
        }
    }

    public static ResponseEntity<Object> ok(Document doc, String fileName) throws TransformerException {
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        String outputFile = Utils.getFilePathExport(fileName);
        StreamResult result = new StreamResult(new File(outputFile));
        DOMSource source = new DOMSource(doc);
        transformer.transform(source, result);
        return getResponseFileEntity(outputFile, false);
    }

    public static ResponseEntity<StreamingResponseBody> getResponseFileStream(byte[] data, String fileName) {

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName);

        headers.setContentLength(data.length);

        StreamingResponseBody responseBody = outputStream -> {
            try (InputStream inputStream = new ByteArrayInputStream(data)) {
                // Tăng kích thước bộ đệm
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                    outputStream.flush();
                }
            } catch (IOException e) {
                log.error("[getResponseFileStream] error", e);
            }
        };

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(responseBody);
    }

    public static ResponseEntity<Object> ok(ExportWorld exportWorld, String fileName) {
        String pathExport = Utils.getFilePathExport(fileName);
        Path path = null;
        try {
            String pdfFileName;
            if (fileName.contains(".docx")) {
                exportWorld.saveFile(pathExport, SaveFormat.DOCX);
                pdfFileName = fileName.replaceAll(".docx", ".pdf");
            } else {
                exportWorld.saveFile(pathExport, SaveFormat.DOC);
                pdfFileName = fileName.replaceAll(".doc", ".pdf");
            }
            File fileExport = new File(pathExport);
            if (fileExport.length() < maxFileSizeToPreview * 1024) { //kiem tra neu dung luong file lon qua
                try {
                    String pathPdf = Utils.getFilePathExport(pdfFileName);
                    exportWorld.saveFile(pathPdf, SaveFormat.PDF);
                    path = Path.of(pathPdf);
                    //File filePdf = new File(pdfFileName);
                    HttpHeaders httpHeaders = getHttpHeaders(pathExport, pdfFileName);
                    httpHeaders.add("showPdf", "true");
                    ByteArrayResource byteArrayResource = new ByteArrayResource(Files.readAllBytes(path));
                    return ResponseEntity.ok()
                            .headers(httpHeaders)
                            .contentLength(byteArrayResource.contentLength())
                            .contentType(MediaType.APPLICATION_OCTET_STREAM)
                            .body(byteArrayResource);
                } catch (Exception e) {
                    log.error("error when convert file to pdf", e);
                }
            }
            return getResponsePreviewError(pathExport);
        } catch (Exception e) {
            log.error("[getResponseFileEntity] has error {0}", e);
            return ResponseUtils.error(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        } finally {
            try {
                if (path != null) {
                    Files.delete(path);
                }
            } catch (Exception ex) {
                log.error("[delete file] has error: {0}", ex);
            }
        }
    }

    public static ResponseEntity<Object> ok(ExportWorld exportWorld, String fileName, boolean isPreview ) {
        String pathExport = Utils.getFilePathExport(fileName);
        Path path = null;
        try {
            String pdfFileName;
            if (fileName.contains(".docx")) {
                exportWorld.saveFile(pathExport, SaveFormat.DOCX);
                pdfFileName = fileName.replaceAll(".docx", ".pdf");
            } else if (fileName.contains(".doc")) {
                exportWorld.saveFile(pathExport, SaveFormat.DOC);
                pdfFileName = fileName.replaceAll(".doc", ".pdf");
            }
            exportWorld.saveFile(pathExport, SaveFormat.PDF);
            pdfFileName = fileName.replaceAll(".doc", ".pdf");
            File fileExport = new File(pathExport);
            if(isPreview){
                if (fileExport.length() < maxFileSizeToPreview * 1024) { //kiem tra neu dung luong file lon qua
                    try {
                        String pathPdf = Utils.getFilePathExport(pdfFileName);
                        exportWorld.saveFile(pathPdf, SaveFormat.PDF);
                        path = Path.of(pathPdf);
                        //File filePdf = new File(pdfFileName);
                        HttpHeaders httpHeaders = getHttpHeaders(pathExport, pdfFileName);
                        httpHeaders.add("showPdf", "true");
                        ByteArrayResource byteArrayResource = new ByteArrayResource(Files.readAllBytes(path));
                        return ResponseEntity.ok()
                                .headers(httpHeaders)
                                .contentLength(byteArrayResource.contentLength())
                                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                                .body(byteArrayResource);
                    } catch (Exception e) {
                        log.error("error when convert file to pdf", e);
                    }
                }
                return getResponsePreviewError(pathExport);
            }

            return getResponseFileEntity(pathExport, false);

        } catch (Exception e) {
            log.error("[getResponseFileEntity] has error {0}", e);
            return ResponseUtils.error(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        } finally {
            try {
                if (path != null) {
                    Files.delete(path);
                }
            } catch (Exception ex) {
                log.error("[delete file] has error: {0}", ex);
            }
        }
    }

    public static ResponseEntity<Object> error(HttpStatus httpStatus, String message) {
        var serviceHeader = extractServiceHeader();
        return ResponseEntity.status(httpStatus)
                .body(new BaseResponse(serviceHeader.getClientMessageId())
                        .withMessage(I18n.getMessage(message)).status(BaseConstants.RESPONSE_STATUS.ERROR));
    }

    public static ResponseEntity<Object> error(HttpStatus httpStatus, String errorCode, String message) {
        var serviceHeader = extractServiceHeader();
        return ResponseEntity.status(httpStatus)
                .body(new BaseResponse(serviceHeader.getClientMessageId())
                        .withMessage(I18n.getMessage(message))
                        .errorCode(errorCode).status(BaseConstants.RESPONSE_STATUS.ERROR));
    }

    public static ResponseEntity<Object> badRequest(String errorCode, String message) {
        var serviceHeader = extractServiceHeader();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new BaseResponse(serviceHeader.getClientMessageId())
                        .withMessage(I18n.getMessage(message)).errorCode(errorCode).status(BaseConstants.RESPONSE_STATUS.ERROR));
    }

    public static ResponseEntity<Object> serverError(String errorCode, String message) {
        var serviceHeader = extractServiceHeader();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new BaseResponse(serviceHeader.getClientMessageId())
                        .withMessage(I18n.getMessage(message)).errorCode(errorCode).status(BaseConstants.RESPONSE_STATUS.ERROR));
    }


    private static ServiceHeaderBean extractServiceHeader() {
        try {
            String str = ThreadContext.get("serviceHeader");
            if (str == null) {
                return new ServiceHeaderBean();
            }
            return Utils.fromJson(str, ServiceHeaderBean.class);
        } catch (Exception e) {
            return new ServiceHeaderBean();
        }
    }

    public static ResponseEntity<Object> getResponseFileEntity(String filePath, boolean isDelete) {
        File file = new File(filePath);
        log.info("[getResponseFileEntity] File path: {}", filePath);
        if (file.exists()) {
            log.info("[getResponseFileEntity] file exists");
        }
        Path path = Paths.get(file.getAbsolutePath());
        try {
            byte[] data = Files.readAllBytes(path);
            return ResponseEntity.ok()
                    .headers(getHttpHeaders(filePath, null))
                    .contentLength(data.length)
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(data);
        } catch (Exception e) {
            log.error("[getResponseFileEntity] has error: {0}", e);
            return null;
        } finally {
            try {
                if (isDelete) {
                    Files.delete(path);
                }
            } catch (Exception ex) {
                log.error("[getResponseFileEntity] has error: {0}", ex);
            }
        }
    }


    public static ResponseEntity<Object> getResponseFileEntity(byte[] fileBytes, String fileName) throws IOException {
        ByteArrayResource resource = new ByteArrayResource(fileBytes);
        String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8.name()).replace("+", "%20");
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", String.format("attachment; inline; filename*=UTF-8''%1$s; filename=%1$s", encodedFileName));
        headers.add("isPreview", "false");
        headers.add("fileDownload", fileName);
        headers.add("Access-Control-Expose-Headers", "Content-Disposition, fileDownload, isPdf, isPreview");
        return ResponseEntity.ok()
                .headers(headers)
                .contentLength(fileBytes.length)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }

    public static ResponseEntity<Object> getResponseFileEntity(byte[] fileBytes, String fileName, boolean isPdf, boolean isImage) throws IOException {
        ByteArrayResource resource = new ByteArrayResource(fileBytes);
        String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8.name()).replace("+", "%20");
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", String.format("attachment; inline; filename*=UTF-8''%1$s; filename=%1$s", encodedFileName));
        headers.add("isPreview", isPdf ? "true" : "false");
        if (isPdf) {
            String pdfFilePath = Utils.getExportFolder() + fileName;
            File file = new File(pdfFilePath);
            Path path = Paths.get(file.getAbsolutePath());
            Files.write(path, fileBytes);
            headers.add("isPdf", "true");
        }
        if (isImage) {
            headers.add("isImage", "true");
        }
        headers.add("fileDownload", fileName);
        headers.add("Access-Control-Expose-Headers", "Content-Disposition, fileDownload, isPdf, isPreview, isImage");
        return ResponseEntity.ok()
                .headers(headers)
                .contentLength(fileBytes.length)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }

    private static ResponseEntity<Object> getResponsePreviewError(String fileName) throws IOException {
        InputStream inputStream = new ClassPathResource("template/export/file-error.html").getInputStream();
        return ResponseEntity.ok()
                .headers(getHttpHeaders(fileName, "file-error.html"))
                .contentLength(inputStream.available())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(new ByteArrayResource(inputStream.readAllBytes()));
    }


    @SneakyThrows
    private static HttpHeaders getHttpHeaders(String fileContent, String fileNamePreview) {
        fileContent = fileContent.substring(fileContent.lastIndexOf("/") + 1);
        String encodedFileName;
        if (!Utils.isNullOrEmpty(fileNamePreview)) {
            encodedFileName = URLEncoder.encode(fileNamePreview, StandardCharsets.UTF_8.name()).replace("+", "%20");
        } else {
            encodedFileName = URLEncoder.encode(fileContent, StandardCharsets.UTF_8.name()).replace("+", "%20");
        }
        String[] arr = encodedFileName.split("%2F");
        encodedFileName = arr[arr.length - 1];
        // add header
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition",
                String.format("attachment; inline; filename=%s;", encodedFileName));
        headers.add("isPreview", String.valueOf(!Utils.isNullOrEmpty(fileNamePreview)));
        if (!Utils.isNullOrEmpty(fileNamePreview)) {
            headers.add("isPdf", String.valueOf(fileNamePreview.toLowerCase().endsWith(".pdf")));
        }
        headers.add("fileDownload", fileContent);
        headers.add("Access-Control-Expose-Headers", "Content-Disposition, fileDownload, isPdf, isPreview");
        return headers;
    }
}
