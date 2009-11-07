package IntelliSoftware.SMSGateway.SDK.IntelliSMSJavaSDK;

import java.io.*;
import IntelliSoftware.Common.*;
import IntelliSoftware.SMSGateway.SDK.IntelliSMSJavaSDK.Internal.*;

public class IntelliSMS
{
	public SendStatusCollection SendMessage ( String[] ToList, String Message, String From ) throws IntelliSMSException
	{
		SMSGatewayProxy objSMSGatewayProxy = new SMSGatewayProxy();

		SetAppDetails ( objSMSGatewayProxy );

		objSMSGatewayProxy.Connect ( PrimaryGateway, BackupGateway, ProxyAddress, ProxyUsername, ProxyPassword );

		StringUtils objStringUtils = new StringUtils();
		String To = objStringUtils.StringArrayToCSV ( ToList );

		SendStatusCollection SendStatusList = objSMSGatewayProxy.SendMessageToMultipleRecipients ( Username, Password, To, From, Message, "", RequestDeliveryRpt, MaxConCatMsgs );

		objSMSGatewayProxy.Close();

		CheckResultCode ( SendStatusList );

		return SendStatusList;
	}

	public SendStatusCollection SendMessageWithUserContext ( String[] ToList, String Message, String From, String UserContext ) throws IntelliSMSException
	{
		SMSGatewayProxy objSMSGatewayProxy = new SMSGatewayProxy();

		SetAppDetails ( objSMSGatewayProxy );

		objSMSGatewayProxy.Connect ( PrimaryGateway, BackupGateway, ProxyAddress, ProxyUsername, ProxyPassword );

		StringUtils objStringUtils = new StringUtils();
		String To = objStringUtils.StringArrayToCSV ( ToList );

		SendStatusCollection SendStatusList = objSMSGatewayProxy.SendMessageToMultipleRecipients ( Username, Password, To, From, Message, UserContext, RequestDeliveryRpt, MaxConCatMsgs );

		objSMSGatewayProxy.Close();

		CheckResultCode ( SendStatusList );

		return SendStatusList;
	}

	public SendStatusCollection SendUnicodeMessage ( String[] ToList, String Message, String From ) throws IntelliSMSException
	{
		SMSGatewayProxy objSMSGatewayProxy = new SMSGatewayProxy();

		SetAppDetails ( objSMSGatewayProxy );

		objSMSGatewayProxy.Connect ( PrimaryGateway, BackupGateway, ProxyAddress, ProxyUsername, ProxyPassword );

		StringUtils objStringUtils = new StringUtils();
		String To = objStringUtils.StringArrayToCSV ( ToList );

		String MessageHex = HexEncoder.EncodeFromUnicode ( Message );

		SendStatusCollection SendStatusList = objSMSGatewayProxy.SendUnicodeMessageHex ( Username, Password, To, From, MessageHex, RequestDeliveryRpt );

		objSMSGatewayProxy.Close();

		CheckResultCode ( SendStatusList );

		return SendStatusList;
	}

	public SendStatusCollection SendUnicodeMessageHex ( String[] ToList, String MessageHex, String From ) throws IntelliSMSException
	{
		SMSGatewayProxy objSMSGatewayProxy = new SMSGatewayProxy();

		SetAppDetails ( objSMSGatewayProxy );

		objSMSGatewayProxy.Connect ( PrimaryGateway, BackupGateway, ProxyAddress, ProxyUsername, ProxyPassword );

		StringUtils objStringUtils = new StringUtils();
		String To = objStringUtils.StringArrayToCSV ( ToList );

		SendStatusCollection SendStatusList = objSMSGatewayProxy.SendUnicodeMessageHex ( Username, Password, To, From, MessageHex, RequestDeliveryRpt );

		objSMSGatewayProxy.Close();

		CheckResultCode ( SendStatusList );

		return SendStatusList;
	}

	public SendStatusCollection SendBinaryMessage ( String[] ToList, String UserDataHexHeader, String UserDataHex, String From ) throws IntelliSMSException
	{
		SMSGatewayProxy objSMSGatewayProxy = new SMSGatewayProxy();

		SetAppDetails ( objSMSGatewayProxy );

		objSMSGatewayProxy.Connect ( PrimaryGateway, BackupGateway, ProxyAddress, ProxyUsername, ProxyPassword );

		StringUtils objStringUtils = new StringUtils();
		String To = objStringUtils.StringArrayToCSV ( ToList );

		SendStatusCollection SendStatusList = objSMSGatewayProxy.SendBinaryMessage ( Username, Password, To, From, UserDataHexHeader, UserDataHex, RequestDeliveryRpt );

		objSMSGatewayProxy.Close();

		CheckResultCode ( SendStatusList );

		return SendStatusList;
	}

	public SendStatusCollection SendWapPushMessage ( String[] ToList, String Title, String HRef, String From ) throws IntelliSMSException
	{
		SMSGatewayProxy objSMSGatewayProxy = new SMSGatewayProxy();

		SetAppDetails ( objSMSGatewayProxy );

		objSMSGatewayProxy.Connect ( PrimaryGateway, BackupGateway, ProxyAddress, ProxyUsername, ProxyPassword );

		StringUtils objStringUtils = new StringUtils();
		String To = objStringUtils.StringArrayToCSV ( ToList );

		SendStatusCollection SendStatusList = objSMSGatewayProxy.SendWapPushMessage ( Username, Password, To, From, Title, HRef, RequestDeliveryRpt );

		objSMSGatewayProxy.Close();

		CheckResultCode ( SendStatusList );

		return SendStatusList;
	}

	public int GetBalance () throws IntelliSMSException
	{
		SMSGatewayProxy objSMSGatewayProxy = new SMSGatewayProxy();

		SetAppDetails ( objSMSGatewayProxy );

		objSMSGatewayProxy.Connect ( PrimaryGateway, BackupGateway, ProxyAddress, ProxyUsername, ProxyPassword );

		int nBalance = objSMSGatewayProxy.GetBalance ( Username, Password );

		objSMSGatewayProxy.Close();

		return nBalance;
	}

	private void SetAppDetails ( SMSGatewayProxy objSMSGatewayProxy )
	{
		objSMSGatewayProxy.SetAppDetails ( "IntelliSMSJava", "1.0.0", "", "", "" );
	}

	private void CheckResultCode ( SendStatusCollection SendStatusList ) throws IntelliSMSException
	{
		if ( SendStatusList.OverallResultCode != ResultCodes.OK )
		{
			throw new IntelliSMSException ( SendStatusList.OverallResultCode, "IntelliSMS request failed (" + SendStatusList.OverallResultCode + ")" );
		}
	}


	public String	PrimaryGateway = "www.intellisoftware.co.uk";
	public String	BackupGateway = "www.intellisoftware2.co.uk";
	public String	Username = "";
	public String	Password = "";
	public String	ProxyAddress;
	public String	ProxyUsername;
	public String	ProxyPassword;
	public boolean	RequestDeliveryRpt = false;
	public int		MaxConCatMsgs = 1;


}
