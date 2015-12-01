/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.platform.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.platform.serialization.SerializationUtility;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Utility class for often used operations on XML DOMs ({@link Document}).
 *
 * @since 5.1
 */
public final class XmlUtility {

  public static final String INDENT_AMOUNT_TRANSFORMER_PROPERTY = "{http://xml.apache.org/xslt}indent-amount";

  private XmlUtility() {
  }

  /**
   * Gets all child {@link Element}s having given tagName and an attribute with the given value.
   *
   * @param parent
   *          The parent {@link Element}
   * @param tagName
   *          The tag name of the child elements.
   * @param requiredAttributeName
   *          The name of the attribute the child elements must have.
   * @param requiredAttributeValue
   *          The value of the attribute with given name.
   * @return A {@link List} with all child {@link Element} having given tagName and an attribute with the given value.
   */
  public static List<Element> getChildElementsWithAttributes(Element parent, String tagName, String requiredAttributeName, String requiredAttributeValue) {
    NodeList endpoints = parent.getElementsByTagName(tagName);
    List<Element> result = new LinkedList<Element>();
    for (int i = 0; i < endpoints.getLength(); i++) {
      Node n = endpoints.item(i);
      if (n.getNodeType() == Node.ELEMENT_NODE) {
        Element e = (Element) n;
        if (e.hasAttribute(requiredAttributeName)) {
          String val = e.getAttribute(requiredAttributeName);
          if (CompareUtility.equals(requiredAttributeValue, val)) {
            result.add(e);
          }
        }
      }
    }
    return result;
  }

  /**
   * Removes all attributes from given {@link Element}.
   *
   * @param element
   *          The element
   */
  public static void clearAttributes(Element element) {
    NamedNodeMap attributes = element.getAttributes();
    for (int i = 0; i < attributes.getLength(); i++) {
      element.removeAttribute(attributes.item(i).getNodeName());
    }
  }

  /**
   * Gets the content of the attribute with given name from the given {@link Element}. The content is then deserialized
   * into the original object.<br>
   * This method is intended to be used with {@link #setObjectAttribute(Element, String, Object)}.
   *
   * @param element
   *          The element on which the attribute exists.
   * @param attribName
   *          The name of the attribute.
   * @return The deserialized object that is stored in the given attribute.
   * @throws IOException
   *           If there is an error deserializing the data.
   * @throws ClassNotFoundException
   *           If there is an error deserializing the data.
   */
  public static Object getObjectAttribute(Element element, String attribName) throws IOException, ClassNotFoundException {
    Object o = null;
    String base64 = element.getAttribute(attribName);
    if (base64.length() > 0) {
      byte[] raw = Base64Utility.decode(base64);
      o = SerializationUtility.createObjectSerializer().deserialize(raw, null);
    }
    return o;
  }

  /**
   * Saves the given object into an attribute with given name on the given {@link Element}.
   *
   * @param element
   *          The element on which the given object should be saved.
   * @param attribName
   *          The attribute name in which the given object should be saved.
   * @param o
   *          The object that should be stored.
   * @throws IOException
   *           If there is an error
   */
  public static void setObjectAttribute(Element element, String attribName, Object o) throws IOException {
    String base64 = null;
    if (o != null) {
      byte[] data = SerializationUtility.createObjectSerializer().serialize(o);
      base64 = Base64Utility.encode(data).trim();
    }
    element.setAttribute(attribName, base64);
  }

  /**
   * Wellforms the given xml {@link Document} and flushes the result into the given output stream.
   *
   * @param document
   *          The document to wellform.
   * @param out
   *          The {@link OutputStream} that takes the wellformed document.
   * @throws ProcessingException
   *           if there is an exception wellforming the document.
   */
  public static void wellformDocument(Document document, OutputStream out) {
    wellformDocument(document, new StreamResult(out));
    try {
      out.flush();
    }
    catch (IOException e) {
      throw new ProcessingException("unable to flush xml document to output.", e);
    }
  }

  /**
   * Wellforms the given xml {@link Document} and flushes the result into the given {@link File}.
   *
   * @param document
   *          The document to wellform.
   * @param f
   *          The {@link File} where the result should be saved.
   * @throws ProcessingException
   *           if there is an exception wellforming the document or saving the result in the file.
   */
  public static void wellformDocument(Document document, File f) {
    File dir = f.getParentFile();
    if (!dir.exists()) {
      boolean success = dir.mkdirs();
      if (!success) {
        throw new ProcessingException("Unable to create directories for file '" + f.getAbsolutePath() + "'.");
      }
    }
    try (OutputStream out = new FileOutputStream(f, false)) {
      wellformDocument(document, out);
    }
    catch (IOException e) {
      throw new ProcessingException("Exception writing xml file: '" + f.getAbsolutePath() + "'.", e);
    }
  }

  /**
   * Wellforms the given xml {@link Document} and flushes the result into the given {@link Writer}
   *
   * @param document
   *          The document to wellform.
   * @param writer
   *          The {@link Writer} where the result should be saved.
   * @throws ProcessingException
   *           if there is an exception wellforming the document or saving the result in the {@link Writer}.
   */
  public static void wellformDocument(Document document, Writer writer) {
    wellformDocument(document, new StreamResult(writer));
    try {
      writer.flush();
    }
    catch (IOException e) {
      throw new ProcessingException("unable to flush xml document to writer.", e);
    }
  }

  private static void wellformDocument(Document document, Result result) {
    try {
      // format transformer
      TransformerFactory tf = TransformerFactory.newInstance();
      Transformer transformer = tf.newTransformer();
      transformer.setOutputProperty(OutputKeys.INDENT, "yes");
      transformer.setOutputProperty(INDENT_AMOUNT_TRANSFORMER_PROPERTY, "2");

      // do transform
      transformer.transform(new DOMSource(document), result);
    }
    catch (TransformerException e) {
      throw new ProcessingException("Unable to wellform xml.", e);
    }
  }

  /**
   * Wellforms the given xml {@link Document} and returns the result as {@link String}.
   *
   * @param document
   *          The document to wellform
   * @return A {@link String} containing the xml {@link Document} content.
   * @throws ProcessingException
   *           if there is an exception wellforming the document.
   */
  public static String wellformDocument(Document document) {
    StringWriter writer = new StringWriter();
    wellformDocument(document, new StreamResult(writer));
    return writer.getBuffer().toString();
  }

  /**
   * Takes the given xml input, creates an xml {@link Document} and returnes it as wellformed {@link String}.
   *
   * @param rawXml
   *          The xml input.
   * @return A {@link String} containing the wellformed xml.
   * @throws ProcessingException
   *           if there is an exception wellforming the input xml.
   */
  public static String wellformXml(String rawXml) {
    return wellformDocument(getXmlDocument(rawXml));
  }

  /**
   * Creates a new empty XML {@link Document} with a root tag with given tag name.<br>
   * The root element can be retrieved using {@link Document#getDocumentElement()}.
   *
   * @param rootTagName
   *          The name of the root tag.
   * @return The new document.
   */
  public static Document createNewXmlDocument(String rootTagName) {
    try {
      DocumentBuilder docBuilder = getDocumentBuilder();
      Document document = docBuilder.newDocument();
      document.appendChild(document.createElement(rootTagName));
      return document;
    }
    catch (ParserConfigurationException e) {
      return null;
    }
  }

  /**
   * Creates an xml {@link Document} filled with the given {@link InputStream}.
   *
   * @param is
   *          The input stream to use as data source.
   * @return A xml {@link Document} containing the data of the given {@link InputStream}.
   * @throws ProcessingException
   *           if there is an error reading from the {@link InputStream} or parsing the content.
   */
  public static Document getXmlDocument(InputStream is) {
    try {
      return getDocumentBuilder().parse(is);
    }
    catch (IOException | SAXException | ParserConfigurationException e) {
      throw new ProcessingException("Unable to load xml document.", e);
    }
  }

  /**
   * Creates an xml {@link Document} filled with the content of the given {@link File}.
   *
   * @param f
   *          The {@link File} to use as data source.
   * @return A xml {@link Document} containing the data of the given {@link File}.
   * @throws ProcessingException
   *           if there is an error reading from the {@link File} or parsing the content.
   */
  public static Document getXmlDocument(File f) {
    try {
      return getDocumentBuilder().parse(f);
    }
    catch (IOException | SAXException | ParserConfigurationException e) {
      throw new ProcessingException("Unable to load xml document.", e);
    }
  }

  /**
   * Creates an xml {@link Document} filled with the content of the given {@link URL}.
   *
   * @param url
   *          The {@link URL} to use as data source.
   * @return A xml {@link Document} containing the data of the given {@link URL}.
   * @throws ProcessingException
   *           if there is an error reading from the {@link URL} or parsing the content.
   */
  public static Document getXmlDocument(URL url) {
    try (InputStream is = url.openStream()) {
      return XmlUtility.getXmlDocument(is);
    }
    catch (IOException e) {
      throw new ProcessingException("Unable to open URL '" + url.toExternalForm() + "'.", e);
    }
  }

  /**
   * Creates an xml {@link Document} filled with the content of the given {@link String}.
   *
   * @param rawXml
   *          The {@link String} holding the xml content.
   * @return A xml {@link Document} containing the data of the given {@link String}.
   * @throws ProcessingException
   *           if there is an error parsing the content of the {@link String} into a {@link Document}.
   */
  public static Document getXmlDocument(String rawXml) {
    try {
      return getDocumentBuilder().parse(new InputSource(new StringReader(rawXml)));
    }
    catch (IOException | SAXException | ParserConfigurationException e) {
      throw new ProcessingException("Unable to load xml document.", e);
    }
  }

  private static DocumentBuilder getDocumentBuilder() throws ParserConfigurationException {
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    DocumentBuilder docBuilder = factory.newDocumentBuilder();
    return docBuilder;
  }

  /**
   * Gets the first child {@link Element} of the given parent {@link Element} that has the given tag name.
   *
   * @param parent
   *          The parent {@link Element}
   * @param tagName
   *          The tag name the child {@link Element} must have. May be null. Then the first child element with any name
   *          is returned.
   * @return The first child {@link Element} of the given parent {@link Element} having given tag name.
   */
  public static Element getFirstChildElement(Element parent, String tagName) {
    NodeList children = null;
    if (tagName == null) {
      children = parent.getChildNodes();
    }
    else {
      children = parent.getElementsByTagName(tagName);
    }
    for (int i = 0; i < children.getLength(); i++) {
      Node n = children.item(i);
      if (n.getNodeType() == Node.ELEMENT_NODE) {
        return (Element) n;
      }
    }
    return null;
  }

  /**
   * Gets all child {@link Element}s of the given parent {@link Element}.
   *
   * @param parent
   *          The parent element.
   * @return A {@link List} containing all child {@link Element} of the given parent {@link Element}.
   */
  public static List<Element> getChildElements(Element parent) {
    return getChildElements(parent, null);
  }

  /**
   * Gets all child {@link Element}s of the given parent {@link Element} having the given tag name.
   *
   * @param parent
   *          The parent {@link Element}.
   * @param tagName
   *          The tag name of the child {@link Element}s to return. May be null. In this case all child {@link Element}s
   *          are returned.
   * @return A {@link List} containing all child {@link Element} of given parent {@link Element} having the given tag
   *         name.
   */
  public static List<Element> getChildElements(Element parent, String tagName) {
    NodeList children = null;
    if (tagName == null) {
      children = parent.getChildNodes();
    }
    else {
      children = parent.getElementsByTagName(tagName);
    }
    List<Element> result = new ArrayList<Element>(children.getLength());
    for (int i = 0; i < children.getLength(); i++) {
      Node n = children.item(i);
      if (n.getNodeType() == Node.ELEMENT_NODE) {
        result.add((Element) n);
      }
    }
    return result;
  }
}
