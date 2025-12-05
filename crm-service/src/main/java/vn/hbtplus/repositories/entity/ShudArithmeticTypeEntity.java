package vn.hbtplus.repositories.entity;

import lombok.Data;

import javax.persistence.*;
import java.util.Date;

@Data
@Entity
@Table(name = "shud_arithmetic_types")
public class ShudArithmeticTypeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "name_slug")
    private String nameSlug;

    @Column(name = "person_type")
    private String personType;

    @Column(name = "weight")
    private Integer weight;

    @Column(name = "created_at")
    @Temporal(javax.persistence.TemporalType.DATE)
    private Date createdAt;

    @Column(name = "updated_at")
    @Temporal(javax.persistence.TemporalType.DATE)
    private Date updatedAt;
}
