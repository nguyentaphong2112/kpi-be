package vn.hbtplus.models.dto;

import lombok.Data;

import java.util.Date;

@Data
public class OrderDetailDto {
    private Long customerReceiveId;
    private Long referralFeePayed;
    private Long welfareFeePayed;
    private Long customerIntroducerId;
    private Long customerId;
    private Long orderId;
    private Long productId;
    private Long introducerAmount;
    private Long caregiverAmount;
    private Long welfareRecipientAmount;
    private Long orderAmount;
    private Long careFeePayed;
    private Long receiverId;
    private Long welfareRecipientId;
    private Date paymentDate;
}
