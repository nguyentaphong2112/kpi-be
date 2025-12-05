package vn.hbtplus.models.dto;

import lombok.Data;

@Data
public class FeeRatio {
    private Long id;
    private Double careRatio;
    private Double referralRatio;
    private Double welfareRatio;
}
