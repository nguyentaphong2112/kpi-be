package vn.hbtplus.exceptions;

import lombok.Data;
import org.springframework.http.HttpStatus;

@Data
public class BaseAppException extends RuntimeException {
    HttpStatus status;
    String errorCode;
    String message;

    public BaseAppException(Throwable cause, HttpStatus status, String errorCode, String message) {
        super(cause);
        this.status = status;
        this.errorCode = errorCode;
        this.message = message;
    }
    public BaseAppException(HttpStatus status, String errorCode, String message) {
        super();
        this.status = status;
        this.errorCode = errorCode;
        this.message = message;
    }
    public BaseAppException( String errorCode, String message) {
        super();
        this.status = HttpStatus.BAD_REQUEST;
        this.errorCode = errorCode;
        this.message = message;
    }
    public BaseAppException(String message) {
        super();
        this.status = HttpStatus.BAD_REQUEST;
        this.message = message;
    }

    public BaseAppException() {}
}
