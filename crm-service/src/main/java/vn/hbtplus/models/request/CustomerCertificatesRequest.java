/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.models.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.util.Date;
import javax.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.utils.StrimDeSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import vn.hbtplus.models.BaseSearchRequest;

/**
 * Lop DTO ung voi bang crm_customer_certificates
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Data
@NoArgsConstructor
public class CustomerCertificatesRequest extends BaseSearchRequest {

    @Data
    @NoArgsConstructor
    @Schema(name = "CustomerCertificatesSubmitForm")
    public static class SubmitForm {
        private Long customerCertificateId;

        private Long customerId;

        private String statusId;

        @Size(max = 11)
        @JsonDeserialize(using = StrimDeSerializer.class)
        private String certificateId;

        @DateTimeFormat(pattern = BaseConstants.COMMON_DATE_FORMAT)
        @JsonFormat(pattern = BaseConstants.COMMON_DATE_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
        private Date issuedDate;

        @Size(max = 1000)
        @JsonDeserialize(using = StrimDeSerializer.class)
        private String note;

        @Size(max = 1000)
        @JsonDeserialize(using = StrimDeSerializer.class)
        private String approvedNote;


    }

    @Data
    @NoArgsConstructor
    @Schema(name = "CustomerCertificatesSearchForm")
    public static class SearchForm extends BaseSearchRequest{

        private Long customerCertificateId;

        private Long customerId;

        private String statusId;

        private String mobileNumber;

        @Size(max = 11)
        @JsonDeserialize(using = StrimDeSerializer.class)
        private String certificateId;

        @DateTimeFormat(pattern = BaseConstants.COMMON_DATE_FORMAT)
        @JsonFormat(pattern = BaseConstants.COMMON_DATE_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
        private Date startDate;

        @DateTimeFormat(pattern = BaseConstants.COMMON_DATE_FORMAT)
        @JsonFormat(pattern = BaseConstants.COMMON_DATE_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
        private Date endDate;

        @DateTimeFormat(pattern = BaseConstants.COMMON_DATE_FORMAT)
        @JsonFormat(pattern = BaseConstants.COMMON_DATE_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
        private Date issuedDate;

        private String mobileNumberFilter;
        private String fullNameFilter;
        private String productNameFilter;
        private String certificateNameFilter;

    }
}
