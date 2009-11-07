package IntelliSoftware.SMSGateway.SDK.IntelliSMSJavaSDK;

/** Represents error returned by the IntelliSoftware SMS Internet gateway */
@SuppressWarnings("serial")
public class IntelliSMSException extends Exception
{
	/** Error received from the IntelliSoftware SMS Internet gateway */
	private final ResultCodes resultCode;

	/**
	 * @param resultCode The {@link ResultCodes} received from the IntelliSoftware SMS Internet gateway
	 * @param message The detail message for the exception
	 */
	public IntelliSMSException(ResultCodes resultCode, String message) {
		super(message);
		this.resultCode = resultCode;
	}

	/**
	 * @param resultCode The {@link ResultCodes} received from the IntelliSoftware SMS Internet gateway
	 * @param message The detail message for the exception
	 * @param cause The cause of the exception
	 */
	public IntelliSMSException(ResultCodes resultCode, String message, Exception cause) {
		super(message, cause);
		this.resultCode = resultCode;
	}

	/** @return {@link #resultCode} */
	public ResultCodes getResultCode() {
		return resultCode;
	}
}
