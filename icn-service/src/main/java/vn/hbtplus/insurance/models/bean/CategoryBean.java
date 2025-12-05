package vn.hbtplus.insurance.models.bean;

import lombok.Data;

import java.util.Map;

@Data
public class CategoryBean {
    private Long categoryId;
    private Long value;
    private String name;
    private String code;

    private Map<String, String> attributes;

}
