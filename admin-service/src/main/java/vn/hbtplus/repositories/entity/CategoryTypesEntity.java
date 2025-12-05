package vn.hbtplus.repositories.entity;

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
@Table(name = "sys_category_types")
public class CategoryTypesEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "category_type_id")
    private Long categoryTypeId;

    @Column(name = "code")
    private String code;

    @Column(name = "name")
    private String name;

    @Column(name = "attributes")
    private String attributes;

    @Column(name = "is_auto_increase")
    private String isAutoIncrease;

    @Column(name = "order_number")
    private Long orderNumber;

    @Column(name = "group_type")
    private String groupType;
}
