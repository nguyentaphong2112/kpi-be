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
public class WbMaterialTypeResponse {
	private Long wbMaterialTypeId;
	private String code;
	private String name;
	private String symbol;
	private String description;
	private String parentName;
	private String createdBy;
	@JsonFormat(pattern = BaseConstants.COMMON_DATE_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
	private Date createdTime;
	private String modifiedBy;
	@JsonFormat(pattern = BaseConstants.COMMON_DATE_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
	private Date modifiedTime;
}
