// Written by Jürgen Moßgraber - mossgrabers.de
// (c) 2019-2025
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package de.mossgrabers.tools.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import de.mossgrabers.tools.XMLUtils;

import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import java.io.IOException;
import java.io.StringReader;


/**
 * Test for XML utilities.
 *
 * @author J&uuml;rgen Mo&szlig;graber
 */
class TestXML
{
    private static final String XML_RESULT = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n<root version=\"0.60\">\r\n    <element>content</element>\r\n</root>\r\n";


    /**
     * Test correct XML formatter output.
     *
     * @throws SAXException Could not parse the test XML document
     * @throws IOException Could not read the input
     * @throws ParserConfigurationException Error in the document
     * @throws TransformerException Could not transform the document to a text
     */
    @Test
    void testCRLF () throws SAXException, IOException, ParserConfigurationException, TransformerException
    {
        final String xml = "<root version=\"0.60\"><element>content</element></root>";
        final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance ();
        final DocumentBuilder builder = factory.newDocumentBuilder ();
        final Document doc = builder.parse (new InputSource (new StringReader (xml)));
        final String result = XMLUtils.toString (doc, "\r\n", 4, "UTF-8", "1.0");
        assertEquals (XML_RESULT, result);
    }
}
