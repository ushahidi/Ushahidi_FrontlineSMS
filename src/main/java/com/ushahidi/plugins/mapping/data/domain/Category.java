package com.ushahidi.plugins.mapping.data.domain;

import java.awt.Color;
import java.io.Serializable;

import javax.persistence.*;

import net.frontlinesms.data.EntityField;

/**
 * Domain object for a category.
 * @author Emmanuel Kala
 *
 */
@Entity
@Table(uniqueConstraints={@UniqueConstraint(columnNames={"category_id","mappingSetup_id"})})
public class Category implements Serializable {
	
	private static final long serialVersionUID = 1L;
	//> COLUMN NAME CONSTANTS
	/** Column name {@link #id} */
	private static final String FIELD_ID = "category_id";
	/** Column name {@link #serverId} */
	private static final String FIELD_SERVER_ID = "serverId";
	/** Column name for {@link #title} */
	private static final String FIELD_TITLE = "title";
	/** Column name for {@link #description} */
	private static final String FIELD_DESCRIPTION = "description";
	/** Column name for {@link #color} */
	private static final String FIELD_COLOR = "color";
	/** Column name for {@link #mappingSetup} */
	private static final String FIELD_MAPPING="mappingSetup";
	/** Column name for {@link #mappingSetup} */
	private static final String FIELD_MAPPING_ID="mappingSetup_id";
	
//>	ENTITY FIELDS
	/** Details of the fields that this class has*/
	public enum Field implements EntityField<Category>{
		/** Field mapping for {@link Category#id} */
		ID(FIELD_ID),
		/** Field mapping for {@link Category#servedId} */
		SERVER_ID(FIELD_SERVER_ID),
		/** Field mapping for {@link Category#title} */
		TITLE(FIELD_TITLE),
		/** Field mapping for {@link Category#description} */
		DESCRIPTION(FIELD_DESCRIPTION),
		/** Field mapping for {@link Category#color} */
		COLOR(FIELD_COLOR),
		/** Field mapping for {@link Category#mappingSetup} */
		MAPPING_SETUP(FIELD_MAPPING),
		/** Field mapping for {@link Category#mappingSetup} */
		MAPPING_SETUP_ID(FIELD_MAPPING_ID);
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
	@Id 
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name=FIELD_ID, unique=true, nullable=false, updatable=false)
	private long id;
	
	@Column(name=FIELD_SERVER_ID, nullable=true)
	private long serverId;
	
	/** Title of this category */
	@Column(name=FIELD_TITLE)
	private String title;
	
	/** Description of this category */
	@Column(name=FIELD_DESCRIPTION)
	private String description;
	
	@ManyToOne
	private MappingSetup mappingSetup;
	
	@Column(name=FIELD_COLOR)
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
	public void setServerId(long serverId){
		this.serverId = serverId;
	}
	
	/**
	 * 
	 * @return {@link #serverId}
	 */
	public long getServerId(){
		return this.serverId;
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
	
	public String getDisplayName(){
		return (description != null && description.trim().equalsIgnoreCase(title.trim()) == false) 
			? String.format("%s (%s)", title, description) 
			: title;
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
	
	public void setMappingSetup(MappingSetup mappingSetup){
		this.mappingSetup = mappingSetup;
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
