package vn.kpi.models.dto;

import lombok.Data;
import vn.kpi.models.response.ObjectAttributesResponse;

import java.util.Date;
import java.util.List;

@Data
public class EmployeeInfoDto {
    private Long employeeId;
    private String employeeCode;
    private String fullName;
    private String email;
    private String positionTitle;
    private String positionName;
    private String otherPositionName;
    private String mobileNumber;
    private Date dateOfBirth;
    private String jobName;
    private Long organizationId;
    private String organizationName;
    private String genderName;
    private String nationName;
    private String ethnicName;
    private String religionName;
    private String maritalStatusName;
    private String educationLevelName;
    private String bankAccount;
    private String identityType;
    private String identityNo;
    private Date identityIssueDate;
    private String identityIssuePlace;
    private String taxNo;
    private String genderId;
    private String religionId;
    private String ethnicId;
    private String maritalStatusId;
    private String educationLevelId;
    private String personalEmail;
    private String empTypeName;
    private String contractTypeName;
    private String insuranceNo;
    private String originalAddress;
    private String placeOfBirth;
    private String permanentAddress;
    private String currentAddress;
    private String majorLevelName;
    private String majorName;
    private String statusName;
    private String status;
    private String trainingSchoolName;
    private String promotionRankName;
    private String promotionRankCode;
    private Integer promotionRankYear;
    private String isHead;
    private String partyNumber;
    private Date partyDate;
    private String partyPlace;
    private Date partyOfficialDate;
    private Integer seniority;
    private String familyPolicyName;
    private String selfPolicyName;
    private String orgLevelManage;
    private String isProbationary;
    private String isJobFree;
    private List<ObjectAttributesResponse> listAttributes;
}
