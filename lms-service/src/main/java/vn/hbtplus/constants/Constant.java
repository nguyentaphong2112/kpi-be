package vn.hbtplus.constants;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Constant {
    public interface RESOURCES {
        String INTERNSHIP_SESSION = "INTERNSHIP_SESSION";
        String EXTERNAL_TRAINING = "EXTERNAL_TRAINING";
        String TRAINING_PROCESS = "TRAINING_PROCESS";
        String MENTORING_TRAINEES = "MENTORING_TRAINEES";
        String MENTORING_TRAINERS = "MENTORING_TRAINERS";
        String RESEARCH = "RESEARCH";
    }

    public static final String COMMON_DATE_FORMAT = "dd/MM/yyyy";
    public static final String LOCALE_VN = "vi_VN";
    public static final String TIMEZONE_VN = "Asia/Ho_Chi_Minh";
    public static final String COMMON_DATE_TIME_FORMAT = "dd/MM/yyyy HH:mm:ss";
    public static final Integer SIZE_PARTITION = 999;


    public interface CATEGORY_CODES {
        String LMS_INTERN_TRUONG_DAO_TAO = "LMS_INTERN_TRUONG_DAO_TAO";
        String LMS_INTERN_CHUYEN_NGANH = "LMS_INTERN_CHUYEN_NGANH";
        String LMS_NOI_DUNG_DAO_TAO = "LMS_NOI_DUNG_DAO_TAO";
        String LMS_NOI_DAO_TAO = "LMS_NOI_DAO_TAO";
        String LMS_KE_HOACH_DAO_TAO = "LMS_KE_HOACH_DAO_TAO";
        String LMS_KHOA_DAO_TAO = "LMS_KHOA_DAO_TAO";
        String LMS_DOI_TUONG_NGOAI = "LMS_DOI_TUONG_NGOAI";
        String GIOI_TINH = "GIOI_TINH";
        String LMS_HINH_THUC_DAO_TAO = "LMS_HINH_THUC_DAO_TAO";
        String LMS_CHUYEN_NGANH_NGOAI_VIEN = "LMS_CHUYEN_NGANH_NGOAI_VIEN";
        String LMS_TINH_TRANG_HOC_PHI = "LMS_TINH_TRANG_HOC_PHI";
        String LMS_NGUON_KINH_PHI = "LMS_NGUON_KINH_PHI";
        String CDT_CHUONG_TRINH_DAO_TAO = "CDT_CHUONG_TRINH_DAO_TAO";
        String CDT_DIA_DIEM_DAO_TAO = "CDT_DIA_DIEM_DAO_TAO";
        String CDT_VAI_TRO = "CDT_VAI_TRO";
        String CDT_CHUYEN_MON = "CDT_CHUYEN_MON";
        String CDT_BENH_VIEN = "CDT_BENH_VIEN";
        String NCKH_VAI_TRO = "NCKH_VAI_TRO";
        String NCKH_TRANG_THAI = "NCKH_TRANG_THAI";
        String NCKH_HE_NGHIEN_CUU = "NCKH_HE_NGHIEN_CUU";
        String NCKH_CAP_NGHIEN_CUU = "NCKH_CAP_NGHIEN_CUU";
        String NCKH_PHAN_LOAI_DE_TAI = "NCKH_PHAN_LOAI_DE_TAI";
    }

    public interface OBJECT_ATTRIBUTES {
        interface TABLE_NAMES {
            String LMS_INTERNSHIP_SESSION = "lms_internship_sessions";
        }

        interface FUNCTION_CODES {

        }
    }

    public interface ATTACHMENT {
        interface FILE_TYPES {
            String LMS_INTERNSHIP_SESSION = "lms_internship_sessions";
            String LMS_TRAINING_PROCESS= "lms_training_process";
            String MENTORING_TRAINEES_EMP = "mentoringTraineesEmp";
            String RESEARCH_PROJECT = "research_projects";
        }

        interface TABLE_NAMES {
            String LMS_INTERNSHIP_SESSION = "lms_internship_sessions";
            String LMS_TRAINING_PROCESS= "lms_training_process";
            String LMS_EXTERNAL_TRAININGS= "lms_external_trainings";
            String MENTORING_TRAINEES = "lms_mentoring_trainees";
            String RESEARCH_PROJECT = "lms_research_projects";
        }

        String MODULE = "lms";
    }

}
