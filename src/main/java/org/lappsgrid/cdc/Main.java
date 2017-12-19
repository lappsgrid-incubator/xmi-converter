package org.lappsgrid.cdc;

import org.lappsgrid.cdc.lif.Lif2Xmi;
import org.lappsgrid.cdc.xmi.Xmi2Lif;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;

/**
 *
 */
public class Main
{
	public Main()
	{

	}

	public void convert(File file) throws ParserConfigurationException, SAXException, ConversionException
	{
		Converter converter = null;
		String name = file.getName();
		if (name.endsWith(".xml") || name.endsWith(".xmi"))
		{
			converter = new Xmi2Lif();
		}
		else if (name.endsWith(".json") || name.endsWith(".lif"))
		{
			converter = new Lif2Xmi();
		}
		else
		{
			System.out.println("Unsupported file type. Only .xml and .json are supported at this time.");
			return;
		}
		System.out.println(converter.convert(file));
	}

	public static void main(String[] args)
	{
		if (args.length == 0)
		{
			System.out.println("No input file name provided.");
			return;
		}
		File file = new File(args[0]);
		try
		{
			new Main().convert(file);
		}
		catch (ParserConfigurationException e)
		{
			e.printStackTrace();
		}
		catch (SAXException e)
		{
			e.printStackTrace();
		}
		catch (ConversionException e)
		{
			e.printStackTrace();
		}
	}
}
