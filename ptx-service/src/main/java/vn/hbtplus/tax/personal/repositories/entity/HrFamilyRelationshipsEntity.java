package vn.hbtplus.tax.personal.repositories.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import vn.hbtplus.repositories.entity.BaseEntity;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

/**
 * Autogen class Entity: Create Entity For Table Name Hr_family_relationships
 *
 * @author ToolGen
 * @date Sun Mar 20 21:42:07 ICT 2022
 */
@Data
@NoArgsConstructor
@Entity
@Table(name = "hr_family_relationships")
public class HrFamilyRelationshipsEntity extends BaseEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "family_relationship_id")
    Long familyRelationshipId;

    @Column(name = "employee_id")
    Long employeeId;

    @Column(name = "full_name")
    String fullName;

    @Column(name = "relation_type_code")
    String relationTypeCode;

    @Column(name = "day_of_birth")
    Long dayOfBirth;

    @Column(name = "month_of_birth")
    Long monthOfBirth;

    @Column(name = "year_of_birth")
    Long yearOfBirth;

    @Temporal(TemporalType.DATE)
    @Column(name = "date_of_birth")
    Date dateOfBirth;

    @Column(name = "job")
    String job;

    @Column(name = "work_organization")
    String workOrganization;

    @Column(name = "note")
    String note;

    @Column(name = "policy_type_code")
    String policyTypeCode;

    @Column(name = "current_address")
    String currentAddress;

    @Column(name = "is_in_company")
    Integer isInCompany;

    @Column(name = "reference_employeee_id")
    Long referenceEmployeeId;

    @Column(name = "personal_id_number")
    String personalIdNumber;

    @Column(name = "tax_number")
    String taxNumber;

    @Column(name = "province_code")
    String provinceCode;

    @Column(name = "district_code")
    String districtCode;

    @Column(name = "ward_code")
    String wardCode;

    @Temporal(TemporalType.DATE)
    @Column(name = "from_date")
    Date fromDate;

    @Temporal(TemporalType.DATE)
    @Column(name = "to_date")
    Date toDate;

    @Column(name = "relation_status_code")
    private String relationStatusCode;

    @Column(name = "is_household_owner")
    private Integer isHouseholdOwner;

    @Column(name = "phone_number")
    private String phoneNumber;
}
