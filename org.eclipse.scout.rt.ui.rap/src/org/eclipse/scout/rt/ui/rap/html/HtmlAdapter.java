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
package org.eclipse.scout.rt.ui.rap.html;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.rwt.RWT;
import org.eclipse.rwt.resources.IResourceManager;
import org.eclipse.scout.commons.HTMLUtility;
import org.eclipse.scout.commons.HTMLUtility.DefaultFont;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.form.fields.htmlfield.IHtmlField;
import org.eclipse.scout.rt.ui.rap.IRwtEnvironment;
import org.eclipse.scout.rt.ui.rap.basic.IRwtScoutComposite;
import org.eclipse.scout.rt.ui.rap.form.fields.IRwtScoutFormField;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Label;

/**
 * @since 3.8.1
 */
public class HtmlAdapter {
  private static IScoutLogger LOG = ScoutLogManager.getLogger(HtmlAdapter.class);

  private IRwtEnvironment m_uiEnvironment;

  public HtmlAdapter(IRwtEnvironment uiEnvironment) {
    m_uiEnvironment = uiEnvironment;
  }

  /**
   * @return styled partial html text (<b>no</b> document with root tag &lt;html&gt;) that can be used inside for
   *         example table headers and table cells.
   *         The html, head and body tags are removed, newlines are replaced by br tags.
   */
  public String adaptHtmlCell(IRwtScoutComposite<?> uiComposite, String rawHtml) {
    /*
     * HTML: <!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">
     * XHTML: <!DOCTYPE HTML PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
     * TODO rwt Issue: As long as rwt-index.html uses HTML and not XHTML. tables and other nested elements ignore style of div parent.
     * Alternative is using color:inherit etc. in inner tables but this does not work in IE8.
     * Therefore we adapt the style tag of <table> and <a> tags.
     */
    int size = 12;
    if (uiComposite.getUiField() != null) {
      FontData[] fa = uiComposite.getUiField().getFont().getFontData();
      if (fa != null && fa.length > 0) {
        if (fa[0].getHeight() > 0) {
          size = fa[0].getHeight();
        }
      }
    }

    String stylePrefix = "color:inherit;background-color:inherit;font-size:" + size + "px;";
    rawHtml = bugfixStyles(rawHtml, stylePrefix);

    rawHtml = replaceImageCids(rawHtml);

    return rawHtml;
  }

  private static final Pattern imageCidPattern = Pattern.compile("(['\"(])(cid:)([^()\"']*)([)'\"])", Pattern.CASE_INSENSITIVE);

  /**
   * Replaces the images in the raw html by the actual resource name.
   * <p>
   * <b>Important:</b> Only images with the prefix <code>cid:</code> will be replaced
   */
  protected String replaceImageCids(String rawHtml) {
    if (rawHtml == null) {
      return null;
    }

    try {
      Matcher m = imageCidPattern.matcher(rawHtml);
      while (m.find()) {
        String cidMarker = m.group(2);
        String imageName = m.group(3);
        String location = resolveImageResourceName(imageName);
        if (location != null) {
          String imageUrl = getImageUrl(location);
          rawHtml = rawHtml.replace(m.group(1) + cidMarker + imageName + m.group(4), m.group(1) + imageUrl + m.group(4));
        }
        else {
          LOG.warn("Image resource name could not be resolved. Image: " + imageName);
        }
      }

    }
    catch (IOException e) {
      LOG.error("Exception occured while replacing image cids.", e);
    }

    return rawHtml;
  }

  protected String getImageUrl(String location) {
    return RWT.getRequest().getContextPath() + "/" + location;
  }

  @SuppressWarnings("restriction")
  protected String resolveImageResourceName(String imageName) throws IOException {
    Image image = m_uiEnvironment.getIcon(imageName);
    if (image == null) {
      return null;
    }

    IResourceManager resourceManager = RWT.getResourceManager();
    return resourceManager.getLocation(image.internalImage.getResourceName());
  }

  private static final Pattern tableTagPattern = Pattern.compile("<table([^>]*)>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
  private static final Pattern styleAttributePattern = Pattern.compile("style\\s*=\\s*\"([^\"]*)\"", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

  private String bugfixStyles(String html, String stylePrefix) {
    Matcher m = tableTagPattern.matcher(html);
    StringBuilder buf = new StringBuilder();
    int lastPos = 0;
    while (m.find()) {
      buf.append(html.substring(lastPos, m.start()));
      String atts = m.group(1);
      Matcher m2 = styleAttributePattern.matcher(atts);
      if (m2.find()) {
        buf.append(html.substring(m.start(), m.start(1) + m2.start(1)));
        buf.append(stylePrefix);
        buf.append(html.substring(m.start(1) + m2.start(1), m.end()));
      }
      else {
        buf.append(html.substring(m.start(), m.start(1)));
        buf.append(" style=\"");
        buf.append(stylePrefix);
        buf.append("\" ");
        buf.append(html.substring(m.start(1), m.end()));
      }
      lastPos = m.end();
    }
    if (lastPos < html.length()) {
      buf.append(html.substring(lastPos));
    }
    return buf.toString();
  }

  private static final Pattern localLinkTagPattern = Pattern.compile("<a (href=\"" + ITable.LOCAL_URL_PREFIX + "[^>]*)>([^>]*)</a>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

  /**
   * @return converted links to local urls in html text. <b>&lt;a&gt;</b> -&gt; styled <b>&lt;span&gt;</b> as in Link
   *         widget.
   */
  public String convertLinksWithLocalUrlsInHtmlCell(IRwtScoutComposite<?> uiComposite, String rawHtml) {
    Matcher m = localLinkTagPattern.matcher(rawHtml);
    StringBuilder buf = new StringBuilder();
    int lastPos = 0;
    while (m.find()) {
      buf.append(rawHtml.substring(lastPos, m.start()));

      buf.append("<span ");
      buf.append("class=\"link\" ");
      buf.append("style=\"");
      buf.append("text-decoration:underline;cursor:pointer;\" ");
      buf.append(rawHtml.substring(m.start(1), m.end(1)));
      buf.append(">");
      buf.append(rawHtml.substring(m.start(2), m.end(2)));
      buf.append("</span>");

      lastPos = m.end();
    }
    if (lastPos < rawHtml.length()) {
      buf.append(rawHtml.substring(lastPos));
    }
    return buf.toString();
  }

  /**
   * @return complete html document (root tag <html>).
   *         The raw html is completed and equipped with default style to match current style sheet (only if no head and
   *         style section exists)
   */
  public String styleHtmlText(IRwtScoutFormField<?> uiComposite, String rawHtml) {
    if (rawHtml == null) {
      rawHtml = "";
    }
    String cleanHtml = rawHtml;

    if (uiComposite.getScoutObject() instanceof IHtmlField) {
      IHtmlField htmlField = (IHtmlField) uiComposite.getScoutObject();
      if (htmlField.isHtmlEditor()) {
        /*
         * In HTML editor mode, the HTML is not styled except that an empty HTML skeleton is created in case the given HTML is empty.
         * In general no extra styling should be applied because the HTML installed in the editor should be the very same as
         * provided. Otherwise, if the user did some modifications in the HTML source and reloads the HTML in the editor anew,
         * unwanted auto-corrections would be applied.
         */
        if (!StringUtility.hasText(cleanHtml)) {
          cleanHtml = "<html><head></head><body></body></html>";
        }
      }
      else {
        /*
         * Because @{link SwtScoutHtmlField} is file based, it is crucial to set the content-type and charset appropriately.
         * Also, the CSS needs not to be cleaned as the native browser is used.
         */
        cleanHtml = HTMLUtility.cleanupHtml(cleanHtml, true, false, createDefaultFontSettings(uiComposite));
      }
    }

    return cleanHtml;
  }

  public DefaultFont createDefaultFontSettings(IRwtScoutFormField<?> uiComposite) {
    DefaultFont defaultFont = new DefaultFont();
    defaultFont.setSize(12);
    defaultFont.setSizeUnit("px");
    defaultFont.setForegroundColor(0x000000);
    defaultFont.setFamilies(new String[]{"sans-serif"});

    if (uiComposite != null && uiComposite.getUiField() != null) {
      FontData[] fontData = uiComposite.getUiField().getFont().getFontData();
      if (fontData == null || fontData.length <= 0) {
        Label label = new Label(uiComposite.getUiContainer(), SWT.NONE);
        fontData = label.getFont().getFontData();
        label.dispose();
      }
      if (fontData != null && fontData.length > 0) {
        int height = fontData[0].getHeight();
        if (height > 0) {
          defaultFont.setSize(height);
        }
        String fontFamily = fontData[0].getName();
        if (StringUtility.hasText(fontFamily)) {
          defaultFont.setFamilies(new String[]{fontFamily, "sans-serif"});
        }
      }
      Color color = uiComposite.getUiField().getForeground();
      if (color != null) {
        defaultFont.setForegroundColor(color.getRed() * 0x10000 + color.getGreen() * 0x100 + color.getBlue());
      }
    }
    return defaultFont;
  }
}
