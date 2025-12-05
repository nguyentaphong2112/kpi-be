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
 * Lop DTO ung voi bang hr_related_organizations
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Data
@NoArgsConstructor
public class RelatedOrganizationsRequest extends BaseSearchRequest {

    @Data
    @NoArgsConstructor
    @Schema(name = "RelatedOrganizationsSubmitForm")
    public static class SubmitForm {
        private Long relatedOrganizationId;

        private Long organizationId;

        private Long constraintOrgId;

    }

    @Data
    @NoArgsConstructor
    @Schema(name = "RelatedOrganizationsSearchForm")
    public static class SearchForm extends BaseSearchRequest{

        private Long relatedOrganizationId;

        private Long organizationId;

        private Long constraintOrgId;

    }
}
