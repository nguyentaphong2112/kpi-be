package vn.hbtplus.models.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PagePdfDto {
    private String name;
    private String image;
    private String nameSlug;
}
