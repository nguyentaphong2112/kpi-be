package vn.hbtplus.tax.personal.models.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.utils.StrimDeSerializer;

import javax.validation.constraints.*;
import java.util.Date;

/**
 * Autogen class DTO: Lớp thao tác danh sach than nhan
 *
 * @author ToolGen
 * @date Sun Mar 20 21:42:06 ICT 2022
 */
@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FamilyRelationshipsDTO {

    private Long familyRelationshipId;

    private Long draftFamilyRelationshipId;

    @NotNull
    private Long employeeId;

    @NotBlank
    @Size(max = 200)
    @JsonDeserialize(using = StrimDeSerializer.class)
    private String fullName;

    @NotNull
    @Size(max = 200)
    private String relationTypeCode;

    private String relationTypeName;

    private Long dayOfBirth;

    @Min(1)
    @Max(12)
    private Long monthOfBirth;

    @Min(1900)
    @Max(9999)
    private Long yearOfBirth;

    @DateTimeFormat(pattern = BaseConstants.COMMON_DATE_FORMAT)
    @JsonFormat(pattern = BaseConstants.COMMON_DATE_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
    private Date dateOfBirth;

    @Size(max = 200)
    @JsonDeserialize(using = StrimDeSerializer.class)
    private String job;

    @Size(max = 200)
    @JsonDeserialize(using = StrimDeSerializer.class)
    private String workOrganization;

    @Size(max = 500)
    @JsonDeserialize(using = StrimDeSerializer.class)
    private String note;

    private String policyTypeCode;

    @Size(max = 200)
    @JsonDeserialize(using = StrimDeSerializer.class)
    private String currentAddress;

    private Integer isInCompany;

    private Long referenceEmployeeId;
    private String referenceEmployeeCode;

    @Size(max = 20)
    @JsonDeserialize(using = StrimDeSerializer.class)
    private String personalIdNumber;

    private String taxNumber;

    @DateTimeFormat(pattern = BaseConstants.COMMON_DATE_FORMAT)
    @JsonFormat(pattern = BaseConstants.COMMON_DATE_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
    private Date fromDate;

    @DateTimeFormat(pattern = BaseConstants.COMMON_DATE_FORMAT)
    @JsonFormat(pattern = BaseConstants.COMMON_DATE_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
    private Date toDate;

    private Integer flagStatus;

    private Integer status;

    @Min(0)
    private Integer startRecord;

    @Min(1)
    private Integer pageSize;

    private Boolean resultSqlExecute;

    @NotNull
    private String relationStatusCode;

    private String relationStatusName;

    private Integer isHouseholdOwner;

    private String employeeCode;

    private String employeeName;

    private Long orgId;

    // Tên đơn vị
    private String orgName;

    // Đối tượng
    private String empTypeCode;

    private String empTypeName;

    private String phoneNumber;

    private String jobName;
    private String policyTypeName;
    private Integer isRegisteredDependent;

    @DateTimeFormat(pattern = BaseConstants.COMMON_DATE_FORMAT)
    @JsonFormat(pattern = BaseConstants.COMMON_DATE_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
    private Date toDateRegisteredDependent;

    private String createBy;

    @DateTimeFormat(pattern = BaseConstants.COMMON_DATETIME_FORMAT)
    @JsonFormat(pattern = BaseConstants.COMMON_DATETIME_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
    private Date createdDate;

    private String updatedBy;

    @DateTimeFormat(pattern = BaseConstants.COMMON_DATETIME_FORMAT)
    @JsonFormat(pattern = BaseConstants.COMMON_DATETIME_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
    private Date updatedDate;

}
