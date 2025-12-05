package vn.hbtplus.databinds;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import lombok.extern.slf4j.Slf4j;
import vn.hbtplus.utils.Utils;

import java.io.IOException;

@Slf4j
public class TrimStringDeSerializer extends JsonDeserializer<String> {

    @Override
    public String deserialize(JsonParser p, DeserializationContext ctx)
            throws IOException {
        String str = p.getText();
        try {
            if (!Utils.isNullOrEmpty(str)) {
                return Utils.convertCp1258ToUTF8(str.trim().replaceAll("  ", ""));
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }
}