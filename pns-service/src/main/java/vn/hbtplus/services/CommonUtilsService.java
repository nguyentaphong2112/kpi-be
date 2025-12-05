
package vn.hbtplus.services;

import com.aspose.words.Document;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

/**
 *
 * @author tudd
 */
public interface CommonUtilsService {
    
    String getFilePathExport(String fileName);

    String getOnlyFilePathExport(String fileName);

    Map<String, MultipartFile> readFileZip(MultipartFile multipartFile) throws IOException;

    Long getEmpIdLogin();

    void savePdfFile(Document doc, String filePath) throws Exception;

}
