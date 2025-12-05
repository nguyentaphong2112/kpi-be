package vn.hbtplus.apidoc;

public class BuildingEquipmentsDoc {

    /**
     * @api {GET} /v1/building-equipments 1. Lấy Danh sách
     * @apiVersion 1.0.0
     * @apiName search-fpn-building-equipments
     * @apiGroup I. Thiết bị gắn toàn nhà
     * @apiPermission ROLE_HR
     *
     * @apiHeader {String} Content-Type=application/json;charset=UTF-8 ContentType
     * @apiHeader {String} Accept=application/json;charset=UTF-8 Accept
     * @apiHeader {String} Authorization <code>Bearer ${access_token}</code>
     * @apiHeaderExample {json} Request Header Param Example:
     * {
     * 	    "Content-Type":"application/json;charset=UTF-8",
     *      "Accept":"application/json;charset=UTF-8",
     *      "Authorization":"${access_token}"
     * }
     *
     * @apiDescription API được sử dụng để lấy danh sách.
     *
     * @apiParam {Integer} [page=0] Trang hiển thị
     * @apiParam {Integer} [size=10] Số bản ghi hiển thị trên trang
     * @apiParam {Long} [buildingId] ID Tòa nhà
     * @apiParam {String} [equipmentName] Tên thiết bị
     * @apiParam {String} [checkingPeriod] Kỳ kiểm tra
     *
     * @apiExample Example usage:
     * curl -i -H "Accept: application/json" -H "Content-Type: application/json" -X GET http://14.225.7.172:8668/icn-service/v1/building-equipments
     *
     *
     * @apiSuccess {Integer} status Mã lỗi trả về từ hệ thống
     * @apiSuccess {String} message Mô tả lỗi trả về từ hệ thống
     * @apiSuccess {Integer} total Tổng số bản ghi hiện có
     * @apiSuccess {Integer} page Trang hiển thị
     * @apiSuccess {Integer} size Số bản ghi hiển thị
     * @apiSuccess {Object[]} data Dữ liệu trả về từ hệ thống.
     * @apiSuccess {Long} data.buildingEquipmentId id khóa chính.
     * @apiSuccess {Long} data.buildingId id tòa nhà.
     * @apiSuccess {String} data.equipmentName Tên thiết bị.
     * @apiSuccess {String} data.equipmentTypeName Loại thiết bị.
     * @apiSuccess {String} data.buildingName Tòa nhà.
     * @apiSuccess {String} data.symbol Ký hiệu.
     * @apiSuccess {Long} data.quantity số lượng.
     * @apiSuccess {Long} data.checkedQuantity số lượng có hồ sơ kiểm định.
     * @apiSuccess {Long} data.repairingQuantity số lượng cần sửa chữa.
     * @apiSuccess {Long} data.replacingQuantity số lượng cần thay thế.
     * @apiSuccess {Long} data.additionalQuantity số lượng cần bổ sung.
     * @apiSuccess {String} data.location Vị trí.
     * @apiSuccess {Long} data.usedYear năm đưa vào sử dụng.
     * @apiSuccess {String} data.checkingPeriod kỳ kiểm tra.
     * @apiSuccess {Date} data.expiryDate Hạn sử dụng.
     * @apiSuccess {String} data.note ghi chú.
     * @apiSuccess {Long} data.inspectionQuantity số lượng chỉ có tem kiểm định của CA PCCC.
     * @apiSuccess {Long} data.maintenanceQuantity số lượng chỉ có team của đơn vị bảo trì.
     * @apiSuccess {Long} data.inspectMaintainQuantity số lượng có cả tem kiểm định và tem của đơn vị bảo trì.
     * @apiSuccess {Long} data.noCheckingQuantity số lượng không có tem.
     *
     * @apiError 200 Gọi API thành công
     * @apiError 400 Bad request
     * @apiError 403 Không có quyền thao tác
     * @apiError 500 Lỗi hệ thống
     *
     * @apiSuccessExample Response (example): 200
     * {
     *     "timestamp": 1697861047471,
     *     "clientMessageId": "d322414c-694f-413e-b416-ada4a6471dc5",
     *     "message": "Successful!",
     *     "data": [
                        {
                        "buildingEquipmentId": 0,
                        "usedYear": 25,
                        "checkingPeriod": "Cuối năm 2023",
                        "quantity": 1,
                        "checkedQuantity": 2,
                        "repairingQuantity": 3,
                        "replacingQuantity": 4,
                        "additionalQuantity": 5,
                        "note": "ghi chú",
                        "inspectionQuantity": 6,
                        "maintenanceQuantity": 7,
                        "inspectMaintainQuantity": 8,
                        "noCheckingQuantity": 9,
                        "expiryDate": "15/01/2024",
                        "location": "Hà Nội",
                        "equipmentName": "Giấy lau tay cao cấp Willow",
                        "equipmentTypeName": "Vật tư chiếu sáng",
                        "buildingName": "Tòa nhà Viettel Hà Giang",
                        "symbol": "VTTHKT"
                        }
     *     ],
     *     "page": 0,
     *     "size": 10,
     *     "total": 1,
     *     "status": 0
     * }
     * @apiErrorExample Response (example): 400 Bad Request
     * {"status": 400, "message": "Bad Request"}
     * @apiErrorExample Response (example): 403 Permission denied
     * {"status": 403, "message": "Permission denied"}
     */


    /**
     * @api {POST} /v1/building-equipments 2. Lưu thông tin
     * @apiVersion 1.0.0
     * @apiName save-fpn-building-equipments
     * @apiGroup I. Thiết bị gắn toàn nhà
     * @apiPermission ROLE_HR
     *
     * @apiHeader {String} Content-Type=application/json;charset=UTF-8 ContentType
     * @apiHeader {String} Accept=application/json;charset=UTF-8 Accept
     * @apiHeader {String} Authorization <code>Bearer ${access_token}</code>
     * @apiHeaderExample {json} Request Header Param Example:
     * {
     * 	    "Content-Type":"application/json;charset=UTF-8",
     *      "Accept":"application/json;charset=UTF-8",
     *      "Authorization":"${access_token}"
     * }
     *
     * @apiDescription API được sử dụng để lưu thông tin.
     *
     * @apiExample Example usage:
     * curl -i -X POST -H "Content-Type: application/json" http://14.225.7.172:8668/icn-service/v1/building-equipments -d 'partyDate=#partyDate'
     *
     * @apiBody {Long} buildingEquipmentId id khóa chính.
     * @apiBody {Long} buildingId id tòa nhà.
     * @apiBody {Long} equipmentId id thiết bị.
     * @apiBody {Long} usedYear năm đưa vào sử dụng.
     * @apiBody {String} checkingPeriod kỳ kiểm tra.
     * @apiBody {Long} quantity số lượng.
     * @apiBody {Long} checkedQuantity số lượng có hồ sơ kiểm định.
     * @apiBody {Long} repairingQuantity số lượng cần sửa chữa.
     * @apiBody {Long} replacingQuantity số lượng cần thay thế.
     * @apiBody {Long} additionalQuantity số lượng cần bổ sung.
     * @apiBody {String} note ghi chú.
     * @apiBody {Long} inspectionQuantity số lượng chỉ có tem kiểm định của CA PCCC.
     * @apiBody {Long} maintenanceQuantity số lượng chỉ có team của đơn vị bảo trì.
     * @apiBody {Long} inspectMaintainQuantity số lượng có cả tema kiểm định và team của đơn vị bảo trì.
     * @apiBody {Long} noCheckingQuantity số lượng không có tem.
     * @apiBody {String} location Vị trí.
     * @apiBody {Date} expiryDate Hạn sử dụng.
     * @apiBody {Object[]} equipmentDetails Chi tiết thiết bị
     * @apiBody {Long} equipmentDetails.buildingEquipmentDetailId id khóa chính.
     * @apiBody {String} equipmentDetails.serialNo số serial.
     * @apiBody {Long} equipmentDetails.buildingEquipmentId bảng master.
     * @apiBody {Date} equipmentDetails.expiredDate hạn sử dụng.
     * @apiBody {String} equipmentDetails.status tình trạng.
     * @apiBody {String} equipmentDetails.note ghi chú.
     * @apiBody {Long} equipmentDetails.usedYear năm đưa vào sử dụng.
     *
     * @apiSuccess {Integer} status Mã lỗi trả về từ hệ thống
     * @apiSuccess {String} message Mô tả lỗi trả về từ hệ thống
     *
     * @apiError 200 Gọi API thành công
     * @apiError 400 Bad request
     * @apiError 401 Token không hợp lệ
     * @apiError 403 Không có quyền thao tác
     * @apiError 500 Lỗi hệ thống
     *
     * @apiSuccessExample Response (example): 200
     * {
     *     "status": 1,
     *     "message": "Thành công"
     * }
     * @apiErrorExample Response (example): 400 Bad Request
     * {"status": 400, "message": "Bad Request"}
     * @apiErrorExample Response (example): 403 Permission denied
     * {"status": 403, "message": "Permission denied"}
     */



    /**
     * @api {DELETE} /v1/building-equipments/${id} 3. Xóa bản ghi
     * @apiVersion 1.0.0
     * @apiName del-fpn-building-equipments
     * @apiGroup I. Thiết bị gắn toàn nhà
     * @apiPermission ROLE_HR
     *
     * @apiHeader {String} Content-Type=application/json;charset=UTF-8 ContentType
     * @apiHeader {String} Accept=application/json;charset=UTF-8 Accept
     * @apiHeader {String} Authorization <code>Bearer ${access_token}</code>
     * @apiHeaderExample {json} Request Header Param Example:
     * {
     * 	    "Content-Type":"application/json;charset=UTF-8",
     *      "Accept":"application/json;charset=UTF-8",
     *      "Authorization":"${access_token}"
     * }
     *
     * @apiDescription API được sử dụng để xóa thông tin theo ID bản ghi
     *
     * @apiExample Example usage:
     * curl -i -X DELETE -H "Content-Type: application/json" http://14.225.7.172:8668/icn-service/v1/building-equipments/${id} -d 'partyDate=#partyDate'
     *
     * @apiSuccess {Integer} status Mã lỗi trả về từ hệ thống
     * @apiSuccess {String} message Mô tả lỗi trả về từ hệ thống
     *
     * @apiError 200 Gọi API thành công
     * @apiError 400 Bad request
     * @apiError 401 Token không hợp lệ
     * @apiError 403 Không có quyền thao tác
     * @apiError 500 Lỗi hệ thống
     *
     * @apiSuccessExample Response (example): 200
     * {
     *     "status": 200,
     *     "message": "Thành công"
     * }
     * @apiErrorExample Response (example): 400 Bad Request
     * {"status": 400, "message": "Bad Request"}
     * @apiErrorExample Response (example): 403 Permission denied
     * {"status": 403, "message": "Permission denied"}
     */



    /**
     * @api {GET} /v1/building-equipments/${id} 4. Lấy chi tiết
     * @apiVersion 1.0.0
     * @apiName detail-fpn-building-equipments
     * @apiGroup I. Thiết bị gắn toàn nhà
     * @apiPermission ROLE_HR
     *
     * @apiHeader {String} Content-Type=application/json;charset=UTF-8 ContentType
     * @apiHeader {String} Accept=application/json;charset=UTF-8 Accept
     * @apiHeader {String} Authorization <code>Bearer ${access_token}</code>
     * @apiHeaderExample {json} Request Header Param Example:
     * {
     * 	    "Content-Type":"application/json;charset=UTF-8",
     *      "Accept":"application/json;charset=UTF-8",
     *      "Authorization":"${access_token}"
     * }
     *
     * @apiDescription API được sử dụng để lấy chi tiết dữ liệu theo ID bản ghi nhằm đáp ứng cho màn hình sửa thông tin
     *
     * @apiExample Example usage:
     * curl -i -X GET -H "Content-Type: application/json" http://14.225.7.172:8668/icn-service/v1/building-equipments/${id} -d 'partyDate=#partyDate'
     *
     * @apiSuccess {Integer} code Mã lỗi trả về từ hệ thống
     * @apiSuccess {String} message Mô tả lỗi trả về từ hệ thống
     * @apiSuccess {JSON} data Dữ liệu trả về từ hệ thống.
     * @apiSuccess {Long} data.buildingEquipmentId id khóa chính.
     * @apiSuccess {Long} data.buildingId id tòa nhà.
     * @apiSuccess {Long} data.equipmentId id thiết bị.
     * @apiSuccess {String} data.createdBy người tạo.
     * @apiSuccess {Date} data.createdTime ngày tạo.
     * @apiSuccess {String} data.modifiedBy người sửa.
     * @apiSuccess {Date} data.modifiedTime ngày sửa.
     * @apiSuccess {Long} data.usedYear năm đưa vào sử dụng.
     * @apiSuccess {String} data.checkingPeriod kỳ kiểm tra.
     * @apiSuccess {Long} data.quantity số lượng.
     * @apiSuccess {Long} data.checkedQuantity số lượng có hồ sơ kiểm định.
     * @apiSuccess {Long} data.repairingQuantity số lượng cần sửa chữa.
     * @apiSuccess {Long} data.replacingQuantity số lượng cần thay thế.
     * @apiSuccess {Long} data.additionalQuantity số lượng cần bổ sung.
     * @apiSuccess {String} data.note ghi chú.
     * @apiSuccess {Long} data.inspectionQuantity số lượng chỉ có tem kiểm định của CA PCCC.
     * @apiSuccess {Long} data.maintenanceQuantity số lượng chỉ có team của đơn vị bảo trì.
     * @apiSuccess {Long} data.inspectMaintainQuantity số lượng có cả tema kiểm định và team của đơn vị bảo trì.
     * @apiSuccess {Long} data.noCheckingQuantity số lượng không có tem.
     * @apiSuccess {String} data.location Vị trí.
     * @apiSuccess {Date} data.expiryDate Hạn sử dụng.
     * @apiSuccess {Object[]} data.equipmentDetails Thông tin chi tiết thiết bị
     * @apiSuccess {Long} data.equipmentDetails.buildingEquipmentDetailId id khóa chính.
     * @apiSuccess {String} data.equipmentDetails.serialNo số serial.
     * @apiSuccess {Long} data.equipmentDetails.buildingEquipmentId bảng master.
     * @apiSuccess {Date} data.equipmentDetails.expiredDate hạn sử dụng.
     * @apiSuccess {String} data.equipmentDetails.status tình trạng.
     * @apiSuccess {String} data.equipmentDetails.note ghi chú.
     * @apiSuccess {Long} data.equipmentDetails.usedYear năm đưa vào sử dụng.
     *
     * @apiError 200 Gọi API thành công
     * @apiError 400 Bad request
     * @apiError 401 Token không hợp lệ
     * @apiError 403 Không có quyền thao tác
     * @apiError 500 Lỗi hệ thống
     *
     * @apiSuccessExample Response (example): 200
     * {
     *     "type": "SUCCESS",
     *     "data": {
     *         "buildingEquipmentId": 1,
     *         "buildingId": 4,
     *         "equipmentId": 1,
     *         "usedYear": 25,
     *         "checkingPeriod": "Cuối năm 2023",
     *         "quantity": 1,
     *         "checkedQuantity": 2,
     *         "repairingQuantity": 3,
     *         "replacingQuantity": 4,
     *         "additionalQuantity": 5,
     *         "note": "ghi chú",
     *         "inspectionQuantity": 6,
     *         "maintenanceQuantity": 7,
     *         "inspectMaintainQuantity": 8,
     *         "noCheckingQuantity": 9,
     *         "expiryDate": "15/01/2024",
     *         "location": "Hà Nội",
     *         "equipmentDetails": [
     *             {
     *                 "isDeleted": "N",
     *                 "createdBy": null,
     *                 "createdTime": null,
     *                 "modifiedBy": null,
     *                 "modifiedTime": null,
     *                 "buildingEquipmentDetailId": 1,
     *                 "serialNo": "A122",
     *                 "buildingEquipmentId": 1,
     *                 "expiredDate": "30/01/2024",
     *                 "status": "Đ",
     *                 "note": "Ghi chú",
     *                 "usedYear": 2024,
     *                 "deleted": false
     *             }
     *         ]
     *     }
     * }
     * @apiErrorExample Response (example): 400 Bad Request
     * {"status": 400, "message": "Bad Request"}
     * @apiErrorExample Response (example): 403 Permission denied
     * {"status": 403, "message": "Permission denied"}
     */



    /**
     * @api {GET} /v1/building-equipments/export 5. Xuất báo cáo
     * @apiVersion 1.0.0
     * @apiName export-fpn-building-equipments
     * @apiGroup I. Thiết bị gắn toàn nhà
     * @apiPermission ROLE_HR
     *
     * @apiHeader {String} Content-Type=application/json;charset=UTF-8 ContentType
     * @apiHeader {String} Accept=application/json;charset=UTF-8 Accept
     * @apiHeader {String} Authorization <code>Bearer ${access_token}</code>
     * @apiHeaderExample {json} Request Header Param Example:
     * {
     * 	    "Content-Type":"application/json;charset=UTF-8",
     *      "Accept":"application/json;charset=UTF-8",
     *      "Authorization":"${access_token}"
     * }
     *
     * @apiDescription API được sử dụng để xuất báo cáo
     *
     * @apiParam {Long} [orgId] ID đơn vị
     * @apiParam {String} [keySearch] Mã/Tên/Email nhân viên
     * @apiParam {String} [empTypeCode] Đối tượng
     *
     * @apiExample Example usage:
     * curl -i -X GET -H "Content-Type: application/json" http://14.225.7.172:8668/icn-service/v1/building-equipments/export
     *
     * @apiSuccess {Integer} status Mã lỗi trả về từ hệ thống
     * @apiSuccess {String} message Mô tả lỗi trả về từ hệ thống
     *
     * @apiError 200 Gọi API thành công
     * @apiError 400 Bad request
     * @apiError 401 Token không hợp lệ
     * @apiError 403 Không có quyền thao tác
     * @apiError 500 Lỗi hệ thống
     *
     * @apiSuccessExample Response (example): 200
     * {
     *     "status": 1,
     *     "message": "Thành công"
     * }
     * @apiErrorExample Response (example): 400 Bad Request
     * {"status": 400, "message": "Bad Request"}
     * @apiErrorExample Response (example): 403 Permission denied
     * {"status": 403, "message": "Permission denied"}
     */


    /**
     * @api {GET} /v1/building-equipment-details/{buildingEquipmentId} 6. Lấy Thông tin chi tiết thiết bị
     * @apiVersion 1.0.0
     * @apiName search-fpn-building-equipment-details
     * @apiGroup I. Thiết bị gắn toàn nhà
     * @apiPermission ROLE_HR
     *
     * @apiHeader {String} Content-Type=application/json;charset=UTF-8 ContentType
     * @apiHeader {String} Accept=application/json;charset=UTF-8 Accept
     * @apiHeader {String} Authorization <code>Bearer ${access_token}</code>
     * @apiHeaderExample {json} Request Header Param Example:
     * {
     * 	    "Content-Type":"application/json;charset=UTF-8",
     *      "Accept":"application/json;charset=UTF-8",
     *      "Authorization":"${access_token}"
     * }
     *
     * @apiDescription API được sử dụng để lấy danh sách.
     *
     * @apiParam {Long} buildingEquipmentId ID bảng mapping thiết bị với tòa nhà
     *
     * @apiExample Example usage:
     * curl -i -H "Accept: application/json" -H "Content-Type: application/json" -X GET http://14.225.7.172:8668/icn-service/v1/building-equipment-details
     *
     *
     * @apiSuccess {Integer} status Mã lỗi trả về từ hệ thống
     * @apiSuccess {String} message Mô tả lỗi trả về từ hệ thống
     * @apiSuccess {Integer} total Tổng số bản ghi hiện có
     * @apiSuccess {Integer} page Trang hiển thị
     * @apiSuccess {Integer} size Số bản ghi hiển thị
     * @apiSuccess {Object[]} data Dữ liệu trả về từ hệ thống.
     * @apiSuccess {Long} data.buildingEquipmentDetailId id khóa chính.
     * @apiSuccess {String} data.serialNo số serial.
     * @apiSuccess {Long} data.buildingEquipmentId bảng master.
     * @apiSuccess {Date} data.expiredDate hạn sử dụng.
     * @apiSuccess {String} data.status tình trạng.
     * @apiSuccess {String} data.note ghi chú.
     * @apiSuccess {Long} data.usedYear năm đưa vào sử dụng.
     *
     * @apiError 200 Gọi API thành công
     * @apiError 400 Bad request
     * @apiError 403 Không có quyền thao tác
     * @apiError 500 Lỗi hệ thống
     *
     * @apiSuccessExample Response (example): 200
     * {
     *     "type": "SUCCESS",
     *     "data": [
     *         {
     *             "isDeleted": "N",
     *             "createdBy": null,
     *             "createdTime": null,
     *             "modifiedBy": null,
     *             "modifiedTime": null,
     *             "buildingEquipmentDetailId": 1,
     *             "serialNo": "A122",
     *             "buildingEquipmentId": 1,
     *             "expiredDate": "30/01/2024",
     *             "status": "Đ",
     *             "note": "Ghi chú",
     *             "usedYear": 2024,
     *             "deleted": false
     *         }
     *     ]
     * }
     * @apiErrorExample Response (example): 400 Bad Request
     * {"status": 400, "message": "Bad Request"}
     * @apiErrorExample Response (example): 403 Permission denied
     * {"status": 403, "message": "Permission denied"}
     */

}
