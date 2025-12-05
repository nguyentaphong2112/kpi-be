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
 * Lop entity ung voi bang wb_material_type
 * @author hailv
 * @since 1.0
 * @version 1.0
 */

@Data
@Entity
@Table(name = "wb_material_type")
public class WbMaterialTypeEntity extends BaseEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Basic(optional = false)
	@Column(name = "wb_material_type_id")
	private Long wbMaterialTypeId;
	
	@Column(name = "parent_id")
	private Long parentId;
	
	@Column(name = "code")
	private String code;

	@Column(name = "name")
	private String name;

	@Column(name = "symbol")
	private String symbol;

	@Column(name = "description")
	private String description;
}
