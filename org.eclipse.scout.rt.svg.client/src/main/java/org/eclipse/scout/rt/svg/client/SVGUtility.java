/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.svg.client;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
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
import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.eclipse.scout.rt.platform.util.XmlUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.w3c.dom.svg.SVGAElement;
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

  private static final Logger LOG = LoggerFactory.getLogger(SVGUtility.class);

  /**
   * Conversion of points/mm/inch and more see http://www.endmemo.com/convert/topography.php
   */
  private static final float PIXEL_PER_POINT = 1.333333f;
  private static final float PIXEL_PER_MM = 3.779528f;
  private static final float PIXEL_PER_INCH = 96f;

  private static final float DEFAULT_FONT_HEIGHT = 14f;

  public interface INodeVisitor {
    /**
     * @return true to continue visiting, false to stop
     */
    boolean visit(Node node) throws Exception;
  }

  private SVGUtility() {
  }

  /**
   * Parses a SVG document read by the given input stream. The document returned can be modified on XML-level. If you
   * need to perform any CSS, text size and and bounding box operations use
   * {@link #readSVGDocumentForGraphicalModification(InputStream)} instead.
   *
   * @param in
   *          input stream the SVG document is read from.
   * @return Returns the SVG document.
   */
  public static SVGDocument readSVGDocument(InputStream in) {
    String cn;
    try {
      cn = Class.forName("org.apache.xerces.parsers.SAXParser").getName();
    }
    catch (ClassNotFoundException t) {// NOSONAR
      try {
        cn = Class.forName("com.sun.org.apache.xerces.internal.parsers.SAXParser").getName();
      }
      catch (ClassNotFoundException e) {
        throw new ProcessingException("Could not find SAXParser", e);
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
    catch (RuntimeException e) {
      LOG.debug("Could not set document uri. Dom level seems to be lass than version 3", e);
    }
    return doc;
  }

  /**
   * Parses a SVG document read by the given input stream and attaches a GVT tree. An attached GVT tree is required for
   * performing CSS, text size and and bounding box operations on the SVG document.
   * <p/>
   * The resulting bridge context holds a reference to the SVG document {@link BridgeContext#getDocument()}
   * <p/>
   * <h1>Important:</h1> Callers are required to invoke {@link BridgeContext#dispose()} on the returned bridge context
   * as soon as the bridge or the document it references is not required anymore.
   * <p/>
   * If the documents needs not be manipulated, use {@link #readSVGDocument(InputStream)} instead.
   *
   * @param in
   *          input stream the SVG document is read from.
   * @return Returns a bridge context that holds references to the SVG document as well as to the GVT tree wrapping
   *         objects.
   */
  public static BridgeContext readSVGDocumentForGraphicalModification(InputStream in) {
    SVGDocument doc = readSVGDocument(in);
    //add a gvt tree for text and alignment calculations
    BridgeContext bc = new BridgeContext(new UserAgentAdapter());
    bc.setDynamic(true);
    GVTTreeBuilder treeBuilder = new GVTTreeBuilder(doc, bc);
    treeBuilder.setPriority(Thread.MAX_PRIORITY);
    treeBuilder.run();
    return bc;
  }

  public static void writeSVGDocument(SVGDocument doc, OutputStream out, String encoding) {
    try {
      DOMSource domSource = new DOMSource(doc);
      StreamResult streamResult = new StreamResult(out);
      Transformer t = XmlUtility.newTransformer();
      if (encoding != null) {
        t.setOutputProperty("encoding", encoding);
      }
      t.transform(domSource, streamResult);
    }
    catch (TransformerException e) {
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
   * Set the text content of a text element, in case it contains newlines then add tspan elements. Requires the GVT tree
   * to be attached to the svg document.
   *
   * @param textElement
   * @param value
   * @param rowGap
   *          in px
   */
  public static void setTextContent(Element e, String value, final Float rowGap) {
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
      setTextContent(textElement, null);
      return;
    }
    if (!value.contains("\n")) {
      setTextContent(textElement, value);
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
      fontHeight = DEFAULT_FONT_HEIGHT;
    }
    Float rGap = rowGap == null ? Float.valueOf(1f) : rowGap;
    float rowHeight = fontHeight + rGap;
    //create tspan lines
    float y = 0;
    setTextContent(textElement, null);
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
   * @param set
   *          the text content on a node by using the child text node. Use this instead of e.setTextContent to be
   *          compatible with batik 1.6 (jdk 1.4)
   */
  public static void setTextContent(Element e, String textContent) {
    //remove children
    while (e.getFirstChild() != null) {
      e.removeChild(e.getFirstChild());
    }
    //add child text node
    Text textNode = e.getOwnerDocument().createTextNode(textContent);
    e.appendChild(textNode);
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
  public static String wrapText(SVGTextContentElement contextElement, String text, Float wordWrap) {
    if (text == null) {
      return "";
    }
    List<String> lines = Arrays.asList(text.split("[\n\r]"));
    if (wordWrap == null || wordWrap <= 0 || text.length() == 0) {
      return text;
    }
    float wrap = wordWrap.floatValue();
    ArrayList<String> wrappedLines = new ArrayList<String>(lines.size());
    for (String line : lines) {
      if (!StringUtility.hasText(line)) {
        wrappedLines.add("");
        continue;
      }
      line = line.replaceAll("[\\s]+", " ").trim();
      try {
        setTextContent(contextElement, line);
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
          //wrap when there is at least one word and text exceeds line
          if (lineBuf.length() > 0 && acc + dw > wrap) {
            //maybe text is absolutely too large
            if (acc > wrap) {
              wrappedLines.add(rtrim(clipText(contextElement, lineBuf.toString(), wrap)));
            }
            else {
              wrappedLines.add(rtrim(lineBuf.toString()));
            }
            lineBuf.setLength(0);
            acc = 0;
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
          //maybe text is absolutely too large
          if (acc > wrap) {
            wrappedLines.add(rtrim(clipText(contextElement, lineBuf.toString(), wrap)));
          }
          else {
            wrappedLines.add(rtrim(lineBuf.toString()));
          }
          lineBuf.setLength(0);
        }
      }
      finally {
        setTextContent(contextElement, null);
      }
    }
    while (wrappedLines.size() > 0 && wrappedLines.get(wrappedLines.size() - 1).length() == 0) {
      wrappedLines.remove(wrappedLines.size() - 1);
    }
    StringBuilder buf = new StringBuilder();
    for (int i = 0, n = wrappedLines.size(); i < n; i++) {
      if (i > 0) {
        buf.append("\n");
      }
      buf.append(wrappedLines.get(i));
    }
    return buf.toString();
  }

  private static String rtrim(String s) {
    int len = s.length();
    int r = len - 1;
    while (r >= 0 && s.charAt(r) <= ' ') {
      r--;
    }
    if (r == len - 1) {
      return s;
    }
    if (r < 0) {
      return "";
    }
    return s.substring(0, r + 1);
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
      setTextContent(contextElement, text + suffix);
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
      setTextContent(contextElement, null);
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
      setTextContent(contextElement, text);
      int textLen = text.length();
      float textWidth = 0;
      for (int i = 0; i < textLen; i++) {
        textWidth += contextElement.getExtentOfChar(i).getWidth();
      }
      return textWidth;
    }
    finally {
      setTextContent(contextElement, null);
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
    if ("px".equals(unit)) {
      return f;
    }
    if ("pt".equals(unit)) {
      return f * PIXEL_PER_POINT;
    }
    if ("mm".equals(unit)) {
      return f * PIXEL_PER_MM;
    }
    if ("in".equals(unit)) {
      return f * PIXEL_PER_INCH;
    }
    return f;
  }

  /**
   * Enclose the element with a link to an url
   * <p>
   * Bug fix: batik sometimes creates a new namespace for xlink even though the namespace is already defined. This
   * utility method fixes that behaviour.
   * <p>
   * Bug:
   * <xmp><a xlink:actuate="onRequest" xlink:type="simple" xlink:show="replace" xmlns:ns3="http://www.w3.org/1999/xlink"
   * ns3:href="http://local/info-prev">....</a></xmp>
   * <p>
   * Fixed:
   * <xmp><a xlink:actuate="onRequest" xlink:type="simple" xlink:show="replace" xlink:href="http://local/info-prev">....
   * </a></xmp>
   */
  public static void addHyperlink(Element e, String url) {
    SVGAElement aElem = (SVGAElement) e.getOwnerDocument().createElementNS(SVG_NS, "a");
    e.getParentNode().insertBefore(aElem, e);
    e.getParentNode().removeChild(e);
    aElem.appendChild(e);
    aElem.getHref().setBaseVal(url);
    //bug fix: remove xmlns:xlink=... attributes, change attributes of ns xlink to have name prefixed with 'xlink'
    NamedNodeMap nnmap = aElem.getAttributes();
    for (int i = 0, n = nnmap.getLength(); i < n; i++) {
      Node node = nnmap.item(i);
      if (node instanceof Attr) {
        Attr a = (Attr) node;
        if (XLINK_NS.equals(a.getNamespaceURI()) && !"xlink".equals(a.getPrefix())) {
          nnmap.removeNamedItemNS(a.getNamespaceURI(), a.getLocalName());
          a.setPrefix("xlink");
          nnmap.setNamedItemNS(a);
        }
      }
    }
    for (int i = 0, n = nnmap.getLength(); i < n; i++) {
      Node node = nnmap.item(i);
      if (node instanceof Attr) {
        Attr a = (Attr) node;
        if (javax.xml.XMLConstants.XMLNS_ATTRIBUTE_NS_URI.equals(a.getNamespaceURI())) {
          nnmap.removeNamedItemNS(a.getNamespaceURI(), a.getLocalName());
        }
      }
    }
  }

  /**
   * removes the link that was set on this element, i.e. removes the PARENT a-tag
   */
  public static void removeHyperlink(Element e) {
    if (e.getParentNode() instanceof SVGAElement) {
      removeHyperlink((SVGAElement) e.getParentNode());
    }
  }

  /**
   * A link is enclosed to the main element
   * <p>
   * Moves move all inner elements out of the link element and removes the link element
   */
  public static void removeHyperlink(SVGAElement e) {
    if (SVGConstants.SVG_A_TAG.equals(e.getTagName())) {
      NodeList nodes = e.getChildNodes();
      for (int i = 0, n = nodes.getLength(); i < n; i++) {
        Node node = nodes.item(i);
        if (node instanceof Element) {
          e.getParentNode().insertBefore(nodes.item(i), e);
        }
      }
      e.getParentNode().removeChild(e);
    }
  }

  /**
   * Add an app-link to the specified element. </br>
   * This is done by adding a class attribute (or enrich its if already present) and adding a data-ref attribute. </br>
   * If class "app-link" is already present, only the data-ref attribute is added or updated.
   */
  public static void addAppLink(Element e, String ref) {

    if (e.hasAttribute(SVGConstants.SVG_CLASS_ATTRIBUTE)) {
      if (!e.getAttribute(SVGConstants.SVG_CLASS_ATTRIBUTE).contains("app-link")) {
        //class attribute present, but does not define class "app-link"
        e.setAttribute(SVGConstants.SVG_CLASS_ATTRIBUTE, e.getAttribute(SVGConstants.SVG_CLASS_ATTRIBUTE) + " " + "app-link");
      }

      //"app-link" class present, do nothing
    }
    else {
      //no class attribute present
      e.setAttribute(SVGConstants.SVG_CLASS_ATTRIBUTE, "app-link");
    }

    //add data-ref attribute
    e.setAttribute("data-ref", ref);
  }

  /**
   * Removes an app link. This is done by removing class "app-link" and, if the former was present, the attribute
   * "data-ref" </br>
   * If no "app-link" class is present, this method does nothing (i.e. does not change the presence of a "data-ref"
   * attribute)
   */
  public static void removeAppLink(Element e) {

    if (e.hasAttribute(SVGConstants.SVG_CLASS_ATTRIBUTE) && e.getAttribute(SVGConstants.SVG_CLASS_ATTRIBUTE).contains("app-link")) {
      String newValue = StringUtility.cleanup(StringUtility.replace(e.getAttribute(SVGConstants.SVG_CLASS_ATTRIBUTE), "app-link", ""));
      if (StringUtility.isNullOrEmpty(newValue)) {
        e.removeAttribute(SVGConstants.SVG_CLASS_ATTRIBUTE);
      }
      else {
        e.setAttribute(SVGConstants.SVG_CLASS_ATTRIBUTE, newValue);
      }
      e.removeAttribute("data-ref");
    }
  }

}
