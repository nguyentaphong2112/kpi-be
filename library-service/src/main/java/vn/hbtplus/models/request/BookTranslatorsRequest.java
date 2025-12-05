/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.models.request;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.hbtplus.models.BaseSearchRequest;
import vn.hbtplus.utils.StrimDeSerializer;

import javax.validation.constraints.Size;


/**
 * Lop DTO ung voi bang lib_book_translators
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Data
@NoArgsConstructor
public class BookTranslatorsRequest extends BaseSearchRequest {

    @Data
    @NoArgsConstructor
    @Schema(name = "BookTranslatorsSubmitForm")
    public static class SubmitForm {
        private Long bookTranslatorId;

        @Size(max = 10)
        @JsonDeserialize(using = StrimDeSerializer.class)
        private String translatorId;

        private Long bookId;

    }

    @Data
    @NoArgsConstructor
    @Schema(name = "BookTranslatorsSearchForm")
    public static class SearchForm extends BaseSearchRequest{

        private Long bookTranslatorId;

        @Size(max = 10)
        @JsonDeserialize(using = StrimDeSerializer.class)
        private String translatorId;

        private Long bookId;

    }
}
