package vn.hbtplus.insurance.models.bean;

import lombok.Data;

@Data
public class CategoryAttributeBean {
    private Long categoryId;
    private String attributeCode;
    private String value;
}
