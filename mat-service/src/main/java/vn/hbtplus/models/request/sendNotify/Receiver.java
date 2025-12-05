package vn.hbtplus.models.request.sendNotify;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Receiver {
    private String type;
    private List<String> value;
}
