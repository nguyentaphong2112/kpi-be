package vn.hbtplus.models.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.hbtplus.models.dto.AdmUsersDTO;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AdmUsersResponse {
    private AdmUsersDTO data;
}
