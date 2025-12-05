package vn.kpi.constants;

public class BaseConstants {
    public static final String COMMON_DATE_FORMAT = "dd/MM/yyyy";
    public static final String SHORT_DATE_FORMAT = "MM/yyyy";
    public static final String MONTH_FORMAT = "MM";
    public static final String YEAR_FORMAT = "yyyy";
    public static final String SQL_DATE_FORMAT = "%d/%m/%Y";
    public static final String SQL_SHORT_DATE_FORMAT = "%m/%Y";
    public static final String SQL_YEAR_FORMAT = "%Y";

    public static final String LOCALE_VN = "vi_VN";
    public static final String TIMEZONE_VN = "Asia/Ho_Chi_Minh";
    public static final String COMMON_DATETIME_FORMAT = "dd/MM/yyyy HH:mm:ss";
    public static final String REQUEST_MAPPING_PREFIX = "";
    public static final String DEFAULT_DATE = "01/01/5555";

    public static final String COMMON_EXPORT_DATE_TIME_FORMAT = "yyyyMMddHHmmss";

    public static final Integer YES = 1;
    public static final Integer NO = 0;

    public static byte[] DOCX_MAGIC_NUMBER = {(byte) 0x50, (byte) 0x4B, (byte) 0x03, (byte) 0x04};
    public static byte[] DOC_MAGIC_NUMBER = {(byte) 0xD0, (byte) 0xCF, (byte) 0x11, (byte) 0xE0, (byte) 0xA1, (byte) 0xB1, (byte) 0x1A, (byte) 0xE1};


    public interface STATUS {
        String DELETED = "Y";
        String NOT_DELETED = "N";
        String ACTIVE = "ACTIVE";
        String INACTIVE = "INACTIVE";
    }

    public interface FLAG_STATUS {
        int ACTIVE = 1;
        int INACTIVE = 0;
    }

    public interface EMP_STATUS {
        String OUT = "3";
    }

    public interface COMMON {
        String CLIENT_MESSAGE_ID = "Client-Message-Id";
        String START_TIME = "request_startTime";
        String SERVICE_HEADER = "serviceHeader";
        String SERVICE_MESSAGE_ID = "serviceMessageId";
        int DEFAULT_PAGE_SIZE = 10;
        String YES = "Y";
        String NO = "N";
    }

    public enum WorkdayTime {
        ALL("ALL", "Cả ngày","1", 1D),
        AM("AM", "Sáng","2", 0.5D),
        PM("PM", "Chiều","3", 0.5D),
        WEEKEND("WEEKEND", "Cuối tuần","4", 0D),
        HOLIDAY("HOLIDAY","Nghỉ lễ cả ngày","5",0D),
        NB("NB","Nghỉ bù ngày lễ cả ngày","6",0D);
        private final String code;
        private final String name;
        private final String value;
        private final Double manDay;

        WorkdayTime(String code, String name , String value, Double manDay) {
            this.code = code;
            this.name = name;
            this.value = value;
            this.manDay = manDay;
        }

        public Double getManDay() {
            return this.manDay;
        }

        public String getCode() {
            return this.code;
        }

        public String getName() {
            return this.name;
        }
        public String getValue(){return this.value;}
    }

    public enum WorkTime {
        ALL("ALL", "Cả ngày","1"),
        AM("AM", "Sáng" , "2"),
        PM("PM", "Chiều", "3"),
        WEEKEND("WEEKEND", "Nghỉ cuối tuần","4"),
        HOLIDAY("HOLIDAY","Nghỉ lễ cả ngày","5"),
        NB("NB","Nghỉ bù ngày lễ cả ngày","6");
        private final String code;
        private final String name;
        private final String value;

        WorkTime(String code, String name, String value) {
            this.code = code;
            this.name = name;
            this.value = value;
        }
        public String getCode() {
            return this.code;
        }

        public String getValue(){
            return this.value;
        }
        public String getName() {
            return this.name;
        }
    }

    public interface RESPONSE_STATUS {
        int SUCCESS = 1;
        int ERROR = 0;
        int IMPORT_ERROR = 5;
    }
    public interface IS_IMPORT {
        String YES = "Y";
        String NO = "N";
    }

    public static final class CATEGORY_CODES {
        public static final String GIOI_TINH = "GIOI_TINH";
        public static final String DAN_TOC = "DAN_TOC";
        public static final String TON_GIAO = "TON_GIAO";
        public static final String LOAI_TAI_KHOAN = "LOAI_TAI_KHOAN";
        public static final String NGAN_HANG = "NGAN_HANG";
        public static final String TINH_TRANG_HON_NHAN = "TINH_TRANG_HON_NHAN";
        public static final String QUOC_GIA = "QUOC_GIA";

        public static final String HR_TRANG_THAI_NHAN_VIEN = "HR_TRANG_THAI_NHAN_VIEN";
        public static final String LOAI_GIAY_TO = "LOAI_GIAY_TO";
        public static final String DOI_TUONG_CHINH_SACH = "DOI_TUONG_CHINH_SACH";
        public static final String TINH_TRANG_TN = "TINH_TRANG_TN";
        public static final String MOI_QUAN_HE_TN = "MOI_QUAN_HE_TN";
        public static final String HINH_THUC_DAO_TAO = "HINH_THUC_DAO_TAO";
        public static final String CHUYEN_NGANH_DAO_TAO = "CHUYEN_NGANH_DAO_TAO";
        public static final String TRUONG_DAO_TAO = "TRUONG_DAO_TAO";
        public static final String XEP_LOAI_TN = "XEP_LOAI_TN";
        public static final String TRINH_DO_DAO_TAO = "TRINH_DO_DAO_TAO";
        public static final String TRINH_DO_VAN_HOA = "TRINH_DO_VAN_HOA";
        public static final String LOAI_CHUNG_CHI = "LOAI_CHUNG_CHI";
        public static final String TEN_CHUNG_CHI = "TEN_CHUNG_CHI";
        public static final String NGACH_LUONG = "NGACH_LUONG";
        public static final String BAC_LUONG = "BAC_LUONG";
        public static final String HINH_THUC_KHEN_THUONG = "HINH_THUC_KHEN_THUONG";
        public static final String HINH_THUC_KY_LUAT = "HINH_THUC_KY_LUAT";
        public static final String LOAI_PHU_CAP = "LOAI_PHU_CAP";
        public static final String LOAI_CHUC_DANH = "LOAI_CHUC_DANH";
        public static final String PHAN_LOAI_HOP_DONG = "PHAN_LOAI_HOP_DONG";
        public static final String DIEN_DOI_TUONG = "DIEN_DOI_TUONG";
        public static final String LICH_LAM_VIEC = "LICH_LAM_VIEC";
        public static final String PHI_DON_HANG = "PHI_DON_HANG";


    }
    public static final String APP_NAME = "HrmService";
    public static final String SYSTEM_JOB = "SYSTEM_JOB";
}
