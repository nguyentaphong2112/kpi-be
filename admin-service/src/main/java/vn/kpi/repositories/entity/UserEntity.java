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
@Table(name = "sys_users")
public class UserEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "login_name")
    private String loginName;
    @Column(name = "full_name")
    private String fullName;
    @Column(name = "email")
    private String email;
    @Column(name = "mobile_number")
    private String mobileNumber;
    @Column(name = "employee_code")
    private String employeeCode;
    @Column(name = "password")
    private String password;

    @Column(name = "status")
    private String status;

    @Column(name = "id_no")
    private String idNo;

    public interface STATUS {
        String ACTIVE = "ACTIVE";
        String INACTIVE = "INACTIVE";
    }
}
