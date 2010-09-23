package com.ushahidi.plugins.mapping.ui.markers;

import com.ushahidi.plugins.mapping.data.domain.Location;

import net.frontlinesms.data.domain.FrontlineMessage;

/**
 * MessageMarker
 * @author dalezak
 *
 */
@SuppressWarnings("serial")
public class MessageMarker extends Marker {

	private FrontlineMessage message;
	
	public MessageMarker(FrontlineMessage message, Location location){
		super("/icons/big_sms.png", location);
		this.message = message;
	}
		
	public FrontlineMessage getFrontlineMessage(){
		return message;
	}
	
	@Override
	public String toString() {
		return String.format("[%s]", message.getTextContent());
	}
	
}