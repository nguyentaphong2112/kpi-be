package vn.hbtplus.exceptions;

import org.springframework.web.multipart.MultipartFile;
import vn.hbtplus.utils.ImportExcel;

public class ErrorImportException extends BaseAppException {
    private MultipartFile fileImport;
    private ImportExcel importExcel;

    public ErrorImportException(MultipartFile fileImport, ImportExcel importExcel) {
        this.fileImport = fileImport;
        this.importExcel = importExcel;
        if (this.importExcel.getImportResult().equals(ImportExcel.IMPORT_RESULT.NO_ERROR)) {
            this.importExcel.setImportResult(ImportExcel.IMPORT_RESULT.DATA_CONTENT_ERROR);
        }
    }

    public MultipartFile getFileImport() {
        return fileImport;
    }

    public ImportExcel getImportExcel() {
        return importExcel;
    }
}
