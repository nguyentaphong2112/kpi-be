package vn.hbtplus.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;
import vn.hbtplus.constant.Constants;
import vn.hbtplus.utils.Utils;

import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.Date;

@Slf4j
public class CommonUtils {
    /**
     * Lay dinh dang file duoi dang so
     */
    public static Long getTypeNumber(String typeName) {
        if (StringUtils.isNotBlank(typeName)) {
            if (typeName.contains(".")) {
                typeName = typeName.replace(".", "");
            }
            if (typeName.equalsIgnoreCase("pdf")) {
                return Constants.FileType.PDF;
            } else if (typeName.equalsIgnoreCase("xls")) {
                return Constants.FileType.EXCEL_XLS;
            } else if (typeName.equalsIgnoreCase("xlsx")) {
                return Constants.FileType.EXCEL_XLSX;
            } else if (typeName.equalsIgnoreCase("dwg")) {
                return Constants.FileType.DWG;
            }
        }
        return 0L;
    }

    /**
     * Chuyen doi tuong Date thanh doi tuong String.
     *
     * @param date Doi tuong Date
     * @return Xau ngay, co dang dd/MM/yyyy
     */
    public static String convertDateToString(Date date, String pattern) {
        if (date == null) {
            return "";
        } else {
            SimpleDateFormat dateFormat = new SimpleDateFormat(pattern);
            return dateFormat.format(date);
        }
    }

    /**
     * removeSignAndMultiSpace
     * @param text
     * @return
     */
    public static String removeSignAndMultiSpace(String text){
        text = Utils.removeSign(text);
        text = text.trim().replaceAll("\\s+"," ");
        return text.replaceAll(" ", "_");
    }

    public static HttpHeaders getHeader(HttpServletRequest request) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.AUTHORIZATION, request.getHeader(HttpHeaders.AUTHORIZATION));
        return headers;
    }

    public static String generatePickingNo(String type, Long objectId) {
        return type + String.format("%06d", objectId);
    }
}
