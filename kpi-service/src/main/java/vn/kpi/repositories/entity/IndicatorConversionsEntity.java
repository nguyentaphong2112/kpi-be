/*
 * Copyright (C) 2022 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.kpi.repositories.entity;

import lombok.Data;

import javax.persistence.*;
import java.util.HashMap;
import java.util.Map;


/**
 * Lop entity ung voi bang kpi_indicator_conversions
 *
 * @author tudd
 * @version 1.0
 * @since 1.0
 */

@Data
@Entity
@Table(name = "kpi_indicator_conversions")
public class IndicatorConversionsEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "indicator_conversion_id")
    private Long indicatorConversionId;

    @Column(name = "indicator_id")
    private Long indicatorId;
    @Column(name = "note")
    private String note;
    @Column(name = "indicator_master_id")
    private Long indicatorMasterId;
    @Column(name = "status")
    private String status;
    @Column(name = "conversion_type")
    private String conversionType;
    @Column(name = "is_focus_reduction")
    private String isFocusReduction;
    @Column(name = "is_required")
    private String isRequired;


    public interface STATUS {
        String PHE_DUYET = "PHE_DUYET";
        String CHO_PHE_DUYET = "CHO_PHE_DUYET";
        String DE_NGHI_XOA = "DE_NGHI_XOA";
        String TU_CHOI_PHE_DUYET = "TU_CHOI_PHE_DUYET";
        String HET_HIEU_LUC = "HET_HIEU_LUC";
        String CHO_PHE_DUYET_HIEU_LUC_LAI = "CHO_PHE_DUYET_HIEU_LUC_LAI";
    }

    public interface CONVERSION_TYPE {
        String CA_NHAN = "CA_NHAN";
        String DON_VI = "DON_VI";
    }

    public interface FOCUS_REDUCTION {
        String Y = "Y";
        String N = "N";
    }

    public static final Map<String, String> REQUIRED_LIST_MAP = new HashMap<>();

    static {
        REQUIRED_LIST_MAP.put("Có", "Y");
        REQUIRED_LIST_MAP.put("Không", "N");
    }

    public static final Map<String, String> CONVERSION_VALUE_MAP = new HashMap<>();

    static {
        CONVERSION_VALUE_MAP.put("thang đo theo kết quả thực hiện", "CA_NHAN");
        CONVERSION_VALUE_MAP.put("thang đo theo mức đăng ký của đơn vị", "DON_VI");
    }
}
