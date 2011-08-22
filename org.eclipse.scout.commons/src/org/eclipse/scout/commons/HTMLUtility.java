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
import javax.swing.text.html.parser.DocumentParser;
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

  /**
   * <p>
   * Creates a {@link HTMLDocument} from the given HTML text.
   * </p>
   * <p>
   * Please note, that all &lt;meta&gt; elements are removed. This is because setting a charset would cause an exception
   * as {@link DocumentParser} does not allow to specify the charset in HTML text. Due to failsafe, all other
   * &lt;meta&gt; tags are removed as well as just informative anyway.
   * </p>
   * 
   * @param rawHtml
   * @return
   */
  public static HTMLDocument toHtmlDocument(String rawHtml) {
    if (StringUtility.isNullOrEmpty(rawHtml)) {
      return null;
    }
    HTMLEditorKit kit = new HTMLEditorKit();
    // remove meta tags (see JavaDoc of @{link HTMLDocument#parseDocument(String)} for more information)
    Pattern[] metaPatterns = new Pattern[]{
          Pattern.compile("<meta\\s+[^>]*>"),
          Pattern.compile("<Meta\\s+[^>]*>"),
          Pattern.compile("<META\\s+[^>]*>"),
          Pattern.compile("</\\s*meta\\s*>"),
          Pattern.compile("</\\s*Meta\\s*>"),
          Pattern.compile("</\\s*META\\s*>"),
        };
    for (Pattern p : metaPatterns) {
      rawHtml = p.matcher(rawHtml).replaceAll("");
    }

    // Remove quotes in style attribute which are not closed.
    rawHtml = HTMLUtility.removeUnclosedStyleQuotes(rawHtml, true);
    rawHtml = HTMLUtility.removeUnclosedStyleQuotes(rawHtml, false);

    // fix incorrect single tag endings ( <br/ > <br /> )
    rawHtml = rawHtml.replaceAll("/\\s+>", "/>");
    rawHtml = rawHtml.replaceAll("([^\\s])\\s+/>", "$1/>");
    // before jre 1.6 eliminate single tags and </br> tags
    if (System.getProperty("java.version", "1.5").substring(0, 3).compareTo("1.6") < 0) {
      rawHtml = rawHtml.replaceAll("/>", ">");
      rawHtml = rawHtml.replaceAll("</\\s*br\\s*>", "");
    }
    StyleSheet styleSheet = new StyleSheet();
    MutableHTMLDocument doc = new MutableHTMLDocument(styleSheet);
    try {
      doc.setParser(new ParserDelegator());
      doc.setTokenThreshold(100);
      doc.remove(0, doc.getLength());
      kit.read(new StringReader(rawHtml), doc, 0);
    }
    catch (Exception e) {
      LOG.warn(null, e);
      return null;
    }
    return doc;
  }

  public static String toHtmlText(HTMLDocument doc) {
    String htmlText = "";
    if (doc == null) {
      return htmlText;
    }

    try {
      HTMLEditorKit kit = new HTMLEditorKit();
      StringWriter buf = new StringWriter();
      kit.write(buf, doc, 0, doc.getLength());
      htmlText = buf.toString();
    }
    catch (Throwable t) {
      LOG.error("failed to extract HTML text from HTML document", t);
    }
    return htmlText;
  }

  /**
   * <p>
   * Applies some intelligence to the HTML document to ensure a valid HTML document.
   * </p>
   * 
   * @param rawHtml
   *          the raw HTML document
   * @param ensureContentType
   *          to add missing meta directive &lt;meta http-equiv="content-type" content="text/html;charset=UTF-8"/>
   * @param cleanupCss
   *          to cleanup CSS as HTML has some trouble with some style constructs.<br/>
   *          <small>For more information, please refer to {@link HTMLUtility#cleanupCss(HTMLDocument, DefaultFont)}
   *          </small>
   * @param defaultFont
   *          to ensure default font set
   * @return the formatted HTML document
   */
  public static String cleanupHtml(String rawHtml, boolean ensureContentType, boolean cleanupCss, DefaultFont defaultFont) {
    return cleanupHtml(rawHtml, ensureContentType, cleanupCss, defaultFont, null);
  }

  /**
   * <p>
   * Applies some intelligence to the HTML document to ensure a valid HTML document.
   * </p>
   * 
   * @param rawHtml
   *          the raw HTML document
   * @param ensureContentType
   *          to add missing meta directive &lt;meta http-equiv="content-type" content="text/html;charset=UTF-8"/>
   * @param cleanupCss
   *          to cleanup CSS as HTML has some trouble with some style constructs.<br/>
   *          <small>For more information, please refer to {@link HTMLUtility#cleanupCss(HTMLDocument, DefaultFont)}
   *          </small>
   * @param defaultFont
   *          to ensure default font set
   * @param cidToUrlMapping
   *          to replace the URL's of the given ContentID's (cid) <br/>
   *          <small>For more information, please refer to {@link HTMLUtility#replaceContendIDs(HTMLDocument, Map)}
   *          </small>
   * @return the formatted HTML document
   */
  public static String cleanupHtml(String rawHtml, boolean ensureContentType, boolean cleanupCss, DefaultFont defaultFont, Map<String, URL> cidToUrlMapping) {
    rawHtml = StringUtility.nvl(rawHtml, "");

    try {
      // 1a) ensure <HTML> tag
      Matcher matcherHtmlTag = createMatcherForTag(rawHtml, "html", false);
      if (!matcherHtmlTag.find()) {
        // no <html> tag found. Insert <html> tag just before first tag
        Matcher matcherFirstTag = Pattern.compile("<([^!>]+?)>", Pattern.CASE_INSENSITIVE).matcher(rawHtml);
        if (matcherFirstTag.find()) {
          rawHtml = rawHtml.substring(0, matcherFirstTag.start()) + "<html>" + rawHtml.substring(matcherFirstTag.start(), rawHtml.length());
        }
        else {
          // no tag found at all
          rawHtml = rawHtml += "<html></html>";
        }
      }

      // 1b) ensure </HTML> tag
      Matcher matcherHtmlEndTag = createMatcherForTag(rawHtml, "html", true);
      if (!matcherHtmlEndTag.find()) {
        rawHtml = rawHtml += "</html>";
      }

      // 2a) ensure <HEAD> tag
      Matcher matcherHeadTag = createMatcherForTag(rawHtml, "head", false);
      if (!matcherHeadTag.find()) {
        // no <head> tag found. Insert just after <html> tag
        matcherHtmlTag = createMatcherForTag(rawHtml, "html", false);
        matcherHtmlTag.find();
        rawHtml = rawHtml.substring(0, matcherHtmlTag.end()) + "<head>" + rawHtml.substring(matcherHtmlTag.end(), rawHtml.length());
      }

      // 2b) ensure </HEAD> tag
      Matcher matcherHeadEndTag = createMatcherForTag(rawHtml, "head", true);
      if (!matcherHeadEndTag.find()) {
        // no </head> tag found. Insert just after <head> tag
        matcherHeadTag = createMatcherForTag(rawHtml, "head", false);
        matcherHeadTag.find();
        rawHtml = rawHtml.substring(0, matcherHeadTag.end()) + "</head>" + rawHtml.substring(matcherHeadTag.end(), rawHtml.length());
      }

      // 3a) ensure <BODY> tag
      Matcher matcherBodyTag = createMatcherForTag(rawHtml, "body", false);
      if (!matcherBodyTag.find()) {
        // no <body> tag found. Insert just after </head> tag
        matcherHeadEndTag = createMatcherForTag(rawHtml, "head", true);
        matcherHeadEndTag.find();
        rawHtml = rawHtml.substring(0, matcherHeadEndTag.end()) + "<body>" + rawHtml.substring(matcherHeadEndTag.end(), rawHtml.length());
      }

      // 3b) ensure </BODY> tag
      Matcher matcherBodyEndTag = createMatcherForTag(rawHtml, "body", true);
      if (!matcherBodyEndTag.find()) {
        // no </body> tag found. Insert just before </html> tag
        matcherHtmlEndTag = createMatcherForTag(rawHtml, "html", true);
        matcherHtmlEndTag.find();
        rawHtml = rawHtml.substring(0, matcherHtmlEndTag.start()) + "</body>" + rawHtml.substring(matcherHtmlEndTag.start(), rawHtml.length());
      }

      // 4) Eliminate vertical scrollbar
      rawHtml = eliminateVerticalScrollbar(rawHtml);

      // 5) cleanup CSS of document
      if (cleanupCss || (cidToUrlMapping != null && cidToUrlMapping.size() > 0)) {
        HTMLDocument htmlDoc = HTMLUtility.toHtmlDocument(rawHtml);
        if (htmlDoc != null) {
          if (cleanupCss) {
            htmlDoc = HTMLUtility.cleanupCss(htmlDoc, defaultFont);
          }

          if (cidToUrlMapping != null) {
            htmlDoc = HTMLUtility.replaceContendIDs(htmlDoc, cidToUrlMapping);
          }
          rawHtml = HTMLUtility.toHtmlText(htmlDoc);
        }
      }

      // 6) ensure <META> element with content-type and charset (This must be done after 5) as <META> tags are removed in cleanup)
      if (ensureContentType) {
        rawHtml = HTMLUtility.addHtmlMetaElement("content-type", "text/html;charset=UTF-8", rawHtml);
      }
    }
    catch (Throwable t) {
      // failsafe
      LOG.warn("failed to cleanup HTML document", t);
    }

    return rawHtml;
  }

  /**
   * HTML has several troubles with some CSS and tag style concepts.
   * 
   * @param htmlDoc
   * @param defaultFont
   * @return
   */
  public static HTMLDocument cleanupCss(HTMLDocument htmlDoc, DefaultFont defaultFont) {
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
    if (defaultFont != null) {
      setDefaultFont(styleSheet, "body", defaultFont);
      setDefaultFont(styleSheet, "p", defaultFont);
      setDefaultFont(styleSheet, "span", defaultFont);
      setDefaultFont(styleSheet, "th", defaultFont);
      setDefaultFont(styleSheet, "td", defaultFont);
    }

    // correct font and color units
    Pattern fontSizeWithUnitPat = Pattern.compile("([0-9]+)([^0-9]+)");
    Pattern colorThreeDigitPat = Pattern.compile("#(.)(.)(.)");
    for (Enumeration<?> en = styleSheet.getStyleNames(); en.hasMoreElements();) {
      String nm = (String) en.nextElement();
      style = styleSheet.getStyle(nm);
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
      @Override
      public void visitElement(Element elem) {
        // nop
      }

      @Override
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

  /**
   * @param htmlText
   * @return
   * @deprecated use {@link HTMLUtility#toHtmlDocument(String)} instead.<br/>
   *             <small>Method will be removed in next release</small>
   */
  @Deprecated
  public static HTMLDocument parseDocument(String htmlText) {
    return HTMLUtility.toHtmlDocument(htmlText);
  }

  /**
   * @param doc
   * @return
   * @deprecated use {@link HTMLUtility#toHtmlText(HTMLDocument)} instead.<br/>
   *             <small>Method will be removed in next release</small>
   */
  @Deprecated
  public static String formatDocument(HTMLDocument doc) {
    return HTMLUtility.toHtmlText(doc);
  }

  /**
   * @param htmlDoc
   * @param defaultFontFamily
   * @param defaultFontSize
   * @return
   * @deprecated use {@link HTMLUtility#cleanupCss(HTMLDocument, DefaultFont)} instead.<br/>
   *             <small>Method will be removed in next release</small>
   */
  @Deprecated
  public static HTMLDocument cleanupDocument(HTMLDocument htmlDoc, String defaultFontFamily, int defaultFontSize) {
    DefaultFont defaultFont = new DefaultFont();
    defaultFont.setFamily(defaultFontFamily);
    defaultFont.setSize(defaultFontSize);
    return HTMLUtility.cleanupCss(htmlDoc, defaultFont);
  }

  /**
   * @param htmlDoc
   * @param defaultFontFamily
   * @param defaultFontSize
   * @return
   * @deprecated use {@link HTMLUtility#cleanupHtml(String, boolean, boolean, DefaultFont, Map)} instead.<br/>
   *             <small>Method will be removed in next release</small>
   */
  @Deprecated
  public static HTMLDocument wellformDocument(HTMLDocument htmlDoc, String defaultFontFamily, int defaultFontSize) {
    String htmlText = HTMLUtility.toHtmlText(htmlDoc);

    DefaultFont defaultFont = new DefaultFont();
    defaultFont.setFamily(defaultFontFamily);
    defaultFont.setSize(defaultFontSize);
    htmlText = HTMLUtility.cleanupHtml(htmlText, false, true, defaultFont);

    return HTMLUtility.toHtmlDocument(htmlText);
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

  private static void setDefaultFont(StyleSheet styleSheet, String styleName, DefaultFont defaultFont) {
    if (defaultFont == null) {
      return;
    }

    Style style = styleSheet.getStyle(styleName);
    if (style == null) {
      style = styleSheet.addStyle(styleName, null);
    }
    if (style.getAttribute(CSS.Attribute.FONT_FAMILY) == null && defaultFont.getFamily() != null) {
      styleSheet.addCSSAttribute(style, CSS.Attribute.FONT_FAMILY, defaultFont.getFamily());
    }
    if (style.getAttribute(CSS.Attribute.FONT_SIZE) == null && defaultFont.getSize() > 0 && defaultFont.getSizeUnit() != null) {
      styleSheet.addCSSAttribute(style, CSS.Attribute.FONT_SIZE, defaultFont.getSize() + defaultFont.getSizeUnit());
    }
  }

  /**
   * <p>
   * To add a meta element into the &lt;head&gt;&lt;/head&gt; section.
   * </p>
   * <p>
   * Precondition: &lt;head&gt; section must be contained in the given HTML text
   * </p>
   * 
   * @param httpEquiv
   *          the content type
   * @param content
   *          the content
   * @param rawHtml
   * @return
   */
  private static String addHtmlMetaElement(String httpEquiv, String content, String rawHtml) {
    if (StringUtility.isNullOrEmpty(rawHtml)) {
      return rawHtml;
    }

    String metaTagContentType = "<meta http-equiv=\"" + httpEquiv + "\" content=\"" + content + "\"/>";
    rawHtml = Pattern.compile("(<meta\\s*http-equiv=\"" + httpEquiv + "\"\\s*content=\"[^\"]+\"\\s*/>)", Pattern.CASE_INSENSITIVE).matcher(rawHtml).replaceFirst(metaTagContentType);
    // check <meta> tag to contain Content-Type
    if (StringUtility.find(rawHtml, metaTagContentType) == -1) {
      // no <meta> tag containing Content-Type found. Insert just after <head> tag
      Matcher matcherHeadTag = createMatcherForTag(rawHtml, "head", false);
      if (matcherHeadTag.find()) {
        rawHtml = rawHtml.substring(0, matcherHeadTag.end()) + metaTagContentType + rawHtml.substring(matcherHeadTag.end(), rawHtml.length());
      }
    }
    return rawHtml;
  }

  /**
   * <p>
   * Removes quotes in style attribute which are not closed.
   * </p>
   * <p>
   * This is important as document cannot be loaded by @{DocumentParser} otherwise.
   * </p>
   * 
   * @param rawHtml
   * @param singleStyleQuote
   *          the quote character for the style attribte
   * @return
   */
  private static String removeUnclosedStyleQuotes(String rawHtml, boolean singleStyleQuote) {
    char styleQuoteChar;
    char innerQuoteChar;
    if (singleStyleQuote) {
      styleQuoteChar = '\'';
      innerQuoteChar = '"';
    }
    else {
      styleQuoteChar = '"';
      innerQuoteChar = '\'';
    }

    String regex = "(<[^>]+?style[\\s]*=[\\s]*" + styleQuoteChar + ")([^" + styleQuoteChar + ">]*?" + innerQuoteChar + "[^" + styleQuoteChar + ">]*?)(" + styleQuoteChar + "[^>]*?>)";
    Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    Matcher matcher = pattern.matcher(rawHtml);
    while (matcher.find()) {
      String preStyleContent = matcher.group(1);
      String styleContent = matcher.group(2);
      String postStyleContent = matcher.group(3);

      int numOfTokens = ("prefix" + styleContent + "suffix").split(innerQuoteChar + "").length; // add some text before and after style content to ensure proper splitting

      // check that inner quote characters are closed meaning that an inner quote is closed by a subsequent inner quote and so on. Otherwise, HTML document cannot be loaded.
      // Inner quotes are not closed properly, if there is an even number of tokens, e.g. <p style='font-family:Cou'rier'>text</p> has 2 tokens.
      if (numOfTokens % 2 == 0) {
        // inner quotes not closed properly. Remove all inner quotes.
        styleContent = styleContent.replaceAll(String.valueOf(innerQuoteChar), "");
        rawHtml = matcher.replaceAll(preStyleContent + styleContent + postStyleContent);
      }
    }
    return rawHtml;
  }

  /**
   * Create a {@link Matcher} for the given tag. Please note that the tag is not considered to be an empty tag, e.g.
   * <code>&lt;br/&gt;</code>
   * 
   * @param rawHtml
   * @param tag
   *          the tag the matcher should be created for
   * @param endTag
   *          to indicate whether to match the start or end tag
   * @return
   */
  private static Matcher createMatcherForTag(String rawHtml, String tag, boolean endTag) {
    String regex = "<\\s*" + (endTag ? "\\/" : "") + "\\s*" + tag + ".*?>";
    return Pattern.compile(regex, Pattern.CASE_INSENSITIVE | Pattern.DOTALL).matcher(rawHtml);
  }

  /**
   * Eliminates the vertical scrollbar by setting overflow:auto in body style attribute.
   * 
   * @param rawHtml
   * @return
   */
  private static String eliminateVerticalScrollbar(String rawHtml) {
    try {
      // find body tag
      Matcher matcherBodyTag = createMatcherForTag(rawHtml, "body", false);
      if (!matcherBodyTag.find()) {
        return rawHtml;
      }
      String contentBodyTag = matcherBodyTag.group();

      // find style attribute
      Pattern pattern = Pattern.compile("style[\\s]*=[\\s]*([\"'])", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
      Matcher matcherStyleTag = pattern.matcher(contentBodyTag);
      if (matcherStyleTag.find()) {
        // style attribute available
        String styleQuoteChar = matcherStyleTag.group(1);

        // extract style attribute content
        pattern = Pattern.compile("(.+?style[\\s]*=[\\s]*" + styleQuoteChar + ")([^" + styleQuoteChar + ">]*?)(" + styleQuoteChar + ".*)", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
        matcherStyleTag = pattern.matcher(contentBodyTag);
        matcherStyleTag.find();
        String preStyleContent = matcherStyleTag.group(1);
        String styleContent = matcherStyleTag.group(2);
        String postStyleContent = matcherStyleTag.group(3);
        // ensure overflow:auto in style attribute
        if (!styleContent.toLowerCase().contains("overflow")) {
          if (styleContent.trim().length() > 0 && !styleContent.endsWith(";")) {
            styleContent += ";";
          }
          styleContent += "overflow:auto;";
          rawHtml = matcherBodyTag.replaceFirst(preStyleContent + styleContent + postStyleContent);
        }
      }
      else {
        // no style attribute available
        int endBracket = contentBodyTag.lastIndexOf(">");
        contentBodyTag = contentBodyTag.substring(0, endBracket) + " style=\"overflow:auto;\">";
        rawHtml = matcherBodyTag.replaceAll(contentBodyTag);
      }
    }
    catch (Throwable e) {
      LOG.warn("failed to eliminate vertical scrollbar", e);
    }

    return rawHtml;
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

  public static class DefaultFont {
    private String m_family;
    private int m_size;
    private String m_sizeUnit;
    private int m_foregroundColor;

    public DefaultFont() {
      m_family = "sans-serif";
      m_size = 12;
      m_sizeUnit = "pt";
      m_foregroundColor = 0x000000;
    }

    public String getFamily() {
      return m_family;
    }

    public void setFamily(String family) {
      m_family = family;
    }

    public int getSize() {
      return m_size;
    }

    public void setSize(int size) {
      m_size = size;
    }

    public String getSizeUnit() {
      return m_sizeUnit;
    }

    public void setSizeUnit(String sizeUnit) {
      m_sizeUnit = sizeUnit;
    }

    public int getForegroundColor() {
      return m_foregroundColor;
    }

    public void setForegroundColor(int foregroundColor) {
      m_foregroundColor = foregroundColor;
    }
  }
}
