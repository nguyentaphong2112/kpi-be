package vn.hbtplus.constants;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Constant {
    public interface RESOURCES {
        String EMPLOYEE = "HR_EMPLOYEES";
        String WORK_PROCESS = "HR_WORK_PROCESS";
        String PERSONAL_WORK_PROCESS = "PERSONAL_WORK_PROCESS";
        String WORKED_HISTORIES = "HR_WORKED_HISTORIES";
        String PERSONAL_WORKED_HISTORIES = "PERSONAL_WORKED_HISTORIES";
        String SALARY_RANKS = "HR_SALARY_RANKS";
        String POSITION_SALARY_PROCESS = "HR_POSITION_SALARY_PROCESS";
        String PERSONAL_POSITION_SALARY_PROCESS = "PERSONAL_POSITION_SALARY_PROCESS";
        String PERSONAL_POLITICAL_PARTICIPATION = "PERSONAL_POLITICAL_PARTICIPATION";
        String PLANNING_ASSIGNMENTS = "HR_PLANNING_ASSIGNMENTS";
        String PERSONAL_PLANNING_ASSIGNMENTS = "PERSONAL_PLANNING_ASSIGNMENTS";
        String PERSONAL_IDENTITIES = "HR_PERSONAL_IDENTITIES";
        String PERSONAL_PERSONAL_IDENTITIES = "PERSONAL_PERSONAL_IDENTITIES";
        String JOB = "HR_JOBS";
        String INSURANCE_SALARY_PROCESS = "HR_INSURANCE_SALARY_PROCESS";
        String PERSONAL_INSURANCE_SALARY_PROCESS = "PERSONAL_INSURANCE_SALARY_PROCESS";
        String FAMILY_RELATIONSHIPS = "HR_FAMILY_RELATIONSHIPS";
        String PERSONAL_FAMILY_RELATIONSHIPS = "PERSONAL_FAMILY_RELATIONSHIPS";
        String EVALUATION_RESULTS = "HR_EVALUATION_RESULTS";
        String PERSONAL_EVALUATION_RESULTS = "PERSONAL_EVALUATION_RESULTS";
        String EMP_TYPES = "HR_EMP_TYPES";
        String HR_POLITICAL_PARTICIPATIONS = "HR_POLITICAL_PARTICIPATIONS";
        String PERSONAL_EDUCATION_PROMOTIONS = "PERSONAL_EDUCATION_PROMOTIONS";
        String EDUCATION_PROMOTIONS = "HR_EDUCATION_PROMOTIONS";
        String EDUCATION_PROCESS = "HR_EDUCATION_PROCESS";
        String PERSONAL_EDUCATION_PROCESS = "PERSONAL_EDUCATION_PROCESS";
        String EDUCATION_DEGREES = "HR_EDUCATION_DEGREES";
        String PERSONAL_EDUCATION_DEGREES = "PERSONAL_EDUCATION_DEGREES";
        String EDUCATION_CERTIFICATES = "HR_EDUCATION_CERTIFICATES";
        String PERSONAL_EDUCATION_CERTIFICATES = "PERSONAL_EDUCATION_CERTIFICATES";
        String DOCUMENT_TYPES = "HR_DOCUMENT_TYPES";
        String DISCIPLINE_PROCESS = "HR_DISCIPLINE_PROCESS";
        String PERSONAL_DISCIPLINE_PROCESS = "PERSONAL_DISCIPLINE_PROCESS";
        String CONTRACT_TYPE = "HR_CONTRACT_TYPES";
        String CONTRACT_PROCESS = "HR_CONTRACT_PROCESS";
        String PERSONAL_CONTRACT_PROCESS = "PERSONAL_CONTRACT_PROCESS";
        String CONCURRENT_PROCESS = "HR_CONCURRENT_PROCESS";
        String PERSONAL_CONCURRENT_PROCESS = "PERSONAL_CONCURRENT_PROCESS";
        String BANK_ACCOUNTS = "HR_BANK_ACCOUNTS";
        String PERSONAL_BANK_ACCOUNTS = "PERSONAL_BANK_ACCOUNTS";
        String AWARD_PROCESS = "HR_AWARD_PROCESS";
        String PERSONAL_AWARD_PROCESS = "PERSONAL_AWARD_PROCESS";
        String ALLOWANCE_PROCESS = "HR_ALLOWANCE_PROCESS";
        String PERSONAL_ALLOWANCE_PROCESS = "PERSONAL_ALLOWANCE_PROCESS";
        String PERSONAL_INFO = "PERSONAL_INFO";
        String POLITICAL_INFO = "HR_POLITICAL_INFO";
        String PERSONAL_POLITICAL_INFO = "PERSONAL_POLITICAL_INFO";
        String POSITION_GROUP = "HR_POSITION_GROUP";
        String ORGANIZATION = "HR_ORGANIZATIONS";
        String EMPLOYEE_DIRECTORY = "HR_EMPLOYEE_DIRECTORY";
        String LOG_TASK = "LOG_TASK";
    }

    public static final String COMMON_DATE_FORMAT = "dd/MM/yyyy";
    public static final String LOCALE_VN = "vi_VN";
    public static final String TIMEZONE_VN = "Asia/Ho_Chi_Minh";
    public static final String COMMON_DATE_TIME_FORMAT = "dd/MM/yyyy HH:mm:ss";
    public static final Integer SIZE_PARTITION = 999;
    public static final Integer YES = 1;
    public static final Integer NO = 0;

    public interface ADDRESS_TYPE {
        String THUONG_TRU = "THUONG_TRU";
        String HIEN_TAI = "HIEN_TAI";
    }

    public interface CATEGORY_CODES {
        String HR_KY_NANG_LUONG = "HR_KY_NANG_LUONG";
        String HR_LOAI_HINH_DON_VI = "HR_LOAI_HINH_DON_VI";
        String NOI_CAP_CCCD = "NOI_CAP_CCCD";
        String NOI_CAP_CMT = "NOI_CAP_CMT";
        String HR_LOAI_XET_NANG_LUONG = "HR_LOAI_XET_NANG_LUONG";
        String HOC_HAM = "HR_HOC_HAM";
        String TINH = "TINH";
        String HUYEN = "HUYEN";
        String XA = "XA";
        String GIOI_TINH = "GIOI_TINH";
        String PHAN_NHOM_VI_TRI = "PHAN_NHOM_VI_TRI";
        String HANG_CHUC_DANH = "HANG_CHUC_DANH";
        String DAN_TOC = "DAN_TOC";
        String TON_GIAO = "TON_GIAO";
        String LOAI_TAI_KHOAN = "LOAI_TAI_KHOAN";
        String TINH_TRANG_HON_NHAN = "TINH_TRANG_HON_NHAN";
        String QUOC_GIA = "QUOC_GIA";
        String LOAI_TO_CHUC_CTR_XH = "LOAI_TO_CHUC_CTR_XH";
        String CHUC_DANH_CTR_XH = "CHUC_DANH_CTR_XH";
        String HR_TRANG_THAI_NHAN_VIEN = "HR_TRANG_THAI_NHAN_VIEN";
        String LOAI_GIAY_TO = "LOAI_GIAY_TO";
        String DOI_TUONG_CHINH_SACH = "DOI_TUONG_CHINH_SACH";
        String TINH_TRANG_TN = "TINH_TRANG_TN";
        String MOI_QUAN_HE_TN = "MOI_QUAN_HE_TN";
        String HINH_THUC_DAO_TAO = "HINH_THUC_DAO_TAO";
        String TRINH_DO_VAN_HOA = "TRINH_DO_VAN_HOA";
        String CHUYEN_NGANH_DAO_TAO = "CHUYEN_NGANH_DAO_TAO";
        String TRUONG_DAO_TAO = "TRUONG_DAO_TAO";
        String XEP_LOAI_TN = "XEP_LOAI_TN";
        String TRINH_DO_DAO_TAO = "TRINH_DO_DAO_TAO";
        String LOAI_CHUNG_CHI = "LOAI_CHUNG_CHI";
        String TEN_CHUNG_CHI = "TEN_CHUNG_CHI";
        String HINH_THUC_KHEN_THUONG = "HINH_THUC_KHEN_THUONG";
        String HINH_THUC_KY_LUAT = "HINH_THUC_KY_LUAT";
        String LOAI_PHU_CAP = "LOAI_PHU_CAP";
        String LOAI_CHUC_DANH = "LOAI_CHUC_DANH";
        String HR_GIAI_DOAN_QUY_HOACH = "HR_GIAI_DOAN_QUY_HOACH";
        String HR_CHUC_VU_QUY_HOACH = "HR_CHUC_VU_QUY_HOACH";
        String HR_LY_DO_RA_QUY_HOACH = "HR_LY_DO_RA_QUY_HOACH";
        String DIEN_DOI_TUONG = "DIEN_DOI_TUONG";
        String LOAI_NGACH_LUONG = "LOAI_NGACH_LUONG";

        String NGAN_HANG = "NGAN_HANG";
        String PHAN_LOAI_LUONG_TRUONG = "PHAN_LOAI_LUONG_TRUONG";
        String KPI_LOAI_DANH_GIA = "KPI_LOAI_DANH_GIA";
        String HRM_DOT_KHAM_SUC_KHOE = "HRM_DOT_KHAM_SUC_KHOE";
        String HRM_XEP_LOAI_SUC_KHOE = "HRM_XEP_LOAI_SUC_KHOE";
        String HRM_BENH_GAP_PHAI = "HRM_BENH_GAP_PHAI";
        String HRM_LOG_TASK = "HRM_LOG_TASK";
    }

    public interface OBJECT_ATTRIBUTES {
        interface TABLE_NAMES {
            String EMPLOYEE = "hr_employees";
            String ORGANIZATION = "hr_organizations";
        }

        interface FUNCTION_CODES {
            String THONG_TIN_CO_BAN = "THONG_TIN_CO_BAN";
            String CTRI_XAHOI = "CTRI_XAHOI";
            String SONG_NUOC_NGOAI = "SONG_NUOC_NGOAI";
            String DOI_TUONG = "DOI_TUONG";
        }
    }

    public interface ATTACHMENT {
        interface FILE_TYPES {
            String EMPLOYEE_AVATAR = "employeeAvatar";
            String WORK_PROCESS_EMP = "workProcessEmp";
            String CONCURRENT_PROCESS_EMP = "concurrentProcessEmp";
            String CONTRACT_PROCESS_EMP = "contractProcessEmp";
            String EDUCATION_DEGREES_EMP = "educationDegreesEmp";
            String PLANNING_ASSIGNMENTS_EMP = "planningAssignmentsEmp";
            String EDUCATION_CERTIFICATES_EMP = "educationCertificatesEmp";
            String INSURANCE_SALARY_PROCESS_EMP = "insuranceSalaryProcessEmp";
            String SALARY_REVIEW_DOCUMENT_SIGNED = "salary-review-signed";
            String POSIITION_SALARY_PROCESS_EMP = "positionSalaryProcessEmp";
        }

        interface TABLE_NAMES {
            String EMPLOYEE = "hr_employees";
            String HR_WORK_PROCESS = "hr_work_process";
            String HR_CONCURRENT_PROCESS = "hr_concurrent_process";
            String HR_CONTRACT_PROCESS = "hr_contract_process";
            String HR_EDUCATION_DEGREES = "hr_education_degrees";
            String HR_PLANNING_ASSIGNMENTS = "hr_planning_assignments";
            String HR_EDUCATION_CERTIFICATES = "hr_education_certificates";
            String HR_INSURANCE_SALARY_PROCESS = "hr_insurance_salary_process";
            String HR_POSIITION_SALARY_PROCESS = "hr_position_salary_process";
            String HR_SALARY_REVIEWS = "hr_salary_reviews";
            String HR_HEALTH_RECORDS = "hr_health_records";
        }

        String MODULE = "hrm";
        String ADMIN_MODULE = "admin";
    }

    public static final class CLASSIFY_CONTRACT {
        public static final String HOP_DONG = "HOP_DONG";
        public static final String PHU_LUC_HOP_DONG = "PHU_LUC";
    }

    public static final class JOB_TYPE {
        public static final String CHUC_VU = "CHUC_VU";
    }

    public interface REPORT_CONFIG_CODES {
        String DANH_SACH_NHAN_VIEN = "HR_DANH_SACH_NHAN_VIEN";
        String DANH_SACH_QT_LUONG_NHA_NUOC = "HR_DANH_SACH_QUA_TRINH_LUONG_NHA_NUOC";
        String DANH_SACH_QT_LUONG_CHUC_DANH = "HR_DANH_SACH_QT_LUONG_CHUC_DANH";
        String DANH_SACH_QT_HOP_DONG = "HR_DANH_SACH_QT_HOP_DONG";
        String DANH_SACH_QT_KHEN_THUONG = "HR_DANH_SACH_QT_KHEN_THUONG";
        String EXPORT_DANH_SACH_KHAM_CHUA_BENH = "EXPORT_DANH_SACH_KHAM_CHUA_BENH";
    }


    public static final class EMP_STATUS {
        public static final Integer WORK_IN = 1;
        public static final Integer PENDING = 2;
        public static final Integer WORK_OUT = 3;
        public static final Integer RETIRED = 4;
    }

}
