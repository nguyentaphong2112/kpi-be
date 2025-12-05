package vn.kpi.exceptions;

import org.springframework.http.HttpStatus;

import java.text.MessageFormat;

public class RecordNotExistsException extends BaseAppException {
    public RecordNotExistsException(HttpStatus status, String errorCode, String message) {
        super(status, errorCode, message);
    }
    public RecordNotExistsException(Long id, Class className) {
        super(HttpStatus.BAD_REQUEST, "RECORD NOT EXISTS", MessageFormat.format("{0} with id {1} is not exists", className.getName(), id));
    }
    public RecordNotExistsException(String message){
        super(message);
    }
}
