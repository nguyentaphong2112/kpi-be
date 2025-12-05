package vn.hbtplus.models.request.sendNotify;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ChannelRequest {
    private String type;
    private String template;
    private String subject;
    private String title;
    private String message;
    private String data;
    private String alias;
}
