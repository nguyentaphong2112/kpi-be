package vn.kpi.repositories.entity;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Data
@Entity
@Table(name = "sys_config_pages")
public class ConfigPageEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "config_page_id")
    private Long configPageId;
    @Column(name = "url")
    private String url;
    @Column(name = "report_codes")
    private String reportCodes;
    @Column(name = "type")
    private String type;
    
}
