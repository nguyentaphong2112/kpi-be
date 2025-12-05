package vn.kpi.utils;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * @author tudd
 */
public class StrimDeSerializer extends JsonDeserializer<String> {
    private static final Logger LOGGER = LoggerFactory.getLogger(StrimDeSerializer.class);

    @Override
    public String deserialize(JsonParser p, DeserializationContext ctx)
            throws IOException {
        String str = p.getText();
        try {
            if (!Utils.isNullOrEmpty(str)) {
                return Utils.convertCp1258ToUTF8(str.trim());
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return null;
    }
}
