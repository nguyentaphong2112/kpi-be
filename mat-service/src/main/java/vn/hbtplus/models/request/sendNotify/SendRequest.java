package vn.hbtplus.models.request.sendNotify;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class SendRequest {

    private String appCode;
    private String appPass;
    private List<Receiver> receivers;
    private List<ChannelRequest> channels;
}
