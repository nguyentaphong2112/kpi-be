package vn.hbtplus.models.bean;

import lombok.Data;
import vn.hbtplus.utils.Utils;

import java.util.Map;

@Data
public class WarehouseNotifyBean {
    private String warehouseName;
    private String senderCode;
    private String senderName;
    private String receiverCode;
    private String pickingNo;
    private String reason;
    private Long id;

    public enum FUNCTION_CODES {
        IMPORT_SEND_TO_APPROVE(
                "Gửi phê duyệt phiếu nhập",
                "Gửi phê duyệt phiếu nhập kho",
                "Kính gửi d/c, <br/>" +
                "Đồng chí {nguoi_gui} đã gửi yêu cầu duyệt phiếu nhập kho {ten_kho} có số phiếu {so_phieu}",
                "Đồng chí {nguoi_gui} đã gửi yêu cầu duyệt phiếu nhập kho {ten_kho} có số phiếu {so_phieu}"
        ),
        IMPORT_APPROVE(
                "Phê duyệt phiếu nhập",
                "Duyệt phiếu nhập kho",
                "Kính gửi d/c, <br/>" +
                "Đồng chí {nguoi_gui} đã duyệt phiếu nhập kho {ten_kho} có số phiếu {so_phieu}",
                "Đồng chí {nguoi_gui} đã  duyệt phiếu nhập kho {ten_kho} có số phiếu {so_phieu}"
        ),
        IMPORT_REJECT(
                "Từ chối phiếu nhập",
                "Từ chối duyệt phiếu nhập kho",
                "Kính gửi d/c, <br/>" +
                "Đồng chí {nguoi_gui} đã từ chối duyệt phiếu nhập kho {ten_kho} có số phiếu {so_phieu} với lý do {ly_do}",
                "Đồng chí {nguoi_gui} đã từ chối duyệt phiếu nhập kho {ten_kho} có số phiếu {so_phieu}"
        ),
        OUTPUT_SEND_TO_APPROVE(
                "Gửi phê duyệt phiếu xuất",
                "Gửi phê duyệt phiếu xuất kho",
                "Kính gửi d/c, <br/>" +
                "Đồng chí {nguoi_gui} đã gửi yêu cầu duyệt phiếu xuất kho {ten_kho} với số phiếu {so_phieu}",
                "Đồng chí {nguoi_gui} đã gửi yêu cầu duyệt phiếu xuất kho {ten_kho} có số phiếu {so_phieu}"
        ),
        OUTPUT_APPROVE(
                "Phê duyệt phiếu xuất",
                "Duyệt phiếu xuất kho",
                "Kính gửi d/c, <br/>" +
                "Đồng chí {nguoi_gui} đã duyệt phiếu xuất kho {ten_kho} có số phiếu {so_phieu}",
                "Đồng chí {nguoi_gui} đã  duyệt phiếu xuất kho {ten_kho} có số phiếu {so_phieu}"
        ),
        OUTPUT_REJECT(
                "Từ chối phiếu xuất",
                "Từ chối duyệt phiếu xuất kho",
                "Kính gửi d/c, <br/>" +
                "Đồng chí {nguoi_gui} đã từ chối duyệt phiếu xuất kho {ten_kho} có số phiếu {so_phieu} với lý do {ly_do}",
                "Đồng chí {nguoi_gui} đã từ chối duyệt phiếu xuất kho {ten_kho} có số phiếu {so_phieu}"
        ),
        TRANSFER_SEND_TO_APPROVE(
                "Gửi phê duyệt phiếu điều chuyển",
                "Gửi phê duyệt phiếu điều chuyển vật tư",
                "Kính gửi d/c, <br/>" +
                "Đồng chí {nguoi_gui} đã gửi yêu cầu duyệt phiếu điều chuyển kho {ten_kho} với số phiếu {so_phieu}",
                "Đồng chí {nguoi_gui} đã gửi yêu cầu duyệt phiếu điều chuyển kho {ten_kho} có số phiếu {so_phieu}"
        ),
        TRANSFER_APPROVE(
                "Phê duyệt phiếu điều chuyển",
                "Duyệt phiếu xuất điều chuyển vật tư",
                "Kính gửi d/c, <br/>" +
                "Đồng chí {nguoi_gui} đã duyệt phiếu điều chuyển kho {ten_kho} có số phiếu {so_phieu}",
                "Đồng chí {nguoi_gui} đã  duyệt phiếu điều chuyển kho {ten_kho} có số phiếu {so_phieu}"
        ),
        TRANSFER_REJECT(
                "Từ chối phiếu điều chuyển",
                "Từ chối duyệt phiếu điều chuyển vật tư",
                "Kính gửi d/c, <br/>" +
                "Đồng chí {nguoi_gui} đã từ chối duyệt phiếu điều chuyển kho {ten_kho} có số phiếu {so_phieu} với lý do {ly_do}",
                "Đồng chí {nguoi_gui} đã từ chối duyệt phiếu điều chuyển kho {ten_kho} có số phiếu {so_phieu}"
        ),
        ADJUSTMENT_SEND_TO_APPROVE(
                "Gửi phê duyệt phiếu kiểm kê",
                "Gửi phê duyệt phiếu kiểm kê vật tư",
                "Kính gửi d/c, <br/>" +
                "Đồng chí {nguoi_gui} đã gửi yêu cầu duyệt phiếu kiểm kê kho {ten_kho} với số phiếu {so_phieu}",
                "Đồng chí {nguoi_gui} đã gửi yêu cầu duyệt phiếu kiểm kê kho {ten_kho} có số phiếu {so_phieu}"
        ),
        ADJUSTMENT_APPROVE(
                "Phê duyệt phiếu kiểm kê",
                "Duyệt phiếu xuất kiểm kê vật tư",
                "Kính gửi d/c, <br/>" +
                "Đồng chí {nguoi_gui} đã duyệt phiếu kiểm kê kho {ten_kho} có số phiếu {so_phieu}",
                "Đồng chí {nguoi_gui} đã  duyệt phiếu kiểm kê kho {ten_kho} có số phiếu {so_phieu}"
        ),
        ADJUSTMENT_REJECT(
                "Từ chối phiếu kiểm kê",
                "Từ chối duyệt phiếu kiểm kê vật tư",
                "Kính gửi d/c, <br/>" +
                "Đồng chí {nguoi_gui} đã từ chối duyệt phiếu kiểm kê kho {ten_kho} có số phiếu {so_phieu} với lý do {ly_do}",
                "Đồng chí {nguoi_gui} đã từ chối duyệt phiếu kiểm kê kho {ten_kho} có số phiếu {so_phieu}"
        );


        FUNCTION_CODES(String name, String emailSubject, String emailContent, String smsContent) {
            this.name = name;
            this.emailSubject = emailSubject;
            this.emailContent = emailContent;
            this.smsContent = smsContent;
        }

        private final String name;
        private final String emailSubject;
        private final String emailContent;
        private final String smsContent;


        public String getName() {
            return name;
        }

        public String getEmailSubject() {
            return emailSubject;
        }

        public String getEmailContent() {
            return emailContent;
        }

        public String getSmsContent() {
            return smsContent;
        }

        public String getEmailContent(Map<String, String> params) {
            String temp = emailContent;
            for (Map.Entry<String, String> entry : params.entrySet()) {
                if (!Utils.isNullOrEmpty(entry.getValue())) {
                    temp = temp.replace("{" + entry.getKey() + "}", entry.getValue());
                }
            }
            return temp;
        }

        public String getSmsContent(Map<String, String> params) {
            String temp = smsContent;
            for (Map.Entry<String, String> entry : params.entrySet()) {
                if (!Utils.isNullOrEmpty(entry.getValue())) {
                    temp = temp.replace("{" + entry.getKey() + "}", entry.getValue());
                }
            }
            return temp;
        }
    }
}
