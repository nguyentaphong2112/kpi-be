package vn.hbtplus.models.response;

import lombok.Data;
import lombok.NoArgsConstructor;
import vn.hbtplus.models.dto.EmployeeDto;
import vn.hbtplus.models.dto.OrganizationDto;
import vn.hbtplus.utils.Utils;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class OrgHierarchyResponse {
    private String hierarchyName;
    private boolean isEmployee;

    private List<DetailBean> details = new ArrayList<>();

    public void addDetails(List<EmployeeDto> employeeDtos, boolean isGroupByJob) {
        employeeDtos.forEach(dto -> {
            details.add(new DetailBean(dto, isGroupByJob));
        });

    }

    public void addOrgDetails(List<OrganizationDto> organizationDtos) {
        organizationDtos.forEach(dto -> {
            details.add(new DetailBean(dto));
        });
    }

    @Data
    @NoArgsConstructor
    public static class DetailBean {
        private Long id;
        private String name;

        public DetailBean(EmployeeDto dto, boolean isGroupByJob) {
            this.id = dto.getEmployeeId();
            if (isGroupByJob) {
                this.name = Utils.join(", ", dto.getEduPromotionName(), dto.getEduMajorLevelName());
            } else {
                this.name = Utils.join(", ", dto.getJobName(), dto.getEduPromotionName(), dto.getEduMajorLevelName());
            }
            this.name = Utils.isNullOrEmpty(this.name) ? dto.getFullName() : this.name + ": " + dto.getFullName();
        }

        public DetailBean(OrganizationDto dto) {
            this.id = dto.getOrganizationId();
            this.name = dto.getName();
        }
    }
}
