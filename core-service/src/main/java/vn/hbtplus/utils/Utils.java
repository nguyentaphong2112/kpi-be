package vn.hbtplus.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.text.WordUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.ThreadContext;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import vn.hbtplus.annotations.EmailFormat;
import vn.hbtplus.annotations.EmailFormatValidator;
import vn.hbtplus.configs.HttpMonitoringInterceptor;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.exceptions.BaseAppException;
import vn.hbtplus.models.AttributeRequestDto;
import vn.hbtplus.models.UserTokenDto;
import vn.hbtplus.utils.validates.PhoneNumberFormat;
import vn.hbtplus.utils.validates.PhoneNumberFormatValidator;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.persistence.Column;
import javax.persistence.Table;
import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintValidator;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.cert.X509Certificate;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@Slf4j
public class Utils {
    private static String exportFolder;
    private static ModelMapper modelMapper;
    private static final ThreadLocal<DecimalFormat> DECIMAL_FORMAT_INT =
            ThreadLocal.withInitial(() -> new DecimalFormat("###,###.#####"));
    private static final ThreadLocal<SimpleDateFormat> DEFAULT_FORMAT_DATE =
            ThreadLocal.withInitial(() -> new SimpleDateFormat("dd/MM/yyyy"));
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .setSerializationInclusion(JsonInclude.Include.NON_NULL)
            .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);

    private static final String[] SIGNED_ARR = new String[]{
            "à", "á", "ạ", "ả", "ã",
            "â", "ầ", "ấ", "ậ", "ẩ", "ẫ",
            "ă", "ằ", "ắ", "ặ", "ẳ", "ẵ",
            "è", "é", "ẹ", "ẻ", "ẽ",
            "ê", "ề", "ế", "ệ", "ể", "ễ",
            "ì", "í", "ị", "ỉ", "ĩ",
            "ò", "ó", "ọ", "ỏ", "õ",
            "ô", "ồ", "ố", "ộ", "ổ", "ỗ",
            "ơ", "ờ", "ớ", "ợ", "ở", "ỡ",
            "ù", "ú", "ụ", "ủ", "ũ",
            "ư", "ừ", "ứ", "ự", "ử", "ữ",
            "ỳ", "ý", "ỵ", "ỷ", "ỹ",
            "đ",
            "À", "Á", "Ạ", "Ả", "Ã",
            "Â", "Ầ", "Ấ", "Ậ", "Ẩ", "Ẫ",
            "Ă", "Ằ", "Ắ", "Ặ", "Ẳ", "Ẵ",
            "È", "É", "Ẹ", "Ẻ", "Ẽ",
            "Ê", "Ề", "Ế", "Ệ", "Ể", "Ễ",
            "Ì", "Í", "Ị", "Ỉ", "Ĩ",
            "Ò", "Ó", "Ọ", "Ỏ", "Õ",
            "Ô", "Ồ", "Ố", "Ộ", "Ổ", "Ỗ",
            "Ơ", "Ờ", "Ớ", "Ợ", "Ở", "Ỡ",
            "Ù", "Ú", "Ụ", "Ủ", "Ũ",
            "Ư", "Ừ", "Ứ", "Ự", "Ử", "Ữ",
            "Ỳ", "Ý", "Ỵ", "Ỷ", "Ỹ",
            "Đ"
    };

    private static final String[] UNSIGNED_ARR = new String[]{
            "a", "a", "a", "a", "a",
            "a", "a", "a", "a", "a", "a",
            "a", "a", "a", "a", "a", "a",
            "e", "e", "e", "e", "e",
            "e", "e", "e", "e", "e", "e",
            "i", "i", "i", "i", "i",
            "o", "o", "o", "o", "o",
            "o", "o", "o", "o", "o", "o",
            "o", "o", "o", "o", "o", "o",
            "u", "u", "u", "u", "u",
            "u", "u", "u", "u", "u", "u",
            "y", "y", "y", "y", "y",
            "d",
            "A", "A", "A", "A", "A",
            "A", "A", "A", "A", "A", "A",
            "A", "A", "A", "A", "A", "A",
            "E", "E", "E", "E", "E",
            "E", "E", "E", "E", "E", "E",
            "I", "I", "I", "I", "I",
            "O", "O", "O", "O", "O",
            "O", "O", "O", "O", "O", "O",
            "O", "O", "O", "O", "O", "O",
            "U", "U", "U", "U", "U",
            "U", "U", "U", "U", "U", "U",
            "Y", "Y", "Y", "Y", "Y",
            "D"
    };

    @Autowired
    public void setModelMapper(ModelMapper modelMapper) {
        Utils.modelMapper = modelMapper;
    }

    @Value("${service.exportFolder}")
    public void setExportFolder(String exportFolder) {
        Utils.exportFolder = exportFolder;
    }


    public static HttpHeaders getRequestHeader(HttpServletRequest request) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.AUTHORIZATION, request.getHeader(HttpHeaders.AUTHORIZATION));
        String clientMessageId;
        String transactionId;
        try {
            HttpMonitoringInterceptor.ServiceHeader serviceHeader =
                    OBJECT_MAPPER.readValue(ThreadContext.get(HttpMonitoringInterceptor.SERVICE_HEADER), HttpMonitoringInterceptor.ServiceHeader.class);

            clientMessageId = serviceHeader.getClientMessageId();
            transactionId = serviceHeader.getTransactionId();
        } catch (Exception ex) {
            clientMessageId = UUID.randomUUID().toString();
            transactionId = UUID.randomUUID().toString();
        }

        headers.add(HttpMonitoringInterceptor.TRANSACTION_ID, transactionId);
        headers.add(HttpMonitoringInterceptor.CLIENT_MESSAGE_ID, clientMessageId);
        return headers;
    }

    public static HttpHeaders getHeader() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpHeaders headers = new HttpHeaders();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            String token = request.getHeader("access-token");
            String bearerToken = token != null ? token : request.getHeader("Authorization");
            headers.set(HttpHeaders.AUTHORIZATION, bearerToken);
        } else {
            String token = getUserEmpCode();
            if (StringUtils.isNotBlank(token)) {
                headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + token);
            }
        }
        String clientMessageId;
        String transactionId;
        try {
            HttpMonitoringInterceptor.ServiceHeader serviceHeader =
                    OBJECT_MAPPER.readValue(ThreadContext.get(HttpMonitoringInterceptor.SERVICE_HEADER), HttpMonitoringInterceptor.ServiceHeader.class);

            clientMessageId = serviceHeader.getClientMessageId();
            transactionId = serviceHeader.getTransactionId();
        } catch (Exception ex) {
            clientMessageId = UUID.randomUUID().toString();
            transactionId = UUID.randomUUID().toString();
        }

        headers.add(HttpMonitoringInterceptor.TRANSACTION_ID, transactionId);
        headers.add(HttpMonitoringInterceptor.CLIENT_MESSAGE_ID, clientMessageId);
        return headers;
    }

    public static String getUserEmpCode() {
        try {
            UserTokenDto tokenDto = (UserTokenDto) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (tokenDto != null) {
                return tokenDto.getEmployeeCode();
            } else {
                return "unknown user";
            }
        } catch (Exception e) {
            log.error("{}", e);
            return "unknown user";
        }
    }

    public static List<String> getRoleCodeList() {
        try {
            UserTokenDto tokenDto = (UserTokenDto) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (tokenDto != null) {
                return tokenDto.getRoleCodeList();
            } else {
                return new ArrayList<>();
            }
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    public static boolean isNullOrEmpty(Object[] resource) {
        return resource == null || resource.length == 0;
    }


    public static <T> T fromJson(String json, Class<T> valueType) {
        if (json == null) {
            return null;
        }
        T object = null;
        try {
            object = OBJECT_MAPPER.readValue(json, valueType);
        } catch (IOException e) {
            log.error("", object);
            log.error(e.getMessage(), e);
        }
        return object;
    }

    public static <T> List<T> fromJsonList(String jsonArray, Class<T> valueType) {
        if (Utils.isNullOrEmpty(jsonArray)) {
            return new ArrayList<>();
        }
        try {
            return OBJECT_MAPPER.readValue(jsonArray,
                    OBJECT_MAPPER.getTypeFactory().constructCollectionType(List.class, valueType));
        } catch (IOException e) {
            log.error("Parse JSON list error", e);
            return new ArrayList<>();
        }
    }

    public static String toJson(Object object) {
        String jsonString;
        try {
            jsonString = OBJECT_MAPPER.writeValueAsString(object);
        } catch (JsonProcessingException ex) {
            log.error("ERROR", ex);
            jsonString = "Can't build json from object";
        }

        return jsonString;
    }

    public static <T> T copyProperties(Object src, T dest) {
        if (src != null) {
            modelMapper.map(src, dest);
            return dest;
        }
        return null;
    }

    public static <D, T> D copyProperties(final T entity, Class<D> outClass) {
        return modelMapper.map(entity, outClass);
    }

    public static Long getUserIdLogin() {
        try {
            UserTokenDto tokenDto = (UserTokenDto) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (tokenDto != null) {
                return tokenDto.getUserId();
            } else {
                return null;
            }
        } catch (Exception e) {
            log.error("{}", e);
            return null;
        }
    }

    public static String getUserNameLogin() {
        try {
            UserTokenDto tokenDto = (UserTokenDto) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (tokenDto != null) {
                return tokenDto.getLoginName();
            } else {
                return "unknown user";
            }
        } catch (Exception e) {
            log.error("{}", e);
            return "unknown user";
        }
    }

    public static boolean isNullOrEmpty(String str) {
        return StringUtils.isBlank(str);
    }

    public static <T> List<List<T>> partition(List<T> listId, int size) {
        return ListUtils.partition(listId, size);
    }

    public static boolean isNullObject(Object columnValue) {
        return columnValue == null;
    }

    public static boolean isNullOrEmpty(final Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }

    public static <T> T NVL(T value, T defaultValue) {
        return value == null ? defaultValue : value;
    }

    public static String NVL(String text, String defaultValue) {
        return StringUtils.isBlank(text) ? defaultValue : text.trim();
    }

    public static String NVL(String text) {
        return NVL(text, "");
    }

    public static Integer NVL(Integer number) {
        return NVL(number, 0);
    }

    public static Long NVL(Long number) {
        return NVL(number, 0L);
    }

    public static Double NVL(Double number) {
        return NVL(number, 0d);
    }

    public static BigDecimal NVL(BigDecimal number) {
        return NVL(number, BigDecimal.ZERO);
    }

    public static Date NVL(Date value) {
        return value == null ? Utils.stringToDate(BaseConstants.DEFAULT_DATE) : value;
    }

    /**
     * Format so.
     *
     * @param d So
     * @return Xau
     */
    public static <T> String formatNumber(T d) {
        return d == null ? "" :
                DECIMAL_FORMAT_INT.get().format(d);

    }

    /**
     * Format so.
     *
     * @param d       So
     * @param pattern
     * @return Xau
     */
    public static String formatNumber(Object d, String pattern) {
        if (d == null) {
            return "";
        } else {
            DecimalFormat format = new DecimalFormat(pattern);
            return format.format(d);
        }
    }

    public static boolean isNumeric(String str) {
        return str != null && NumberUtils.isCreatable(str);
    }


    /**
     * Chuyen doi tuong Date thanh doi tuong String.
     *
     * @param date Doi tuong Date
     * @return Xau ngay, co dang dd/MM/yyyy
     */
    public static String formatDate(Date date) {
        return date == null ? "" : DEFAULT_FORMAT_DATE.get().format(date);
    }


    /**
     * Chuyen doi tuong Date thanh doi tuong String
     *
     * @param date          Doi tuong Date
     * @param formatPattern Kieu format ngay thang
     * @return Xau ngay voi kieu format truyen vao
     */
    public static String formatDate(Date date, String formatPattern) {
        if (date == null) {
            return null;
        }
        if (formatPattern == null) {
            return formatDate(date);
        } else {
            SimpleDateFormat dateFormat = new SimpleDateFormat(formatPattern);
            return dateFormat.format(date);
        }
    }

    /**
     * Chuyen doi tuong Date thanh doi tuong String.
     *
     * @param date Doi tuong Date
     * @return Xau ngay, co dang dd/MM/yyyy
     */
    public static Date stringToDate(String date) {
        try {
            return DEFAULT_FORMAT_DATE.get().parse(date);
        } catch (Exception e) {
            return null;
        }
    }

    public static Date stringToDate(String date, String pattern) {
        if (date == null || date.trim().isEmpty()) {
            return null;
        } else {
            try {
                return DateUtils.parseDate(date, pattern);
            } catch (ParseException ex) {
                log.debug(ex.toString());
                return null;
            }
        }
    }

    public static String removeHtml(String html) {
        return html.replaceAll("\\<[^>]*>", "");
    }

    public static String removeSign(String originalName) {
        if (originalName == null) {
            return "";
        }
        String result = originalName.trim();
        for (int i = 0; i < SIGNED_ARR.length; i++) {
            result = result.replaceAll(SIGNED_ARR[i], UNSIGNED_ARR[i]);
        }
        return result;
    }


    private static Date resetTime(Calendar calendar, int dayOfMonth) {
        if (calendar == null) return null;
        if (dayOfMonth > 0) {
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        }
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    public static Date getLastDay(Date date) {
        if (date == null) return null;
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return resetTime(cal, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
    }

    public static Date getFirstDay(Date date) {
        if (date == null) return null;
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return resetTime(cal, 1);
    }

    public static Date truncDate(Date date) {
        if (date == null) return null;
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return resetTime(cal, -1); // -1 = không thay đổi ngày, chỉ reset giờ
    }

    public static List<Long> stringToListLong(String dataList, String separator) {
        if (Utils.isNullOrEmpty(dataList)) {
            return new ArrayList<>();
        }
        String str[] = dataList.split(separator);
        List<Long> results = new ArrayList<>();
        for (String id : str) {
            if (!Utils.isNullOrEmpty(id)) {
                results.add(Long.valueOf(id.trim()));
            }
        }
        return results;
    }

    public static String getFilePathExport(String fileName) {
        String userName = Utils.getUserNameLogin();
        return exportFolder + Utils.formatDate(new Date(), "yyyyMMddHHmmss") + "_" + userName + "_" + fileName;
    }

    public static String getFilePathExportDefault(String fileName) {
        return exportFolder + fileName;
    }

    public static String getExportFolder() {
        return exportFolder;
    }


    public static String readNumber(Long number) {

        int billion = (int) (number / 1000000000);
        number -= billion * 1000000000L;

        int million = (int) (number / 1000000);
        number -= million * 1000000L;

        int thousand = (int) (number / 1000);
        int unit = (int) (number - thousand * 1000);

        String s = "";
        if (billion != 0) {
            if (billion > 999) {
                s = s + readNumber((long) billion) + " tỷ, ";
            } else {
                s = s + readTriple(billion) + " tỷ, ";
            }
        }
        if (million != 0) {
            s = s + readTriple(million) + " triệu, ";
        }
        if (thousand != 0) {
            s = s + readTriple(thousand) + " nghìn, ";
        }
        if (unit != 0) {
            s = s + readTriple(unit);
        }
        s = s.trim();
        if (s.endsWith(",")) {
            s = s.substring(0, s.length() - 1);
        }
        return s;
    }

    private static String readTriple(int number) {
        final String[] HUNDRED_DIGITS = {"", "một trăm", "hai trăm", "ba trăm", "bốn trăm", "năm trăm", "sáu trăm", "bảy trăm", "tám trăm", "chín trăm"};
        final String[] TEN_DIGITS = {" ", " mười ", " hai mươi ", " ba mươi ", " bốn mươi ", " năm mươi ", " sáu mươi ", " bảy mươi ", " tám mươi ", " chín mươi "};
        final String[] UNIT_DIGITS = {"", "một", "hai", "ba", "bốn", "lăm", "sáu", "bảy", "tám", "chín"};
        final String[] UNIT_DIGITS_2 = {"", "mốt", "hai", "ba", "tư", "lăm", "sáu", "bảy", "tám", "chín"};

        int hundred = number / 100;
        number -= hundred * 100;
        int ten = number / 10;
        int unit = number - ten * 10;
        StringBuilder strBuilder = new StringBuilder(HUNDRED_DIGITS[hundred]);
        if (strBuilder.isEmpty()) {
            strBuilder.append(TEN_DIGITS[ten] + (ten <= 1 ? UNIT_DIGITS[unit] : UNIT_DIGITS_2[unit]));
        } else {
            if (ten == 0) {
                strBuilder.append(" linh ");
                strBuilder.append(unit == 5 ? "năm" : UNIT_DIGITS[unit]);
            } else {
                strBuilder.append(TEN_DIGITS[ten] + (ten <= 1 ? UNIT_DIGITS[unit] : UNIT_DIGITS_2[unit]));
            }
        }

        return strBuilder.toString().trim();
    }

    public static <T> List<T> castToList(T obj) {
        List<T> temp = new ArrayList<T>();
        temp.add(obj);
        return temp;
    }

    public static List<Map<String, Object>> castMap(List<Map<String, Object>> listData, String append) {
        List<Map<String, Object>> listResult = new ArrayList<>();
        int index = 1;
        for (Map<String, Object> mapData : listData) {
            Map<String, Object> mapResult = new HashMap<>();
            for (String key : mapData.keySet()) {
                mapResult.put(key + append, mapData.get(key));
            }
            mapResult.put("stt" + append, index++);
            listResult.add(mapResult);
        }
        return listResult;
    }

    public static HttpHeaders getVHRApiHeader(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("x-gravitee-api-key", "045dde32-a764-4a02-a57f-d0c838909b58");
        if (!Utils.isNullOrEmpty(token)) {
            headers.add("Authorization", "Bearer " + token);
        }
        return headers;
    }


    private static String getJsonPropertyName(Field field) {
        Annotation[] annotations = field.getDeclaredAnnotations();
        for (Annotation annotation : annotations) {
            if (annotation instanceof JsonProperty) {
                JsonProperty property = (JsonProperty) annotation;
                return property.value();
            }
        }
        return "";
    }

    private static String getColumnName(Field f) {
        Column column = f.getAnnotation(Column.class);
        if (column != null) {
            return column.name();
        } else {
            return "";
        }
    }

    public static boolean isNullAnyString(String... inputs) {
        if (inputs == null || inputs.length == 0) {
            return true;
        }

        for (String input : inputs) {
            if (StringUtils.isBlank(input)) {
                return true;
            }
        }

        return false;
    }

    public static <T> boolean isNullOrEmpty(List<T> dataList) {
        return dataList == null || dataList.isEmpty();
    }

    public static String intToRoman(int decimal) {
        final String[] ROMAN_CODE = {"M", "CM", "D", "CD", "C", "XC", "L",
                "XL", "X", "IX", "V", "IV", "I"};
        final int[] DECIMAL_VALUE = {1000, 900, 500, 400, 100, 90, 50,
                40, 10, 9, 5, 4, 1};
        if (decimal <= 0 || decimal >= 4000) {
            throw new NumberFormatException("Value outside roman numeral range.");
        }
        StringBuilder roman = new StringBuilder();
        for (int i = 0; i < ROMAN_CODE.length; i++) {
            while (decimal >= DECIMAL_VALUE[i]) {
                decimal -= DECIMAL_VALUE[i];
                roman.append(ROMAN_CODE[i]);
            }
        }
        return roman.toString();
    }

    public static String normalizeFullName(String fullName) {
        fullName = fullName.toLowerCase().trim().replaceAll("\\s+", " ");
        return WordUtils.capitalize(convertCp1258ToUTF8(fullName));
    }

    private static final char[] MARK = new char[]{
            '̀', '́', '̃', '̉', '̣', 'x'
    };
    private static final char[] VOWEL = new char[]{
            'a',
            'ă',
            'â',
            'e',
            'ê',
            'i',
            'o',
            'ô',
            'ơ',
            'u',
            'ư',
            'y',
            'A',
            'Ă',
            'Â',
            'E',
            'Ê',
            'I',
            'O',
            'Ô',
            'Ơ',
            'U',
            'Ư',
            'Y'
    };
    private static final char[] CP1258_TO_UTF8 = new char[]{
            'à', 'á', 'ã', 'ả', 'ạ',
            'ằ', 'ắ', 'ẵ', 'ẳ', 'ặ',
            'ầ', 'ấ', 'ẫ', 'ẩ', 'ậ',
            'è', 'é', 'ẽ', 'ẻ', 'ẹ',
            'ề', 'ế', 'ễ', 'ể', 'ệ',
            'ì', 'í', 'ĩ', 'ỉ', 'ị',
            'ò', 'ó', 'õ', 'ỏ', 'ọ',
            'ồ', 'ố', 'ỗ', 'ổ', 'ộ',
            'ờ', 'ớ', 'ỡ', 'ở', 'ợ',
            'ù', 'ú', 'ũ', 'ủ', 'ụ',
            'ừ', 'ứ', 'ữ', 'ử', 'ự',
            'ỳ', 'ý', 'ỹ', 'ỷ', 'ỵ',
            'À', 'Á', 'Ã', 'Ả', 'Ạ',
            'Ằ', 'Ắ', 'Ẵ', 'Ẳ', 'Ặ',
            'Ầ', 'Ấ', 'Ẫ', 'Ẩ', 'Ậ',
            'È', 'É', 'Ẽ', 'Ẻ', 'Ẹ',
            'Ề', 'Ế', 'Ễ', 'Ể', 'Ệ',
            'Ì', 'Í', 'Ĩ', 'Ỉ', 'Ị',
            'Ò', 'Ó', 'Õ', 'Ỏ', 'Ọ',
            'Ồ', 'Ố', 'Ỗ', 'Ổ', 'Ộ',
            'Ờ', 'Ớ', 'Ỡ', 'Ở', 'Ợ',
            'Ù', 'Ú', 'Ũ', 'Ủ', 'Ụ',
            'Ừ', 'Ứ', 'Ữ', 'Ử', 'Ự',
            'Ỳ', 'Ý', 'Ỹ', 'Ỷ', 'Ỵ'
    };

    public static String convertCp1258ToUTF8(String input) {
        int i = 1;
        while (i < input.length()) {
            char currentChar = input.charAt(i);
            int markIndex = MARK.length - 2;
            while ((markIndex >= 0) && (MARK[markIndex] != currentChar)) {
                markIndex--;
            }
            if (markIndex >= 0) {
                char previousChar = input.charAt(i - 1);
                for (int vowelIndex = 0; vowelIndex < VOWEL.length; vowelIndex++) {
                    if (previousChar == VOWEL[vowelIndex]) {
                        input = input.substring(0, i - 1) + CP1258_TO_UTF8[vowelIndex * (MARK.length - 1) + markIndex] + input.substring(i + 1);
                        break;
                    }
                }
            }
            i++;
        }
        return input;
    }

    public static String join(String separator, String... obj) {
        List<String> strList = new ArrayList<>();
        for (String str : obj) {
            if (StringUtils.isNotBlank(str)) {
                strList.add(str);
            }
        }
        if (isNullOrEmpty(strList)) {
            return StringUtils.EMPTY;
        }
        return StringUtils.join(strList, separator);
    }

    public static String join(String separator, List<Object> obj) {
        List<Object> strList = new ArrayList<>();
        for (Object str : obj) {
            if (obj != null) {
                strList.add(str);
            }
        }
        if (isNullOrEmpty(strList)) {
            return StringUtils.EMPTY;
        }
        return StringUtils.join(strList, separator);
    }

    public static String join(String separator, Collection<String> collection) {
        if (collection == null || collection.isEmpty()) {
            return StringUtils.EMPTY;
        }
        List<String> strList = new ArrayList<>();
        collection.forEach(item -> {
            if (StringUtils.isNotBlank(item)) {
                strList.add(item);
            }
        });
        if (isNullOrEmpty(strList)) {
            return StringUtils.EMPTY;
        }
        return StringUtils.join(strList, separator);
    }

    public static boolean isValidTaxNo(String taxNo) {
        if (Utils.isNullOrEmpty(taxNo)) {// tranh truong hop taxNo k bat buoc nhap
            return true;
        }
        if (taxNo.length() != 10) {
            return false;
        }

        if (!taxNo.matches("\\d+")) {
            return false;
        }

        int i = 0;
        int n1 = Integer.valueOf(taxNo.substring(i++, i)) * 31;
        int n2 = Integer.valueOf(taxNo.substring(i++, i)) * 29;
        int n3 = Integer.valueOf(taxNo.substring(i++, i)) * 23;
        int n4 = Integer.valueOf(taxNo.substring(i++, i)) * 19;
        int n5 = Integer.valueOf(taxNo.substring(i++, i)) * 17;
        int n6 = Integer.valueOf(taxNo.substring(i++, i)) * 13;
        int n7 = Integer.valueOf(taxNo.substring(i++, i)) * 7;
        int n8 = Integer.valueOf(taxNo.substring(i++, i)) * 5;
        int n9 = Integer.valueOf(taxNo.substring(i++, i)) * 3;
        int n10 = Integer.valueOf(taxNo.substring(i++, i));
        int div = (n1 + n2 + n3 + n4 + n5 + n6 + n7 + n8 + n9) % 11;
        int checkValue = Math.abs(10 - div);
        return checkValue == n10;
    }

    public static boolean isValidPhoneNumber(String phoneNumber) {
        if (isNullOrEmpty(phoneNumber)) {
            return true;
        }

        ConstraintValidator<PhoneNumberFormat, String> validator = new PhoneNumberFormatValidator();
        return validator.isValid(phoneNumber, null);
    }

    public static Date getMaxDate() {
        return Utils.stringToDate("31/12/9999");
    }

    public static String getExtension(String fileName) {
        if (isNullOrEmpty(fileName)) {
            return "";
        } else {
            int index = fileName.lastIndexOf(".");
            return fileName.substring(index + 1);
        }
    }

    public static <D, T> List<D> mapAll(final Collection<T> entityList, Class<D> outCLass) {
        if (Utils.isNullOrEmpty(entityList)) {
            return null;
        } else {
            return entityList.stream()
                    .map(entity -> copyProperties(entity, outCLass))
                    .collect(Collectors.toList());
        }
    }

    public static Long getDataFromMap(Map<String, Object> map, String key) {
        Object empId = map.get(key);
        if (empId instanceof BigDecimal) {
            return ((BigDecimal) empId).longValue(); // Chuyển BigDecimal thành Long
        } else if (empId instanceof Long) {
            return (Long) empId; // Nếu là Long, giữ nguyên
        }
        return 0L;
    }

    public static String getStringFromMap(Map<String, Object> map, String key) {
        Object object = map.get(key);
        if (object != null) {
            return object.toString();
        }
        return "";
    }

    public static boolean compareDate(Date date1, Date date2) {
        if (date1 == null && date2 == null) {
            return true;
        } else if (date1 != null && date2 != null) {
            date1 = removeTime(date1);
            date2 = removeTime(date2);
            return date1.equals(date2);
        } else {
            return false;
        }
    }

    public static Boolean compareDate(Date earlierDate, Date laterDate, Boolean equal) {
        Calendar earlierDateCal = Calendar.getInstance();
        Calendar laterDateCal = Calendar.getInstance();

        earlierDateCal.setTime(earlierDate);
        laterDateCal.setTime(laterDate);

        if (equal) {
            return laterDateCal.after(earlierDateCal) || laterDateCal.equals(earlierDateCal);
        } else {
            return laterDateCal.after(earlierDateCal);
        }
    }

    public static Date removeTime(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    public static void validateDate(Date fromDate, Date toDate) throws BaseAppException {
        if (fromDate != null && toDate != null && !Utils.compareDate(fromDate, toDate, true)) {
            throw new BaseAppException("ERROR_DATE_INPUT", I18n.getMessage("error.dateInput.validate.rangeDateError"));
        }
    }

    public static int daysBetween(Date d1, Date d2) {
        return (int) ((d2.getTime() - d1.getTime()) / (1000 * 60 * 60 * 24));
    }

    public static boolean isConflictDate(Date start1, Date end1, Date start2, Date end2) {
        return (null == end2 || start1.compareTo(end2) <= 0) && (null == end1 || start2.compareTo(end1) <= 0);
    }


    public static String convertDateTimeToString(Date date) {
        if (date == null) {
            return "";
        } else {
            return new SimpleDateFormat("yyyyMMddHHmmss").format(date);
        }
    }

    public static int getMonthByDate(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal.get(Calendar.MONTH) + 1;
    }

    public static int getYearByDate(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal.get(Calendar.YEAR);
    }

    public static int getDaysOfMonth(int month, int year) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month - 1, 1);
        return calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
    }

    public static Date getLastDayOfMonth(int month, int year) {
        if (month > 9) {
            return stringToDate(getDaysOfMonth(month, year) + "/" + month + "/" + year);
        } else {
            return stringToDate(getDaysOfMonth(month, year) + "/0" + month + "/" + year);
        }
    }

    public static Date getLastDayOfMonth(Date date) {
        if (date != null) {
            int month = getMonthByDate(date);
            int year = getYearByDate(date);
            return getLastDayOfMonth(month, year);
        } else {
            return null;
        }
    }

    public static String getCheckSum(Long attachmentId) {
        if (attachmentId == null) {
            return "";
        }
        String salt = Utils.getUserNameLogin() + Utils.formatDate(new Date(), "ddMMyyyy");
        return PlainTextEncoder.encode(attachmentId.toString(), salt);
    }

    public static void disableSslVerification() {
        try {
            TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        public X509Certificate[] getAcceptedIssuers() {
                            return null;
                        }

                        public void checkClientTrusted(X509Certificate[] certs, String authType) {
                        }

                        public void checkServerTrusted(X509Certificate[] certs, String authType) {
                        }
                    }
            };

            // Tạo một SSLContext với TrustManager tùy chỉnh
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

            // Thiết lập URLConnection để bỏ qua xác thực SSL
            HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
                public boolean verify(String hostname, SSLSession session) {
                    return true; // Bỏ qua kiểm tra hostname
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getStrDayOfWeek(Date date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE", Locale.US);
        return dateFormat.format(date);
    }


    public static Pair<StringBuilder, StringBuilder> compareEntity(Object entityOld, Object entityNew) {

        StringBuilder strBuilderOld = new StringBuilder();
        StringBuilder strBuilderNew = new StringBuilder();
        if (entityNew == null) {
            return new MutablePair<>(strBuilderOld, strBuilderNew);
        }
        Field fields[] = entityNew.getClass().getDeclaredFields();

        try {
            Table table = entityNew.getClass().getAnnotation(Table.class);
            String tableName = table.name().toLowerCase();
            for (Field f : fields) {
                f.setAccessible(true);
                String columnName = f.getName();
                if (List.of("created_by", "created_time", "modified_by", "modified_time").contains(columnName)) {
                    continue;
                }
                Object valueOld = entityOld == null ? null : f.get(entityOld);
                Object valueNew = f.get(entityNew);

                if (valueOld instanceof Date) {
                    valueOld = formatDate((Date) valueOld, BaseConstants.COMMON_DATETIME_FORMAT).replace("00:00:00", "").trim();
                }

                if (valueNew instanceof Date) {
                    valueNew = formatDate((Date) valueNew, BaseConstants.COMMON_DATETIME_FORMAT).replace("00:00:00", "").trim();
                }

                if ((valueOld != null && !valueOld.equals(valueNew)) || (valueNew != null && !valueNew.equals(valueOld))) {
                    strBuilderOld.append(I18n.getMessage("logAction." + tableName + "." + columnName)).append(": ").append(valueOld == null ? "\n" : valueOld + "\n");
                    strBuilderNew.append(I18n.getMessage("logAction." + tableName + "." + columnName)).append(": ").append(valueNew == null ? "\n" : valueNew + "\n");
                }
            }

        } catch (IllegalAccessException e) {
            log.error(e.getMessage(), e);
        }
        return new MutablePair<>(strBuilderOld, strBuilderNew);

    }

    public static Pair<StringBuilder, StringBuilder> compareEntity(Object entityOld, Object entityNew, List<AttributeRequestDto> oldAttributes, List<AttributeRequestDto> newAttributes) {
        oldAttributes = Objects.requireNonNullElse(oldAttributes, new ArrayList<>());
        newAttributes = Objects.requireNonNullElse(newAttributes, new ArrayList<>());

        Pair<StringBuilder, StringBuilder> pair = compareEntity(entityOld, entityNew);
        if (!Utils.isNullOrEmpty(oldAttributes) || !Utils.isNullOrEmpty(newAttributes)) {
            StringBuilder strBuilderOld = pair.getLeft();
            StringBuilder strBuilderNew = pair.getRight();
            for (AttributeRequestDto attributeNew : newAttributes) {
                String valueNew = attributeNew.getAttributeValue();
                String valueOld = null;
                Optional<AttributeRequestDto> attributeOld = oldAttributes.stream().filter(item -> StringUtils.equals(item.getAttributeCode(), attributeNew.getAttributeCode())).findFirst();
                if (attributeOld.isPresent()) {
                    valueOld = attributeOld.get().getAttributeValue();
                }

                if ((valueOld != null && !valueOld.equals(valueNew)) || (valueNew != null && !valueNew.equals(valueOld))) {
                    strBuilderOld.append(attributeNew.getAttributeName()).append(": ").append(valueOld == null ? "\n" : valueOld + "\n");
                    strBuilderNew.append(attributeNew.getAttributeName()).append(": ").append(valueNew == null ? "\n" : valueNew + "\n");
                }
            }
            return new MutablePair<>(strBuilderOld, strBuilderNew);
        } else {
            return pair;
        }
    }


    public static boolean checkMagicHeaderFile(byte[] fileBytes, byte[] magicNumber) {
        if (fileBytes.length < magicNumber.length) {
            return false;
        }

        for (int i = 0; i < magicNumber.length; i++) {
            if (fileBytes[i] != magicNumber[i]) {
                return false;
            }
        }

        return true;
    }

    public static Double min(Double... values) {
        if (values == null || values.length == 0) {
            return null; // hoặc 0d nếu bạn muốn mặc định
        }

        Double min = Utils.NVL(values[0], Double.MAX_VALUE);
        for (Double value : values) {
            if (Utils.NVL(value, Double.MAX_VALUE) < min) {
                min = value;
            }
        }
        return Utils.NVL(min, 0d); // đảm bảo không trả về null
    }


    /**
     * So sánh 2 ngày nếu date1 <= date2 thì trả về true, ngược lại false
     *
     * @param date1
     * @param date2
     * @return
     */
    public static boolean compareDateLessThanOrEqual(Date date1, Date date2, boolean isEqual) {
        if (date1 == null && date2 == null) {
            return true;
        } else if (date1 != null && date2 != null) {
            date1 = removeTime(date1);
            date2 = removeTime(date2);

            if (isEqual) {
                return date1.getTime() <= date2.getTime();
            } else {
                return date1.getTime() < date2.getTime();
            }
        } else if (date1 != null && date2 == null) {
            return true;
        }

        return false;
    }

    public static Date getToDateContract(Date fromDate, Integer duration) {
        if (Utils.isNullObject(duration) || fromDate == null) {
            return null;
        }
        return DateUtils.addDays(DateUtils.addMonths(fromDate, duration), -1);
    }

    public static boolean isValidEmail(String emailAddress) {
        if (isNullOrEmpty(emailAddress)) {
            return true;
        }

        ConstraintValidator<EmailFormat, String> validator = new EmailFormatValidator();
        return validator.isValid(emailAddress, null);
    }

    public static int calculateMonthsBetween(Date startDate, Date endDate) throws ParseException {
        Calendar startCalendar = Calendar.getInstance();
        startCalendar.setTime(startDate);

        Calendar endCalendar = Calendar.getInstance();
        endCalendar.setTime(endDate);

        int yearDiff = endCalendar.get(Calendar.YEAR) - startCalendar.get(Calendar.YEAR);
        int monthDiff = endCalendar.get(Calendar.MONTH) - startCalendar.get(Calendar.MONTH);

        return yearDiff * 12 + monthDiff;
    }

    public static double round(double value, int decimalPlaces) {
        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(decimalPlaces, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

}
