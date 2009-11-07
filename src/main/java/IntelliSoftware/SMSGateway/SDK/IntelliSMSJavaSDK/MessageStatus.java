package IntelliSoftware.SMSGateway.SDK.IntelliSMSJavaSDK;

import java.io.*;
import IntelliSoftware.Common.*;

/// <summary>
/// Status of the message sent through the IntelliSoftware SMS Gateway
/// </summary>
public enum MessageStatus
{
	Unknown,
	OK,
	/// <summary>
	/// Message is queued on the IntelliSoftware Messaging Server
	/// </summary>
	MessageQueued,
	/// <summary>
	/// Message has been sent by the IntelliSoftware Messaging Server
	/// </summary>
	MessageSent,
	/// <summary>
	/// Delivery report has been received from the recipients handset
	/// </summary>
	MessageDelivered,
	/// <summary>
	/// Error occured with the request
	/// </summary>
	ErrorWithRequest,
	/// <summary>
	/// Unable to delivery message to recipient, check phone number
	/// </summary>
	UnableToDeliver,
	/// <summary>
	/// Unable to delivery message to recipient, check phone number
	/// </summary>
	RoutingError,
	/// <summary>
	/// Message expired before it could be delivered to the recipient
	/// </summary>
	MessageExpired
};
