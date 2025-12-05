/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.kpi.models.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import java.util.*;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.kpi.constants.BaseConstants;
import vn.kpi.constants.Constant;
import vn.kpi.utils.Utils;


/**
 * Lop Response DTO ung voi bang kpi_organization_evaluations
 *
 * @author tudd
 * @version 1.0
 * @since 1.0
 */

@Data
@NoArgsConstructor
@JsonInclude(Include.NON_NULL)
public class OrganizationEvaluationsResponse {
    @Data
    @NoArgsConstructor
    @Schema(name = "OrganizationEvaluationsResponseSearchResult")
    public static class SearchResult extends KpiBaseResponse {
        private Long organizationEvaluationId;
        private Long organizationId;
        private String organizationName;
        private String orgNameLevel1;
        private String orgNameLevel2;
        private String orgNameLevel3;
        private Long evaluationPeriodId;
        private String evaluationPeriodName;
        private String status;
        private String empManagerName;
        private String empManagerCode;
        private Long empManagerId;
        private String resultId;
        private String finalResultId;
        private Double selfTotalPoint;
        private Double managerTotalPoint;
        private String approvedBy;
        private Long orgTypeId;
        private Long pathLevel;
        @JsonFormat(pattern = BaseConstants.COMMON_DATETIME_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
        private Date approvedTime;

        public String getOrganizationName() {
            if (!Utils.isNullOrEmpty(orgNameLevel3)) {
                return orgNameLevel3 + " - " + orgNameLevel2;
            } else {
                return Utils.NVL(orgNameLevel2, orgNameLevel1);
            }
        }

        private Double finalPoint;
        private String reason;
        private String reasonRequest;
        private String managerGrade;
        private List<Long> listId;
    }

    @Data
    @NoArgsConstructor
    public static class OrganizationDto {
        private Long organizationId;
        private String organizationName;

        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            OrganizationDto that = (OrganizationDto) o;
            return Objects.equals(organizationId, that.organizationId);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(organizationId);
        }
    }

    @Data
    @NoArgsConstructor
    @Schema(name = "OrganizationEvaluationsResponseIndicatorDetail")
    public static class IndicatorDetail {
        private Long organizationIndicatorId;

        private Long indicatorConversionId;

        private Long indicatorId;

        private Long organizationEvaluationId;

        private Double percent;

        private String target;
    }

    @Data
    @NoArgsConstructor
    @Schema(name = "OrganizationEvaluationsResponseDetailBean")
    public static class DetailBean {
        private List<OrganizationIndicatorsResponse.OrganizationEvaluation> listData;
        private String adjustReason;
    }

    @Data
    @NoArgsConstructor
    @Schema(name = "OrganizationEvaluationsResponseOrgParent")
    public static class OrgParent {
        private String target;
        private String indicatorName;
        private Double percent;
        private String leaderName;
        private String collaboratorName;
    }

    @Data
    @NoArgsConstructor
    @Schema(name = "OrganizationEvaluationsResponseValidate")
    public static class Validate {
        private boolean adjust;
        private boolean adjustKHCT;
    }


    @Data
    @NoArgsConstructor
    @Schema(name = "OrganizationEvaluationsResponseContent")
    public static class Content {
        private Long organizationEvaluationId;
        private String key;
        private String keyValue;
        private String param;
        private String note;
        private String unit;
        private String resultManage;
        private String managePoint;
        private String stepOne;
        private String stepTwo;
        private String fullYear;
    }


    public static Map<Long, String> getXepLoaiString() {
        Map<Long, String> map = new HashMap<>();
        map.put(Constant.XepLoaiConstant.TY_LE_VUOT_DAY_MANH, "xep_loai_ty_le_vuot_day_manh");
        map.put(Constant.XepLoaiConstant.TONG_TRONG_SO_VUOT_DAY_MANH, "xep_loai_tong_trong_so_vuot_day_manh");
        map.put(Constant.XepLoaiConstant.MUC_VUOT_SO_VOI_DAY_MANH, "xep_loai_muc_vuot_so_voi_day_manh");
        map.put(Constant.XepLoaiConstant.KHCT_DON_VI, "xep_loai_khct_don_vi");
        map.put(Constant.XepLoaiConstant.TY_LE_VUOT_MUC_KHCT, "xep_loai_ty_le_vuot_muc_khct");
        map.put(Constant.XepLoaiConstant.TY_LE_KHONG_HOAN_THANH_KHCT, "xep_loai_ty_le_khong_hoan_thanh_khct");
        map.put(Constant.XepLoaiConstant.TONG_SO_GIO_GIANG, "xep_loai_tong_so_gio_giang");
        map.put(Constant.XepLoaiConstant.TY_LE_VUOT_MUC_CBQT, "xep_loai_ty_le_vuot_muc_cbqt");
        map.put(Constant.XepLoaiConstant.TY_LE_VUOT_MUC_CBTC, "xep_loai_ty_le_vuot_muc_cbtc");
        map.put(Constant.XepLoaiConstant.PHAT_TRIEN_DAO_TAO, "xep_loai_phat_trien_dao_tao");
        map.put(Constant.XepLoaiConstant.KIEM_DINH_DAI_HOC, "xep_loai_kiem_dinh_dai_hoc");
        map.put(Constant.XepLoaiConstant.KIEM_DINH_THAC_SI, "xep_loai_kiem_dinh_thac_si");
        map.put(Constant.XepLoaiConstant.TY_LE_TOT_NGHIEP_DUNG_HAN, "xep_loai_ty_le_tot_nghiep_dung_han");
        map.put(Constant.XepLoaiConstant.SO_GIANG_VIEN_DAT_NN, "xep_loai_so_giang_vien_dat_nn");
        map.put(Constant.XepLoaiConstant.SO_GIANG_VIEN_BAO_VE_LATS, "xep_loai_so_giang_vien_bao_ve_lats");
        return map;
    }

}
