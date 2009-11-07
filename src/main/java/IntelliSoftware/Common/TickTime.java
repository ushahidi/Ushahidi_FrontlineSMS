package IntelliSoftware.Common;

import java.io.*;

public class TickTime
{
	long m_TickCount = 0;
	boolean m_bNULLTime = true;

	public TickTime()
	{

	}

	public TickTime ( long TickCount )
	{
		m_TickCount = TickCount;
		m_bNULLTime = false;
	}


	public static TickTime GetCurrentTime()
	{
		return new TickTime ( System.currentTimeMillis() );

	}


	//Does the same as the constructor, but included so that code
	//that uses it will be more readable
	static public TickTime NULLTime()
	{
		return new TickTime();
	}


	public String toString()
	{
		if ( m_bNULLTime )
		{
			return "null";
		}
		else
		{
			return Long.toString(m_TickCount);
		}
	}


	public TickTime AddMilliseconds ( long nMillieseconds )
	{
		if ( !m_bNULLTime )
		{
			m_TickCount += nMillieseconds;
		}

		return this;
	}


	public TickTime SubtractMilliseconds( long nMillieseconds )
	{
		if ( !m_bNULLTime )
		{
			m_TickCount -= nMillieseconds;
		}

		return this;
	}


	//Is Time greater than TimeIn
	public static boolean IsGreaterThan ( TickTime TimeIn, TickTime Time )
	{
		if ( Time.m_bNULLTime )
		{
			return false;
		}
		else if ( TimeIn.m_bNULLTime )
		{
			return true;
		}
		else
		{
			return Time.m_TickCount > TimeIn.m_TickCount;
		}
	}


	//Is Time less than TimeIn
	public static boolean IsLessThan ( TickTime TimeIn, TickTime Time )
	{
		if ( Time.m_bNULLTime )
		{
			return true;
		}
		else if ( TimeIn.m_bNULLTime )
		{
			return false;
		}
		else
		{
			return Time.m_TickCount < TimeIn.m_TickCount;
		}
	}


	public boolean IsNULL()
	{
		return m_bNULLTime;
	}
}

