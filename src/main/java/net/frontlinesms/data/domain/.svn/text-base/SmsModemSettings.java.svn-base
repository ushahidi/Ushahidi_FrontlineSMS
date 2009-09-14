/**
 * 
 */
package net.frontlinesms.data.domain;

import javax.persistence.*;

/**
 * @author Alex
 */
@Entity
public class SmsModemSettings {
	/** Field names */
	public static final String FIELD_SERIAL = "serial";
	
//> INSTANCE PROPERTIES
	/** Unique id for this entity.  This is for hibernate usage. */
	@Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(unique=true,nullable=false,updatable=false) @SuppressWarnings("unused")
	private long id;
	@Column(name=FIELD_SERIAL)
	private String serial;
	private boolean useForSending;
	private boolean useForReceiving;
	private boolean deleteMessagesAfterReceiving;
	private boolean useDeliveryReports;
	
//> CONSTRUCTORS
	/** Empty constructor for hibernate */
	SmsModemSettings() {}
	
	/**
	 * Sets the details for the supplied SMS device
	 * @param serial The serial number of the device
	 * @param useForSending whether the device should be used for sending SMS
	 * @param useForReceiving whether the device should be used for receiving SMS
	 * @param deleteMessagesAfterReceiving whether messages should be deleted from the device after being read by FrontlineSMS 
	 * @param useDeliveryReports whether delivery reports should be used with this device
	 */
	public SmsModemSettings(String serial, boolean useForSending, boolean useForReceiving, boolean deleteMessagesAfterReceiving, boolean useDeliveryReports) {
		this.serial = serial;
		this.useForSending = useForSending;
		this.useForReceiving = useForReceiving;
		this.deleteMessagesAfterReceiving = deleteMessagesAfterReceiving;
		this.useDeliveryReports = useDeliveryReports;
	}

//> ACCESSOR METHODS
	public String getSerial() {
		return serial;
	}
	public boolean useForSending() {
		return useForSending;
	}
	public void setUseForSending(boolean useForSending) {
		this.useForSending = useForSending;
	}
	public boolean useForReceiving() {
		return useForReceiving;
	}
	public void setUseForReceiving(boolean useForReceiving) {
		this.useForReceiving = useForReceiving;
	}
	public boolean deleteMessagesAfterReceiving() {
		return deleteMessagesAfterReceiving;
	}
	public void setDeleteMessagesAfterReceiving(boolean deleteMessagesAfterReceiving) {
		this.deleteMessagesAfterReceiving = deleteMessagesAfterReceiving;
	}
	public boolean useDeliveryReports() {
		return useDeliveryReports;
	}
	public void setUseDeliveryReports(boolean useDeliveryReports) {
		this.useDeliveryReports = useDeliveryReports;
	}

//> GENERATED METHODS
	/** @see java.lang.Object#hashCode() */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (deleteMessagesAfterReceiving ? 1231 : 1237);
		result = prime * result + (int) (id ^ (id >>> 32));
		result = prime * result + ((serial == null) ? 0 : serial.hashCode());
		result = prime * result + (useDeliveryReports ? 1231 : 1237);
		result = prime * result + (useForReceiving ? 1231 : 1237);
		result = prime * result + (useForSending ? 1231 : 1237);
		return result;
	}

	/** @see java.lang.Object#equals(java.lang.Object) */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SmsModemSettings other = (SmsModemSettings) obj;
		if (deleteMessagesAfterReceiving != other.deleteMessagesAfterReceiving)
			return false;
		if (id != other.id)
			return false;
		if (serial == null) {
			if (other.serial != null)
				return false;
		} else if (!serial.equals(other.serial))
			return false;
		if (useDeliveryReports != other.useDeliveryReports)
			return false;
		if (useForReceiving != other.useForReceiving)
			return false;
		if (useForSending != other.useForSending)
			return false;
		return true;
	}
}
