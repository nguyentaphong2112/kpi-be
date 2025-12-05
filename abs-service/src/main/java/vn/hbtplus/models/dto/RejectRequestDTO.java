package vn.hbtplus.models.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RejectRequestDTO {
    private List<Long> listId;
    private String rejectReason;
}
