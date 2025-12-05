package vn.hbtplus.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.utils.I18n;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;

@Data
@AllArgsConstructor
@RequiredArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BaseResponse<T> implements Serializable {
    private static final long serialVersionUID = 1L;

    private Date timestamp;
    private String clientMessageId;

    private String message;
    private String code;

    private T data;

    private int status = BaseConstants.RESPONSE_STATUS.SUCCESS;
//    private List<String> errors;

    public BaseResponse(String clientMessageId) {
        this.timestamp = new Date();
        this.message = I18n.getMessage("global.success");
        this.clientMessageId = clientMessageId;
    }


    public BaseResponse<T> success(T o) {
        this.code = code == null ? "SUCCESS" : code;
        this.status =  BaseConstants.RESPONSE_STATUS.SUCCESS;
        if (o != null) {
            this.data = o;
        } else {
            this.data = (T) new HashMap<String, String>();
        }
        return this;
    }

    public BaseResponse<T> status(int status) {
        this.status = status;
        return this;
    }

    public BaseResponse<T> withMessage(String message) {
        this.message = message;
        return this;
    }

    public BaseResponse<T> errorCode(String errorCode) {
        this.code = errorCode;
        return this;
    }
}
