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
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.bridge.UserAgentAdapter;
import org.apache.batik.dom.svg.SAXSVGDocumentFactory;
import org.apache.batik.dom.svg.SVGDOMImplementation;
import org.apache.batik.dom.svg.SVGOMRect;
import org.apache.batik.swing.svg.GVTTreeBuilder;
import org.apache.batik.util.SVGConstants;
import org.apache.batik.util.XMLConstants;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.svg.SVGDocument;
import org.w3c.dom.svg.SVGElement;
import org.w3c.dom.svg.SVGPoint;
import org.w3c.dom.svg.SVGStylable;
import org.w3c.dom.svg.SVGTSpanElement;
import org.w3c.dom.svg.SVGTextContentElement;
import org.w3c.dom.svg.SVGTransform;
import org.w3c.dom.svg.SVGTransformList;
import org.w3c.dom.svg.SVGTransformable;

public final class SVGUtility {
  public static final String SVG_NS = SVGDOMImplementation.SVG_NAMESPACE_URI;
  public static final String XLINK_NS = XMLConstants.XLINK_NAMESPACE_URI;

  public static interface INodeVisitor {
    /**
     * @return true to continue visiting, false to stop
     */
    boolean visit(Node node) throws Exception;
  }

  private SVGUtility() {
  }

  /**
   * @param in
   * @param attachGVTTree
   *          true if a GVT tree should be attached. This is necessary if css, text size, and bounding box operations
   *          are to be performed on the svg document
   */
  public static SVGDocument readSVGDocument(InputStream in, boolean attachGVTTree) throws ProcessingException {
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
    if (attachGVTTree) {
      //add a gvt tree for text and alignment calculations
      BridgeContext bc = new BridgeContext(new UserAgentAdapter());
      bc.setDynamic(true);
      GVTTreeBuilder treeBuilder = new GVTTreeBuilder(doc, bc);
      treeBuilder.setPriority(Thread.MAX_PRIORITY);
      treeBuilder.run();
    }
    return doc;
  }

  public static void writeSVGDocument(SVGDocument doc, OutputStream out, String encoding) throws ProcessingException {
    try {
      DOMSource domSource = new DOMSource(doc);
      StreamResult streamResult = new StreamResult(out);
      Transformer t = TransformerFactory.newInstance().newTransformer();
      if (encoding != null) {
        t.setOutputProperty("encoding", encoding);
      }
      t.transform(domSource, streamResult);
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

  /**
   * @return true if whole tree was visited, false if a {@link INodeVisitor#visit(Node)} returned false
   */
  public static boolean visitDocument(Document doc, INodeVisitor v) throws Exception {
    return visitNode(doc.getDocumentElement(), v);
  }

  /**
   * @return true if whole sub-tree was visited, false if a {@link INodeVisitor#visit(Node)} returned false
   */
  public static boolean visitNode(Node parent, INodeVisitor v) throws Exception {
    NodeList nl = parent.getChildNodes();
    boolean b;
    for (int i = 0; i < nl.getLength(); i++) {
      b = v.visit(nl.item(i));
      if (!b) {
        return false;
      }
      b = visitNode(nl.item(i), v);
      if (!b) {
        return false;
      }
    }
    return true;
  }

  public static void setTransform(SVGElement e, float x, float y, float rotation) {
    SVGTransformList list = ((SVGTransformable) e).getTransform().getBaseVal();
    list.clear();
    if (rotation != 0) {
      SVGTransform tx = e.getOwnerSVGElement().createSVGTransform();
      tx.setRotate(rotation, 0, 0);
      list.appendItem(tx);
    }
    if (x != 0 || y != 0) {
      SVGTransform tx = e.getOwnerSVGElement().createSVGTransform();
      tx.setTranslate(x, y);
      list.appendItem(tx);
    }
  }

  /**
   * Set the text content of a text element, in case it contains newlines then add tspan elements.
   * Requires the GVT tree to be attached to the svg document.
   * 
   * @param textElement
   * @param value
   * @param rowGap
   *          in px
   */
  public static void setTextContent(Element e, String value, Float rowGap) {
    if (e == null) {
      return;
    }
    SVGTextContentElement textElement = (SVGTextContentElement) e;
    //remove inner tspan elements
    NodeList nl = textElement.getElementsByTagName(SVGConstants.SVG_TSPAN_TAG);
    for (int i = 0; i < nl.getLength(); i++) {
      nl.item(i).getParentNode().removeChild(nl.item(i));
    }
    if (value == null || value.length() == 0) {
      textElement.setTextContent(null);
      return;
    }
    if (!value.contains("\n")) {
      textElement.setTextContent(value);
      return;
    }
    //get font height
    float fontHeight = 0f;
    Node tmpNode = textElement;
    while (fontHeight == 0f && tmpNode != null) {
      if (tmpNode instanceof SVGStylable) {
        //get font height
        String fontSizeText = ((SVGStylable) tmpNode).getStyle().getPropertyValue(SVGConstants.CSS_FONT_SIZE_PROPERTY);
        if (fontSizeText != null) {
          fontHeight = convertToPx(fontSizeText);
          break;
        }
      }
      //next
      tmpNode = tmpNode.getParentNode();
    }
    if (fontHeight == 0f) {
      fontHeight = 14f;
    }
    if (rowGap == null) {
      rowGap = 1f;
    }
    float rowHeight = fontHeight + rowGap;
    //create tspan lines
    float y = 0;
    textElement.setTextContent(null);
    for (String line : value.split("[\n\r]")) {
      SVGTSpanElement tspanElem = (SVGTSpanElement) textElement.getOwnerDocument().createElementNS(SVG_NS, SVGConstants.SVG_TSPAN_TAG);
      textElement.appendChild(tspanElem);
      tspanElem.setTextContent(line);
      tspanElem.setAttribute("x", "0");
      tspanElem.setAttribute("y", String.valueOf(y));
      y += rowHeight;
    }
  }

  /**
   * This feature is experimental as a convenience since svg does not support native text operations.
   * 
   * @param contextElement
   *          is the {@link SVGTextContentElement} containing optional style and font information context for the
   *          wrapping algorithm
   * @param text
   * @param wordWrapWidth
   *          in px
   * @return the wrapped text with additional newline characters where it was wrapped.
   */
  public static String[] wrapText(SVGTextContentElement contextElement, String text, Float wordWrap) {
    if (text == null) {
      return new String[0];
    }
    List<String> lines = Arrays.asList(text.split("[\n\r]"));
    if (wordWrap == null || wordWrap <= 0 || text.length() == 0) {
      return new String[]{text};
    }
    float wrap = wordWrap.floatValue();
    ArrayList<String> wrappedLines = new ArrayList<String>(lines.size());
    for (String line : lines) {
      if (line.trim().length() == 0) {
        wrappedLines.add(line);
        continue;
      }
      line = line.replaceAll("[\\s]+", " ").trim();
      try {
        contextElement.setTextContent(line);
        float[] w = new float[line.length()];
        for (int i = 0; i < w.length; i++) {
          w[i] = contextElement.getExtentOfChar(i).getWidth();
        }
        //
        String[] words = line.split("[ ]");
        int startIndex = 0;
        int endIndex = 0;
        float acc = 0;
        StringBuilder lineBuf = new StringBuilder();
        for (int wordIndex = 0; wordIndex < words.length; wordIndex++) {
          String word = words[wordIndex];
          endIndex = startIndex + word.length();
          float dw = 0;
          for (int i = startIndex; i < endIndex; i++) {
            dw += w[i];
          }
          if (lineBuf.length() > 0 && acc + dw > wrap) {
            acc = 0;
            wrappedLines.add(lineBuf.toString());
            lineBuf.setLength(0);
          }
          acc += dw;
          lineBuf.append(word);
          //also add following space
          if (endIndex < w.length) {
            acc += w[endIndex];
            lineBuf.append(" ");
          }
          //next (+1: skip following space)
          startIndex = endIndex + 1;
        }
        //remaining text
        if (lineBuf.length() > 0) {
          wrappedLines.add(lineBuf.toString());
          lineBuf.setLength(0);
        }
      }
      finally {
        contextElement.setTextContent(null);
      }
    }
    return wrappedLines.toArray(new String[wrappedLines.size()]);
  }

  /**
   * This feature is experimental as a convenience since svg does not support native text operations.
   * 
   * @param contextElement
   *          is the {@link SVGTextContentElement} containing optional style and font information context for the
   *          wrapping algorithm
   * @param text
   *          is one line of text (no newlines)
   * @param clipWidth
   *          in px
   * @return the text clipped to fit the clipWidth. If the text is too large it is cropped and "..." is appended at the
   *         end.
   */
  public static String clipText(SVGTextContentElement contextElement, String text, float clipWidth) {
    if (text == null || text.length() == 0) {
      return text;
    }
    if (clipWidth <= 0) {
      return text;
    }
    String suffix = "...";
    try {
      contextElement.setTextContent(text + suffix);
      int textLen = text.length();
      int suffixLen = suffix.length();
      float textWidth = 0;
      float suffixWidth = 0;
      float[] w = new float[textLen + suffixLen];
      for (int i = 0; i < w.length; i++) {
        w[i] = contextElement.getExtentOfChar(i).getWidth();
        if (i < textLen) {
          textWidth += w[i];
        }
        else {
          suffixWidth += w[i];
        }
      }
      if (textWidth <= clipWidth) {
        return text;
      }
      int i = textLen - 1;
      while (i > 0 && textWidth + suffixWidth > clipWidth) {
        textWidth -= w[i];
        i--;
      }
      return text.substring(0, i + 1) + suffix;
    }
    finally {
      contextElement.setTextContent(null);
    }
  }

  /**
   * This feature is experimental as a convenience since svg does not support native text operations.
   * 
   * @param contextElement
   *          is the {@link SVGTextContentElement} containing optional style and font information context for the
   *          wrapping algorithm
   * @param text
   *          is one line of text (no newlines)
   * @return the text width in pixels
   */
  public static float getTextWidth(SVGTextContentElement contextElement, String text) {
    if (text == null || text.length() == 0) {
      return 0;
    }
    try {
      contextElement.setTextContent(text);
      int textLen = text.length();
      float textWidth = 0;
      for (int i = 0; i < textLen; i++) {
        textWidth += contextElement.getExtentOfChar(i).getWidth();
      }
      return textWidth;
    }
    finally {
      contextElement.setTextContent(null);
    }
  }

  private static float convertToPx(String valueWithUnit) {
    Matcher m = Pattern.compile("([0-9.]+)([^0-9.]*)").matcher(valueWithUnit);
    m.matches();
    float f = Float.valueOf(m.group(1));
    String unit = m.group(2);
    if (unit == null) {
      return f;
    }
    unit = unit.toLowerCase();
    if (unit.equals("px")) {
      return f;
    }
    if (unit.equals("pt")) {
      return f * 1.333333f;
    }
    if (unit.equals("mm")) {
      return f * 3.78f;
    }
    if (unit.equals("in")) {
      return f * 96f;
    }
    return f;
  }

  /**
   * Enclose the element with a link to an url
   */
  public static void addHyperlink(Element e, String url) {
    Element aElem = e.getOwnerDocument().createElementNS(SVG_NS, "a");
    e.getParentNode().insertBefore(aElem, e);
    e.getParentNode().removeChild(e);
    aElem.appendChild(e);
    aElem.setAttributeNS(XLINK_NS, "href", url);
  }

}
