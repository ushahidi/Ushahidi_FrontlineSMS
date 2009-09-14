package IntelliSoftware.SMSGateway.SDK.IntelliSMSJavaSDK;

import java.io.*;

/// <summary>
/// Represents error returned by the IntelliSoftware SMS Internet gateway
/// </summary>
public class IntelliSMSException extends Exception
{
	/// <summary>
	/// Error received from the IntelliSoftware SMS Internet gateway
	/// </summary>
	public ResultCodes ResultCode;

	public Exception InnerException = null;

	public IntelliSMSException ( ResultCodes resultCode, String message )
	{
		super(message);

		ResultCode = resultCode;
	}

	public IntelliSMSException ( ResultCodes resultCode, String message, Exception innerException )
	{
		super(message);

		ResultCode = resultCode;
		InnerException = innerException;
	}

}
