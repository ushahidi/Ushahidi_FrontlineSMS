package IntelliSoftware.SMSGateway.SDK.IntelliSMSJavaSDK.Internal;

import java.io.*;
import java.lang.*;
import IntelliSoftware.Common.*;
import IntelliSoftware.SMSGateway.SDK.IntelliSMSJavaSDK.*;

class SMSGatewayService
{
	public static int FailOverRecoverySecs = 5 * 60;   //5 mins

	public SMSGatewayService()
	{
		Intialise();

		m_HTTPConnection.UserAgent = "IntelliSMS";
	}

	private static void Intialise()
	{
		if ( g_LastFailedTime == null )
		{
			g_LastFailedTime = new TickTime[2];

			if ( g_LastFailedTime[0]==null )
			{
				g_LastFailedTime[0] = new TickTime();
			}

			if ( g_LastFailedTime[1]==null )
			{
				g_LastFailedTime[1] = new TickTime();
			}
		}
	}

	public static void ResetGatewayFaiover ()
	{
		Intialise();
		g_LastFailedTime[0] = new TickTime();
		g_LastFailedTime[1] = new TickTime();
	}

	public void Connect ( String sPrimaryGateway, String sBackupGateway, String sProxyAddress, String sProxyUsername, String sProxyPassword )
	{
		if ( sProxyAddress==null || sProxyAddress.length()==0 )
		{
			m_HTTPConnection.Open ( false, "", "", "" );
		}
		else
		{
			m_HTTPConnection.Open ( true, sProxyAddress, sProxyUsername, sProxyPassword );
		}

		m_sPrimaryGateway = sPrimaryGateway;
		m_sBackupGateway = sBackupGateway;

		if ( sPrimaryGateway.indexOf("http")!=0 )
		{
			m_sPrimaryGateway = "http://" + sPrimaryGateway;
		}

		if ( sBackupGateway.indexOf("http")!=0 )
		{
			m_sBackupGateway = "http://" + sBackupGateway;
		}
	}

	public void Close()
	{
		m_HTTPConnection.Close();
	}


	public boolean IsConnected()
	{
		return m_HTTPConnection.IsConnected();
	}


	public void SetOnceOnlyLogParams ( String sOnceOnlyLogParams )
	{
		m_sOnceOnlyLogParams = sOnceOnlyLogParams;
	}


	public SMSGatewayRequestResult MakeServiceRequest ( String sURL, String sFormData, String sReturnCodePrefix, boolean bSendLogParams ) throws IntelliSMSException
	{
		SMSGatewayRequestResult objSMSGatewayRequestResult = MakeGatewayRequest ( sURL, sFormData, sReturnCodePrefix, bSendLogParams );

		if ( objSMSGatewayRequestResult.ResultCode==ResultCodes.DelayRequired )
		{
			int nDelay = Integer.parseInt ( objSMSGatewayRequestResult.RetCode );
			if ( nDelay > (60*2) )
			{
				nDelay = 15;
			}

			try
			{
				Thread.currentThread().sleep ( nDelay * 1000 );
			}
			catch ( InterruptedException e )
			{
				throw new IntelliSMSException ( ResultCodes.InternalError, "Sleep operation interupted", e );
			}

			objSMSGatewayRequestResult = MakeGatewayRequest ( sURL, sFormData, sReturnCodePrefix, bSendLogParams );
		}

		if ( objSMSGatewayRequestResult.ResultCode == ResultCodes.DelayRequired )
		{
			objSMSGatewayRequestResult.ResultCode = ResultCodes.ServerTooBusy;
		}

		return objSMSGatewayRequestResult;
	}

	public SMSGatewayRequestResult MakeGatewayRequest ( String sURL, String sFormData, String sReturnCodePrefix, boolean bSendLogParams ) throws IntelliSMSException
	{
		TickTime RetyCutOffTime = TickTime.GetCurrentTime();
		RetyCutOffTime.SubtractMilliseconds ( FailOverRecoverySecs * 1000 );

		int GatewayNo = 0;

		String[] Gateways = new String[2];
		Gateways[0] = m_sPrimaryGateway;
		Gateways[1] = m_sBackupGateway;

		SMSGatewayRequestResult objSMSGatewayRequestResult = null;

		for ( int i=0; i<2; i++ )
		{
			if ( g_LastFailedTime[0].IsNULL() || TickTime.IsLessThan(RetyCutOffTime,g_LastFailedTime[0]) )
			{
				GatewayNo = 0;
			}
			else
			{
				if ( g_LastFailedTime[1].IsNULL() || TickTime.IsLessThan(RetyCutOffTime,g_LastFailedTime[1]) )
				{
					GatewayNo = 1;
				}
				else
				{
					if ( GatewayNo == 0 ) GatewayNo = 1;
					else GatewayNo = 0;
				}
			}


			String sFullURL = Gateways[GatewayNo] + "/" + sURL;
			String sFullPostData = sFormData;

			if ( bSendLogParams && GatewayNo==0 && !m_bOnceOnlyLogParamsSent )
			{
				if ( m_sOnceOnlyLogParams!="" )
				{
					sFullPostData += "&" + m_sOnceOnlyLogParams;
				}
			}

			objSMSGatewayRequestResult = MakeHTTPRequest ( sFullURL, sFullPostData, sReturnCodePrefix );
			g_LastFailedTime[GatewayNo] = TickTime.NULLTime();

			if ( objSMSGatewayRequestResult.ResultCode == ResultCodes.OK )
			{
				if ( bSendLogParams && GatewayNo==0 && !m_bOnceOnlyLogParamsSent )
				{
					m_bOnceOnlyLogParamsSent = true;
				}
			}


			if ( objSMSGatewayRequestResult.ResultCode==ResultCodes.InternalError || objSMSGatewayRequestResult.ResultCode==ResultCodes.HTTPConnectionError || objSMSGatewayRequestResult.ResultCode==ResultCodes.GatewayError )
			{
				g_LastFailedTime[GatewayNo] = TickTime.GetCurrentTime();
			}
			else
			{
				break;
			}
		}

		if ( objSMSGatewayRequestResult.ResultCode==ResultCodes.HTTPConnectionError && objSMSGatewayRequestResult.InternalException!=null )
		{
			throw new IntelliSMSException ( objSMSGatewayRequestResult.ResultCode, "HTTP Error occurred trying to connecting to the IntelliSoftware gateway, see InnerException for details", objSMSGatewayRequestResult.InternalException );
		}

		return objSMSGatewayRequestResult;
	}


	public SMSGatewayRequestResult MakeHTTPRequest ( String sURL, String sFormData, String sReturnCodePrefix )
	{
		SMSGatewayRequestResult objSMSGatewayRequestResult = new SMSGatewayRequestResult();

		String sDelayTimePrefix = "DELAY:";

		String sRetCode = "";

		try
		{
			//NOTE: "application/x-www-form-urlencoded; charset:utf-8" is a non-standard ASP.NET
			//      the IntelliSoftware gateway has been specifically written to recognise the content type
			String sResponse = m_HTTPConnection.HTTPPost ( sURL, sFormData, "application/x-www-form-urlencoded; charset:utf-8" );

			objSMSGatewayRequestResult.RawResponse = sResponse;

			if ( sResponse.lastIndexOf(sReturnCodePrefix) != -1 )
			{
				int IdStart = sResponse.lastIndexOf(sReturnCodePrefix) + sReturnCodePrefix.length();

				StringUtils objStringUtils = new StringUtils();
				int IdEnd = objStringUtils.LastindexOfAny( sResponse, "0123456789" );

				objSMSGatewayRequestResult.RetCode = sResponse.substring ( IdStart, IdEnd /*-IdStart*/ +1 );

				objSMSGatewayRequestResult.ResultCode = ResultCodes.OK;
			}
			else if ( sResponse.lastIndexOf(sDelayTimePrefix) != -1 )
			{
				int IdStart = sResponse.lastIndexOf(sDelayTimePrefix) + sDelayTimePrefix.length();

				StringUtils objStringUtils = new StringUtils();
				int IdEnd = objStringUtils.LastindexOfAny( sResponse, "0123456789" );

				objSMSGatewayRequestResult.RetCode = sResponse.substring ( IdStart, IdEnd /*-IdStart*/ +1 );

				objSMSGatewayRequestResult.ResultCode = ResultCodes.DelayRequired;
			}
			else if ( sResponse.lastIndexOf("ERR:") != -1 )
			{
				objSMSGatewayRequestResult.ResultCode = ParseResultCode ( sResponse );
			}
			else
			{
				objSMSGatewayRequestResult.ResultCode = ResultCodes.HTTPConnectionError;			
				objSMSGatewayRequestResult.InternalException = new Exception ( "Invalid HTTP Response from IntelliSoftware gateway (" + sResponse + ")" );
			}
		}
		catch ( Exception e )
		{
			objSMSGatewayRequestResult.ResultCode = ResultCodes.HTTPConnectionError;

			objSMSGatewayRequestResult.InternalException = e;
		}

		return objSMSGatewayRequestResult;
	}


	public ResultCodes ParseResultCode ( String sResponse )
	{
		ResultCodes ResultCode = ResultCodes.Unknown;

		if ( sResponse.lastIndexOf("NO_USERNAME") != -1 )
		{
			ResultCode = ResultCodes.NoUsername;
		}
		else if ( sResponse.lastIndexOf("NO_PASSWORD") != -1 )
		{
			ResultCode = ResultCodes.NoPassword;
		}
		else if ( sResponse.lastIndexOf("NO_TO") != -1 )
		{
			ResultCode = ResultCodes.NoTo;
		}
		else if ( sResponse.lastIndexOf("NO_TEXT") != -1 )
		{
			ResultCode = ResultCodes.NoText;
		}
		else if ( sResponse.lastIndexOf("LOGIN_INVALID") != -1 )
		{
			ResultCode = ResultCodes.LoginInvalid;
		}
		else if ( sResponse.lastIndexOf("INSUFFICIENT_CREDIT") != -1 )
		{
			ResultCode = ResultCodes.InsufficientCredit;
		}
		else if ( sResponse.lastIndexOf("GATEWAY_ERROR") != -1 )
		{
			ResultCode = ResultCodes.GatewayError;
		}
		else if ( sResponse.lastIndexOf("INTERNAL_ERROR") != -1 )
		{
			ResultCode = ResultCodes.InternalError;
		}
		else if ( sResponse.lastIndexOf("INVALID_NUMBER") != -1 )
		{
			ResultCode = ResultCodes.InvalidNumber;
		}
		else if ( sResponse.lastIndexOf("INVALID_REQUEST") != -1 )
		{
			ResultCode = ResultCodes.InvalidRequest;
		}
		else if ( sResponse.lastIndexOf("MSGID_INVALID") != -1 )
		{
			ResultCode = ResultCodes.MsgIdInvalid;
		}
		else if ( sResponse.lastIndexOf("PARAMETER_MISSING") != -1 )
		{
			ResultCode = ResultCodes.ParameterMissing;
		}
		else if ( sResponse.lastIndexOf("PARAMETER_INVALID") != -1 )
		{
			ResultCode = ResultCodes.ParameterInvalid;
		}
		else if ( sResponse.lastIndexOf("ACCOUNT_EXISTS") != -1 )
		{
			ResultCode = ResultCodes.AccountExists;
		}

		return ResultCode;
	}

	private HTTPConnection m_HTTPConnection = new HTTPConnection();

	private static TickTime[] g_LastFailedTime;

	private String m_sOnceOnlyLogParams;
	static boolean m_bOnceOnlyLogParamsSent = false;

	private String m_sPrimaryGateway;
	private String m_sBackupGateway;
}


