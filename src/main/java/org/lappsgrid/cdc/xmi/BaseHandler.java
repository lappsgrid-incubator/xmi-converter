package org.lappsgrid.cdc.xmi;

import org.xml.sax.Attributes;

/**
 *
 */
interface BaseHandler {
	void handle(String localName, Attributes atts) throws UnexpectedElementException;
}
