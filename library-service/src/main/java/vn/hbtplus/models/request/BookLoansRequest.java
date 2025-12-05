/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.models.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.hbtplus.models.BaseSearchRequest;

import javax.validation.constraints.NotEmpty;
import java.util.List;


/**
 * Lop DTO ung voi bang lib_book_loans
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Data
@NoArgsConstructor
public class BookLoansRequest extends BaseSearchRequest {

    @Data
    @NoArgsConstructor
    @Schema(name = "BookLoansSubmitForm")
    public static class SubmitForm {
        @NotEmpty
        private List<Long> bookEditionDetailIds;
    }

    @Data
    @NoArgsConstructor
    @Schema(name = "BookLoansSearchForm")
    public static class SearchForm extends BaseSearchRequest{

        private Long bookLoanId;

        private Long bookId;

    }
}
