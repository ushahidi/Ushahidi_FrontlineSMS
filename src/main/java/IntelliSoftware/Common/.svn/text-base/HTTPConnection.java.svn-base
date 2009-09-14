package IntelliSoftware.Common;

import java.io.*;
import java.net.*;
import java.security.*;

public class HTTPConnection
{
	private boolean m_bUseProxy = false;
	private String m_sProxyAddress = null;
	private String m_sProxyUsername = null;
	private String m_sProxyPassword = null;

	public int Timeout = 60 * 1000;
	public String UserAgent = "IntelliSoftware";


	private static boolean m_bSSLInitialised = false;


	public HTTPConnection()
	{
	}

	public void Open ( boolean bUseProxy, String sProxyAddress, String sProxyUsername, String sProxyPassword )
	{
		m_bUseProxy = bUseProxy;
		m_sProxyAddress = sProxyAddress;
		m_sProxyUsername = sProxyUsername;
		m_sProxyPassword = sProxyPassword;
	}

	public void Close ()
	{
	}


	public boolean IsConnected()
	{
		return false;
	}


	public String HTTPPost ( String sURL, String sPostData ) throws MalformedURLException, IOException
	{
		return HTTPPost ( sURL, sPostData, "application/x-www-form-urlencoded" );
	}

	public String HTTPPost ( String sURL, String sPostData, String ContentType ) throws MalformedURLException, IOException
	{
		return HTTPRequest ( "POST", sURL, ContentType, sPostData );
	}

	private String HTTPRequest ( String sMethod, String sUrl, String sContentType, String sFormData ) throws MalformedURLException, IOException
	{
		URL objURL = new URL(sUrl);

		URLConnection objURLConnection = objURL.openConnection();

		if ( sUrl.startsWith("https:") )
		{
			if ( !m_bSSLInitialised )
			{
				//Only one of the following two blocks (1-2) should be uncommented
				//If you are have compilation problem and you do not need SSL then comment-out both blocks 1 and 2

				//=====================================================================
				// OPTION 1 - JDK 1.2-compatible virtual machines (includes JDK 1.2 on Microsoft Platform)
				//=====================================================================
				//
				//NOTE: The technique for using JDK 1.2-compatible VMs relies primarily on the Java Secure Sockets Extension (JSSE) 1.0.1. 
				//      Before that technique will work, you must install the JSSE and add it to the class path of the client VM in question
				//
				System.setProperty("java.protocol.handler.pkgs","com.sun.net.ssl.internal.www.protocol");
				Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider());


				//=====================================================================
				// OPTION 2 - Microsoft JView VM
				//=====================================================================
				//URL.setURLStreamHandlerFactory(new com.ms.net.wininet.WininetStreamHandlerFactory());

				m_bSSLInitialised = true;
			}
		}

		objURLConnection.setRequestProperty( "Content-Type", sContentType ); 

		objURLConnection.setDoOutput(true);

		//Write Form Data
		OutputStreamWriter objOutputStreamWriter = new OutputStreamWriter(objURLConnection.getOutputStream());
		objOutputStreamWriter.write(sFormData);
		objOutputStreamWriter.flush();

		//Get Response
		StringBuilder objStringBuilder = new StringBuilder();
		BufferedReader objBufferedReader = new BufferedReader(new InputStreamReader(objURLConnection.getInputStream()));
		String sLine;
		while ((sLine = objBufferedReader.readLine()) != null) 
		{
			objStringBuilder.append ( sLine + "\n" ) ;
		}

		objOutputStreamWriter.close();
		objBufferedReader.close();
	
		return objStringBuilder.toString();
	} 
}

