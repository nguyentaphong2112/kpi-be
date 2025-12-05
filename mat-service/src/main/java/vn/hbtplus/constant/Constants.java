package vn.hbtplus.constant;

import java.util.HashMap;
import java.util.Map;

public class Constants {

    public static final String SHORT_FORMAT_DATE = "MM/yyyy";

    public static final int BATCH_SIZE = 999;


    public interface CONFIG_PARAMETERS {
        String ROOT_LEGACY_ID = "ID_DON_VI_GOC";
    }


    public static final class RESOURCE {
        public static final String FPN_DOCUMENT_GROUPS = "FPN_DOCUMENT_GROUPS";
        public static final String FPN_BUILDING_DOCUMENTS = "FPN_BUILDING_DOCUMENTS";
        public static final String FPN_BUILDING_EQUIPMENT_DETAILS = "FPN_BUILDING_EQUIPMENT_DETAILS";
        public static final String FPN_BUILDING_EQUIPMENTS = "FPN_BUILDING_EQUIPMENTS";
        public static final String SYS_CATEGORY = "SYS_CATEGORY";
        public static final String MAT_EQUIPMENT_GROUPS = "MAT_EQUIPMENT_GROUPS";
        public static final String FPN_EQUIPMENTS = "FPN_EQUIPMENTS";
        public static final String MAT_EQUIPMENT_TYPES = "MAT_EQUIPMENT_TYPES";
        public static final String MAT_EQUIPMENT_UNITS = "MAT_EQUIPMENT_UNITS";
        public static final String FPN_PLANNING_DETAILS = "FPN_PLANNING_DETAILS";
        public static final String FPN_PLANNINGS = "FPN_PLANNINGS";
        public static final String FPN_STANDARD_EQUIPMENTS = "FPN_STANDARD_EQUIPMENTS";
        public static final String RESOURCE_CATEGORY = "resource.category";
        public static final String MATERIAL_TYPE = "MATERIAL_TYPE";
        public static final String STOCK_REPORT = "STOCK_REPORT";
        public static final String MATERIAL = "MATERIAL";
        public static final String WAREHOUSES = "WAREHOUSES";
        public static final String INCOMING = "INCOMING";
        public static final String INVENTORY = "INVENTORY";
        public static final String OUTGOING = "OUTGOING";
        public static final String TRANSFERRING = "TRANSFERRING";
    }

    public static final class CATEGORY_TYPE {
        public static final String PLANNING_TYPE = "PLANNING_TYPE";
        public static final String BUILDING_EQUIPMENT_STATUS = "BUILDING_EQUIPMENT_STATUS";
        public static final String BUILDING_GROUP = "BUILDING_GROUP";

        public static final String DOCUMENT_GROUP_TYPE = "DOCUMENT_GROUP_TYPE";
        // loai kho
        public static final String WAREHOUSE_TYPE = "WAREHOUSE_TYPE";
        public static final String EQUIPMENT_GROUP = "EQUIPMENT_GROUP";
        public static final String EQUIPMENT_TYPE = "EQUIPMENT_TYPE";
        public static final String EQUIPMENT_UNIT = "EQUIPMENT_UNIT";
        public static final String INCOMING_SHIPMENTS_TYPE = "INCOMING_SHIPMENTS_TYPE";
        public static final String MAT_STATUS = "MAT_STATUS";
        public static final String MAT_OUTGOING_SHIPMENT_TYPE = "MAT_OUTGOING_SHIPMENT_TYPE";
        public static final String MAT_INVENTORY_ADJUSTMENT_TYPE = "MAT_INVENTORY_ADJUSTMENT_TYPE";

    }

    public static final class STATUS {
        public static final String INIT = "DU_THAO";
        public static final String APPROVE = "PHE_DUYET";
        public static final String REJECT = "TU_CHOI";

        public static final String ACTIVE = "ACTIVE";
        public static final String INACTIVE = "INACTIVE";
    }

    public static class FileType {
        public static final Long PDF = 1L;
        public static final Long DWG = 2L;
        public static final Long EXCEL_XLS = 3L;
        public static final Long IMG = 4L;
        public static final Long EXCEL_XLSX = 5L;
        public static final Long DOCX = 6L;
    }

    public static class UPLOAD_FILE_TYPE {
        public static Long getFileTypeId(String extention) {
            for (Long id : listExtention.keySet()) {
                if (listExtention.get(id).contains(extention)) {
                    return id;
                }
            }
            return 0L;
        }

        public static final Map<Long, String> listExtention = new HashMap<>();

        static {
            listExtention.put(1L, ".pdf");
            listExtention.put(2L, ".dwg");
            listExtention.put(3L, ".xls");
            listExtention.put(4L, ".png");
            listExtention.put(5L, ".xlsx");
            listExtention.put(6L, ".docx");
            listExtention.put(7L, ".doc");
            listExtention.put(8L, ".jpg");
            listExtention.put(9L, ".jpeg");
            listExtention.put(10L, ".zip");
        }
    }

    public interface ERROR_CODE {
        String RECORD_CONFLICT = "RECORD_CONFLICT";
    }
    public static String CHARACTER_COMMA = ",";

    public static class WAREHOUSE_STATUS {
        public static final String HOAT_DONG = "HOAT_DONG";
        public static final String KHONG_HOAT_DONG = "KHONG_HOAT_DONG";

    }

    public static class WAREHOUSE_MANAGER_ROLES {
        public static final String THU_KHO = "THU_KHO";
        public static final String NHAN_VIEN = "NHAN_VIEN";
    }

    public static class WAREHOUSE_MANAGER_APPROVE {
        public static final String YES = "Y";
        public static final String NO = "N";
    }

    public static class PREFIX_PICKING_NO {
        public static final String INCOMING = "QLVTTH_PNK_";
        public static final String OUTGOING = "QLVTTH_PXK_";
        public static final String TRANSFERRING = "";
    }

    public static class WAREHOUSE_TYPE {
        public static final String KHO_TONG = "KHO_TONG";
    }

    public interface ATTACHMENT {
        interface FILE_TYPES {
            String MAT_INCOMING_SHIPMENTS = "mat_incoming_shipments";
            String MAT_INVENTORY_ADJUSTMENTS= "mat_inventory_adjustments";
            String MAT_OUTGOING_SHIPMENTS = "mat_outgoing_shipments";
            String MAT_TRANSFERRING_SHIPMENTS = "mat_transferring_shipments";
        }

        interface TABLE_NAMES {
            String MAT_INCOMING_SHIPMENTS = "mat_incoming_shipments";
            String MAT_INVENTORY_ADJUSTMENTS= "mat_inventory_adjustments";
            String MAT_OUTGOING_SHIPMENTS= "mat_outgoing_shipments";
            String MAT_TRANSFERRING_SHIPMENTS = "mat_transferring_shipments";
        }

        String MODULE = "mat";
    }
}
