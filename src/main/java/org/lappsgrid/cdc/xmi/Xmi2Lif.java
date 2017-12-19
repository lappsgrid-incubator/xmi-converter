package org.lappsgrid.cdc.xmi;

import org.lappsgrid.cdc.ConversionException;
import org.lappsgrid.cdc.Converter;
import org.lappsgrid.serialization.Serializer;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * Reads an XMI file and return the corresponding LIF json file.
 */
public class Xmi2Lif implements Converter
{
	private SAXParser parser;
	private Handler handler;

	public Xmi2Lif() throws ParserConfigurationException, SAXException
	{
		SAXParserFactory factory = SAXParserFactory.newInstance();
		factory.setNamespaceAware(true);
		parser = factory.newSAXParser();
		handler = new Handler();
	}

	public String convert(File file) throws ConversionException
	{
		try
		{
			return convert(new FileInputStream(file));
		}
		catch (FileNotFoundException e)
		{
			throw new ConversionException(e);
		}
	}

	public String convert(String string) throws ConversionException
	{
		return convert(new ByteArrayInputStream(string.getBytes()));
	}

	public String convert(InputStream stream) throws ConversionException
	{
		try
		{
			parser.parse(stream, handler);
		}
		catch (SAXException | IOException e)
		{
			throw new ConversionException(e);
		}
		return Serializer.toPrettyJson(handler.getContainer());
	}

	public static void main(String[] args)
	{
		if (args.length == 0)
		{
			System.out.println("No input filename provided.");
			return;
		}
		try
		{
			File file = new File(args[0]);
			InputStream stream = new FileInputStream(file);
			System.out.println(new Xmi2Lif().convert(stream));
		}
		catch (SAXException | ParserConfigurationException | IOException e)
		{
			e.printStackTrace();
		}
	}
}
