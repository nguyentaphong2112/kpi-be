package vn.kpi.repositories.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Date;

@Data
@Entity
@Table(name = "sys_user_login_history")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserLoginHistoryEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "user_login_history_id")
    private Long userLoginHistoryId;

    @Column(name = "login_name")
    private String loginName;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "login_time")
    private Date loginTime;

    @Column(name = "ip_address")
    private String ipAddress;
}
