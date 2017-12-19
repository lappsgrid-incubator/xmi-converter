package org.lappsgrid.cdc.xmi;

import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileFilter;
import java.io.FileWriter;
import java.io.IOException;

/**
 *
 */
public class ParseAll
{
	public ParseAll()
	{

	}

	public void run() throws ParserConfigurationException, SAXException, IOException
	{
		Xmi2Lif parser = new Xmi2Lif();

		File dir = new File("src/test/resources/data");
		File dest = new File("target/json");
		if (!dest.exists())
		{
			if (!dest.mkdirs())
			{
				throw new IOException("Unable to create destination directory.");
			}
		}
		FileFilter filter = f -> { return f.getName().endsWith(".xml"); };
		File[] files = dir.listFiles(filter);
		for (File file : files)
		{
			String json = parser.convert(file);
			String filename = file.getName().replace(".xml", ".json");
			File outfile = new File(dest, filename);
			FileWriter writer = new FileWriter(outfile);
			writer.write(json);
			writer.close();
			System.out.println("Wrote " + outfile.getPath());
		}
	}

	public static void main(String[] args)
	{
		try
		{
			new ParseAll().run();
		}
		catch (ParserConfigurationException | SAXException | IOException e)
		{
			e.printStackTrace();
		}
	}
}
