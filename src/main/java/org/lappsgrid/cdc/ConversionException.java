package org.lappsgrid.cdc;

import java.io.IOException;

/**
 *
 */
public class ConversionException extends IOException
{
	public ConversionException()
	{

	}

	public ConversionException(String message)
	{
		super(message);
	}

	public ConversionException(String message, Throwable cause)
	{
		super(message, cause);
	}

	public ConversionException(Throwable cause)
	{
		super(cause);
	}
}
