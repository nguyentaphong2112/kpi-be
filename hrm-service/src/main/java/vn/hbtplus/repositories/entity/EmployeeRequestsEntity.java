package vn.hbtplus.repositories.entity;

import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@Table(name = "hr_employee_requests")
public class EmployeeRequestsEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "employee_request_id")
    private Long employeeRequestId;

    @Column(name = "employee_id")
    private Long employeeId;
    @Column(name = "request_type")
    private String requestType;
    @Column(name = "status")
    private String status;
    @Column(name = "content")
    private String content;

}