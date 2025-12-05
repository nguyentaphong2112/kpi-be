/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.models.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Temporal;
import java.util.Date;

@Data
@NoArgsConstructor
@JsonInclude(Include.NON_NULL)
public class EmployeesResponse {

    private Long employeeId;
    private String employeeCode;
    private String fullName;
    private String email;
    private String mobileNumber;
    private Long organizationId;
    private Long positionId;
    private Long jobId;
    private String aliasName;
    @Temporal(javax.persistence.TemporalType.DATE)
    private Date dateOfBirth;
    private String genderId;
    private String religionId;
    private String ethnicId;
    private String maritalStatusId;
    private String personalEmail;
    private String placeOfBirth;
    private String originalAddress;
    private String permanentAddress;
    private String currentAddress;
    private String taxNo;
    private String insuranceNo;
    private Integer status;
    private Long empTypeId;
    private Long educationLevelId;
    private String familyPolicyId;
    private String selfPolicyId;
    private Date partyDate;
    private Date partyOfficialDate;
    private String partyPlace;
    private String partyNumber;
}
