package vn.kpi.models.response;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import vn.kpi.models.TableResponse;

public class TableResponseEntity<T> extends ResponseEntity<TableResponse<T>> {
    public TableResponseEntity(TableResponse<T> body) {
        super(body, HttpStatus.OK);
    }
}
