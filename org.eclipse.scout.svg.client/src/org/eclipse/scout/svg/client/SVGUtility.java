/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.svg.client;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.batik.dom.svg.SAXSVGDocumentFactory;
import org.apache.batik.dom.svg.SVGOMRect;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.svg.SVGDocument;
import org.w3c.dom.svg.SVGPoint;

public final class SVGUtility {

  private SVGUtility() {
  }

  public static SVGDocument readSVGDocument(InputStream in) throws ProcessingException {
    String cn;
    try {
      cn = Class.forName("org.apache.xerces.parsers.SAXParser").getName();
    }
    catch (Throwable t) {
      try {
        cn = Class.forName("com.sun.org.apache.xerces.internal.parsers.SAXParser").getName();
      }
      catch (Exception e) {
        throw new ProcessingException("Finding SAXParser", e);
      }
    }
    SAXSVGDocumentFactory documentFactory = new SAXSVGDocumentFactory(cn);
    documentFactory.setValidating(false);
    SVGDocument doc;
    try {
      doc = documentFactory.createSVGDocument(null, in);
    }
    catch (Exception e) {
      throw new ProcessingException("Reading SVG Failed", e);
    }
    try {
      doc.setDocumentURI("urn:svg");//needed to make anchors work but only works in dom level 3
    }
    catch (Throwable t) {
      //nop, dom level less than 3
    }
    return doc;
  }

  public static void writeSVGDocument(SVGDocument doc, OutputStream out) throws ProcessingException {
    try {
      DOMSource domSource = new DOMSource(doc);
      StreamResult streamResult = new StreamResult(out);
      Transformer xformer = TransformerFactory.newInstance().newTransformer();
      xformer.transform(domSource, streamResult);
      out.close();
    }
    catch (Exception e) {
      throw new ProcessingException("Writing SVG Failed", e);
    }
  }

  public static List<Element> getElementsAt(SVGDocument doc, SVGPoint point) {
    ArrayList<Element> list = new ArrayList<Element>();
    SVGOMRect svgOMRect = new SVGOMRect(point.getX(), point.getY(), 1, 1);
    NodeList intersectedElements = doc.getRootElement().getIntersectionList(svgOMRect, null);
    int n = intersectedElements.getLength();
    for (int i = 0; i < n; i++) {
      Node node = intersectedElements.item(i);
      if (node instanceof Element) {
        list.add((Element) node);
      }
    }
    return list;
  }

}
