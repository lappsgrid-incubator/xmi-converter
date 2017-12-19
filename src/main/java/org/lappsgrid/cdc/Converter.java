package org.lappsgrid.cdc;

import java.io.File;
import java.io.InputStream;

/**
 *
 */
public interface Converter
{
	String convert(File file) throws ConversionException;
	String convert(InputStream stream) throws ConversionException;
	String convert(String string) throws ConversionException;
}
