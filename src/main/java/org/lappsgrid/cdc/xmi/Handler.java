package org.lappsgrid.cdc.xmi;

import org.lappsgrid.serialization.LifException;
import org.lappsgrid.serialization.lif.Annotation;
import org.lappsgrid.serialization.lif.Container;
import org.lappsgrid.serialization.lif.View;
import org.lappsgrid.cdc.Namespace;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.HashMap;
import java.util.Map;

/**
 * The handler invoked by the SAX parser.
 *
 * The Handler classes dispatches to one of the other handler types based on the namespace
 * of the element.
 *
 * We make some simplifying assumptions to reduce the number of methods we need to override:
 *
 * 1. There are no nested elements other than the root XMI element.
 * 2. No element contains character data
 *
 * With these assumptions we can do everything we need to in the startElement method.
 */
public class Handler extends DefaultHandler
{
	private Map<String, BaseHandler> handlers;
	private BaseHandler unknown;
	private Container container;
	private View view;

	public Handler()
	{
		handlers = new HashMap<>();
		handlers.put(CasHandler.NAMESPACE, new CasHandler());
		handlers.put(TcasHandler.NAMESPACE, new TcasHandler());
		handlers.put(VaersHandler.NAMESPACE, new VaersHandler());
		handlers.put(XmiHandler.NAMESPACE, new XmiHandler());
		unknown = new UnknownHandler();
	}

	public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException
	{
		BaseHandler handler = handlers.get(uri);
		if (handler != null)
		{
			handler.handle(localName, atts);
		}
		else
		{
			unknown.handle(localName, atts);
		}
	}

//	public void endElement(String uri, String localName, String qName)
//	{
//		if (XmiHandler.NAMESPACE.equals(uri))
//		{
//			String json = Serializer.toPrettyJson(container);
//			System.out.println(json);
//		}
//	}

	/**
	 * Copies an attribute from the attributes parsed from the XML to a Map instance.
	 *
	 * The map instance will typically be the metadata or features for an annotation.
	 *
	 * @param map the map to be updated
	 * @param key the key used in the map and the attributes
	 * @param atts a list of attributes parsed from the XML
	 */
	protected void update(Map<String,String> map, String key, Attributes atts)
	{
		map.put(key, atts.getValue(key));
	}

	public Container getContainer()
	{
		return container;
	}

	/**
	 * Handles elements in the XMI namespace.
	 */
	class XmiHandler implements BaseHandler
	{
		public static final String NAMESPACE = Namespace.XMI;

		/**
		 * Prepare for parsing a new XMI file.  We assume there will be exactly one element, the
		 * root element, in the XMI namespace.
		 */
		public void handle(String localName, Attributes atts)
		{
			container = new Container();
			try {
				view = container.newView();
			}
			catch (LifException e) {
				// This should not happen. If it does there has been a breaking change in the
				// API classes that should be reported as a serious bug.
				throw new RuntimeException(e);
			}
		}
	}

	class CasHandler implements BaseHandler
	{
		public static final String NAMESPACE = Namespace.CAS;

		public void handle(String localName, Attributes atts) throws UnexpectedElementException
		{
			if ("NULL".equals(localName))
			{
				view.setId(atts.getValue("xmi:id"));
			}
			else if ("Sofa".equals(localName))
			{
				Map<String,String> metadata = container.getMetadata();
				update(metadata, "sofaID", atts);
				update(metadata, "sofaNum", atts);
				update(metadata, "xmi:id", atts);
				container.setText(atts.getValue("sofaString"));
			}
			else if ("View".equals(localName))
			{
				unknown.handle(localName, atts);
			}
			else
			{
				throw new UnexpectedElementException(NAMESPACE, localName);
			}
		}
	}

	class TcasHandler implements BaseHandler
	{
		public static final String NAMESPACE = Namespace.TCAS;

		public void handle(String localName, Attributes atts) throws UnexpectedElementException
		{
			if ("DocumentAnnotation".equals(localName))
			{
				String id = atts.getValue("xmi:id");
				String type = NAMESPACE + "/DocumentAnnotation";
				Annotation a = view.newAnnotation(id, type);
				a.setStart(Long.parseLong(atts.getValue("begin")));
				a.setEnd(Long.parseLong(atts.getValue("end")));
				a.addFeature("sofa", atts.getValue("sofa"));
				container.setLanguage(atts.getValue("language"));
			}
			else if ("DocumentMetaData".equals(localName))
			{
				Map<String,String> metadata = container.getMetadata();
				String[] names = {"sofa", "begin", "end", "documentTitle", "documentId", "isLastSegment"};
				for ( String name : names )
				{
					update(metadata, name, atts);
				}
			}
			else
			{
				throw new UnexpectedElementException(NAMESPACE, localName);
			}
		}
	}

	class VaersHandler implements BaseHandler
	{
		public static final String NAMESPACE = Namespace.VAERS;

		public void handle(String localName, Attributes atts)
		{
			Annotation a = view.newAnnotation();
			a.setAtType(NAMESPACE + "/" + localName);
			int n = atts.getLength();
			for (int i = 0; i < n; ++i)
			{
				String qName = atts.getQName(i);
				String value = atts.getValue(i);
				if ("xmi:id".equals(qName))
				{
					a.setId(value);
				}
				else if ("begin".equals(qName))
				{
					a.setStart(Long.parseLong(value));
				}
				else if ("end".equals(qName))
				{
					a.setEnd(Long.parseLong(value));
				}
				else
				{
					a.addFeature(qName, value);
				}
			}

		}
	}

	/**
	 * Handles all XML elements that do not declare a namespace
	 */
	class UnknownHandler implements BaseHandler
	{
		@Override
		public void handle(String localName, Attributes atts) throws UnexpectedElementException
		{
			if (!"View".equals(localName))
			{
				throw new UnexpectedElementException("DEFAULT", localName);
			}
			Map<String,String> metadata = view.getMetadata();
			update(metadata, "members", atts);
			update(metadata, "sofa", atts);
		}
	}}

