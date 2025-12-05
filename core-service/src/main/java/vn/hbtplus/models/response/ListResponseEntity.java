package vn.hbtplus.models.response;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import vn.hbtplus.models.BaseResponse;

import java.util.List;

public class ListResponseEntity<T> extends ResponseEntity<BaseResponse<List<T>>> {
    public ListResponseEntity(BaseResponse<List<T>> body) {
        super(body, HttpStatus.OK);
    }
}
