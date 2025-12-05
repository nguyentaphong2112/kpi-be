/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.models.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.hbtplus.models.BaseSearchRequest;


/**
 * Lop DTO ung voi bang lib_book_reservations
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Data
@NoArgsConstructor
public class BookReservationsRequest extends BaseSearchRequest {

    @Data
    @NoArgsConstructor
    @Schema(name = "BookReservationsSubmitForm")
    public static class SubmitForm {
        private Long libBookReservationId;

        private Long bookId;

    }

    @Data
    @NoArgsConstructor
    @Schema(name = "BookReservationsSearchForm")
    public static class SearchForm extends BaseSearchRequest{

        private Long libBookReservationId;

        private Long bookId;

    }
}
