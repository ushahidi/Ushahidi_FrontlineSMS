package com.ushahidi.plugins.mapping.data.domain;

import java.awt.Color;

import javax.persistence.*;

import net.frontlinesms.data.EntityField;

/**
 * Domain object for a category.
 * @author Emmanuel Kala
 *
 */
@Entity
@Table(uniqueConstraints={@UniqueConstraint(columnNames={"frontendId","mappingSetup_id"})})
public class Category {
	
//> COLUMN NAME CONSTANTS
	/** Column name {@link #frontendId} */
	private static final String FIELD_FRONTEND_ID = "frontendId";
	/** Column name for {@link #title} */
	private static final String FIELD_TITLE = "title";
	/** Column name for {@link #description} */
	private static final String FIELD_DESCRIPTION = "description";
	/** Column name for {@link #color} */
	private static final String FIELD_COLOR = "color";
	
//>	ENTITY FIELDS
	/** Details of the fields that this class has*/
	public enum Field implements EntityField<Category>{
		/** Field mapping for {@link Category#frontendId} */
		FRONTEND_ID(FIELD_FRONTEND_ID),
		/** Field mapping for {@link Category#title} */
		TITLE(FIELD_TITLE),
		/** Field mapping for {@link Category#description} */
		DESCRIPTION(FIELD_DESCRIPTION),
		/** Field mapping for {@link Category#color} */
		COLOR(FIELD_COLOR),
		/** Field mapping for {@link Category#mappingSetup} */
		MAPPING_SETUP("mappingSetup");
		/** name of a field */
		private final String fieldName;		
		/**
		 * Creates a new {@link Field}
		 * @param fieldName name of the field
		 */
		Field(String fieldName){this.fieldName = fieldName; }
		/** @see EntityField#getFieldName() */
		public String getFieldName(){ return this.fieldName; }
	}
	
//>	INSTANCE PROPERTIES
	/** Unique id for this entity */
	@Id @GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(unique=true,nullable=false,updatable=false)
	private long id;
	
	@Column(name=FIELD_FRONTEND_ID)
	private long frontendId;
	
	/** Title of this category */
	@Column(name=FIELD_TITLE)
	private String title;
	
	/** Description of this category */
	@Column(name=FIELD_DESCRIPTION)
	private String description;
	
	@ManyToOne
	private MappingSetup mappingSetup;
	
	private Color color;
	
	/**
	 * Sets the category id
	 * @param id
	 */
	public void setId(long id){
		this.id = id;
	}
	
	/**
	 * Gets the category id
	 * @return {@link #id}
	 */
	public long getId(){
		return id;
	}
	
	/**
	 * Sets the id fetched from the frontend i.e. the online instance
	 * @param id
	 */
	public void setFrontendId(long id){
		this.frontendId = id;
	}
	
	/**
	 * 
	 * @return {@link #frontendId}
	 */
	public long getFrontendId(){
		return this.frontendId;
	}
	
	/**
	 * Sets the category's title
	 * @param title
	 */
	public void setTitle(String title){
		this.title = title;
	}
	
	/**
	 * Gets the category's title
	 * @return {@link #title}
	 */
	public String getTitle(){
		return title;
	}
	
	/**
	 * Sets the category's description
	 * @param description
	 */
	public void setDescription(String description){
		this.description = description;
	}
	
	/**
	 * Get's the category description
	 * @return {@link #description}
	 */
	public String getDescription(){
		return description;
	}
	
	public void setMappingSetup(MappingSetup setup){
		this.mappingSetup = setup;
	}
	
	public MappingSetup getMappingSetup(){
		return this.mappingSetup;
	}
	
	public Color getColor() {
		return this.color;
	}
	
	public void setColor(Color color) { 
		this.color = color;
	}
 }
