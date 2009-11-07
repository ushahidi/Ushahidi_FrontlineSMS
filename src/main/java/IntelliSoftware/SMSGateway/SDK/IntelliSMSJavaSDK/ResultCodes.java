package IntelliSoftware.SMSGateway.SDK.IntelliSMSJavaSDK;

import java.io.*;
import IntelliSoftware.Common.*;

/// <summary>
/// Error received from the IntelliSoftware SMS Internet gateway
/// </summary>
public enum ResultCodes
{
	Unknown,
	/// <summary>
	/// Request completed OK
	/// </summary>
	OK,
	/// <summary>
	/// Username paramter was missing
	/// </summary>
	NoUsername,
	/// <summary>
	/// Password paramter was missing
	/// </summary>
	NoPassword,
	/// <summary>
	/// To paramter was missing
	/// </summary>
	NoTo,
	/// <summary>
	/// Text paramter was missing
	/// </summary>
	NoText,
	/// <summary>
	/// Username/password is invalid
	/// </summary>
	LoginInvalid,
	/// <summary>
	/// Insufficent credit in your account to complete the request
	/// </summary>
	InsufficientCredit,
	/// <summary>
	/// An error occurred internal to the IntelliSoftware SMS Gateway, retry your request later
	/// </summary>
	GatewayError,
	/// <summary>
	/// An error occurred internal to the IntelliSoftware SMS Gateway, retry your request later
	/// </summary>
	InternalError,
	/// <summary>
	/// A HTTP error occurred trying to connect to the IntelliSoftware SMS Gateway, check proxy settings
	/// </summary>
	HTTPConnectionError,
	/// <summary>
	/// The recipient number is invalid
	/// </summary>
	InvalidNumber,
	/// <summary>
	/// The request is invalid
	/// </summary>
	InvalidRequest,
	/// <summary>
	/// The IntelliSoftware SMS Gateway, please try again after a short delay
	/// </summary>
	ServerTooBusy,
	/// <summary>
	/// The specified MessageId is invalid for your account
	/// </summary>
	MsgIdInvalid,
	/// <summary>
	/// A mandatory parameter is missing
	/// </summary>
	ParameterMissing,
	/// <summary>
	/// A provide parameter is invalid
	/// </summary>
	ParameterInvalid,
	/// <summary>
	/// Account already exists
	/// </summary>
	AccountExists,
	/// <summary>
	/// Internal use only
	/// </summary>
	DelayRequired /*= 100*/
};

