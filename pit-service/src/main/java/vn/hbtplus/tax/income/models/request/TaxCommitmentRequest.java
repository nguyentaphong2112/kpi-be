package vn.hbtplus.tax.income.models.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.models.BaseSearchRequest;

import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
public class TaxCommitmentRequest {
    @Data
    @NoArgsConstructor
    public static class SearchForm extends BaseSearchRequest {

        private Long orgId;

        @DateTimeFormat(pattern = BaseConstants.COMMON_DATE_FORMAT)
        @JsonFormat(pattern = BaseConstants.COMMON_DATE_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
        private Date startDate;

        @DateTimeFormat(pattern = BaseConstants.COMMON_DATE_FORMAT)
        @JsonFormat(pattern = BaseConstants.COMMON_DATE_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
        private Date endDate;

        private List<Long> listOrgId;
    }

    @Data
    @NoArgsConstructor
    public static class CreateForm {
        @NotNull(message = "Bạn phải nhập nhân viên")
        private Long employeeId;

        @NotNull(message = "Bạn phải nhập số tiền cam kết")
        private Long incomeAmount;

        @NotNull(message = "Bạn phải nhập từ kỳ")
        @DateTimeFormat(pattern = BaseConstants.COMMON_DATE_FORMAT)
        @JsonFormat(pattern = BaseConstants.COMMON_DATE_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
        private Date startDate;

        @DateTimeFormat(pattern = BaseConstants.COMMON_DATE_FORMAT)
        @JsonFormat(pattern = BaseConstants.COMMON_DATE_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
        private Date endDate;
        private MultipartFile attachFile;
        private List<MultipartFile> fileList;
        private String description;
        public void setDescription(String description) {
            if (StringUtils.isNotEmpty(description)) {
                this.description = StringUtils.trim(description);
            } else
                this.description = description;
        }
        List<Long> attachmentDeleteIds;
    }

    @Data
    @NoArgsConstructor
    public static class UpdateForm extends CreateForm {
        private Long taxCommitmentId;
    }
}
