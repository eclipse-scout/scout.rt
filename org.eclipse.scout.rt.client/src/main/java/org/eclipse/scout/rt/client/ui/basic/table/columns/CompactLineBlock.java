/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.basic.table.columns;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.html.HTML;
import org.eclipse.scout.rt.platform.html.HtmlHelper;
import org.eclipse.scout.rt.platform.util.ObjectUtility;
import org.eclipse.scout.rt.platform.util.StringUtility;

public class CompactLineBlock {
  private String m_text;
  private String m_processedText;
  private String m_icon;
  private boolean m_encodeHtmlEnabled = true;
  private boolean m_nlToBrEnabled;
  private boolean m_htmlToPlainTextEnabled;
  private HtmlHelper m_htmlHelper;

  public CompactLineBlock() {
  }

  public CompactLineBlock(String text, String icon) {
    setText(text);
    setIcon(icon);
  }

  public String getText() {
    return m_text;
  }

  public void setText(String text) {
    m_text = text;
  }

  public String getProcessedText() {
    if (m_processedText == null) {
      m_processedText = processText();
    }
    return m_processedText;
  }

  public String getIcon() {
    return m_icon;
  }

  public void setIcon(String icon) {
    m_icon = icon;
  }

  public boolean isEncodeHtmlEnabled() {
    return m_encodeHtmlEnabled;
  }

  /**
   * Has no effect if {@link #isHtmlToPlainTextEnabled()} is true.
   */
  public void setEncodeHtmlEnabled(boolean encodeHtmlEnabled) {
    if (m_encodeHtmlEnabled != encodeHtmlEnabled) {
      m_processedText = null;
    }
    m_encodeHtmlEnabled = encodeHtmlEnabled;
  }

  public boolean isNlToBrEnabled() {
    return m_nlToBrEnabled;
  }

  public void setNlToBrEnabled(boolean nlToBrEnabled) {
    if (m_nlToBrEnabled != nlToBrEnabled) {
      m_processedText = null;
    }
    m_nlToBrEnabled = nlToBrEnabled;
  }

  public boolean isHtmlToPlainTextEnabled() {
    return m_htmlToPlainTextEnabled;
  }

  /**
   * Wins over {@link #isEncodeHtmlEnabled()}
   */
  public void setHtmlToPlainTextEnabled(boolean htmlToPlainTextEnabled) {
    if (m_htmlToPlainTextEnabled != htmlToPlainTextEnabled) {
      m_processedText = null;
    }
    m_htmlToPlainTextEnabled = htmlToPlainTextEnabled;
  }

  protected String processText() {
    String text = getText();
    if (isHtmlToPlainTextEnabled()) {
      if (isNlToBrEnabled()) {
        // Preserve new lines, toPlainText would replace \n with " "
        text = getHtmlHelper().newLineToBr(text);
      }
      text = getHtmlHelper().toPlainText(text);
    }
    else if (isEncodeHtmlEnabled()) {
      text = getHtmlHelper().escape(text);
    }
    if (isNlToBrEnabled()) {
      text = getHtmlHelper().newLineToBr(text);
    }
    return text;
  }

  protected HtmlHelper getHtmlHelper() {
    if (m_htmlHelper == null) {
      m_htmlHelper = BEANS.get(HtmlHelper.class);
    }
    return m_htmlHelper;
  }

  public String build() {
    if (getIcon() != null) {
      return HTML.icon(getIcon()).addCssClass("icon " + (StringUtility.hasText(getProcessedText()) ? "with-text" : "")) + ObjectUtility.nvl(getProcessedText(), "");
    }
    return getProcessedText();
  }
}
