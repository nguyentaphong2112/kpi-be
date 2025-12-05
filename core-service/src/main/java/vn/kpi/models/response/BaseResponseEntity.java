package vn.kpi.models.response;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import vn.kpi.models.BaseResponse;

public class BaseResponseEntity<T> extends ResponseEntity<BaseResponse<T>> {
    public BaseResponseEntity(BaseResponse<T> body) {
        super(body, HttpStatus.OK);
    }
}
