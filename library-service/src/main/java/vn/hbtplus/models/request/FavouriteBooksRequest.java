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
 * Lop DTO ung voi bang lib_favourite_books
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Data
@NoArgsConstructor
public class FavouriteBooksRequest extends BaseSearchRequest {

    @Data
    @NoArgsConstructor
    @Schema(name = "FavouriteBooksSubmitForm")
    public static class SubmitForm {
        private Long favouriteBookId;

        private Long bookId;

        private Long userId;

    }

    @Data
    @NoArgsConstructor
    @Schema(name = "FavouriteBooksSearchForm")
    public static class SearchForm extends BaseSearchRequest{

        private Long favouriteBookId;

        private Long bookId;

        private Long userId;

    }
}
