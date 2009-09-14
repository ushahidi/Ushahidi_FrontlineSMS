package IntelliSoftware.Common;

import java.io.*;

public class HexEncoder
{
	public HexEncoder()
	{
	}

	public static String EncodeFromUnicode ( String UnicodeStringIn )
	{
		StringBuilder AsciiHexString = new StringBuilder();

		int nUnicodeStringInLength = UnicodeStringIn.length();
		for ( int nIdx=0; nIdx<nUnicodeStringInLength; ++nIdx )
		{
			char Ch = UnicodeStringIn.charAt(nIdx);
			
			AsciiHexString.append ( ToHexString ( (int)Ch, 4 ) );
		}

		return AsciiHexString.toString();
	}
	
	public static String ToHexString ( int nNumber, int nPadToLength )
	{
		String sHex = Integer.toHexString(nNumber);
		
		while ( sHex.length() < nPadToLength )
		{
			sHex = "0" + sHex;
		}

		return sHex.toUpperCase();
	}
}

