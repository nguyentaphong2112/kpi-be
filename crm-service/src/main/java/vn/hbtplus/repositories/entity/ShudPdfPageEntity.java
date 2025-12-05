package vn.hbtplus.repositories.entity;

import lombok.Data;

import javax.persistence.*;
import java.util.Date;

@Data
@Entity
@Table(name = "shud_pdf_pages")
public class ShudPdfPageEntity {
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

    @Column(name = "number")
    private Integer number;

    @Column(name = "image")
    private String image;

    @Column(name = "created_at")
    @Temporal(javax.persistence.TemporalType.DATE)
    private Date createdAt;

    @Column(name = "updated_at")
    @Temporal(javax.persistence.TemporalType.DATE)
    private Date updatedAt;
}
