package vn.kpi.exceptions;

import org.springframework.http.HttpStatus;
import vn.kpi.utils.I18n;
import vn.kpi.utils.Utils;

public class RecordConflictException extends BaseAppException {


    public RecordConflictException(Throwable cause, HttpStatus status, String errorCode, String message) {
        super(cause, status, errorCode, message);
    }
    public RecordConflictException(String key, Object ...objects) {
        super();
        this.status = HttpStatus.BAD_REQUEST;
        this.errorCode = "RECORD_CONFLICT";
        if(Utils.isNullOrEmpty(key)){
            this.message = "Record is conflict with other record in database";
        }
        this.message = I18n.getMessage(key, objects);
    }

}
