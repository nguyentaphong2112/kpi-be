package vn.kpi.repositories.entity;

import lombok.Data;
import org.springframework.data.domain.AuditorAware;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

@Data
@Entity
@Table(name = "ddl_changes_log")
@EntityListeners(AuditorAware.class)
public class DdlChangeLogEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Long id;

    @Column(name = "event_time")
    private Date eventTime;
    @Column(name = "userhost")
    private String userhost;
    @Column(name = "db_name")
    private String dbName;
    @Column(name = "command")
    private String command;
}
