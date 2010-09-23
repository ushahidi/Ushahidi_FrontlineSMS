package com.ushahidi.plugins.mapping.data.domain;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue(value="news")
public class News extends Media {

	public News(){}
	public News(long serverId, String link) {
		super(Type.NEWS.getCode(), serverId, link);
	}
	
}