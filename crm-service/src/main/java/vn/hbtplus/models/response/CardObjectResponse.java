package vn.hbtplus.models.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.utils.Utils;

import java.util.Date;

@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CardObjectResponse {

    @Data
    @NoArgsConstructor
    @Schema(name = "CardObjectResponseSearchResult")
    public static class SearchResult {
        private Long objId;
        private String objType;
        private String objName;
        private String fullName;
        private String mobileNumber;
        @JsonFormat(pattern = BaseConstants.COMMON_DATE_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
        private Date dateOfBirth;
        private String email;
        private String relationTypeName;
        private String parentName;
        private String parentMobileNumber;
        private String currentAddress;
        private Long totalAmount;
        private Long totalPayment;
        private Integer daysUntilBirthday;
        private Long paidAmount;
        private Long totalOrderAmount;
        @JsonIgnore
        private String productDetail;

        public String getProductName(){
            return Utils.isNullOrEmpty(productDetail) ? null : productDetail.split("#")[0];
        }
        public Long getProductPrice(){
            return Utils.isNullOrEmpty(productDetail) ? null : Double.valueOf(productDetail.split("#")[1]).longValue();
        }
        public Long getOwedAmount (){
            return Utils.NVL(totalOrderAmount) - Utils.NVL(paidAmount);
        }
    }


    @Data
    @NoArgsConstructor
    @Schema(name = "CardObjectResponseDetailBean")
    public static class DetailBean {
        private Long objId;
        private String objType;
        private String objName;
        private String name;
        private String fullName;
        private String aliasName;
        private String mobileNumber;
        private Date dateOfBirth;
        private String email;

        public String getNameAndPhone() {
            return mobileNumber + " - " + fullName;
        }
    }

}
