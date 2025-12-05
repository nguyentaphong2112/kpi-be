package vn.kpi.repositories.entity;


import lombok.Data;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Data
@Entity
@Table(name = "sys_config_object_attributes")
public class ConfigObjectAttributeEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "config_object_attribute_id")
    private Long configObjectAttributeId;

    @Column(name = "table_name")
    private String tableName;

    @Column(name = "function_code")
    private String functionCode;

    @Column(name = "attributes")
    private String attributes;

    @Column(name = "name")
    private String name;

    @Column(name = "note")
    private String note;
}
