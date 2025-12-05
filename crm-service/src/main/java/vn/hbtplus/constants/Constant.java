package vn.hbtplus.constants;

import vn.hbtplus.utils.I18n;

public class Constant {
    public static String DEFAULT_PASSWORD = "123456a@";

    public interface RESOURCES {
        String EMPLOYEE = "CRM_EMPLOYEES";
        String ORDER = "CRM_ORDERS";
        String CRM_PYTAGO_RESEARCHS = "CRM_PYTAGO_RESEARCHS";
        String CRM_PARTNERS = "CRM_PARTNERS";
        String CRM_CUSTOMERS = "CRM_CUSTOMERS";
        String CRM_TRAINING_PROGRAMS = "CRM_TRAINING_PROGRAMS";
        String CRM_COURSES = "CRM_COURSES";
        String CRM_PRODUCTS = "CRM_PRODUCTS";
        String CRM_CUSTOMER_CERTIFICATES = "CRM_CUSTOMER_CERTIFICATES";
        String CRM_CUSTOMER_CARE_RECORDS = "CRM_CUSTOMER_CARE_RECORDS";
        String CRM_ORDER_PAYABLE = "CRM_ORDER_PAYABLE";
    }

    public interface CATEGORY_TYPES {
        String TINH = "TINH";
        String HUYEN = "HUYEN";
        String XA = "XA";
        String CRM_LOAI_DOI_TAC = "CRM_LOAI_DOI_TAC";
        String GIOI_TINH = "GIOI_TINH";
        String DON_VI = "CRM_DON_VI";
        String CHUC_VU = "CRM_CHUC_VU";
        String VAI_TRO = "CRM_VAI_TRO";
        String MOI_QUAN_HE_TN = "MOI_QUAN_HE_TN";
        String CRM_CHUNG_CHI = "CRM_CHUNG_CHI";
        String CRM_STATUS_CERTIFICATE = "CRM_STATUS_CERTIFICATE";
        String CRM_ORDER_PAYABLES_STATUS = "CRM_ORDER_PAYABLES_STATUS";
        String TINH_TRANG_TN = "TINH_TRANG_TN";
        String CRM_PHAN_LOAI = "CRM_PHAN_LOAI";
        String CRM_TRANG_THAI_CHAM_SOC = "CRM_TRANG_THAI_CHAM_SOC";
        String CRM_TINH_TRANG = "CRM_TINH_TRANG";
        String CRM_TRANG_THAI_SAN_PHAM = "CRM_TRANG_THAI_SAN_PHAM";
        String CRM_NHOM_SAN_PHAM = "CRM_NHOM_SAN_PHAM";
        String CRM_DVT_SAN_PHAM = "CRM_DVT_SAN_PHAM";
        String CRM_LOAI_THANH_TOAN = "CRM_LOAI_THANH_TOAN";
        String CRM_HO_SO_NHAN_VIEN = "CRM_HO_SO_NHAN_VIEN";
        String CRM_TRANG_THAI_KHACH_HANG = "CRM_TRANG_THAI_KHACH_HANG";
        String CRM_TRANG_THAI_TGIA_HOC = "CRM_TRANG_THAI_TGIA_HOC";
    }

    public interface STATUS {
    }
    public interface ATTACHMENT {
        String MODULE = "crm";
        String ADMIN_MODULE = "admin";
        interface TABLE_NAMES {
            String EMPLOYEE_PROFILE = "crm_employee_profiles";
            String PAYMENT = "crm_payments";
            String CRM_PRODUCTS = "crm_products";
        }
    }

    public interface CUSTOMER_CARE_TYPE {
        String KHACH_HANG = "KHACH_HANG";
        String DOI_TAC = "DOI_TAC";
    }

    public interface CARD_TYPE {
        String SINH_NHAT = "SINH_NHAT";
        String THU_MOI = "THU_MOI";
        String CHUNG_NHAN = "CHUNG_NHAN";
    }

    public static class LOG_ACTION {
        public static LOG_ACTION INSERT = new LOG_ACTION("INSERT", I18n.getMessage("global.insert"));
        public static LOG_ACTION UPDATE = new LOG_ACTION("UPDATE", I18n.getMessage("global.update"));
        public static LOG_ACTION LOCKED = new LOG_ACTION("LOCKED", I18n.getMessage("global.locked"));
        public static LOG_ACTION UNLOCK = new LOG_ACTION("UNLOCK", I18n.getMessage("global.unlock"));
        public static LOG_ACTION DELETE = new LOG_ACTION("DELETE", I18n.getMessage("global.delete"));
        public static LOG_ACTION PRE_SIGNED = new LOG_ACTION("PRE_SIGNED", I18n.getMessage("global.preSigned"));
        public static LOG_ACTION SIGNED = new LOG_ACTION("SIGNED", I18n.getMessage("global.signed"));
        public static LOG_ACTION SEND_NOTIFY = new LOG_ACTION("SEND_NOTIFY", I18n.getMessage("global.signed"));
        public static LOG_ACTION APPROVE = new LOG_ACTION("APPROVE", I18n.getMessage("global.approve"));
        public static LOG_ACTION REJECT = new LOG_ACTION("REJECT", I18n.getMessage("global.reject"));


        private final String action;
        private final String name;

        public LOG_ACTION(String action, String name) {
            this.name = name;
            this.action = action;
        }

        public String getName() {
            return name;
        }

        public String getAction() {
            return action;
        }
    }

    public static class TableObjectAttribute {
        public static String CRM_EMPLOYEES = "crm_employees";
        public static String CRM_CUSTOMERS = "crm_customers";
    }

    public static class Role {
        public static String CRM_ADMIN = "CRM_ADMIN";
    }
}
