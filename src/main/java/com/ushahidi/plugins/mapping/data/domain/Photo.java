package com.ushahidi.plugins.mapping.data.domain;

import java.awt.Image;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;

@Entity
@DiscriminatorValue(value="photo")
public class Photo extends Media {

	public Photo(){}
	public Photo(long serverId, String link, String localPath) {
		super(Type.PHOTO.getCode(), serverId, link);
		this.localPath = localPath;
	}
	
	@Column(name="localpath")
	private String localPath;
	
	public String getLocalPath() {
		return localPath;
	}
	
	@Transient
	private Image image;
	
	public Image getImage() {
		if (image == null) {
			try {
				File file = new File(localPath);
				if (file.exists()) {
					image = ImageIO.read(file);
				}
			} 
			catch (IOException e) {
				e.printStackTrace();
			}
		}
		return image;
	}

}