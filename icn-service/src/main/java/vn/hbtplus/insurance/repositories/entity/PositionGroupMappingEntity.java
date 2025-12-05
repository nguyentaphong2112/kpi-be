package vn.hbtplus.insurance.repositories.entity;

import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@Table(name = "hr_position_group_mappings")
public class PositionGroupMappingEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "position_group_mapping_id")
    private Long positionGroupMappingId;

    @Column(name = "org_id")
    private Long orgId;

    @Column(name = "job_id")
    private Long jobId;

    @Column(name = "group_type")
    private String groupType;

    @Column(name = "value")
    private String value;
}
