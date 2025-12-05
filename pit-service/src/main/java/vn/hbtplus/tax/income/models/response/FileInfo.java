package vn.hbtplus.tax.income.models.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author hieuhc
 * @since 09/07/2024
 * @version 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FileInfo {
    private Long attachmentFileId;
    private String fileName;
}
