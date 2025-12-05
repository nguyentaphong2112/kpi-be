package vn.hbtplus.constants;

public class Constant {
    public interface RESOURCES {
        String EMPLOYEE = "HR_EMPLOYEES";
        String ABS_CALENDAR = "ABS_CALENDAR";
        String ABS_TIMEKEEPING = "ABS_TIMEKEEPING";
        String ABS_OVERTIME_TIMEKEEPING = "ABS_OVERTIME_TIMEKEEPING";
        String ABS_DUTY_SCHEDULES = "ABS_DUTY_SCHEDULES";
        String ABS_DUTY_SCHEDULE_MONTH = "ABS_DUTY_SCHEDULE_MONTH";
        String ABS_REASON_TYPES = "ABS_REASON_TYPES";
        String ABS_WORKDAY_TYPES = "ABS_WORKDAY_TYPES";
        String ABS_REQUEST_MANAGER = "ABS_REQUEST_MANAGER";
        String ABS_ANNUAL_LEAVES = "ABS_ANNUAL_LEAVES";
        String ABS_OVERTIME_RECORDS = "ABS_OVERTIME_RECORDS";
        String ABS_TIMEKEEPING_APPROVAL = "ABS_TIMEKEEPING_APPROVAL";
        String ABS_ATTENDANCE_HISTORIES = "ABS_ATTENDANCE_HISTORIES";
    }

    public static final String COMMON_DATE_FORMAT = "dd/MM/yyyy";
    public static final String LOCALE_VN = "vi_VN";
    public static final String TIMEZONE_VN = "Asia/Ho_Chi_Minh";
    public static final String COMMON_DATE_TIME_FORMAT = "dd/MM/yyyy HH:mm:ss";
    public static final Integer SIZE_PARTITION = 999;
    public static final Integer YES = 1;
    public static final Integer NO = 0;


    public interface CATEGORY_CODES {


        String VI_TRI_TRUC = "ABS_VI_TRI_TRUC";
        String PHAN_LOAI_CONG = "ABS_PHAN_LOAI_CONG";
        String PHU_LUC_TAM_HOAN = "PLTH";
        String ABS_LOAI_DK_LAM_THEM = "ABS_LOAI_DK_LAM_THEM";
    }

    public interface OBJECT_ATTRIBUTES {
        interface TABLE_NAMES {
        }

        interface FUNCTION_CODES {
        }
    }

    public interface ATTACHMENT {
        interface FILE_TYPES {
            String REQUEST_CONTENT = "requestContent";
        }

        interface TABLE_NAMES {
            String REQUEST = "abs_request";
        }

        String MODULE = "abs";
    }

    public static final class CLASSIFY_CONTRACT {
        public static final String HOP_DONG = "HOP_DONG";
        public static final String PHU_LUC_HOP_DONG = "PHU_LUC";
    }

    public enum ABS_REQUEST_STATUS {
        DU_THAO,
        CHO_PHE_DUYET,
        DA_PHE_DUYET,
        DA_HUY,
        DA_TU_CHOI
    }

    public static final class EMP_STATUS {
        public static final Integer WORK_IN = 1;
        public static final Integer PENDING = 2;
        public static final Integer WORK_OUT = 3;
    }

    public static final class TIME_OFF_TYPE {
        public static final Long DAYS = 1L;
        public static final Long MONTHS = 2L;
    }

    public enum WorkdayType {
        NL("NL", "Nghỉ lễ", false),
        NB("NB", "Nghỉ bù", false),
        NCT("NCT", "Nghỉ cuối tuần", false),
        LV("ALL", "Ngày làm việc", true);
        private final String code;
        private final String name;
        private final boolean isWorkingDay;

        WorkdayType(String code, String name, boolean isWorkingDay) {
            this.code = code;
            this.name = name;
            this.isWorkingDay = isWorkingDay;
        }

        public boolean isWorkingDay() {
            return this.isWorkingDay;
        }

        public String getCode() {
            return this.code;
        }

        public String getName() {
            return this.name;
        }
    }

    public enum WorkdayTime {
        ALL("ALL", "Cả ngày", 1D),
        AM("AM", "Sáng", 0.5D),
        PM("PM", "Chiều", 0.5D);
        private final String code;
        private final String name;
        private final Double manDay;

        WorkdayTime(String code, String name, Double manDay) {
            this.code = code;
            this.name = name;
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
    }


    public static final class REQUEST_STATUS {
        public static final String DRAFT = "DU_THAO";//Dự thảo
        public static final String WAIT_APPROVE = "CHO_PHE_DUYET";//Chờ phê duyệt
        public static final String NOT_APPROVED = "CHUA_PHE_DUYET";//Chua phê duyệt
        public static final String APPROVED = "DA_PHE_DUYET";//Đã phê duyệt
        public static final String REJECT = "DA_TU_CHOI";//Từ chối phê duyệt
        public static final String CANCEL = "DA_HUY";//Đã Hủy
    }

    public static final class REQUEST_APPROVE_STATUS {
        public static final String NOT_APPROVED = "CHUA_PHE_DUYET";//Chua phê duyệt
        public static final String APPROVED = "DA_PHE_DUYET";//Đã phê duyệt
        public static final String REJECT = "TU_CHOI";//Từ chối phê duyệt
    }

    public static final class ATTENDANCE_HISTORY_STATUS {
        public static final String CHO_PHE_DUYET = "CHO_PHE_DUYET";//Chua phê duyệt
        public static final String PHE_DUYET = "PHE_DUYET";//Đã phê duyệt
        public static final String TU_CHOI = "TU_CHOI";//Từ chối phê duyệt
    }

    public interface CATEGORY_TYPES {
        String ABS_VI_TRI_TRUC = "ABS_VI_TRI_TRUC";

    }

}
