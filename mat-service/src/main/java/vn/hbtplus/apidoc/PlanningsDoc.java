package vn.hbtplus.apidoc;

public class PlanningsDoc {

    /**
     * @api {GET} /v1/plannings 1. Lấy Danh sách
     * @apiVersion 1.0.0
     * @apiName search-fpn-plannings
     * @apiGroup II. Kế hoạch diễn tập
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
     * @apiParam {String} [createdTime] Thời gian tạo
     * @apiParam {String} [planningTypeCode] Loại kế hoạch
     * @apiParam {String} [startDate] Từ ngày
     * @apiParam {String} [endDate] Đến ngày
     *
     * @apiExample Example usage:
     * curl -i -H "Accept: application/json" -H "Content-Type: application/json" -X GET http://14.225.7.172:8668/icn-service/v1/plannings
     *
     *
     * @apiSuccess {Integer} status Mã lỗi trả về từ hệ thống
     * @apiSuccess {String} message Mô tả lỗi trả về từ hệ thống
     * @apiSuccess {Integer} total Tổng số bản ghi hiện có
     * @apiSuccess {Integer} page Trang hiển thị
     * @apiSuccess {Integer} size Số bản ghi hiển thị
     * @apiSuccess {Object[]} data Dữ liệu trả về từ hệ thống.
     * @apiSuccess {Long} data.planningId id khóa chính.
     * @apiSuccess {String} data.documentNo số phiếu kế hoạch.
     * @apiSuccess {String} data.planningTypeCode Loại kế hoạch. Mapping với category_type =PLANNING_TYPE.
     * @apiSuccess {String} data.planningTypeName Tên Loại kế hoạch.
     * @apiSuccess {String} data.note Ghi chú.
     * @apiSuccess {String} data.createdBy người tạo.
     * @apiSuccess {Date} data.createdTime ngày tạo.
     * @apiSuccess {String} data.modifiedBy người sửa.
     * @apiSuccess {Date} data.modifiedTime ngày sửa.
     * @apiSuccess {Object[]} data.attachmentFiles Danh sách file đính kèm
     * @apiSuccess {Long} data.attachmentFiles.attachmentFileId ID file
     * @apiSuccess {String} data.attachmentFiles.fileName Tên file
     *
     * @apiError 200 Gọi API thành công
     * @apiError 400 Bad request
     * @apiError 403 Không có quyền thao tác
     * @apiError 500 Lỗi hệ thống
     *
     * @apiSuccessExample Response (example): 200
     * {
     *     "draw": null,
     *     "first": "null",
     *     "recordsFiltered": "3",
     *     "recordsTotal": "3",
     *     "data": [
     *         {
     *             "planningId": 1,
     *             "documentNo": "AM001",
     *             "planningTypeCode": "KH_PCCC",
     *             "note": "GHi chú",
     *             "planningTypeName": "KH bảo trì PCCC",
     *             "attachmentFiles": [
     *                 {
     *                     "attachmentFileId": 129796,
     *                     "fileName": "20231217094306_060645_Bieu-mau-luong-thang-nam-2023 (3).xlsx",
     *                     "objectId": 1,
     *                     "type": 22
     *                 }
     *             ]
     *         },
     *         {
     *             "planningId": 2,
     *             "documentNo": "AM002",
     *             "planningTypeCode": "KH_TTDT",
     *             "note": "Ghi chú AM002",
     *             "planningTypeName": "KH Thực tập, diễn tập",
     *             "attachmentFiles": [
     *                 {
     *                     "attachmentFileId": 129798,
     *                     "fileName": "20231217094306_060645_Bieu-mau-luong-thang-nam-2023 (1).xlsx",
     *                     "objectId": 2,
     *                     "type": 22
     *                 }
     *             ]
     *         },
     *         {
     *             "planningId": 3,
     *             "documentNo": "AM003",
     *             "planningTypeCode": "KH_THEO_DOI_CTHL",
     *             "note": "Ghi chú AM003",
     *             "planningTypeName": "Theo dõi công tác huấn luyện"
     *         }
     *     ],
     *     "headerConfig": null
     * }
     * @apiErrorExample Response (example): 400 Bad Request
     * {"status": 400, "message": "Bad Request"}
     * @apiErrorExample Response (example): 403 Permission denied
     * {"status": 403, "message": "Permission denied"}
     */


    /**
     * @api {POST} /v1/plannings 2. Lưu thông tin
     * @apiVersion 1.0.0
     * @apiName save-fpn-plannings
     * @apiGroup II. Kế hoạch diễn tập
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
     * curl -i -X POST -H "Content-Type: application/json" http://14.225.7.172:8668/icn-service/v1/plannings -d 'partyDate=#partyDate'
     *
     * @apiBody {Long} planningId id khóa chính.
     * @apiBody {String} documentNo số phiếu kế hoạch.
     * @apiBody {String} planningTypeCode Loại kế hoạch. Mapping với category_type =PLANNING_TYPE.
     * @apiBody {String} note Ghi chú.
     * @apiBody {Object[]} planningDetails Chi tiết kế hoạch.
     * @apiBody {Object[]} files File đính kèm. List MultipartFile
     * @apiBody {Long} planningDetails.buildingId id tòa nhà.
     * @apiBody {String} planningDetails.content nội dung kế hoạch.
     * @apiBody {String} planningDetails.departmentName phòng ban thực hiện.
     * @apiBody {Long} planningDetails.employeeId id nhân viên thực hiện.
     * @apiBody {String} planningDetails.startDate thời gian start.
     * @apiBody {String} planningDetails.endDate thời gian end.
     * @apiBody {String} planningDetails.result kết quả thực hiện.
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
     * @api {DELETE} /v1/plannings/${id} 3. Xóa bản ghi
     * @apiVersion 1.0.0
     * @apiName del-fpn-plannings
     * @apiGroup II. Kế hoạch diễn tập
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
     * curl -i -X DELETE -H "Content-Type: application/json" http://14.225.7.172:8668/icn-service/v1/plannings/${id} -d 'partyDate=#partyDate'
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
     * @api {GET} /v1/plannings/${id} 4. Lấy chi tiết
     * @apiVersion 1.0.0
     * @apiName detail-fpn-plannings
     * @apiGroup II. Kế hoạch diễn tập
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
     * curl -i -X GET -H "Content-Type: application/json" http://14.225.7.172:8668/icn-service/v1/plannings/${id} -d 'partyDate=#partyDate'
     *
     * @apiSuccess {Integer} code Mã lỗi trả về từ hệ thống
     * @apiSuccess {String} message Mô tả lỗi trả về từ hệ thống
     * @apiSuccess {JSON} data Dữ liệu trả về từ hệ thống.
     * @apiSuccess {Long} data.planningId id khóa chính.
     * @apiSuccess {String} data.documentNo số phiếu kế hoạch.
     * @apiSuccess {String} data.planningTypeCode Loại kế hoạch. Mapping với category_type =PLANNING_TYPE.
     * @apiSuccess {String} data.note Ghi chú.
     * @apiSuccess {String} data.createdBy người tạo.
     * @apiSuccess {Date} data.createdTime ngày tạo.
     * @apiSuccess {String} data.modifiedBy người sửa.
     * @apiSuccess {Date} data.modifiedTime ngày sửa.
     * @apiSuccess {Object[]} planningDetails Chi tiết kế hoạch.
     * @apiSuccess {Long} data.planningDetails.buildingId id tòa nhà.
     * @apiSuccess {Long} data.planningDetails.buildingName Tên tòa nhà.
     * @apiSuccess {String} data.planningDetails.content nội dung kế hoạch.
     * @apiSuccess {String} data.planningDetails.departmentName phòng ban thực hiện.
     * @apiSuccess {Long} data.planningDetails.employeeId ID nhân viên.
     * @apiSuccess {String} data.planningDetails.employeeCode Mã nhân viên.
     * @apiSuccess {String} data.planningDetails.fullName Tên nhân viên.
     * @apiSuccess {String} data.planningDetails.startDate thời gian start.
     * @apiSuccess {String} data.planningDetails.endDate thời gian end.
     * @apiSuccess {String} data.planningDetails.result kết quả thực hiện.
     * @apiSuccess {String} data.planningDetails.createdBy người tạo.
     * @apiSuccess {String} data.planningDetails.createdTime ngày tạo.
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
     *         "planningId": 1,
     *         "documentNo": "AM001",
     *         "planningTypeCode": "KH_PCCC",
     *         "note": "GHi chú",
     *         "planningDetails": [
     *             {
     *                 "planningDetailId": 1,
     *                 "buildingId": 4,
     *                 "planningId": 1,
     *                 "content": "content",
     *                 "departmentName": "Trung tâm công nghệ thông tin",
     *                 "employeeId": 1,
     *                 "startDate": "01/01/2024",
     *                 "endDate": "29/01/2024",
     *                 "result": "result",
     *                 "buildingName": "Tòa nhà Viettel Hà Giang",
     *                 "employeeCode": "102024",
     *                 "fullName": "Nguyễn Văn Biền"
     *             },
     *             {
     *                 "planningDetailId": 2,
     *                 "buildingId": 5,
     *                 "planningId": 1,
     *                 "content": "Nội dung",
     *                 "departmentName": "Trung tâm kinh doanh",
     *                 "employeeId": 2,
     *                 "startDate": "23/01/2024",
     *                 "endDate": "05/03/2024",
     *                 "result": "kết quả",
     *                 "buildingName": "Tòa nhà Viettel Điện Biên",
     *                 "employeeCode": "102546",
     *                 "fullName": "Hoàng Chí Hiếu"
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
     * @api {GET} /v1/plannings/export 5. Xuất báo cáo
     * @apiVersion 1.0.0
     * @apiName export-fpn-plannings
     * @apiGroup II. Kế hoạch diễn tập
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
     * curl -i -X GET -H "Content-Type: application/json" http://14.225.7.172:8668/icn-service/v1/plannings/export
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
     * @api {GET} /v1/planning-details/{planningId} 6. Lấy Thông tin chi tiết kế hoạch
     * @apiVersion 1.0.0
     * @apiName search-planning-details
     * @apiGroup II. Kế hoạch diễn tập
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
     * @apiParam {Long} planningId ID Kế hoạch
     *
     * @apiExample Example usage:
     * curl -i -H "Accept: application/json" -H "Content-Type: application/json" -X GET http://14.225.7.172:8668/icn-service/v1/planning-details/{planningId}
     *
     *
     * @apiSuccess {Integer} status Mã lỗi trả về từ hệ thống
     * @apiSuccess {String} message Mô tả lỗi trả về từ hệ thống
     * @apiSuccess {Long} data.planningDetailId id khóa chính.
     * @apiSuccess {Long} data.buildingId id tòa nhà.
     * @apiSuccess {Long} data.buildingName Tên tòa nhà.
     * @apiSuccess {Long} data.planningId ID kế hoạch.
     * @apiSuccess {String} data.content nội dung kế hoạch.
     * @apiSuccess {String} data.departmentName phòng ban thực hiện.
     * @apiSuccess {Long} data.employeeId ID nhân viên.
     * @apiSuccess {String} data.employeeCode Mã nhân viên.
     * @apiSuccess {String} data.fullName Tên nhân viên.
     * @apiSuccess {Date} data.startDate thời gian start.
     * @apiSuccess {Date} data.endDate thời gian end.
     * @apiSuccess {String} data.result kết quả thực hiện.
     * @apiSuccess {String} data.createdBy người tạo.
     * @apiSuccess {Date} data.createdTime ngày tạo.
     * @apiSuccess {String} data.modifiedBy người sửa.
     * @apiSuccess {Date} data.modifiedTime ngày sửa.
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
     *             "planningDetailId": 1,
     *             "buildingId": 4,
     *             "planningId": 1,
     *             "content": "content",
     *             "departmentName": "Trung tâm công nghệ thông tin",
     *             "employeeId": 1,
     *             "startDate": "01/01/2024",
     *             "endDate": "29/01/2024",
     *             "result": "result",
     *             "buildingName": "Tòa nhà Viettel Hà Giang",
     *             "employeeCode": "102024",
     *             "fullName": "Nguyễn Văn Biền"
     *         },
     *         {
     *             "planningDetailId": 2,
     *             "buildingId": 5,
     *             "planningId": 1,
     *             "content": "Nội dung",
     *             "departmentName": "Trung tâm kinh doanh",
     *             "employeeId": 2,
     *             "startDate": "23/01/2024",
     *             "endDate": "05/03/2024",
     *             "result": "kết quả",
     *             "buildingName": "Tòa nhà Viettel Điện Biên",
     *             "employeeCode": "102546",
     *             "fullName": "Hoàng Chí Hiếu"
     *         }
     *     ]
     * }
     * @apiErrorExample Response (example): 400 Bad Request
     * {"status": 400, "message": "Bad Request"}
     * @apiErrorExample Response (example): 403 Permission denied
     * {"status": 403, "message": "Permission denied"}
     */

}
