package vn.hbtplus.repositories.entity;

import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@Table(name = "hr_contract_types")
public class HrContractTypesEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "contract_type_id")
    private Long contractTypeId;

    @Column(name = "code")
    private String code;

    @Column(name = "name")
    private String name;

    @Column(name = "classify_code")
    private String classifyCode;

    @Column(name = "order_number")
    private Integer orderNumber;

    @Column(name = "emp_type_id")
    private Long empTypeId;

}
