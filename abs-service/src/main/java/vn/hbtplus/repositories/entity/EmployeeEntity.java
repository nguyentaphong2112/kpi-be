package vn.hbtplus.repositories.entity;

import lombok.Data;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Data
@Entity
@Table(name = "hr_employees")
public class EmployeeEntity {
    @Id
    @Basic(optional = false)
    @Column(name = "employee_id")
    private Long employeeId;
    @Column(name = "full_name")
    private String fullName;
    @Column(name = "employeeCode")
    private String employeeCode;
}
