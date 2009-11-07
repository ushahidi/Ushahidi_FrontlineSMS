package IntelliSoftware.SMSGateway.SDK.IntelliSMSJavaSDK.Internal;

import java.io.*;
import java.net.*;
import IntelliSoftware.Common.*;
import IntelliSoftware.SMSGateway.SDK.IntelliSMSJavaSDK.*;


public class SMSGatewayProxy
{
	public SMSGatewayProxy()
	{
	}


	public void Connect ( String sPrimaryGateway, String sBackupGateway, String sProxyAddress, String sProxyUsername, String sProxyPassword )
	{
		m_SMSGatewayService.Connect ( sPrimaryGateway, sBackupGateway, sProxyAddress, sProxyUsername, sProxyPassword );
	}


	public void Close()
	{
		m_SMSGatewayService.Close();
	}

	public boolean IsConnected ()
	{
		return m_SMSGatewayService.IsConnected();
	}

	public void SetAppDetails ( String sClientId, String sClientVer, String sAppId, String sAppVer, String sAppLic )
	{
		String sLogParams;

		sLogParams =  "clientid=" + sClientId;
		sLogParams += "&clientver=" + sClientVer;

		if ( sAppId!="" ) sLogParams += "&appid=" + sAppId;
		if ( sAppVer!="" ) sLogParams += "&appver=" + sAppVer;
		if ( sAppLic!="" ) sLogParams += "&applic=" + sAppLic;

		m_SMSGatewayService.SetOnceOnlyLogParams ( sLogParams );
	}


/*	public String SendMessage ( 
		String sUsername, String sPassword, 
		String sTo, String sFrom, String sText,
		String sUserContext,
		boolean bRequestDeliveryRpt,
		int nMaxConCatMsgs,
		out ResultCodes ResultCode )
	{
		ResultCode = ResultCodes.Unknown;

		String sTextProcessed = sText;

		String sUrl = "smsgateway/default.aspx";

		String sFormData = GenerateFormDataForSendMessage ( sUsername, sPassword, sTo, sFrom, sText, sUserContext, bRequestDeliveryRpt, nMaxConCatMsgs );

		String sRawResponse;
		String sMsgId = m_SMSGatewayService.MakeServiceRequest ( sUrl, sFormData, "ID:", true, out sRawResponse, out ResultCode );

		return sMsgId;
	}
*/


	public SendStatusCollection SendMessageToMultipleRecipients ( 
		String sUsername, String sPassword, 
		String sTo, String sFrom, String sText,
		String sUserContext,
		boolean bRequestDeliveryRpt,
		int nMaxConCatMsgs					) throws IntelliSMSException
	{
		String sUrl = "smsgateway/default.aspx";

		String sFormData = GenerateFormDataForSendMessage ( sUsername, sPassword, sTo, sFrom, sText, sUserContext, bRequestDeliveryRpt, nMaxConCatMsgs );

		SMSGatewayRequestResult objSMSGatewayRequestResult = m_SMSGatewayService.MakeServiceRequest ( sUrl, sFormData, "ID:", true );

		SendStatusCollection objSendStatusCollection = PopulateSendStatus ( sTo, objSMSGatewayRequestResult );

		return objSendStatusCollection;
	}


	public String GenerateFormDataForSendMessage ( 
		String sUsername, String sPassword, 
		String sTo, String sFrom, String sText,
		String sUserContext,
		boolean bRequestDeliveryRpt,
		int nMaxConCatMsgs ) throws IntelliSMSException
	{
		String sFormData = "";

		try
		{
			StringUtils objStringUtils = new StringUtils();
			sText = objStringUtils.TruncateString ( sText, c_nTextMessage_MaxSize * 5 );

			sFormData += "username=" + URLEncoder.encode(sUsername,"UTF-8") + "&";
			sFormData += "password=" + URLEncoder.encode(sPassword,"UTF-8") + "&";
			sFormData += "to=" + URLEncoder.encode(sTo,"UTF-8") + "&";
			sFormData += "from=" + URLEncoder.encode(sFrom,"UTF-8") + "&";
			sFormData += "text=" +URLEncoder.encode(sText,"UTF-8");

			if ( sUserContext.length() != 0 )
			{
				sFormData += "&usercontext=" +URLEncoder.encode(sUserContext,"UTF-8");
			}

			if ( bRequestDeliveryRpt )
			{
				sFormData += "&delrpt=1";
			}

			if ( nMaxConCatMsgs != 1 )
			{
				sFormData += "&maxconcat=" + URLEncoder.encode( Integer.toString(nMaxConCatMsgs), "UTF-8" );
			}
		}
		catch ( UnsupportedEncodingException e )
		{
			throw new IntelliSMSException ( ResultCodes.InternalError, "Unable to encode supplied text strings", e );
		}

		return sFormData;
	}


	public SendStatusCollection SendUnicodeMessageHex ( 
		String sUsername, String sPassword, 
		String sTo, String sFrom, 
		String sUnicodeTextHex,
		boolean bRequestDeliveryRpt ) throws IntelliSMSException
	{
		String sUrl = "smsgateway/default.aspx";

		String sUnicodeTextHexProcessed = StringUtils.TruncateString ( sUnicodeTextHex, c_nTextMessage_MaxSize * 5 * 2 );

		String sFormData = "";
		try
		{
			sFormData += "username=" + URLEncoder.encode(sUsername,"UTF-8") + "&";
			sFormData += "password=" + URLEncoder.encode(sPassword,"UTF-8") + "&";
			sFormData += "to=" + URLEncoder.encode(sTo,"UTF-8") + "&";
			sFormData += "from=" + URLEncoder.encode(sFrom,"UTF-8") + "&";
			sFormData += "type=2&";
			sFormData += "hex=" + URLEncoder.encode(sUnicodeTextHexProcessed,"UTF-8");
		}
		catch ( UnsupportedEncodingException e )
		{
			throw new IntelliSMSException ( ResultCodes.InternalError, "Unable to encode supplied text strings", e );
		}

		if ( bRequestDeliveryRpt )
		{
			sFormData += "&delrpt=1";
		}

		SMSGatewayRequestResult objSMSGatewayRequestResult = m_SMSGatewayService.MakeServiceRequest ( sUrl, sFormData, "ID:", true );

		SendStatusCollection objSendStatusCollection = PopulateSendStatus ( sTo, objSMSGatewayRequestResult );

		return objSendStatusCollection;
	}


	public SendStatusCollection SendBinaryMessage ( 
		String sUsername, String sPassword, 
		String sTo, String sFrom, 
		String sUserDataHeaderHex, String sUserDataHex,
		boolean bRequestDeliveryRpt ) throws IntelliSMSException
	{
		String sUrl = "smsgateway/default.aspx";

		String sUserDataHeaderHexProcessed = StringUtils.TruncateString ( sUserDataHeaderHex, 140 );
		String sUserDataHexProcessed = StringUtils.TruncateString ( sUserDataHex, 140 * 2 * 5 );

		String sFormData = "";
		try
		{
			sFormData += "username=" + URLEncoder.encode(sUsername,"UTF-8") + "&";
			sFormData += "password=" + URLEncoder.encode(sPassword,"UTF-8") + "&";
			sFormData += "to=" + URLEncoder.encode(sTo,"UTF-8") + "&";
			sFormData += "from=" + URLEncoder.encode(sFrom,"UTF-8") + "&";
			sFormData += "type=3&";
			sFormData += "udh=" + URLEncoder.encode(sUserDataHeaderHexProcessed,"UTF-8") + "&";
			sFormData += "ud=" + URLEncoder.encode(sUserDataHexProcessed,"UTF-8");
		}
		catch ( UnsupportedEncodingException e )
		{
			throw new IntelliSMSException ( ResultCodes.InternalError, "Unable to encode supplied text strings", e );
		}

		if ( bRequestDeliveryRpt )
		{
			sFormData += "&delrpt=1";
		}

		SMSGatewayRequestResult objSMSGatewayRequestResult = m_SMSGatewayService.MakeServiceRequest ( sUrl, sFormData, "ID:", true );

		SendStatusCollection objSendStatusCollection = PopulateSendStatus ( sTo, objSMSGatewayRequestResult );

		return objSendStatusCollection;
	}


	public SendStatusCollection SendWapPushMessage ( 
		String sUsername, String sPassword, 
		String sTo, String sFrom, 
		String sTitle, String sHRef,
		boolean bRequestDeliveryRpt ) throws IntelliSMSException
	{
		String sUrl = "smsgateway/default.aspx";

		String sTitleProcessed = StringUtils.TruncateString ( sTitle, 300 );
		String sHRefProcessed = StringUtils.TruncateString ( sHRef, 300 );

		String sFormData = "";
		try
		{
			sFormData += "username=" + URLEncoder.encode(sUsername,"UTF-8") + "&";
			sFormData += "password=" + URLEncoder.encode(sPassword,"UTF-8") + "&";
			sFormData += "to=" + URLEncoder.encode(sTo,"UTF-8") + "&";
			sFormData += "from=" + URLEncoder.encode(sFrom,"UTF-8") + "&";
			sFormData += "type=4&";
			sFormData += "text=" + URLEncoder.encode(sTitleProcessed,"UTF-8") + "&";
			sFormData += "href=" + URLEncoder.encode(sHRefProcessed,"UTF-8");
		}
		catch ( UnsupportedEncodingException e )
		{
			throw new IntelliSMSException ( ResultCodes.InternalError, "Unable to encode supplied text strings", e );
		}

		if ( bRequestDeliveryRpt )
		{
			sFormData += "&delrpt=1";
		}

		SMSGatewayRequestResult objSMSGatewayRequestResult = m_SMSGatewayService.MakeServiceRequest ( sUrl, sFormData, "ID:", true );

		SendStatusCollection objSendStatusCollection = PopulateSendStatus ( sTo, objSMSGatewayRequestResult );

		return objSendStatusCollection;
	}


	public int GetBalance ( String sUsername, String sPassword ) throws IntelliSMSException
	{
		String sBal;

		String sUrl = "smsgateway/getbalance/default.aspx";
		
		String sFormData = "";
		try
		{
			sFormData += "username=" + URLEncoder.encode(sUsername,"UTF-8") + "&";
			sFormData += "password=" + URLEncoder.encode(sPassword,"UTF-8");
		}
		catch ( UnsupportedEncodingException e )
		{
			throw new IntelliSMSException ( ResultCodes.InternalError, "Unable to encode supplied text strings", e );
		}

		String sRawResponse;
		SMSGatewayRequestResult objSMSGatewayRequestResult = m_SMSGatewayService.MakeServiceRequest ( sUrl, sFormData, "BALANCE:", false );

		int nBal = 0;
		if ( objSMSGatewayRequestResult.ResultCode == ResultCodes.OK )
		{
			nBal = Integer.parseInt(objSMSGatewayRequestResult.RetCode);
		}
		else
		{
			throw new IntelliSMSException ( objSMSGatewayRequestResult.ResultCode, "IntelliSMS request failed (" + objSMSGatewayRequestResult.ResultCode + ")" );
		}

		return nBal;
	}

	public ResultCodes CreateSubAccount ( 
		String sUsername, String sPassword, 
		String sSubAcc_Username, String sSubAcc_Password, 
		int nSubAccService,
		String sSubAcc_Fullname, String sSubAcc_Company, 
		String sSubAcc_Address,  String sSubAcc_Town, 
		String sSubAcc_County,   String sSubAcc_Country, 
		String sSubAcc_Postcode, String sSubAcc_Email, 
		String sSubAcc_Phone,    String sSubAcc_Fax  ) throws IntelliSMSException
	{
		String sUrl = "smsgateway/createsubacc.aspx";

		String sFormData = "";
		try
		{
			sFormData += "username=" + URLEncoder.encode(sUsername,"UTF-8") + "&";
			sFormData += "password=" + URLEncoder.encode(sPassword,"UTF-8") + "&";
			sFormData += "subacc_username=" + URLEncoder.encode(sSubAcc_Username,"UTF-8") + "&";
			sFormData += "subacc_password=" + URLEncoder.encode(sSubAcc_Password,"UTF-8") + "&";
			sFormData += "subacc_service="  + URLEncoder.encode(Integer.toString(nSubAccService),"UTF-8") + "&";
			sFormData += "subacc_fullname=" + URLEncoder.encode(sSubAcc_Fullname,"UTF-8") + "&";
			sFormData += "subacc_company="  + URLEncoder.encode(sSubAcc_Company,"UTF-8")  + "&";
			sFormData += "subacc_address="  + URLEncoder.encode(sSubAcc_Address,"UTF-8")  + "&";
			sFormData += "subacc_town="     + URLEncoder.encode(sSubAcc_Town,"UTF-8")     + "&";
			sFormData += "subacc_county="   + URLEncoder.encode(sSubAcc_County,"UTF-8")   + "&";
			sFormData += "subacc_country="  + URLEncoder.encode(sSubAcc_Country,"UTF-8")  + "&";
			sFormData += "subacc_postcode=" + URLEncoder.encode(sSubAcc_Postcode,"UTF-8") + "&";
			sFormData += "subacc_email="    + URLEncoder.encode(sSubAcc_Email,"UTF-8")    + "&";
			sFormData += "subacc_phone="    + URLEncoder.encode(sSubAcc_Phone,"UTF-8")    + "&";
			sFormData += "subacc_fax="      + URLEncoder.encode(sSubAcc_Fax,"UTF-8");
		}
		catch ( UnsupportedEncodingException e )
		{
			throw new IntelliSMSException ( ResultCodes.InternalError, "Unable to encode supplied text strings", e );
		}

		SMSGatewayRequestResult objSMSGatewayRequestResult = m_SMSGatewayService.MakeServiceRequest ( sUrl, sFormData, "OK", false );

		return objSMSGatewayRequestResult.ResultCode;
	}


	public SendStatusCollection PopulateSendStatus ( String sTo, SMSGatewayRequestResult objSMSGatewayRequestResult )
	{
		SendStatusCollection SendStatusList = new SendStatusCollection();

		SendStatusList.OverallResultCode = objSMSGatewayRequestResult.ResultCode;

		if ( objSMSGatewayRequestResult.ResultCode==ResultCodes.OK ||
			 objSMSGatewayRequestResult.ResultCode==ResultCodes.InvalidNumber ||
			 objSMSGatewayRequestResult.ResultCode==ResultCodes.InvalidRequest )
		{
			String[] Responses = objSMSGatewayRequestResult.RawResponse.split( "\n" );

			if ( Responses.length != 0 )
			{
				SendStatusList.OverallResultCode = ResultCodes.OK;

				for ( int nIdx=0; nIdx<Responses.length; nIdx++ )
				{
					String sResponse = Responses[nIdx];

					String[] ResponseParts = sResponse.split( "," );

					if ( ResponseParts.length >= 2 )
					{
						String Part2 = ResponseParts[1];
						SendStatus objSendStatus = ParseStatus ( Part2 );

						objSendStatus.To = ResponseParts[0];

						SendStatusList.add ( objSendStatus );
					}
					else
					{
						if ( sResponse.trim().length() != 0 )
						{
							SendStatus objSendStatus = ParseStatus ( sResponse );
							objSendStatus.To = sTo.replace ( ",", "" );

							SendStatusList.add ( objSendStatus );
						}
					}
				}
			}
		}

		return SendStatusList;
	}

	public SendStatus ParseStatus ( String sResponse )
	{
		SendStatus objSendStatus = new SendStatus();

		if ( sResponse.indexOf(c_sIdPrefix) != -1 )
		{
			int IdStart = sResponse.indexOf ( c_sIdPrefix ) + c_sIdPrefix.length();

			StringUtils objStringUtils = new StringUtils();
			int IdEnd = objStringUtils.LastindexOfAny ( sResponse, "0123456789" );

			objSendStatus.MessageId = sResponse.substring ( IdStart, IdEnd /*-IdStart*/ +1 );

			objSendStatus.ResultCode = ResultCodes.OK;
		}
		else if ( sResponse.indexOf("ERR:") != -1 )
		{
			objSendStatus.MessageId = "";

			objSendStatus.ResultCode = m_SMSGatewayService.ParseResultCode ( sResponse );
		}

		return objSendStatus;
	}


	private SMSGatewayService m_SMSGatewayService = new SMSGatewayService();

	private /*const*/ String c_sIdPrefix = "ID:";
	private /*const*/ int c_nTextMessage_MaxSize = 160;
}


