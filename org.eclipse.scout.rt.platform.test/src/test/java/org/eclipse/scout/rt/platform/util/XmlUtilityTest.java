package org.eclipse.scout.rt.platform.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * <h3>{@link XmlUtilityTest}</h3>
 *
 * @since 5.2
 */
public class XmlUtilityTest {
  private static final String XML_PROLOG = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
  private static final String EMPTY_XML = XML_PROLOG + "<root/>";
  private static final String SIMPLE_XML = XML_PROLOG + "<root><element/></root>";
  private static final String NESTED_XML = XML_PROLOG + "<root><otherElement><element nested=\"true\" attrib=\"value\"/></otherElement><element nested=\"false\" attrib=\"value\"/></root>";

  @Test
  public void testDocumentBuilderSimple() throws Exception {
    Document doc = XmlUtility.newDocumentBuilder().parse(new ByteArrayInputStream(SIMPLE_XML.getBytes(StandardCharsets.UTF_8)));
    assertEquals("root", doc.getDocumentElement().getNodeName());
  }

  @Test
  public void testNewXMLInputFactory() throws XMLStreamException {
    XMLInputFactory factory = XmlUtility.newXMLInputFactory();
    Assert.assertNotNull(factory);

    XMLEventReader reader = factory.createXMLEventReader(new ByteArrayInputStream(SIMPLE_XML.getBytes(StandardCharsets.UTF_8)));
    StringBuilder b = new StringBuilder();
    while (reader.hasNext()) {
      XMLEvent event = reader.nextEvent();
      b.append(event.getEventType());
    }
    assertEquals("711228", b.toString());
  }

  @Test
  public void testNewTransformer() throws TransformerException {
    Transformer transformer = XmlUtility.newTransformer();
    Assert.assertNotNull(transformer);

    StreamSource s = new StreamSource(new ByteArrayInputStream(SIMPLE_XML.getBytes(StandardCharsets.UTF_8)));
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    StreamResult r = new StreamResult(out);
    transformer.transform(s, r);
    assertEquals(SIMPLE_XML, new String(out.toByteArray(), StandardCharsets.UTF_8));
  }

  @Test
  public void testSAXParserSimple() throws Exception {
    final StringBuilder buf = new StringBuilder();
    XmlUtility.newSAXParser().parse(new ByteArrayInputStream(SIMPLE_XML.getBytes(StandardCharsets.UTF_8)), new DefaultHandler() {
      @Override
      public void startDocument() throws SAXException {
        buf.append("startDocument\n");
      }

      @Override
      public void endDocument() throws SAXException {
        buf.append("endDocument\n");
      }

      @Override
      public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        buf.append("startElement " + qName + "\n");
      }

      @Override
      public void endElement(String uri, String localName, String qName) throws SAXException {
        buf.append("endElement " + qName + "\n");
      }
    });
    assertEquals("startDocument\nstartElement root\nstartElement element\nendElement element\nendElement root\nendDocument\n", buf.toString());
  }

  @Test
  public void testGetChildElementsEmptyDocument() {
    Element root = XmlUtility.getXmlDocument(EMPTY_XML).getDocumentElement();
    List<Element> children = XmlUtility.getChildElements(root, "element");
    assertEquals(0, children.size());
  }

  @Test
  public void testGetChildElementsSingleElement() {
    Element root = XmlUtility.getXmlDocument(SIMPLE_XML).getDocumentElement();
    List<Element> children = XmlUtility.getChildElements(root, "element");
    assertEquals(1, children.size());
  }

  @Test
  public void testGetChildElementsNestedElements() {
    Element root = XmlUtility.getXmlDocument(NESTED_XML).getDocumentElement();
    List<Element> children = XmlUtility.getChildElements(root, "element");
    assertEquals(1, children.size());
    assertEquals("false", CollectionUtility.firstElement(children).getAttribute("nested"));

    // verify nested element
    Element otherElement = XmlUtility.getFirstChildElement(root, "otherElement");
    assertNotNull(otherElement);
    children = XmlUtility.getChildElements(otherElement, "element");
    assertEquals(1, children.size());
    assertEquals("true", CollectionUtility.firstElement(children).getAttribute("nested"));
  }

  @Test
  public void testGetFirstChildElementEmptyDocument() {
    Element root = XmlUtility.getXmlDocument(EMPTY_XML).getDocumentElement();
    Element child = XmlUtility.getFirstChildElement(root, "element");
    assertNull(child);
  }

  @Test
  public void testGetFirstChildElementSingleElement() {
    Element root = XmlUtility.getXmlDocument(SIMPLE_XML).getDocumentElement();
    Element child = XmlUtility.getFirstChildElement(root, "element");
    assertNotNull(child);
  }

  @Test
  public void testGetFirstChildElementNestedElements() {
    Element root = XmlUtility.getXmlDocument(NESTED_XML).getDocumentElement();
    Element child = XmlUtility.getFirstChildElement(root, "element");
    assertNotNull(child);
    assertEquals("false", child.getAttribute("nested"));

    // verify nested element
    Element otherElement = XmlUtility.getFirstChildElement(root, "otherElement");
    assertNotNull(otherElement);
    child = XmlUtility.getFirstChildElement(otherElement, "element");
    assertNotNull(child);
    assertEquals("true", child.getAttribute("nested"));
  }

  @Test
  public void testGetChildElementsWithAttribute() {
    Element root = XmlUtility.getXmlDocument(NESTED_XML).getDocumentElement();
    List<Element> children = XmlUtility.getChildElementsWithAttributes(root, "element", "attrib", "value");
    assertEquals(1, children.size());
    assertEquals("false", CollectionUtility.firstElement(children).getAttribute("nested"));

    // verify nested element
    Element otherElement = XmlUtility.getFirstChildElement(root, "otherElement");
    assertNotNull(otherElement);
    children = XmlUtility.getChildElementsWithAttributes(otherElement, "element", "attrib", "value");
    assertEquals(1, children.size());
    assertEquals("true", CollectionUtility.firstElement(children).getAttribute("nested"));
  }
}
