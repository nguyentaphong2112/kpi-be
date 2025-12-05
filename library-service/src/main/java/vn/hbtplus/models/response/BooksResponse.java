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
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.models.Attachment;
import vn.hbtplus.models.request.BooksRequest;
import vn.hbtplus.repositories.entity.AttachmentEntity;
import vn.hbtplus.utils.StrimDeSerializer;

import javax.validation.constraints.Size;


/**
 * Lop Response DTO ung voi bang lib_books
 *
 * @author tudd
 * @version 1.0
 * @since 1.0
 */

@Data
@NoArgsConstructor
@JsonInclude(Include.NON_NULL)
public class BooksResponse {
    @Data
    @NoArgsConstructor
    @Schema(name = "BooksResponseSearchResult")
    public static class SearchResult {
        private Long bookId;
        private String title;
        private String authorName;
        private String avatar;
    }

    @Data
    @NoArgsConstructor
    @Schema(name = "BooksResponseAuthorResult")
    public static class AuthorResult {
        private String authorId;
        private String authorName;
        private Integer total;
    }

    @Data
    @NoArgsConstructor
    @Schema(name = "BooksResponseTranslatorResult")
    public static class TranslatorResult {
        private String translatorId;
        private String translatorName;
        private Integer total;
    }

    @Data
    @NoArgsConstructor
    @Schema(name = "BooksResponseGenreResult")
    public static class GenreResult {
        private Long genreId;
        private String genreName;
        private Integer total;
    }


    @Data
    @NoArgsConstructor
    @Schema(name = "BooksResponseDetailResult")
    public static class DetailResult {
        private String title;
        private String originalTitle;
        private String subtitle;
        private String genreName;
        private String authorName;
        private String translatorName;
        private String genreId;
        private String authorId;
        private String languageId;
        private List<String> translatorIds;
        private String summary;
        private String isFavourite;
        private String tableOfContents;
        private String language;
        private String type;
        private String tags;
        private List<BooksResponse.Edition> listEditions;
        private List<Attachment> fileAvatar;
        private List<Attachment> fileContent;
    }

    @Data
    @NoArgsConstructor
    @Schema(name = "BooksResponseEdition")
    public static class Edition {
        private Long bookEditionId;
        private Integer publishedYear;
        private String storeName;
        private Integer totalPages;
        private String bookFormatName;
        private String publisherName;
        private String storeId;
        private String bookFormatId;
        private String publisherId;
        private Long availableNumber;
        private Long borrowNumber;
    }

}
