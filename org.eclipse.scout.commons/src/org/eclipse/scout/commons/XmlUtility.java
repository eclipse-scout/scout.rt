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
package org.eclipse.scout.commons;

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

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.serialization.SerializationUtility;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 *
 */
public final class XmlUtility {
  private XmlUtility() {
  }

  public static List<Element> getChildElementsWithAttributes(Element parent, String tagName, String requiredAttributeName, String requiredAttributeValue) {
    NodeList endpoints = parent.getElementsByTagName(tagName);
    LinkedList<Element> result = new LinkedList<Element>();
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

  public static void clearAttributes(Element element) {
    NamedNodeMap attributes = element.getAttributes();
    for (int i = 0; i < attributes.getLength(); i++) {
      element.removeAttribute(attributes.item(i).getNodeName());
    }
  }

  public static Object getObjectAttribute(Element element, String attribName) throws IOException, ClassNotFoundException {
    Object o = null;
    String base64 = element.getAttribute(attribName);
    if (base64.length() > 0) {
      byte[] raw = Base64Utility.decode(base64);
      o = SerializationUtility.createObjectSerializer().deserialize(raw, null);
    }
    return o;
  }

  public static void setObjectAttribute(Element element, String attribName, Object o) throws IOException {
    String base64 = null;
    if (o != null) {
      byte[] data = SerializationUtility.createObjectSerializer().serialize(o);
      base64 = Base64Utility.encode(data).trim();
    }
    element.setAttribute(attribName, base64);
  }

  public static void wellformDocument(Document document, OutputStream out) throws ProcessingException {
    wellformDocument(document, new StreamResult(out));
  }

  public static void wellformDocument(Document document, File f) throws ProcessingException {
    File dir = f.getParentFile();
    if (!dir.exists()) {
      boolean success = dir.mkdirs();
      if (!success) {
        throw new ProcessingException("Unable to create directories for file '" + f.getAbsolutePath() + "'.");
      }
    }
    OutputStream out = null;
    try {
      out = new FileOutputStream(f, false);
      wellformDocument(document, out);
    }
    catch (IOException e) {
      throw new ProcessingException("Exception writing xml file: '" + f.getAbsolutePath() + "'.", e);
    }
    finally {
      if (out != null) {
        try {
          out.close();
        }
        catch (IOException e) {
        }
      }
    }
  }

  public static void wellformDocument(Document document, Writer writer) throws ProcessingException {
    wellformDocument(document, new StreamResult(writer));
  }

  private static void wellformDocument(Document document, Result result) throws ProcessingException {
    try {
      // format transformer
      TransformerFactory tf = TransformerFactory.newInstance();
      tf.setAttribute("indent-number", 3);
      Transformer transformer = tf.newTransformer();
      transformer.setOutputProperty(OutputKeys.INDENT, "yes");

      // do transform
      transformer.transform(new DOMSource(document), result);
    }
    catch (TransformerException e) {
      throw new ProcessingException("Unable to wellform xml.", e);
    }
  }

  public static String wellformDocument(Document document) throws ProcessingException {
    StringWriter writer = new StringWriter();
    wellformDocument(document, new StreamResult(writer));
    return writer.getBuffer().toString();
  }

  public static String wellformXml(String rawXml) throws ProcessingException {
    return wellformDocument(getXmlDocument(rawXml));
  }

  public static Document createNewXmlDocument(String rootTagName) {
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    try {
      DocumentBuilder docBuilder = factory.newDocumentBuilder();
      Document document = docBuilder.newDocument();
      document.appendChild(document.createElement(rootTagName));
      return document;
    }
    catch (ParserConfigurationException e) {
      return null;
    }
  }

  public static Document getXmlDocument(InputStream is) throws ProcessingException {
    try {
      return getDocumentBuilder().parse(is);
    }
    catch (IOException e) {
      throw new ProcessingException("Unable to load xml document.", e);
    }
    catch (SAXException e) {
      throw new ProcessingException("Unable to load xml document.", e);
    }
    catch (ParserConfigurationException e) {
      throw new ProcessingException("Unable to load xml document.", e);
    }
  }

  public static Document getXmlDocument(File f) throws ProcessingException {
    try {
      return getDocumentBuilder().parse(f);
    }
    catch (IOException e) {
      throw new ProcessingException("Unable to load xml document.", e);
    }
    catch (SAXException e) {
      throw new ProcessingException("Unable to load xml document.", e);
    }
    catch (ParserConfigurationException e) {
      throw new ProcessingException("Unable to load xml document.", e);
    }
  }

  public static Document getXmlDocument(URL url) throws ProcessingException {
    InputStream is = null;
    try {
      is = url.openStream();
      return XmlUtility.getXmlDocument(is);
    }
    catch (IOException e) {
      throw new ProcessingException("Unable to open URL '" + url.toExternalForm() + "'.", e);
    }
    finally {
      if (is != null) {
        try {
          is.close();
        }
        catch (IOException e) {
        }
      }
    }
  }

  public static Document getXmlDocument(String rawXml) throws ProcessingException {
    try {
      return getDocumentBuilder().parse(new InputSource(new StringReader(rawXml)));
    }
    catch (IOException e) {
      throw new ProcessingException("Unable to load xml document.", e);
    }
    catch (SAXException e) {
      throw new ProcessingException("Unable to load xml document.", e);
    }
    catch (ParserConfigurationException e) {
      throw new ProcessingException("Unable to load xml document.", e);
    }
  }

  private static DocumentBuilder getDocumentBuilder() throws ParserConfigurationException {
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    DocumentBuilder docBuilder = factory.newDocumentBuilder();
    return docBuilder;
  }

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

  public static List<Element> getChildElements(Element parent) {
    return getChildElements(parent, null);
  }

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
