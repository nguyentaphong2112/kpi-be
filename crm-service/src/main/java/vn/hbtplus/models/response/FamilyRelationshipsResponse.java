/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.models.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.utils.StrimDeSerializer;

import javax.validation.constraints.Size;


/**
 * Lop Response DTO ung voi bang crm_family_relationships
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Data
@NoArgsConstructor
@JsonInclude(Include.NON_NULL)
public class FamilyRelationshipsResponse {

    private Long familyRelationshipId;
    private String objectType;
    private Long objectId;
    private String relationTypeId;
    private String relationTypeName;

    @JsonFormat(pattern = BaseConstants.COMMON_DATE_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
    private Date dateOfBirth;
    private String fullName;
    private String mobileNumber;
    private String email;
    private String zaloAccount;
    private String facebookAccount;
    private String currentAddress;
    private String relationStatusId;

    @Size(max = 255)
    @JsonDeserialize(using = StrimDeSerializer.class)
    private String job;

    @Size(max = 255)
    @JsonDeserialize(using = StrimDeSerializer.class)
    private String departmentName;
    @Size(max = 20)
    @JsonDeserialize(using = StrimDeSerializer.class)
    private String provinceId;

    @Size(max = 20)
    @JsonDeserialize(using = StrimDeSerializer.class)
    private String districtId;

    @Size(max = 20)
    @JsonDeserialize(using = StrimDeSerializer.class)
    private String wardId;

    @Size(max = 255)
    @JsonDeserialize(using = StrimDeSerializer.class)
    private String villageAddress;

    private String createdBy;

    @JsonFormat(pattern = BaseConstants.COMMON_DATETIME_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
    private Date createdTime;
    private String modifiedBy;

    @JsonFormat(pattern = BaseConstants.COMMON_DATETIME_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
    private Date modifiedTime;
    private List<ObjectAttributesResponse> listAttributes;

}
