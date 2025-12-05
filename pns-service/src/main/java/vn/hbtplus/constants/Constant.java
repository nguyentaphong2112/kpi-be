package vn.hbtplus.constants;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Constant {
    private static final Logger LOGGER = LoggerFactory.getLogger(Constant.class);
    public static final String REQUEST_MAPPING_PREFIX = "";
    public static final String COMMON_DATE_FORMAT = "dd/MM/yyyy";
    public static final String COMMON_DATE_TIME_FORMAT = "dd/MM/yyyy HH:mm:ss";
    public static final String SHORT_DATE_FORMAT = "MM/yyyy";
    public static final String LOCALE_VN = "vi_VN";
    public static final String TIMEZONE_VN = "Asia/Ho_Chi_Minh";
    public static final Integer SIZE_PARTITION = 999;
    public static final Integer YES = 1;
    public static final String SECRET_KEY = "hcm#^&@202207";
    public static final Integer CONTINUE_SIGN = 2;

    public static final String CONTRACT_TYPE_AMOUNT_FEE = "PHD_FEE";
    public static final Long CONTRACT_TYPE_PROBATION = 16L;
    public static final String ROLE_DVNS = "HCM-PNS-XLKT-DVNS";

    public static final class RESOURCE {
        public static final String PNS_CONTRACT_TYPES = "PNS_CONTRACT_TYPES";
        public static final String PNS_CONFIG_APPROVALS = "PNS_CONFIG_APPROVALS";
        public static final String PNS_CONFIG_SEQ = "PNS_CONFIG_SEQ";
        public static final String PNS_CONTRACT_APPROVAL = "PNS_CONTRACT_APPROVAL";
        public static final String PNS_FEE = "CONTRACT-FEE";
        public static final String PNS_CONTRACT_INFO = "PNS_CONTRACT_INFO";
        public static final String PNS_CONTRACT_CONFIG = "PNS_CONTRACT_CONFIG";
        public static final String PNS_LIQUIDATION = "PNS-LIQUIDATIONS";
        public static final String HR_PROFILE = "HR_PROFILE";    //1.1.16 FILE đính
        public static final String PNS_CONTRACT_PROPOSALS = "PNS_CONTRACT_PROPOSALS";
        public static final String PNS_CONTRACT_TEMPLATES = "PNS_CONTRACT_TEMPLATES";
    }

    public interface LOOKUP_CODES {
        String TINH = "TINH";
        String HUYEN = "HUYEN";
        String XA = "XA";
        String DOI_TUONG_CV = "DOI_TUONG_CV";
        String LOAI_HINH_CHI_NHANH = "HR_LOAI_HINH_DON_VI";
        String LY_DO_NGHI = "LY_DO_NGHI";
        String HO_SO_THANH_LY_HD = "HO_SO_THANH_LY_HD";
        String TT_THAM_VAN_THANH_LY_HD = "TT_THAM_VAN_THANH_LY_HD";
        String PNS_CONFIG_APPROVAL_TYPES = "PNS_CONFIG_APPROVAL_TYPES";
    }

    public static final class STATUS {

        private STATUS() {
            LOGGER.info("STATUS init");
        }

        public static final Integer ACTIVE = 1;
        public static final Integer DE_ACTIVE = 0;

    }

    public static final class CONTRACT_TYPE {

        private CONTRACT_TYPE() {
            LOGGER.info("CONTRACT_TYPE init");
        }

        public static final Integer NEW = 1;
        public static final Integer CONTINUE = 2;
        public static final Integer APPENDIX_SALARY = 3;
        public static final Integer CONTRACT_FEE = 4;

    }

    public static final class CONTRACT_STATUS {

        private CONTRACT_STATUS() {
            LOGGER.info("CONTRACT_STATUS init");
        }

        public static final Integer INIT = 1;//Lập danh sách
        public static final Integer MANAGER_EVALUATE = 2; //Quản lý trực tiếp đã đánh giá
        public static final Integer WAITING_APPROVE = 3;// Cấp xét duyệt đã đánh giá
        public static final Integer WAITING_SIGN = 4;// Hoàn thành Đánh giá - Chờ ký
        public static final Integer SIGNED = 5;// Đã ký
        public static final Integer COMPLETE = 6;// Đã duyệt file ký
        public static final Integer REJECT_FILE_SIGNED = 7;// Từ chối duyệt file ký
        public static final Integer LIQUIDATION = 8;// Thanh lý hợp đồng

    }

    public static final class APPROVE_LEVEL {

        private APPROVE_LEVEL() {
            LOGGER.info("APPROVE_LEVEL init");
        }

        public static final Integer MANAGER = 1;
        public static final Integer APPROVER = 2;
        public static final Integer BOSS = 3;

    }

    public static final class CONFIG_APPROVAL_TYPE {

        private CONFIG_APPROVAL_TYPE() {
            LOGGER.info("CONFIG_APPROVAL_TYPE init");
        }

        public static final Integer SIGNER = 1;
        public static final Integer APPROVER = 2;
        public static final Integer CHANGE_APPROVER = 3;

    }

    public static final class CLASSIFY_CODE {

        private CLASSIFY_CODE() {
            LOGGER.info("CLASSIFY_CODE init");
        }

        public static final String HDLD = "HD";
        public static final String PLHD = "PHD";

    }

    public static final class IS_LIQUIDATION {

        private IS_LIQUIDATION() {
            LOGGER.info("IS_LIQUIDATION init");
        }

        public static int KEEP_SIGNING = 1;
        public static int LIQUIDATION = 0;

    }


    public static final class SQL_CONFIG_TYPES {
        public static Integer BANG_TINH_CHE_DO = 5;
        public static Integer TB_CHAM_DUT = 6;
    }


    public static final class CONTRACT_FEE {
        public static final Integer INIT = 0; // du thao
        public static final Integer APPROVED = 1; // phe duyet
        public static final Integer REJECT = 2; //tu choi
    }

    public static final class EMP_TYPE {
        public static final String OS = "3";
        public static final String EMP = "1";
    }

    public static final class GROUP_SEND_MAIL {

        private GROUP_SEND_MAIL() {
            LOGGER.info("GROUP_SEND_MAIL init");
        }

        public static final String PNS_SEND_MAIL_CONTRACT = "PNS_SEND_MAIL_CONTRACT";
        public static final String MANAGER_BEFORE_EXPIRED_CONTRACT_1 = "PNS_MANAGER_BEFORE_EXPIRED_CONTRACT_1";
        public static final String MANAGER_BEFORE_EXPIRED_CONTRACT_2 = "PNS_MANAGER_BEFORE_EXPIRED_CONTRACT_2";
        public static final String MANAGER_AFTER_EXPIRED_CONTRACT_1 = "PNS_MANAGER_AFTER_EXPIRED_CONTRACT_1";
        public static final String MANAGER_AFTER_EXPIRED_CONTRACT_2 = "PNS_MANAGER_AFTER_EXPIRED_CONTRACT_2";
        public static final String EMP_BEFORE_EXPIRED_CONTRACT = "PNS_EMP_BEFORE_EXPIRED_CONTRACT";
        public static final String EMP_APPROVE_FILE_SIGNED = "PNS_EMP_APPROVE_FILE_SIGNED";

        public static final String HR_BEFORE_EXPIRED_CONTRACT_1 = "PNS_HR_BEFORE_EXPIRED_CONTRACT_1";
        public static final String HR_BEFORE_EXPIRED_CONTRACT_2 = "PNS_HR_BEFORE_EXPIRED_CONTRACT_2";
        public static final String HR_AFTER_EXPIRED_CONTRACT_1 = "PNS_HR_AFTER_EXPIRED_CONTRACT_1";
        public static final String HR_AFTER_EXPIRED_CONTRACT_2 = "PNS_HR_AFTER_EXPIRED_CONTRACT_2";

        public static final String EMP_NEW_CONTRACT = "PNS_EMP_NEW_CONTRACT";
        public static final String HR_NEW_CONTRACT = "PNS_HR_NEW_CONTRACT";
        public static final String HR_APPENDIX_CONTRACT = "PNS_HR_APPENDIX_CONTRACT";
        public static final String EMP_APPENDIX_CONTRACT = "PNS_EMP_APPENDIX_CONTRACT";

    }

    public static final class EMP_STATUS {
        public static final Integer WORK_IN = 1;
    }

    public static final String API_BEARER = "Bearer %s";
    public static final String AUTHORIZATION = "Authorization";
    public static final String SERVICE_HEADER = "serviceHeader";
    public static final String CLIENT_MESSAGE_ID = "clientMessageId";
    public static final String TRANSACTION_ID = "transactionId";

    public interface ATTACHMENT {
        interface FILE_TYPES {
            String PNS_CONFIG_APPROVALS = "PNS_CONFIG_APPROVALS";
            String PNS_CONTRACT_PROPOSALS = "PNS_CONTRACT_PROPOSALS";
            String PNS_CONTRACT_EVALUATIONS = "PNS_CONTRACT_EVALUATIONS";
            String PNS_LIQUIDATION_CONSULTS = "PNS_LIQUIDATION_CONSULTS";
            String CONTRACT_SIGNED = "CONTRACT_SIGNED";
        }

        interface TABLE_NAMES {
            String PNS_CONFIG_APPROVALS = "pns_config_approvals";
            String PNS_CONTRACT_PROPOSALS = "pns_contract_proposals";
            String PNS_CONTRACT_EVALUATIONS = "pns_contract_evaluations";
            String PNS_LIQUIDATION_CONSULTS = "pns_liquidation_consults";
        }

        String MODULE = "pns";
    }

}
