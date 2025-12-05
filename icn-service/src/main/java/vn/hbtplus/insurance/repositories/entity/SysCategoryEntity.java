package vn.hbtplus.insurance.repositories.entity;

import lombok.Data;
import vn.hbtplus.repositories.entity.BaseEntity;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Data
@Entity
@Table(name = "sys_categories")
public class SysCategoryEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @NotNull
    @Column(name = "category_id")
    private Long categoryId;
    @Column(name = "value")
    private String value;
    @Column(name = "code")
    private String code;
    @Column(name = "name")
    private String name;
    @Column(name = "category_type")
    private String categoryType;

    @Column(name = "order_number")
    private Integer orderNumber;
    @Column(name = "is_deleted")
    private String isDeleted;

}
