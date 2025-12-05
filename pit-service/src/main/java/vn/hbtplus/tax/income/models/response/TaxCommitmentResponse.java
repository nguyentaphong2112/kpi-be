package vn.hbtplus.tax.income.models.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.models.Attachment;

import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TaxCommitmentResponse {
    private Long taxCommitmentId;

    private Long employeeId;
    private String employeeCode;
    private String fullName;
    private Long incomeAmount;
    @JsonFormat(pattern = BaseConstants.SHORT_DATE_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
    private Date startDate;
    @JsonFormat(pattern = BaseConstants.SHORT_DATE_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
    private Date endDate;

    @JsonFormat(pattern = BaseConstants.SHORT_DATE_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
    private Date lastUpdateTime;

    private String createdBy;
    @JsonFormat(pattern = BaseConstants.COMMON_DATE_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
    private Date createdTime;

    private String modifiedBy;
    @JsonFormat(pattern = BaseConstants.COMMON_DATE_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
    private Date modifiedTime;

    private String description;
    private String orgName;

    private List<AttachmentFileResponse> attachmentList;
    private List<Attachment> attachFileList;
    private Double seniority;
}
