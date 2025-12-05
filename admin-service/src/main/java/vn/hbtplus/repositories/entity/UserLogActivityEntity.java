package vn.hbtplus.repositories.entity;

import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@Table(name = "sys_user_log_activity")
public class UserLogActivityEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "user_log_activity_id")
    private Long userLogActivityId;

    @Column(name = "login_name")
    private String loginName;

    @Column(name = "method")
    private String method;

    @Column(name = "data")
    private String data;

    @Column(name = "uri")
    private String uri;

    @Column(name = "ip_address")
    private String ipAddress;
}
