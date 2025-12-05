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
 * Lop DTO ung voi bang crm_employee_profiles
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Data
@NoArgsConstructor
public class EmployeeProfilesRequest extends BaseSearchRequest {

    @Data
    @NoArgsConstructor
    @Schema(name = "EmployeeProfilesSubmitForm")
    public static class SubmitForm {
        private Long employeeProfileId;

        private Long employeeId;

        @Size(max = 255)
        @JsonDeserialize(using = StrimDeSerializer.class)
        private String attachmentType;

        @Size(max = 255)
        @JsonDeserialize(using = StrimDeSerializer.class)
        private String note;

    }

    @Data
    @NoArgsConstructor
    @Schema(name = "EmployeeProfilesSearchForm")
    public static class SearchForm extends BaseSearchRequest{

        private Long employeeProfileId;

        private Long employeeId;

        @Size(max = 255)
        @JsonDeserialize(using = StrimDeSerializer.class)
        private String attachmentType;

        @Size(max = 255)
        @JsonDeserialize(using = StrimDeSerializer.class)
        private String note;

    }
}
