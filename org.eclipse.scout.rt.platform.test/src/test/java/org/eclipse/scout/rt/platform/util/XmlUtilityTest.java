package org.eclipse.scout.rt.platform.util;

import java.io.ByteArrayInputStream;

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
  private static final String SIMPLE_XML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><root></root>";

  @Test
  public void testDocumentBuilderSimple() throws Exception {
    Document doc = XmlUtility.newDocumentBuilder().parse(new ByteArrayInputStream(SIMPLE_XML.getBytes("UTF-8")));
    Assert.assertEquals("root", doc.getDocumentElement().getNodeName());
  }

  @Test
  public void testSAXParserSimple() throws Exception {
    final StringBuilder buf = new StringBuilder();
    XmlUtility.newSAXParser().parse(new ByteArrayInputStream(SIMPLE_XML.getBytes("UTF-8")), new DefaultHandler() {
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
    Assert.assertEquals("startDocument\nstartElement root\nendElement root\nendDocument\n", buf.toString());
  }

}
