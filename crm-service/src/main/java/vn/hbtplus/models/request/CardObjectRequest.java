package vn.hbtplus.models.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import vn.hbtplus.models.BaseSearchRequest;

@Data
@Builder
@ToString
public class CardObjectRequest extends BaseSearchRequest {

    @Data
    @NoArgsConstructor
    @Schema(name = "CardObjectRequestSearchForm")
    public static class SearchForm extends BaseSearchRequest{

        private String objType;
        private String fullName;
        private String email;
        private String mobileNumber;
        private String fullNameFilter;
        private String mobileNumberFilter;
        private String daysUntilBirthdayFilter;
        private String emailFilter;
        private String productNameFilter;
        private String productPriceFilter;
        private String owedAmountFilter;
        private String currentAddressFilter;
        private String parentNameFilter;
        private String parentMobileNumberFilter;
        private String relationTypeNameFilter;
    }
}
