package IntelliSoftware.Common;

import java.io.*;


public class StringUtils
{
	public static String StringArrayToCSV ( String[] ArrayIn )
	{
		return StringArrayToCSV ( ArrayIn, "," );
	}

	public static String StringArrayToCSV ( String[] ArrayIn, String Separator )
	{
		return StringArrayToCSV( ArrayIn, Separator, "" );
	}

	public static String StringArrayToCSV ( String[] ArrayIn, String Separator, String Prefix )
	{
		String CSV = "";

		int ArrayInLength = ArrayIn.length;
		for ( int nIdx=0; nIdx<ArrayInLength; nIdx++ )
		{
			String Str = ArrayIn[nIdx];

			if ( CSV.length()!=0 )
			{
				CSV += Separator;
			}

			CSV += Prefix + Str;
		}

		return CSV;
	}

	public static String TruncateString ( String StringIn, int MaxLength )
	{
		if ( StringIn==null || StringIn.length()<=MaxLength )
		{
			return StringIn;
		}
		else
		{
			return StringIn.substring ( 0, MaxLength );
		}
	}

	public static int LastindexOfAny ( String StringIn, String FindCharList )
	{
		int nFindIdx = -1;

		int nFindCharListLength = FindCharList.length();
		for ( int nIdx=0; nIdx<nFindCharListLength; nIdx++ )
		{
			char ch = FindCharList.charAt(nIdx);

			int nThisCharFindIdx = StringIn.lastIndexOf ( ch );

			if ( nThisCharFindIdx > nFindIdx )
			{
				nFindIdx = nThisCharFindIdx;
			}
		}

		return nFindIdx;
	}
}

