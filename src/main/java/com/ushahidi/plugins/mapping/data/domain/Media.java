package com.ushahidi.plugins.mapping.data.domain;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Table;

@Entity
@Table(name="media")
@DiscriminatorColumn(name="media", discriminatorType=DiscriminatorType.STRING)
@Inheritance(strategy=InheritanceType.SINGLE_TABLE)
public abstract class Media {
	
	protected Media(){}
	protected Media(int type, long serverId, String link) {
		this.type = type;
		this.serverId = serverId;
		this.link = link;
	}
	
	@Id 
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="id", unique=true, nullable=false, updatable=false)
	protected long id;
	
	@Column(name="server_id", nullable=true)
	protected long serverId;
	
	@Column(name="link", nullable=true)
	protected String link;
	
	@Column(name="type", nullable=false)
	protected int type;
	
	public long getId() {
		return id;
	}
	
	public long getServerId() {
		return serverId;
	}
	
	public String getLink() {
		return link;
	}
	
	public void setLink(String link) {
		this.link = link;
	}

	public int getType() {
		return type;
	}
	
	public enum Type {
		UNKNOWN(0),
		PHOTO(1),
		VIDEO(2),
		AUDIO(3),
		NEWS(4);
		
		private int code;
	
		private Type(int code) {
			this.code = code;
		}
	
		public int getCode() { 
			return code; 
		}
		
		public static Type get(int code) { 
			return lookup.get(code); 
		}
		
		private static final Map<Integer,Type> lookup = new HashMap<Integer,Type>();
	
		static {
			for(Type type : EnumSet.allOf(Type.class)) {
				lookup.put(type.getCode(), type);
			}
		}
	};
}