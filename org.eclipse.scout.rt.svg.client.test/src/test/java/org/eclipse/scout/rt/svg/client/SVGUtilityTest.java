/*******************************************************************************
 * Copyright (c) 2010-2016 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.svg.client;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;

import org.apache.batik.util.SVGConstants;
import org.eclipse.scout.rt.platform.util.XmlUtility;
import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.svg.SVGAElement;
import org.w3c.dom.svg.SVGDocument;
import org.xml.sax.SAXException;

/**
 * Tests for {@link SVGUtility}
 */

public class SVGUtilityTest {

  private SVGDocument getTestDocument() throws IOException {
    try (InputStream is = getClass().getClassLoader().getResourceAsStream("test.svg");) {
      return SVGUtility.readSVGDocument(is);
    }
  }

  private void assertAppLinkAdded(Element e, String ref) {
    Assert.assertTrue(e.hasAttribute(SVGConstants.SVG_CLASS_ATTRIBUTE) && e.getAttribute(SVGConstants.SVG_CLASS_ATTRIBUTE).contains("app-link"));
    Assert.assertTrue(e.hasAttribute("data-ref") && e.getAttribute("data-ref").equals(ref));
  }

  private void assertHyperlinkAdded(Element e, String url) {
    if (!(e.getParentNode() instanceof SVGAElement)) {
      Assert.fail("parent is not an 'a-tag'");
    }
    SVGAElement parent = (SVGAElement) e.getParentNode();
    Assert.assertEquals(parent.getHref().getBaseVal(), url);
  }

  private Document asXML(SVGDocument d) throws SAXException, IOException, ParserConfigurationException {
    try (OutputStream os = new ByteArrayOutputStream();) {
      SVGUtility.writeSVGDocument(d, os, "utf8");
      return XmlUtility.getXmlDocument(os.toString());
    }
  }

  /**
   * Compare the documents as XML, not svg, since svg comparison seems to always yield false
   */
  private boolean isXMLEqual(SVGDocument svg1, SVGDocument svg2) throws Exception {

    Document d1 = asXML(svg1);
    d1.normalizeDocument();
    Document d2 = asXML(svg2);
    d2.normalizeDocument();

    return d1.isEqualNode(d2);
  }

  /**
   * Batik which is used for SVG includes its own XML factories (e.g. xalan). Also test the XmlUtility methods using
   * these factories.
   */
  @Test
  public void testXmlUtilityWithBatikClasspath() throws ParserConfigurationException, SAXException, TransformerConfigurationException {
    Assert.assertNotNull(XmlUtility.newDocumentBuilder());
    Assert.assertNotNull(XmlUtility.newSAXParser());
    Assert.assertNotNull(XmlUtility.newTransformer());
    Assert.assertNotNull(XmlUtility.newXMLInputFactory());
  }

  @Test
  public void testAddAppLink() throws IOException {
    String ref = "test";
    String otherRef = "another test";
    SVGDocument doc = getTestDocument();
    Element e = doc.getElementById("outerRect");

    //test case 1: no class attribute present
    SVGUtility.addAppLink(e, ref);
    assertAppLinkAdded(e, ref);

    //test case 2: class attribute present
    e = doc.getElementById("innerCircle");
    SVGUtility.addAppLink(e, ref);
    assertAppLinkAdded(e, ref);

    //test case 2: app link present
    e = doc.getElementById("innerCircle");
    SVGUtility.addAppLink(e, otherRef);
    assertAppLinkAdded(e, otherRef);
  }

  @Test
  public void testRemoveAppLink() throws Exception {
    String ref = "test";
    SVGDocument doc = getTestDocument();
    SVGDocument baseline = getTestDocument();

    //test case 1: no class attribute present.
    Element e = doc.getElementById("outerCircle");
    SVGUtility.addAppLink(e, ref);
    SVGUtility.removeAppLink(e);
    Assert.assertTrue(isXMLEqual(doc, baseline));

    //test case 2: class attribute present.
    doc = getTestDocument();
    e = doc.getElementById("innerRect");
    SVGUtility.addAppLink(e, ref);
    SVGUtility.removeAppLink(e);
    Assert.assertTrue(isXMLEqual(doc, baseline));

    //test case 3: no app link present, no class Attribute present: expected nop
    doc = getTestDocument();
    e = doc.getElementById("outerRect");
    SVGUtility.removeAppLink(e);
    Assert.assertTrue(isXMLEqual(doc, baseline));

    //test case 3: no app link present, class Attribute present: expected nop
    doc = getTestDocument();
    e = doc.getElementById("innerCircle");
    SVGUtility.removeAppLink(e);
    Assert.assertTrue(isXMLEqual(doc, baseline));
  }

  @Test
  public void testAddHyperlink() throws IOException {
    String url = "http://www.test.example.org";
    SVGDocument doc = getTestDocument();
    Element e = doc.getElementById("outerRect");

    SVGUtility.addHyperlink(e, url);
    assertHyperlinkAdded(e, url);

    e = doc.getElementById("innerCircle");
    SVGUtility.addHyperlink(e, url);
    assertHyperlinkAdded(e, url);
  }

  @Test
  public void testRemoveHyperlink() throws Exception {
    String url = "http://www.test.example.org";
    SVGDocument doc = getTestDocument();
    SVGDocument baseline = getTestDocument();

    Element e = doc.getElementById("outerCircle");
    SVGUtility.addHyperlink(e, url);
    e = doc.getElementById("outerCircle");
    SVGUtility.removeHyperlink(e);
    Assert.assertTrue(isXMLEqual(doc, baseline));

    doc = getTestDocument();
    e = doc.getElementById("innerCircle");
    SVGUtility.addHyperlink(e, url);
    e = doc.getElementById("innerCircle");
    SVGUtility.removeHyperlink(e);
    Assert.assertTrue(isXMLEqual(doc, baseline));

    //no link present, expected nop
    doc = getTestDocument();
    e = doc.getElementById("innerCircle");
    SVGUtility.removeHyperlink(e);
    Assert.assertTrue(isXMLEqual(doc, baseline));
  }

}
