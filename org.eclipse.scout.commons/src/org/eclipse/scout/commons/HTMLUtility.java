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
package org.eclipse.scout.commons;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.Style;
import javax.swing.text.html.CSS;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;
import javax.swing.text.html.parser.ParserDelegator;

import org.eclipse.scout.commons.holders.IntegerHolder;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;

/**
 * Utility to manipulate HTML documents regardless of ui (swt / swing). This
 * class is used from swt as well as in swing code
 */
public final class HTMLUtility {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(HTMLUtility.class);

  private static HashMap<String, CSS.Attribute> cssMap;

  private HTMLUtility() {
  }

  static {
    CSSPatch.apply();
    cssMap = new HashMap<String, CSS.Attribute>();
    cssMap.put(CSS.Attribute.COLOR.toString(), CSS.Attribute.COLOR);
    cssMap.put(CSS.Attribute.BACKGROUND_COLOR.toString(), CSS.Attribute.BACKGROUND_COLOR);
    cssMap.put(CSS.Attribute.BORDER_COLOR.toString(), CSS.Attribute.BORDER_COLOR);
    cssMap.put(CSS.Attribute.FONT_SIZE.toString(), CSS.Attribute.FONT_SIZE);
    cssMap.put(CSS.Attribute.BACKGROUND_IMAGE.toString(), CSS.Attribute.BACKGROUND_IMAGE);
    cssMap.put(CSS.Attribute.LIST_STYLE_IMAGE.toString(), CSS.Attribute.LIST_STYLE_IMAGE);
  }

  public static HTMLDocument parseDocument(String htmlText) {
    if (htmlText == null || htmlText.equals("")) {
      return null;
    }
    // remove meta tags
    HTMLEditorKit kit = new HTMLEditorKit();
    Pattern[] metaPatterns = new Pattern[]{
        Pattern.compile("<meta\\s+[^>]*>"),
        Pattern.compile("<Meta\\s+[^>]*>"),
        Pattern.compile("<META\\s+[^>]*>"),
        Pattern.compile("</\\s*meta\\s*>"),
        Pattern.compile("</\\s*Meta\\s*>"),
        Pattern.compile("</\\s*META\\s*>"),
        };
    for (Pattern p : metaPatterns) {
      htmlText = p.matcher(htmlText).replaceAll("");
    }
    // fix incorrect single tag endings ( <br/ > <br /> )
    htmlText = htmlText.replaceAll("/\\s+>", "/>");
    htmlText = htmlText.replaceAll("([^\\s])\\s+/>", "$1/>");
    // before jre 1.6 eliminate single tags and </br> tags
    if (System.getProperty("java.version", "1.5").substring(0, 3).compareTo("1.6") < 0) {
      htmlText = htmlText.replaceAll("/>", ">");
      htmlText = htmlText.replaceAll("</\\s*br\\s*>", "");
    }
    StyleSheet styleSheet = new StyleSheet();
    MutableHTMLDocument doc = new MutableHTMLDocument(styleSheet);
    try {
      doc.setParser(new ParserDelegator());
      doc.setTokenThreshold(100);
      doc.remove(0, doc.getLength());
      kit.read(new StringReader(htmlText), doc, 0);
    }
    catch (Exception e) {
      LOG.warn(null, e);
      return null;
    }
    return doc;
  }

  public static String formatDocument(HTMLDocument doc) {
    String htmlText = "";
    if (doc != null) {
      HTMLEditorKit kit = new HTMLEditorKit();
      StringWriter buf = new StringWriter();
      try {
        kit.write(buf, doc, 0, doc.getLength());
        htmlText = buf.toString();
      }
      catch (Exception e) {
        LOG.warn(null, e);
      }
    }
    return htmlText;
  }

  /**
   * HTML has several troubles with some CSS and tag style concepts This method
   * tries to correct the most needed ones see {@link #wellformDocument(HTMLDocument, String, int)} for further options
   */
  public static HTMLDocument cleanupDocument(HTMLDocument htmlDoc, String defaultFontFamily, int defaultFontSize) {
    if (htmlDoc == null) return htmlDoc;
    MutableHTMLDocument doc = (MutableHTMLDocument) htmlDoc;
    StyleSheet styleSheet = doc.getStyleSheet();
    Style style;
    // p style
    style = styleSheet.getStyle("p");
    if (style == null) {
      style = styleSheet.addStyle("p", null);
      styleSheet.addCSSAttribute(style, CSS.Attribute.MARGIN_LEFT, "0");
      styleSheet.addCSSAttribute(style, CSS.Attribute.MARGIN_RIGHT, "0");
      styleSheet.addCSSAttribute(style, CSS.Attribute.MARGIN_TOP, "4");
      styleSheet.addCSSAttribute(style, CSS.Attribute.MARGIN_BOTTOM, "4");
      styleSheet.addCSSAttribute(style, CSS.Attribute.PADDING_LEFT, "0");
      styleSheet.addCSSAttribute(style, CSS.Attribute.PADDING_RIGHT, "0");
      styleSheet.addCSSAttribute(style, CSS.Attribute.PADDING_TOP, "0");
      styleSheet.addCSSAttribute(style, CSS.Attribute.PADDING_BOTTOM, "0");
    }
    // a style
    style = styleSheet.getStyle("a");
    if (style == null) {
      style = styleSheet.addStyle("a", null);
    }
    if (style.getAttribute(CSS.Attribute.COLOR) == null) {
      styleSheet.addCSSAttribute(style, CSS.Attribute.COLOR, "#3366cc");
    }
    if (style.getAttribute(CSS.Attribute.TEXT_DECORATION) == null) {
      styleSheet.addCSSAttribute(style, CSS.Attribute.TEXT_DECORATION, "underline");
    }
    // body style
    style = styleSheet.getStyle("body");
    if (style == null) {
      style = styleSheet.addStyle("body", null);
    }
    if (style.getAttribute(CSS.Attribute.MARGIN_TOP) == null) {
      styleSheet.addCSSAttribute(style, CSS.Attribute.MARGIN_TOP, "0");
    }
    if (style.getAttribute(CSS.Attribute.MARGIN_LEFT) == null) {
      styleSheet.addCSSAttribute(style, CSS.Attribute.MARGIN_LEFT, "0");
    }
    if (style.getAttribute(CSS.Attribute.MARGIN_BOTTOM) == null) {
      styleSheet.addCSSAttribute(style, CSS.Attribute.MARGIN_BOTTOM, "0");
    }
    if (style.getAttribute(CSS.Attribute.MARGIN_RIGHT) == null) {
      styleSheet.addCSSAttribute(style, CSS.Attribute.MARGIN_RIGHT, "0");
    }
    // default fonts
    if (defaultFontFamily != null && defaultFontSize > 0) {
      setDefaultFont(styleSheet, "body", defaultFontFamily, defaultFontSize);
      setDefaultFont(styleSheet, "p", defaultFontFamily, defaultFontSize);
      setDefaultFont(styleSheet, "span", defaultFontFamily, defaultFontSize);
      setDefaultFont(styleSheet, "th", defaultFontFamily, defaultFontSize);
      setDefaultFont(styleSheet, "td", defaultFontFamily, defaultFontSize);
    }
    // eliminate vertical scrollbar
    doc.writeLockEx();
    visitDocument(doc, new IDocumentVisitor() {
      public void visitElement(Element elem) {
        if ("body".equalsIgnoreCase(elem.getName())) {
          AttributeSet atts = elem.getAttributes();
          if (atts instanceof MutableAttributeSet) {
            MutableAttributeSet mutableAtts = (MutableAttributeSet) atts;
            Object styleAtt = mutableAtts.getAttribute(HTML.Attribute.STYLE);
            if (styleAtt == null) {
              styleAtt = "overflow: auto;";
              mutableAtts.addAttribute(HTML.Attribute.STYLE, styleAtt);
            }
            else if (styleAtt.toString().indexOf("overflow") < 0) {
              styleAtt = "overflow: auto; " + styleAtt.toString();
              mutableAtts.addAttribute(HTML.Attribute.STYLE, styleAtt);
            }
          }
        }
      }

      public void visitAttribute(Element elem, AttributeSet atts, Object nm, Object value) {
        // nop
      }
    });
    doc.writeUnlockEx();
    return htmlDoc;
  }

  /**
   * HTML has several troubles with some CSS and tag style concepts This method
   * calls {@link #cleanupDocument(HTMLDocument, String, int)} and in addition
   * tries to auto-correct, wellform and fix all known issues of a html document
   * for usage in swing's JTextPane
   */
  public static HTMLDocument wellformDocument(HTMLDocument htmlDoc, String defaultFontFamily, int defaultFontSize) {
    if (htmlDoc == null) return htmlDoc;
    //
    cleanupDocument(htmlDoc, defaultFontFamily, defaultFontSize);
    //
    MutableHTMLDocument doc = (MutableHTMLDocument) htmlDoc;
    StyleSheet styleSheet = doc.getStyleSheet();
    // correct font and color units
    Pattern fontSizeWithUnitPat = Pattern.compile("([0-9]+)([^0-9]+)");
    Pattern colorThreeDigitPat = Pattern.compile("#(.)(.)(.)");
    for (Enumeration<?> en = styleSheet.getStyleNames(); en.hasMoreElements();) {
      String nm = (String) en.nextElement();
      Style style = styleSheet.getStyle(nm);
      for (Enumeration<?> en2 = style.getAttributeNames(); en2.hasMoreElements();) {
        Object attKey = en2.nextElement();
        CSS.Attribute cssAtt = cssMap.get("" + attKey);
        String value = "" + style.getAttribute(attKey);
        if (cssAtt != null) {
          if (cssAtt == CSS.Attribute.FONT_SIZE) {
            Matcher m = fontSizeWithUnitPat.matcher(value);
            if (m.matches()) {
              value = m.group(1);
              styleSheet.addCSSAttribute(style, cssAtt, value);
            }
          }
          else if (cssAtt == CSS.Attribute.COLOR || cssAtt == CSS.Attribute.BACKGROUND_COLOR || cssAtt == CSS.Attribute.BORDER_COLOR) {
            Matcher m = colorThreeDigitPat.matcher(value);
            if (m.matches()) {
              value = "#" + m.group(1) + m.group(1) + m.group(2) + m.group(2) + m.group(3) + m.group(3);
              styleSheet.addCSSAttribute(style, cssAtt, value);
            }
          }
        }
      }
    }
    // clean font tags if any styles are present
    doc.writeLockEx();
    visitDocument(doc, new IDocumentVisitor() {
      public void visitElement(Element elem) {
        // nop
      }

      public void visitAttribute(Element elem, AttributeSet atts, Object nm, Object value) {
        if (nm == HTML.Attribute.FACE || nm == HTML.Attribute.SIZE || nm == CSS.Attribute.FONT_FAMILY || nm == CSS.Attribute.FONT_SIZE) {
          if (atts instanceof MutableAttributeSet) {
            ((MutableAttributeSet) atts).removeAttribute(nm);
          }
        }
      }
    });
    doc.writeUnlockEx();
    return htmlDoc;
  }

  /**
   * Mail HTML may contain cid:xxxx URLs for local contents. This method
   * replaces such local cid URLs by real file URLs (normally a temporary
   * folder)
   */
  public static HTMLDocument replaceContendIDs(HTMLDocument htmlDoc, final Map<String, URL> cidToUrlMapping) {
    if (htmlDoc == null) return htmlDoc;
    MutableHTMLDocument doc = (MutableHTMLDocument) htmlDoc;
    StyleSheet styleSheet = doc.getStyleSheet();
    final IntegerHolder changeCount = new IntegerHolder(0);
    // visit all css attributes for occurrences of "cid:" URLs
    Pattern cidPattern = Pattern.compile(".*url\\s*\\(\\s*cid:(.*)\\s*\\).*");
    for (Enumeration<?> en = styleSheet.getStyleNames(); en.hasMoreElements();) {
      String nm = (String) en.nextElement();
      Style style = styleSheet.getStyle(nm);
      for (Enumeration<?> en2 = style.getAttributeNames(); en2.hasMoreElements();) {
        Object attKey = en2.nextElement();
        CSS.Attribute cssAtt = cssMap.get("" + attKey);
        String value = "" + style.getAttribute(attKey);
        if (cssAtt != null && (!value.equals("null")) && value.length() > 0) {
          Matcher m = cidPattern.matcher(value);
          if (m.matches()) {
            String cid = m.group(1);
            URL url = cidToUrlMapping.get(cid);
            if (url != null) {
              styleSheet.addCSSAttribute(style, cssAtt, "url(" + url.toExternalForm() + ")");
              changeCount.setValue(changeCount.getValue() + 1);
            }
          }
        }
      }
    }
    // visit all img tags for occurrences of "cid:" URLs
    doc.writeLockEx();
    visitDocument(doc, new IDocumentVisitor() {
      public void visitElement(Element elem) {
      }

      public void visitAttribute(Element elem, AttributeSet atts, Object nm, Object value) {
        if (nm == HTML.Attribute.SRC || nm == HTML.Attribute.HREF) {
          String src = "" + value;
          if (src.startsWith("cid:")) {
            String cid = src.substring(4);
            URL url = cidToUrlMapping.get(cid);
            if (url != null) {
              if (atts instanceof MutableAttributeSet) {
                ((MutableAttributeSet) atts).addAttribute(nm, url.toExternalForm());
                changeCount.setValue(changeCount.getValue() + 1);
              }
            }
          }
        }
      }
    });
    doc.writeUnlockEx();
    return htmlDoc;
  }

  public static HTMLDocument copyReferencedFilesToCache(HTMLDocument htmlDoc, final File cacheDir) {
    if (htmlDoc == null) return htmlDoc;
    MutableHTMLDocument doc = (MutableHTMLDocument) htmlDoc;
    // StyleSheet styleSheet=doc.getStyleSheet();
    final IntegerHolder changeCount = new IntegerHolder(0);
    // visit all img tags for occurrences of "cid:" URLs
    doc.writeLockEx();
    visitDocument(doc, new IDocumentVisitor() {
      public void visitElement(Element elem) {
      }

      public void visitAttribute(Element elem, AttributeSet atts, Object nm, Object value) {
        if (nm == HTML.Attribute.SRC || nm == HTML.Attribute.HREF) {
          String src = "" + value;
          URL url;
          try {
            url = new URL(src);
            File f = new File(url.getFile());
            if (f.exists() && !f.getParentFile().getAbsolutePath().equalsIgnoreCase(cacheDir.getAbsolutePath())) {
              // this file is not yet in cache and needs to be copied to cache
              File cacheFile = new File(cacheDir, f.getName());
              if (cacheFile.exists() && cacheFile.length() != f.length()) {
                IOUtility.writeContent(new FileOutputStream(cacheFile), IOUtility.getContent(new FileInputStream(f)));
              }
              // change attribute value
              if (atts instanceof MutableAttributeSet) {
                ((MutableAttributeSet) atts).addAttribute(nm, f.getName());
                changeCount.setValue(changeCount.getValue() + 1);
              }
            }
          }
          catch (MalformedURLException e) {
          }
          catch (Exception e) {
            LOG.warn(null, e);
          }
        }
      }
    });
    doc.writeUnlockEx();
    return htmlDoc;
  }

  public static String getPlainText(HTMLDocument htmlDoc) {
    if (htmlDoc == null) return "";
    try {
      return htmlDoc.getText(0, htmlDoc.getLength());
    }
    catch (BadLocationException e) {
      return "";
    }
  }

  /**
   * @return simple and quick conversion of html text to plain text without parsing and building of a html model
   *         <p>
   *         Rule based conversion:
   * 
   *         <pre>
   * <xmp>
   * <br>|<br/>
   * |
   * 
   *         </p>
   *         |
   *         <p/>
   *         |</tr>|</table> create newlines </xmp></pre>
   */
  public static String getPlainText(String s) {
    s = StringUtility.getTag(s, "body");
    if (s == null || s.length() == 0) {
      return s;
    }
    //newlines
    s = s.replaceAll("\n", " ");
    s = s.replaceAll("<br>|<br/>|</p>|<p/>|</tr>|</table>", "\n");
    //remove tags
    s = Pattern.compile("<[^>]+>", Pattern.DOTALL).matcher(s).replaceAll(" ");
    //remove multiple spaces
    s = s.replaceAll("[ ]+", " ");
    s = StringUtility.htmlDecode(s);
    s = s.trim();
    return s;
  }

  /**
   * @return encoded text, ready to be included in a html text
   *         <xmp>replaces &, ", ', <, > and all whitespace</xmp>
   */
  public static String encodeText(String s) {
    return StringUtility.htmlEncode(s, true);
  }

  /**
   * @return decoded text, ready to be printed as text
   *         <xmp>replaces &, ", ', <, > and all whitespace</xmp>
   */
  public static String decodeText(String s) {
    return StringUtility.htmlDecode(s);
  }

  private static void visitDocument(HTMLDocument doc, IDocumentVisitor v) {
    visitElementRec(doc.getDefaultRootElement(), v);
  }

  private static void visitElementRec(Element elem, IDocumentVisitor v) {
    v.visitElement(elem);
    visitAttributeRec(elem, elem.getAttributes(), v);
    if (elem instanceof AbstractDocument.AbstractElement) {
      for (Enumeration en = ((AbstractDocument.AbstractElement) elem).children(); en != null && en.hasMoreElements();) {
        visitElementRec((Element) en.nextElement(), v);
      }
    }
  }

  private static void visitAttributeRec(Element elem, AttributeSet atts, IDocumentVisitor v) {
    for (Enumeration<?> en = atts.getAttributeNames(); en.hasMoreElements();) {
      Object nm = en.nextElement();
      Object value = atts.getAttribute(nm);
      if (value instanceof AttributeSet) {
        visitAttributeRec(elem, (AttributeSet) value, v);
      }
      else {
        v.visitAttribute(elem, atts, nm, value);
      }
    }
  }

  private static void setDefaultFont(StyleSheet styleSheet, String styleName, String defaultFontFamily, int defaultFontSize) {
    Style style = styleSheet.getStyle(styleName);
    if (style == null) {
      style = styleSheet.addStyle(styleName, null);
    }
    if (style.getAttribute(CSS.Attribute.FONT_FAMILY) == null) {
      styleSheet.addCSSAttribute(style, CSS.Attribute.FONT_FAMILY, defaultFontFamily);
    }
    if (style.getAttribute(CSS.Attribute.FONT_SIZE) == null) {
      styleSheet.addCSSAttribute(style, CSS.Attribute.FONT_SIZE, defaultFontSize + "pt");
    }
  }

  private static class MutableHTMLDocument extends HTMLDocument {
    private static final long serialVersionUID = 1L;

    public MutableHTMLDocument() {
      super();
    }

    public MutableHTMLDocument(StyleSheet styles) {
      super(styles);
    }

    public MutableHTMLDocument(Content c, StyleSheet styles) {
      super(c, styles);
    }

    public void writeLockEx() {
      writeLock();
    }

    public void writeUnlockEx() {
      writeUnlock();
    }
  }

  private static interface IDocumentVisitor {
    void visitElement(Element elem);

    void visitAttribute(Element elem, AttributeSet atts, Object nm, Object value);
  }

}
