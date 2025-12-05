package vn.hbtplus.models.request;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Map;

@Data
@Builder
@ToString
public class CardTemplateRequest {
    BufferedImage baseImage;
    List<Map<String, String>> listMapParams;
    List<String> listContent;
    int top;
    int left;
    int right;
    int lineHeight;
    int paragraphHeight;
    boolean isTextCenter;
    Font font;
    Color color;
}
