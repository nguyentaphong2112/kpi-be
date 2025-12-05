package vn.hbtplus.models.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SendMailParamDTO {

    private String groupMail;
    private String toAddress;
    private List<String> listToAddress;
    private String ccAddress;
    private List<String> listCcAddress;
    private Map<String, String> params;
    private MultipartFile file;
    private String fileName;

    public SendMailParamDTO(String groupMail, String toAddress, Map<String, String> params, String fileName){
        this.groupMail = groupMail;
        this.toAddress = toAddress;
        this.params = params;
        this.fileName = fileName;
    }

    public SendMailParamDTO(String groupMail, String toAddress, Map<String, String> params){
        this.groupMail = groupMail;
        this.toAddress = toAddress;
        this.params = params;
    }
}
