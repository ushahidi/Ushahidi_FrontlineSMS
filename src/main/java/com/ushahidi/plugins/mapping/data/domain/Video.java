package com.ushahidi.plugins.mapping.data.domain;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue(value="video")
public class Video extends Media {

	public Video(){}
	public Video(long serverId, String link) {
		super(Type.VIDEO.getCode(), serverId, link);
	}

}