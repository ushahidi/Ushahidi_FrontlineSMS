/**
 * 
 */
package net.frontlinesms.data.domain;

import java.lang.reflect.Method;
import java.util.Map;

import javax.persistence.*;

import net.frontlinesms.Utils;
import net.frontlinesms.smsdevice.SmsInternetService;
import net.frontlinesms.smsdevice.properties.*;

/**
 * Class encapsulating settings of a {@link SmsInternetService}.
 * @author Alex
 */
@Entity
public class SmsInternetServiceSettings {
//> INSTANCE PROPERTIES
	/** Unique id for this entity.  This is for hibernate usage. */
	@Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(unique=true,nullable=false,updatable=false) @SuppressWarnings("unused")
	private long id;
	private String serviceClassName;
	private transient Map<String, Object> properties;
	
//> CONSTRUCTORS
	/** Empty constructor for hibernate */
	SmsInternetServiceSettings() {}
	
	/**
	 * Create a new instance of service settings for the supplied service.
	 * @param service
	 */
	public SmsInternetServiceSettings(SmsInternetService service) {
		this.serviceClassName = service.getClass().getCanonicalName();
	}
	
//> ACCESSOR METHODS
	/**
	 * Sets the value of a setting.
	 * FIXME value should not just be an OBJECT - some interface at least i would expect!
	 */
	public void set(String key, Object value) {
		this.properties.put(key, value);
	}
	
	/**
	 * Gets the class name of {@link SmsInternetService} implementation that these settings apply to.
	 * @return
	 */
	public String getServiceClassName() {
		return this.serviceClassName;
	}
	
	/**
	 * Get an ordered list of the properties set on this object.
	 * @return
	 */
	public Map<String, String> getProperties() {
		// FIXME implement this properly
		return null;
	}
	

//> STATIC HELPER METHODS
	/**
	 * Converts the supplied property value to the string representation of it. 
	 * @param value
	 * @return
	 */
	public static String getValueAsString(Object value) {
		if (value instanceof String) return (String)value;
		if (value instanceof Boolean) return Boolean.toString((Boolean)value);
		if (value instanceof Integer) return Integer.toString((Integer)value);
		if (value instanceof PasswordString) return Utils.encodeBase64(((PasswordString)value).getValue());
		if (value instanceof OptionalSection) return Boolean.toString(((OptionalSection)value).getValue());
		if (value instanceof Enum<?>) return ((Enum<?>)value).name();
		if (value instanceof PhoneSection) return ((PhoneSection)value).getValue();
		if (value instanceof OptionalRadioSection) {
			OptionalRadioSection<?> ors = (OptionalRadioSection<?>) value;
			return ors.getValue().name();
		}
		throw new RuntimeException("Unsupported property type: " + value.getClass());
	}

	/**
	 * Gets a property value from a string, and the canonical name of that class.
	 * @param clazzName
	 * @param stringValue
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static Object getValueFromString(Object property, String value) {
		if (property.getClass().equals(String.class))
			return value;
		if (property.getClass().equals(Boolean.class))
			return Boolean.parseBoolean(value);
		if (property.getClass().equals(Integer.class))
			return Integer.parseInt(value);
		if (property.getClass().equals(PasswordString.class))
			return new PasswordString(Utils.decodeBase64(value));
		if (property.getClass().equals(OptionalSection.class)) {
			return Boolean.parseBoolean(value);
		}
		if (property.getClass().equals(PhoneSection.class))
			return new PhoneSection(value);
		if (property.getClass().equals(OptionalRadioSection.class)) {
			try {
				OptionalRadioSection section = (OptionalRadioSection) property;
				Method getValueOf = section.getValue().getClass().getMethod("valueOf", String.class);
				Enum enumm = (Enum) getValueOf.invoke(null, value);
				return new OptionalRadioSection(enumm);
			} catch (Throwable t) {
				throw new RuntimeException(t);
			}
		}
		try {
			if (property.getClass().isEnum()) {
				Method getValueOf = property.getClass().getMethod("valueOf", String.class);
				Enum enumm = (Enum) getValueOf.invoke(null, value);
				return enumm;
			}
		} catch (Throwable t) {
			throw new RuntimeException(t);
		}
		throw new RuntimeException("Unsupported property type: " + property.getClass());
	}
	
//> GENERATED METHODS

	/** @see java.lang.Object#hashCode() */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (id ^ (id >>> 32));
		result = prime
				* result
				+ ((serviceClassName == null) ? 0 : serviceClassName.hashCode());
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
		SmsInternetServiceSettings other = (SmsInternetServiceSettings) obj;
		if (id != other.id)
			return false;
		if (serviceClassName == null) {
			if (other.serviceClassName != null)
				return false;
		} else if (!serviceClassName.equals(other.serviceClassName))
			return false;
		return true;
	}
}