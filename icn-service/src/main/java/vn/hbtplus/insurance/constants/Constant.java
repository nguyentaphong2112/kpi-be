package vn.hbtplus.insurance.constants;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Constant {
    public static final int BATCH_SIZE = 999;
    public interface RESOURCES {
    }

    public static final String COMMON_DATE_FORMAT = "dd/MM/yyyy";
    public static final String LOCALE_VN = "vi_VN";
    public static final String TIMEZONE_VN = "Asia/Ho_Chi_Minh";
    public static final String COMMON_DATE_TIME_FORMAT = "dd/MM/yyyy HH:mm:ss";
    public static final Integer SIZE_PARTITION = 999;
    public static final String SHORT_FORMAT_DATE = "MM/yyyy";
    public static final String REQUEST_MAPPING_PREFIX = "";


    public interface CATEGORY_CODES {
    }

    public interface OBJECT_ATTRIBUTES {
        interface TABLE_NAMES {

        }

        interface FUNCTION_CODES {

        }
    }

    public interface ATTACHMENT {
        interface FILE_TYPES {

        }

        interface TABLE_NAMES {

        }

        String MODULE = "hrm";
    }

    public static final String KEY_MASTER_DETAIL = "master%s#detail%s";

    public interface COMMON {
        Long ID_KHOI_CO_QUAN = 9004488l;
    }
    public interface CONFIG_PARAMETERS {
        String ROOT_LEGACY_ID = "ID_DON_VI_GOC";
    }


    public static final class RESOURCE {
        public static final String ICN_CONFIG_PARAMETER = "ICN_CONFIG_PARAMETER";
        public static final String PIT_INCOME_ITEM_TEMPLATE = "PIT_INCOME_ITEM_TEMPLATE";
        public static final String ICN_INSURANCE_CONTRIBUTIONS = "ICN_INSURANCE_CONTRIBUTIONS";
        public static final String PIT_TAX_COMMITMENTS = "PIT_TAX_COMMITMENTS";
        public static final String SYS_CATEGORY = "SYS_CATEGORY";
        public static final String ICN_EMPLOYEE_CHANGES = "ICN_EMPLOYEE_CHANGES";
        public static final String ICN_INSURANCE_RETRACTIONS = "ICN_INSURANCE_RETRACTIONS";
        public static final String INSURANCE_CONTRIBUTIONS_REPORT = "INSURANCE_CONTRIBUTIONS_REPORT";
        public static final String ICN_CONTRIBUTION_RATES = "ICN_CONTRIBUTION_RATES";



    }

    public static final class CATEGORY_TYPE {
        public static final String LOAI_DS_TRICH_NOP = "LOAI_DS_TRICH_NOP";
        public static final String TRANG_THAI_THU_BHXH = "TRANG_THAI_THU_BHXH";
        public static final String NOI_TGIA_BHXH = "NOI_TGIA_BHXH";
        public static final String PHAN_LOAI_LAO_DONG = "ICN_PHAN_LOAI_LD";
        public static final String DOI_TUONG = "DOI_TUONG";
        public static final String DIEN_DOI_TUONG_THUE = "THUE_DOI_TUONG";
        public static final String THUE_DON_VI_KE_KHAI = "THUE_DON_VI_KE_KHAI";
        public static final String THUE_TRANG_THAI = "THUE_TRANG_THAI";
        public static final String CACH_TINH_THUE = "THUE_CACH_TINH";
        public static final String TRANG_THAI_KKT = "TRANG_THAI_KKT";//trạng thái kê khai thuế
        public static final String ACTION_TYPE = "THUE_KIEU_NHAP_LIEU";//Loại hành động(Import, Tổng hợp,...)
        public static final String LOAI_PHU_CAP = "LOAI_PHU_CAP";
        public static final String MOI_QUAN_HE = "MOI_QUAN_HE";
        public static final String CAP_BAC_QUAN_HAM = "CAP_BAC_QUAN_HAM";
        public static final String GIOI_TINH = "GIOI_TINH";
        public static final String LOAI_THAY_DOI = "LOAI_THAY_DOI";
        public static final String TRANG_THAI_TRICH_NOP = "TRANG_THAI_TRICH_NOP";
        public static final String PHAN_NHOM_CHUC_DANH = "PHAN_NHOM_CHUC_DANH";
        public static final String TRANG_THAI_RA_SOAT_DU_LIEU = "TRANG_THAI_RA_SOAT_DU_LIEU";
        public static final String THUE_LOAI_THU_NHAP = "THUE_LOAI_THU_NHAP";
    }

    public static final class STATUS {
        public static final String INIT = "DU_THAO";
        public static final String APPROVE = "PHE_DUYET";
        public static final String REJECT = "TU_CHOI";
        public static final String DELETED = "Y";
        public static final String NOT_DELETED = "N";
    }

    public static final class IncomeType {
        /**
         * tính thuế
         */
        public static final String TAXABLE = "TAXABLE"; //loai thu nhap chịu thue
        /**
         * Không thuế
         */
        public static final String NON_TAX = "NON_TAX"; //loai thu nhap mien thue
        /**
         * Thuế 10%
         */
        public static final String TAX_10 = "TAX_10"; //thu nhap chiu thue 10 %
        /**
         * Thuế 20%
         */
        public static final String TAX_20 = "TAX_20"; //thu nhap chiu thue 20 %
        /**
         * Giảm thuế
         */
        public static final String DEDUCT = "DEDUCT"; //khoan khau tru thue
        public static final String MINIMUM = "MINIMUM"; //thu nhap chiu thu nhung khong tinh thue do duoi thu nhap toi thieu
    }

    public interface INSURANCE_AGENCY {
        String BHXH_BQP = "BQP";
        String BHXH_BA_DINH = "BĐ";

    }
    public interface TAXES_METHOD {
        String LUY_TIEN = "LUY_TIEN";
        String CAM_KET = "CAM_KET";
        String KO_CAM_KET = "KO_CAM_KET";
    }

    public interface LABOUR_TYPE {
        String TT = "TT";
        String GT = "GT";
    }

    public interface PERIOD_TYPE {
        String MONTH = "MONTH";
        String YEAR = "YEAR";
    }

    public interface FILE_TYPE {
        /**
         * Cam kết thu nhập
         */
        Long FILE_TAX_COMMITMENT = 1L;
    }

}
