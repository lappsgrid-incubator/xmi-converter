package org.lappsgrid.cdc.xmi;

import org.xml.sax.SAXException;

/**
 *
 */
public class UnexpectedElementException extends SAXException
{
	public UnexpectedElementException(String namespace, String localName)
	{
		super(String.format("Unexpected element %s in namespace %s", localName, namespace));
	}
}
