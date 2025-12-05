package vn.hbtplus.models.response;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import vn.hbtplus.models.TableResponse;

public class TableResponseEntity<T> extends ResponseEntity<TableResponse<T>> {
    public TableResponseEntity(TableResponse<T> body) {
        super(body, HttpStatus.OK);
    }
}
