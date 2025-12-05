package vn.hbtplus.insurance.models.response;

import lombok.Data;

@Data
public class CategoryResponse {
    private String value;
    private String name;
    private String code;
    private Integer orderNumber;
    private String description;
    private Long categoryId;
}
