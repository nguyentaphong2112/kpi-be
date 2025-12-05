/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vn.kpi.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.kpi.utils.StrimDeSerializer;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.List;

@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BaseRejectRequest {
    private Long id;
    private List<Long> listId;
    @NotBlank
    @Size(max = 500)
    @JsonDeserialize(using = StrimDeSerializer.class)
    private String rejectReason;

}
