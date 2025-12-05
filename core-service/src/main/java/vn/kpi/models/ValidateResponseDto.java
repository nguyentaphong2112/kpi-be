package vn.kpi.models;

import lombok.Data;

@Data
public class ValidateResponseDto {
    private String type;
    private String code;
    private String message;

    public ValidateResponseDto() {
        this.type = "OK";
        this.code = "OK";
    }

    public ValidateResponseDto(String type, String code, String message) {
        this.type = type;
        this.code = code;
        this.message = message;
    }
}
