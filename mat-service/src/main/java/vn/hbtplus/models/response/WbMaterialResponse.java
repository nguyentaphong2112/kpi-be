package vn.hbtplus.models.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.hbtplus.constants.BaseConstants;

import java.util.Date;

@Data
@NoArgsConstructor
@JsonInclude(Include.NON_NULL)
public class WbMaterialResponse {
	private Long wbMaterialId;
	private String code;
	private String name;
	private String barcode;
	private String seri;
	private String number;
	private Long wbMaterialTypeId;
	private Long companyId;
	private Long countryId;
	private Long unitId;
	private String wbMaterialTypeName;
	private String companyName;
	private String countryName;
	private String description;
	private String unitName;
	private String createdBy;
	@JsonFormat(pattern = BaseConstants.COMMON_DATE_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
	private Date createdTime;
	private String modifiedBy;
	@JsonFormat(pattern = BaseConstants.COMMON_DATE_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
	private Date modifiedTime;
}
