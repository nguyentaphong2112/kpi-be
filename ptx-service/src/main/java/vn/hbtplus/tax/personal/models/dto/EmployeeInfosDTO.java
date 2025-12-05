/*
 * Copyright (C) 2022 EcoIT. All rights reserved.
 * EcoIT. Use is subject to license terms.
 */
package vn.hbtplus.tax.personal.models.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.utils.StrimDeSerializer;

import javax.validation.constraints.Size;
import java.util.Date;


/**
 * Lop entity ung voi bang HR_EMPLOYEE_INFOS
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EmployeeInfosDTO {

    private Long employeeId;

    private String firstName;

    private String lastName;

    private String middleName;

    private String nickName;

    private String partyNumber;

    @DateTimeFormat(pattern = BaseConstants.COMMON_DATE_FORMAT)
    @JsonFormat(pattern = BaseConstants.COMMON_DATE_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
    private Date partyDate;

    @DateTimeFormat(pattern = BaseConstants.COMMON_DATE_FORMAT)
    @JsonFormat(pattern = BaseConstants.COMMON_DATE_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
    private Date partyOfficialDate;

    @Size(max = 200)
    @JsonDeserialize(using = StrimDeSerializer.class)
    private String partyPlace;

    private String eduSubjectCode;

    private String eduPlaceCode;

    private String eduRankCode;

    private Integer eduGraduateYear;

    private String passportNumber;

    @DateTimeFormat(pattern = BaseConstants.COMMON_DATE_FORMAT)
    @JsonFormat(pattern = BaseConstants.COMMON_DATE_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
    private Date passportIssueDate;

    private String passportIssuePlace;

    @DateTimeFormat(pattern = BaseConstants.COMMON_DATE_FORMAT)
    @JsonFormat(pattern = BaseConstants.COMMON_DATE_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
    private Date passportExpiredDate;

    private Long isArmy;

    private String armyLevelCode;

    @DateTimeFormat(pattern = BaseConstants.COMMON_DATE_FORMAT)
    @JsonFormat(pattern = BaseConstants.COMMON_DATE_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
    private Date armyJoinDate;

    private String bankAccountNo;

    private Long bankId;

    private String pernamentProvinceCode;

    private String pernamentProvinceName;

    private String pernamentDistrictCode;

    private String pernamentDistrictName;

    private String pernamentWardCode;

    private String pernamentWardName;

    private String pernamentDetail;

    private String currentProvinceCode;

    private String currentProvinceName;

    private String currentDistrictCode;

    private String currentDistrictName;

    private String currentWardCode;

    private String currentWardName;

    private String currentDetail;

    private String placeOfBirth;

    private String originalAddress;

    private String pernamentAddress;

    private String currentAddress;

    private String armyLevelName;

    private String workAddress;
}
