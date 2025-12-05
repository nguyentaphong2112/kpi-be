package vn.hbtplus.constants;

public class Constant {
    public interface ROLE_CODES {
        String ADMIN_FEEDBACK = "ADMIN_FEEDBACK";
    }

    public interface RESOURCES {
        String USER = "SYS_USER";
        String CONFIG_PAGE = "SYS_CONFIG_PAGE";
        String CONFIG_PARAMETER = "SYS_CONFIG_PARAMETER";
        String WARNING_CONFIG = "SYS_WARNING_CONFIGS";
        String DYNAMIC_REPORTS = "SYS_DYNAMIC_REPORT";
        String RESOURCE = "SYS_RESOURCES";
        String CATEGORY = "SYS_CATEGORY";
        String CATEGORY_TYPE = "SYS_CATEGORY_TYPE";
        String ROLE = "SYS_ROLES";
        String USER_ROLE = "SYS_USER_ROLE";
        String CONFIG_OBJECT_ATTRIBUTE = "SYS_CONFIG_ATTRIBUTE";
        String USER_BOOKMARKS = "SYS_USER_BOOKMARKS";
        String SYS_CARD_TEMPLATES = "SYS_CARD_TEMPLATES";
        String SYS_CONFIG_CHART = "SYS_CONFIG_CHART";
        String SYS_MAPPING_VALUES = "SYS_MAPPING_VALUES";
    }

    public interface CATEGORY_TYPE {
        String TINH = "TINH";
        String HUYEN = "HUYEN";
        String XA = "XA";
        String LOAI_BIEU_MAU = "LOAI_BIEU_MAU";
        String SYS_LOAI_BIEU_DO = "SYS_LOAI_BIEU_DO";
    }
    public interface ATTRIBUTE_CODES {
        String MA_TINH = "MA_TINH";
        String MA_HUYEN = "MA_HUYEN";
    }

    public interface ATTACHMENT {
        interface FILE_TYPES {
            String DYNAMIC_REPORT_FILE_TEMPLATE = "dynamicReportFileTemplate";
            String CARD_TEMPLATES = "sysCardTemplates";
            String SYS_FEEDBACKS = "sysFeedbacks";
        }

        interface TABLE_NAMES {
            String DYNAMIC_REPORTS = "sys_dynamic_reports";
            String CARD_TEMPLATES = "sys_card_templates";
            String CONFIG_PAGES = "sys_config_pages";
            String CONFIG_CHARTS = "sys_config_charts";
            String SYS_FEEDBACKS = "sys_feedbacks";
        }

        String MODULE = "admin";
    }

    public interface DYNAMIC_REPORT_TYPE {
        String DOC = "DOC";
        String EXCEL = "EXCEL";
    }

    public interface DYNAMIC_REPORT_DATA_TYPE {
        String LONG = "LONG";
        String DOUBLE = "DOUBLE";
        String STRING = "STRING";
        String LIST = "LIST";
        String MULTI_LIST = "MULTI_LIST";
        String EMP = "EMP";
        String ORG = "ORG";
        String MULTI_ORG = "MULTI_ORG";
        String DATE = "DATE";
    }

    public interface CONFIG_MAPPING_DATA_TYPE {
        String INT = "int";
        String DOUBLE = "double";
        String STRING = "string";
        String DATE = "date";
    }
}
