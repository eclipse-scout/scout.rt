package org.eclipse.scout.rt.platform.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

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
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * <h3>{@link XmlUtilityTest}</h3>
 *
 * @since 5.2
 */
public class XmlUtilityTest {
  private static final String SIMPLE_XML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><root><content/></root>";

  @Test
  public void testDocumentBuilderSimple() throws Exception {
    Document doc = XmlUtility.newDocumentBuilder().parse(new ByteArrayInputStream(SIMPLE_XML.getBytes(StandardCharsets.UTF_8)));
    Assert.assertEquals("root", doc.getDocumentElement().getNodeName());
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
    Assert.assertEquals("711228", b.toString());
  }

  @Test
  public void testNewTransformer() throws TransformerException {
    Transformer transformer = XmlUtility.newTransformer();
    Assert.assertNotNull(transformer);

    StreamSource s = new StreamSource(new ByteArrayInputStream(SIMPLE_XML.getBytes(StandardCharsets.UTF_8)));
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    StreamResult r = new StreamResult(out);
    transformer.transform(s, r);
    Assert.assertEquals(SIMPLE_XML, new String(out.toByteArray(), StandardCharsets.UTF_8));
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
    Assert.assertEquals("startDocument\nstartElement root\nstartElement content\nendElement content\nendElement root\nendDocument\n", buf.toString());
  }

}
