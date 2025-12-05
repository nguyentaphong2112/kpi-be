package vn.hbtplus.tax.personal.constants;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Constant {
    public interface RESOURCES {
    }

    public static final String COMMON_DATE_FORMAT = "dd/MM/yyyy";
    public static final String LOCALE_VN = "vi_VN";
    public static final String TIMEZONE_VN = "Asia/Ho_Chi_Minh";
    public static final String COMMON_DATE_TIME_FORMAT = "dd/MM/yyyy HH:mm:ss";
    public static final Integer SIZE_PARTITION = 999;
    public static final String DECLARATION_REGISTER = "DECLARATION_REGISTER";
    public static final String YES_STR = "YES_STR";
    public static final String REQ_EMP_MAPPING_PREFIX = "";
    public static final String REQ_ADMIN_MAPPING_PREFIX = "";


    public static final class RESOURCE {
        public static final String TAX_NUMBER_REGISTERS = "TAX_NUMBER_REGISTERS";
        public static final String SYS_CATEGORY = "SYS_CATEGORY";
    }



    public interface CATEGORY_CODES {
        public static final String DOI_TUONG_CV = "DOI_TUONG_CV";
        public static final String TINH = "TINH";
        public static final String HUYEN = "HUYEN";
        public static final String XA = "XA";
        public static final String MOI_QUAN_HE_TN = "MOI_QUAN_HE_TN";
        public static final String QUOC_GIA = "QUOC_GIA";
        public static final String LOAI_GIAY_TO = "LOAI_GIAY_TO";
        public static final String GIOI_TINH = "GIOI_TINH";
        public static final String TINH_TRANG_NT = "TINH_TRANG_NT";
        public static final String DOITUONG_CHINHSACH = "DOITUONG_CHINHSACH";



    }

    public interface OBJECT_ATTRIBUTES {
        interface TABLE_NAMES {
            String PTX_DEPENDENT_REGISTERS = "PTX_DEPENDENT_REGISTERS";
        }

        interface FUNCTION_CODES {
            String CONFIRM_PROVIDE  = "CONFIRM_PROVIDE";
            String DECLARATION_REGISTERS = "DECLARATION_REGISTERS";
            String DEPENDENT_REGISTERS = "DEPENDENT_REGISTERS";
            String REPORT_DEPENDENT = "REPORT_DEPENDENT";
            String TAX_SEARCH = "TAX_SEARCH";
            String RECEIVE_INVOICE  = "RECEIVE_INVOICE";
            String TAX_REGISTER = "TAX_REGISTER";
            String TAX_CHANGE = "TAX_CHANGE";

        }
    }

    public interface ATTACHMENT {
        interface FILE_TYPES {

        }

        interface TABLE_NAMES {
            String PTX_DEPENDENT_REGISTERS = "PTX_DEPENDENT_REGISTERS";
            String PTX_TAX_NUMBER_REGISTERS = "PTX_TAX_NUMBER_REGISTERS";
        }

        String MODULE = "hrm";
    }

    public interface METHOD_CODE {
        String AUTHORITY = "AUTHORITY";
        String SELF_SETTLEMENT = "SELF_SETTLEMENT";
    }

    public interface TAX_STATUS {
        Integer ACCOUNTANT_RECEIVED = 0;
        Integer ACCOUNTANT_PROCESSING = 1;
        Integer DRAFT = 2;
        Integer TAX_APPROVAL = 3;
        Integer CONFIRMED = 4;
        Integer WAITING_APPROVAL = 5;
        Integer TAX_REJECT = 6;
        Integer REGISTERED = 7;
        Integer ACCOUNTANT_REJECT = 8;

    }

    public interface EMP_STATUS {
        String WORK_IN = "WORK_IN";
    }

    public interface EMP_TYPE {
        String EMP = "EMP";
    }


    public interface REG_TYPE {
        String DEPENDENT_CREATE = "DEPENDENT_CREATE";
        String TAX_CREATE = "TAX_CREATE";
        String DEPENDENT_CANCEL = "DEPENDENT_CANCEL";
        String TAX_CHANGE = "TAX_CHANGE";
    }

//    public interface REG_TYPE_NUMBER {
//        String TAX_CREATE = "TAX_CREATE";
//        String DEPENDENT_CREATE = "DEPENDENT_CREATE";
//    }

    public static final class REG_TYPE_NUMBER {

        public static final Integer TAX_CREATE = 1;
        //        public static final Integer TAX_CHANGE = 2;
        public static final Integer DEPENDENT_CREATE = 3;
//        public static final Integer DEPENDENT_CANCEL = 4;
//        public static final Integer DECLARATION_DECLARE = 5;
//        public static final Integer DECLARATION_REV_INVOICE = 6;
//        public static final Integer CONFIRM_REG = 7;
    }

    public interface INVOICE_STATUS {
        Integer PROCESSING = 0;
        Integer ACCOUNTANT_RECEIVED = 1;
    }

    public interface LOG_OBJECT_TYPE {
        String CONFIRM = "CONFIRM";
        String INVOICE = "INVOICE";
        String DECLARATION = "DECLARATION";
        String DEPENDENT = "DEPENDENT";
        String TAX_NUMBER  = "TAX_NUMBER";
    }

    public interface GROUP_SEND_MAIL {
        String CONFIRM_DECLARATION = "CONFIRM_DECLARATION";
        String RESULT_DEPENDENT_PERSON = "RESULT_DEPENDENT_PERSON";
        String REMIND_DECLARE_REGISTER = "REMIND_DECLARE_REGISTER";
        String REMIND_TAX_REGISTER = "REMIND_TAX_REGISTER";
        String RESULT_NEW_TAX_NUMBER = "RESULT_NEW_TAX_NUMBER";
        String RESULT_CHANGE_TAX_NUMBER = "RESULT_CHANGE_TAX_NUMBER";
    }

    public interface DOCUMENTS {
        String DEPENDENT = "DEPENDENT";
        String TAX_NUMBER = "TAX_NUMBER";
    }

    public interface TAX_STATUS_STR {
        String TAX_APPROVAL = "TAX_APPROVAL";
    }

    public interface CATEGORY_TYPE {
        String TINH = "TINH";
        String HUYEN = "HUYEN";
        String XA = "XA";
        String THUONG_TRU = "THUONG_TRU";
        String HIEN_TAI = "HIEN_TAI";
        String LOAI_BIEU_MAU = "LOAI_BIEU_MAU";
        String SYS_LOAI_BIEU_DO = "SYS_LOAI_BIEU_DO";
    }
}
