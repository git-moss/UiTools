// Written by Jürgen Moßgraber - mossgrabers.de
// (c) 2019-2025
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package de.mossgrabers.tools;

import org.w3c.dom.DOMConfiguration;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSOutput;
import org.w3c.dom.ls.LSSerializer;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Properties;


/**
 * Helper functions for dealing with XML files.
 *
 * @author J&uuml;rgen Mo&szlig;graber
 */
public class XMLUtils
{
    private static final DocumentBuilderFactory NON_VALIDATING_FACTORY = DocumentBuilderFactory.newInstance ();
    private static final Properties             TRANSFORM_PROPERTIES   = new Properties ();
    private static DocumentBuilder              documentBuilder;
    private static ParserConfigurationException parseConfException;

    static
    {
        TRANSFORM_PROPERTIES.setProperty (OutputKeys.METHOD, "xml");
        TRANSFORM_PROPERTIES.setProperty (OutputKeys.ENCODING, "UTF-8");
        TRANSFORM_PROPERTIES.setProperty (OutputKeys.INDENT, "yes");
        // Forces newline, if standalone attribute is omitted
        TRANSFORM_PROPERTIES.setProperty (OutputKeys.DOCTYPE_PUBLIC, "");

        try
        {
            NON_VALIDATING_FACTORY.setValidating (false);
            NON_VALIDATING_FACTORY.setNamespaceAware (true);
            // Prevent external resource access from XML document
            NON_VALIDATING_FACTORY.setAttribute (XMLConstants.ACCESS_EXTERNAL_DTD, "");
            NON_VALIDATING_FACTORY.setAttribute (XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");

            documentBuilder = NON_VALIDATING_FACTORY.newDocumentBuilder ();
        }
        catch (final ParserConfigurationException ex)
        {
            parseConfException = ex;
            documentBuilder = null;
        }
    }


    /**
     * Private due helper class.
     */
    private XMLUtils ()
    {
        // Intentionally empty
    }


    /**
     * Parses the given input source. Does not validate against the XML schema.
     *
     * @param inputSource The input source from which to parse the XML document
     * @return The parsed document
     * @throws SAXException Could not read the document or not parse the XML
     */
    public static Document parseDocument (final InputSource inputSource) throws SAXException
    {
        if (parseConfException != null)
            throw new SAXException (parseConfException);
        try
        {
            return documentBuilder.parse (inputSource);
        }
        catch (final IOException exception)
        {
            throw new SAXException (exception);
        }
    }


    /**
     * Creates a new XML document. Does not validate against the XML schema.
     *
     * @throws ParserConfigurationException Could not instantiate the XML parser/writer
     * @return The new XML document
     */
    public static Document newDocument () throws ParserConfigurationException
    {
        if (parseConfException != null)
            throw parseConfException;
        return documentBuilder.newDocument ();
    }


    /**
     * Returns the direct sub-element of a node with the name 'name' or null if not found.
     *
     * @param parent The parent node of the sub-element to lookup
     * @param name The tag-name of the sub-element
     * @return The sub-element or null
     */
    public static Element getChildElementByName (final Node parent, final String name)
    {
        final NodeList list = parent.getChildNodes ();
        for (int i = 0; i < list.getLength (); i++)
        {
            final Node item = list.item (i);
            if (item instanceof final Element element && name.equals (item.getNodeName ()))
                return element;
        }
        return null;
    }


    /**
     * Returns the direct sub-elements of a node.
     *
     * @param parent The parent node of the sub-elements to lookup
     * @return The sub-elements or an empty list if none is found
     */
    public static List<Element> getChildElements (final Node parent)
    {
        final NodeList list = parent.getChildNodes ();
        final int size = list.getLength ();
        final List<Element> children = new ArrayList<> (size);
        for (int i = 0; i < size; i++)
        {
            final Node item = list.item (i);
            if (item instanceof final Element element)
                children.add (element);
        }
        return children;
    }


    /**
     * Returns the direct sub-elements of a node with the name 'name'.
     *
     * @param parent The parent node of the sub-elements to lookup
     * @param name The tag-name of the sub-elements
     * @return The sub-elements or an empty list
     */
    public static List<Element> getChildElementsByName (final Node parent, final String name)
    {
        return getChildElementsByName (parent, name, false);
    }


    /**
     * Returns the direct or recursive sub-elements of a node with the name 'name'.
     *
     * @param parent The parent node of the sub-elements to lookup
     * @param name The tag-name of the sub-elements
     * @param recursive Get recursively all sub-children, get only the direct ones if false
     * @return The sub-elements or an empty list
     */
    public static List<Element> getChildElementsByName (final Node parent, final String name, final boolean recursive)
    {
        final NodeList list;
        if (recursive)
        {
            if (!(parent instanceof final Element parentElement))
                return Collections.emptyList ();
            list = parentElement.getElementsByTagName (name);
        }
        else
            list = parent.getChildNodes ();

        final int size = list.getLength ();
        final List<Element> children = new ArrayList<> (size);
        for (int i = 0; i < size; i++)
        {
            final Node item = list.item (i);
            if (item instanceof final Element element && name.equals (item.getNodeName ()))
                children.add (element);
        }
        return children;
    }


    /**
     * Reads a string value from a XML attribute.
     *
     * @param parent The XML node to which the attribute belongs
     * @param name The name of the XML attribute
     * @return The attributes value or null if not found
     */
    public static String read (final Element parent, final String name)
    {
        final Element node = getChildElementByName (parent, name);
        return node == null ? null : readTextContent (node);
    }


    /**
     * Reads the text content of a node. If the node has no children null is returned. If it
     * contains several CDATA sections the content of all sections are concatenated and returned. If
     * it contains only one text the trimmed result is returned.
     *
     * @param node The node from which to get the text content
     * @return The text or an empty string
     */
    public static String readTextContent (final Node node)
    {
        final Node first = node.getFirstChild ();
        if (first == null)
            return "";

        String content = first.getNodeValue ();
        if (content == null)
            content = "";
        final NodeList list = node.getChildNodes ();
        // If there is more than one node, take the data of the node
        // which is a CDATA section. If there is no CDATA section use
        // the first text node.
        final StringBuilder builder = new StringBuilder ();
        for (int i = 0; i < list.getLength (); i++)
            if (list.item (i).getNodeType () == Node.CDATA_SECTION_NODE)
                builder.append (list.item (i).getNodeValue ());

        // !!! This is not nice, but the JAXP parser seems to add also
        // the returns that surround the CDATA section to the data
        // in the CDATA section. !!!
        return builder.isEmpty () ? content.trim () : builder.toString ();
    }


    /**
     * Returns the text content of a sub-element of a node with the name 'name' or null if not
     * found.
     *
     * @param parent The parent node of the sub-element to lookup
     * @param name The tag-name of the sub-element
     * @return The sub-elements' content or null
     */
    public static String getChildElementContent (final Node parent, final String name)
    {
        final Element contentElement = getChildElementByName (parent, name);
        return contentElement == null ? "" : readTextContent (contentElement);
    }


    /**
     * Returns the text content interpreted as an integer of a sub-element of a node with the name
     * 'name' or null if not found.
     *
     * @param parent The parent node of the sub-element to lookup
     * @param name The tag-name of the sub-element
     * @param defaultValue The default value to return if the element is not present or does not
     *            contain a valid integer
     * @return The sub-elements' integer content or null
     */
    public static int getChildElementIntegerContent (final Node parent, final String name, final int defaultValue)
    {
        final String content = getChildElementContent (parent, name);
        if (content.isBlank ())
            return defaultValue;
        try
        {
            return Integer.parseInt (content);
        }
        catch (final NumberFormatException ex)
        {
            return defaultValue;
        }
    }


    /**
     * Returns the text content interpreted as an integer of a sub-element of a node with the name
     * 'name' or null if not found.
     *
     * @param parent The parent node of the sub-element to lookup
     * @param name The tag-name of the sub-element
     * @param defaultValue The default value to return if the element is not present or does not
     *            contain a valid double
     * @return The sub-elements' integer content or null
     */
    public static double getChildElementDoubleContent (final Node parent, final String name, final double defaultValue)
    {
        final String content = getChildElementContent (parent, name);
        if (content.isBlank ())
            return defaultValue;
        try
        {
            return Double.parseDouble (content);
        }
        catch (final NumberFormatException ex)
        {
            return defaultValue;
        }
    }


    /**
     * Get an integer attribute from an element.
     *
     * @param element The element
     * @param attributeName The name of the attribute from which to get the value
     * @param defaultValue If the attribute is not present or it does not contain a valid integer
     *            this default value is returned
     * @return The value
     */
    public static int getIntegerAttribute (final Element element, final String attributeName, final int defaultValue)
    {
        final String attribute = element.getAttribute (attributeName);
        if (attribute == null)
            return defaultValue;
        try
        {
            return Integer.parseInt (attribute);
        }
        catch (final NumberFormatException ex)
        {
            return defaultValue;
        }
    }


    /**
     * Get a double attribute from an element.
     *
     * @param element The element
     * @param attributeName The name of the attribute from which to get the value
     * @param defaultValue If the attribute is not present or it does not contain a valid double
     *            this default value is returned
     * @return The value
     */
    public static double getDoubleAttribute (final Element element, final String attributeName, final double defaultValue)
    {
        final String attribute = element.getAttribute (attributeName);
        if (attribute == null)
            return defaultValue;
        try
        {
            return Double.parseDouble (attribute);
        }
        catch (final NumberFormatException ex)
        {
            return defaultValue;
        }
    }


    /**
     * Get a boolean attribute (false/true) from an element.
     *
     * @param element The element
     * @param attributeName The name of the attribute from which to get the value
     * @param defaultValue If the attribute is not present or it does not contain a valid boolean
     *            this default value is returned
     * @return The value
     */
    public static boolean getBooleanAttribute (final Element element, final String attributeName, final boolean defaultValue)
    {
        final String attribute = element.getAttribute (attributeName);
        return attribute == null ? defaultValue : Boolean.parseBoolean (attribute);
    }


    /**
     * Adds a child element with a text content.
     *
     * @param document The document to create the element
     * @param parentElement The parent element where to add the new text element
     * @param elementName The name of the text element
     * @param text The text content of the element
     */
    public static void addTextElement (final Document document, final Element parentElement, final String elementName, final String text)
    {
        addElement (document, parentElement, elementName).setTextContent (text);
    }


    /**
     * Adds a child element.
     *
     * @param document The document to create the element
     * @param parentElement The parent element where to add the new element
     * @param elementName The name of the element
     * @return The added element
     */
    public static Element addElement (final Document document, final Element parentElement, final String elementName)
    {
        final Element childElement = document.createElement (elementName);
        parentElement.appendChild (childElement);
        return childElement;
    }


    /**
     * Set an integer attribute on an element.
     *
     * @param element The element
     * @param attributeName The name of the attribute
     * @param value The value to set
     */
    public static void setIntegerAttribute (final Element element, final String attributeName, final int value)
    {
        element.setAttribute (attributeName, Integer.toString (value));
    }


    /**
     * Set a double attribute on an element.
     *
     * @param element The element
     * @param attributeName The name of the attribute
     * @param value The value to set
     * @param fractions The number of fractions to format
     */
    public static void setDoubleAttribute (final Element element, final String attributeName, final double value, final int fractions)
    {
        final String formatPattern = "%." + fractions + "f";
        element.setAttribute (attributeName, String.format (Locale.US, formatPattern, Double.valueOf (value)));
    }


    /**
     * Set a boolean attribute on an element.
     *
     * @param element The element
     * @param attributeName The name of the attribute
     * @param value The value to set
     */
    public static void setBooleanAttribute (final Element element, final String attributeName, final boolean value)
    {
        element.setAttribute (attributeName, Boolean.toString (value));
    }


    /**
     * Formats the XML document into a string. Uses the Transformer API.
     *
     * @param document The XML document
     * @return The created text
     * @throws TransformerException Could not transform the document
     */
    public static String toString (final Document document) throws TransformerException
    {
        final TransformerFactory factory = TransformerFactory.newInstance ();
        factory.setAttribute (XMLConstants.ACCESS_EXTERNAL_DTD, "");
        factory.setAttribute (XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");
        final Transformer transformer = factory.newTransformer ();
        transformer.setOutputProperties (TRANSFORM_PROPERTIES);
        final Writer writer = new StringWriter ();
        transformer.transform (new DOMSource (document), new StreamResult (writer));
        return writer.toString ();
    }


    /**
     * Formats the XML document into a string. Allows control over the line-breaks. Uses
     * LSSerializer.
     *
     * @param document The XML document
     * @param newLine The characters to use for the new line break
     * @return The created text
     * @throws TransformerException Could not transform the document
     */
    public static String toString (final Document document, final String newLine) throws TransformerException
    {
        final DOMImplementationLS dom;
        try
        {
            dom = (DOMImplementationLS) DOMImplementationRegistry.newInstance ().getDOMImplementation ("LS");
        }
        catch (final ClassNotFoundException | InstantiationException | IllegalAccessException | ClassCastException ex)
        {
            throw new TransformerException (ex);
        }
        final LSSerializer serializer = dom.createLSSerializer ();
        serializer.setNewLine (newLine);

        // Setting the newline does have no effect until pretty print is on
        final DOMConfiguration domConfig = serializer.getDomConfig ();
        domConfig.setParameter ("format-pretty-print", Boolean.TRUE);
        domConfig.setParameter ("xml-declaration", Boolean.FALSE);

        final LSOutput destination = dom.createLSOutput ();
        destination.setEncoding (StandardCharsets.UTF_8.name ());

        final Writer writer = new StringWriter ();
        destination.setCharacterStream (writer);
        serializer.write (document, destination);

        // Stupidly, no newline is added after XML header, so we need to add it ourselves
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + newLine + writer.toString ();
    }


    /**
     * Formats the XML document into a string. Provides control over line-breaks, indentation and
     * the XML header. Uses XMLStreamWriter and does currently not support all node types.
     *
     * @param document The XML document
     * @param newLine The characters to use for the new line break
     * @param indent The number of spaces to indent the tags
     * @param encoding The encoding inserted into the header, null to remove
     * @param version The XML version to insert into the header
     * @return The created text
     * @throws TransformerException Could not transform the document
     */
    public static String toString (final Document document, final String newLine, final int indent, final String encoding, final String version) throws TransformerException
    {
        final XMLOutputFactory factory = XMLOutputFactory.newInstance ();

        try
        {
            final StringWriter stringWriter = new StringWriter ();
            final XMLStreamWriter writer = factory.createXMLStreamWriter (stringWriter);
            writer.writeStartDocument (encoding, version);
            writer.writeCharacters (newLine);
            writeNode (document.getDocumentElement (), writer, newLine, indent, 0);
            writer.flush ();
            writer.close ();
            return stringWriter.toString ();
        }
        catch (final Exception ex)
        {
            throw new TransformerException (ex);
        }
    }


    private static void writeNode (final Node node, final XMLStreamWriter writer, final String newLine, final int indent, final int level) throws Exception
    {
        final String indentSpaces = " ".repeat (indent * level);

        final short nodeType = node.getNodeType ();
        switch (nodeType)
        {
            case Node.ELEMENT_NODE:
                writer.writeCharacters (indentSpaces);

                final boolean emptyElement = !node.hasChildNodes ();
                if (emptyElement)
                    writer.writeEmptyElement (node.getNodeName ());
                else
                    writer.writeStartElement (node.getNodeName ());

                final NamedNodeMap attributes = node.getAttributes ();
                for (int i = 0; i < attributes.getLength (); i++)
                {
                    final Node attribute = attributes.item (i);
                    writer.writeAttribute (attribute.getNodeName (), attribute.getNodeValue ());
                }

                final boolean hasElements = node.getChildNodes ().item (0) instanceof Element;
                if (emptyElement || hasElements)
                    writer.writeCharacters (newLine);

                if (!emptyElement)
                {
                    final NodeList children = node.getChildNodes ();
                    for (int i = 0; i < children.getLength (); i++)
                    {
                        final Node child = children.item (i);
                        writeNode (child, writer, newLine, indent, level + 1);
                    }
                    if (hasElements)
                        writer.writeCharacters (indentSpaces);
                    writer.writeEndElement ();
                    writer.writeCharacters (newLine);
                }
                break;

            case Node.TEXT_NODE:
                writer.writeCharacters (node.getNodeValue ());
                break;

            default:
                throw new TransformerException ("Node type not implemented: " + nodeType);
        }
    }
}
