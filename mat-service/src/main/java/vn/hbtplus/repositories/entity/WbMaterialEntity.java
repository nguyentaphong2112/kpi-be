package vn.hbtplus.repositories.entity;

import lombok.Data;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Lop entity ung voi bang wb_material
 * @author hailv
 * @since 1.0
 * @version 1.0
 */

@Data
@Entity
@Table(name = "wb_material")
public class WbMaterialEntity  extends BaseEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Basic(optional = false)
	@Column(name = "wb_material_id")
	private Long wbMaterialId;
	
	@Column(name = "code")
	private String code;

	@Column(name = "name")
	private String name;

	@Column(name = "barcode")
	private String barcode;

	@Column(name = "seri")
	private String seri;
	
	@Column(name = "number")
	private String number;
	
	@Column(name = "wb_material_type_id")
	private Long wbMaterialTypeId;
	
	@Column(name = "company_id")
	private Long companyId;
	
	@Column(name = "country_id")
	private Long countryId;
	
	@Column(name = "unit_id")
	private Long unitId;
	
	@Column(name = "description")
	private String description;
}
