package com.ushahidi.plugins.mapping.data.domain;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.imageio.ImageIO;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;

import com.ushahidi.plugins.mapping.util.MappingLogger;

@Entity
@DiscriminatorValue(value="photo")
public class Photo extends Media {
	
	private static MappingLogger LOG = new MappingLogger(Photo.class);
	
	public Photo(){}
	public Photo(String localPath) {
		super(Type.PHOTO.getCode(), 0, null);
		this.localPath = localPath;
	}
	public Photo(long serverId, String link, String localPath) {
		super(Type.PHOTO.getCode(), serverId, link);
		this.localPath = localPath;
	}
	
	@Column(name="localpath")
	private String localPath;
	
	public String getLocalPath() {
		return localPath;
	}
	
	public File getFilePath() {
		return new File(localPath);
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
	
	/**
	 * Import photo to Photos directory
	 * @param srcPath source path
	 * @param destDirectory destination directory
	 * @return Photo
	 */
	public static Photo importPhoto(String srcPath, File destDirectory) {
		return Photo.importPhoto(srcPath, destDirectory.getAbsolutePath());
	}
	
	/**
	 * Import photo to Photos directory
	 * @param srcPath source path
	 * @param destDirectory destination directory
	 * @return Photo
	 */
	public static Photo importPhoto(String srcPath, String destDirectory) {
		try {
			File file = new File(srcPath);
			if (file.exists()) {
				BufferedImage image = ImageIO.read(file);
				DateFormat format = new SimpleDateFormat("yyyyMMddhhmmss");
				String destExtension = getExtension(file);
				if (destExtension != null) {
					String destFileName = String.format("%s.%s", format.format(new Date()), destExtension);
					File destFilePath = new File(destDirectory, destFileName);
					ImageIO.write(image, destExtension, destFilePath);
					return new Photo(destFilePath.getAbsolutePath());	
				}
			}
		} 
		catch (Exception ex) {
			LOG.error("Error copying image: %s", ex);
		}
		return null;
	}
	
	private static String getExtension(File file) {
        if (file.isDirectory()) {
        	return null;
        }
        String name = file.getName();
        int index = name.lastIndexOf('.');
        if (index > 0 && index < name.length() - 1) {
            return name.substring(index+1).toLowerCase();
        }
        return null;
    }

}