package com.ushahidi.plugins.mapping.data.domain;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue(value="audio")
public class Audio extends Media {
	
	public Audio(){}
	public Audio(long serverId, String link) {
		super(Type.AUDIO.getCode(), serverId, link);
	}
	
}