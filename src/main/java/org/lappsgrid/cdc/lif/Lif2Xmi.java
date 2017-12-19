package org.lappsgrid.cdc.lif;

import org.lappsgrid.cdc.Converter;
import org.lappsgrid.serialization.Serializer;
import org.lappsgrid.serialization.lif.Annotation;
import org.lappsgrid.serialization.lif.Container;
import org.lappsgrid.serialization.lif.View;
import org.lappsgrid.cdc.ConversionException;
import org.lappsgrid.cdc.Namespace;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.stream.Collectors;

/**
 *
 */
public class Lif2Xmi implements Converter
{
	private Document document;
	private Map<String,String> prefixMapping;

	public Lif2Xmi() throws ParserConfigurationException
	{
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		document = builder.newDocument();
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

	public String convert(InputStream stream) throws ConversionException
	{

		String json = new BufferedReader(new InputStreamReader(stream)).lines().collect(Collectors.joining("\n"));
		return convert(json);
	}

	public String convert(String json) throws ConversionException
	{
		Container container = Serializer.parse(json, Container.class);
		return convert(container);
	}

	protected String convert(Container container) throws ConversionException
	{
		Element root = document.createElementNS(Namespace.XMI, "xmi:XMI");
		root.setAttributeNS(Namespace.XMI, "xmi:version", "2.0");
		root.setAttribute("xmlns:vaers", Namespace.VAERS);
		root.setAttribute("xmlns:cas", Namespace.CAS);
		root.setAttribute("xmlns:tcas", Namespace.TCAS);
		root.setAttribute("xmlns:xmi", Namespace.XMI);

		document.appendChild(root);

		View  view = container.getView(0);
		Element cas = document.createElementNS(Namespace.CAS, "cas:NULL");
		root.appendChild(cas);
		cas.setAttributeNS(Namespace.XMI, "xmi:id", view.getId());
		for (Annotation a : view.getAnnotations())
		{
			String prefix = "vaers";
			String atType = a.getAtType();
			int index = atType.lastIndexOf('/');
			String namespace = atType.substring(0, index);
			if (namespace.endsWith("tcas.ecore"))
			{
				prefix = "tcas";
			}
			String name = prefix + ":" + atType.substring(index + 1);
			Element child = document.createElementNS(namespace, name);
			root.appendChild(child);
			child.setAttribute("begin", String.valueOf(a.getStart()));
			child.setAttribute("end", String.valueOf(a.getEnd()));
			child.setAttributeNS(Namespace.XMI, "xmi:id", a.getId());
			for(Object object : a.getFeatures().entrySet())
			{
				Map.Entry<String,String> entry = (Map.Entry<String,String>) object;
				child.setAttribute(entry.getKey(), entry.getValue());
			}
		}
		//cas:Sofa mimeType="text" sofaID="_InitialView" sofaNum="1" sofaString
		String sofaID = container.getMetadata("sofaID").toString();
		String sofaNum = container.getMetadata("sofaNum").toString();
		String xmiId = container.getMetadata("xmi:id").toString();
		Element sofa = document.createElementNS(Namespace.CAS, "cas:Sofa");
		root.appendChild(sofa);
		sofa.setAttribute("mimeType", "text");
		sofa.setAttribute("sofaID", sofaID);
		sofa.setAttribute("sofaNum", sofaNum);
		sofa.setAttribute("sofaText", container.getText());
		sofa.setAttributeNS(Namespace.XMI, "xmi:id", xmiId);

		Element element = document.createElementNS(Namespace.CAS, "cas:View");
		root.appendChild(element);
		element.setAttribute("members", view.getMetadata().get("members").toString());
		element.setAttribute("sofa", view.getMetadata().get("sofa").toString());

		String result = null;
		try
		{
			result = print();
		}
		catch (TransformerException e)
		{
			throw new ConversionException(e);
		}
		return result;
	}

	private String print() throws TransformerException
	{
		TransformerFactory factory = TransformerFactory.newInstance();
		Transformer transformer = factory.newTransformer();
		transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
		transformer.setOutputProperty(OutputKeys.METHOD, "xml");
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
		transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");		DOMSource source = new DOMSource(document);

		StringWriter writer = new StringWriter();
		StreamResult result = new StreamResult(writer);
		transformer.transform(source, result);
		return writer.toString();
	}

	public static void main(String[] args)
	{
		try
		{
			byte[] bytes = Files.readAllBytes(Paths.get("target/json/outputClinicalETHER.json"));
			String string = new String(bytes);
			Lif2Xmi writer = new Lif2Xmi();
			writer.convert(string);
			writer.print();

		}
		catch (IOException | ParserConfigurationException | TransformerException e)
		{
			e.printStackTrace();
		}
	}
}
