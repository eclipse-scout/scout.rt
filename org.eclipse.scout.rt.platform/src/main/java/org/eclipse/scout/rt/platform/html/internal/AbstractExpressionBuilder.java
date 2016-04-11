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
package org.eclipse.scout.rt.platform.html.internal;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.html.HtmlBinds;
import org.eclipse.scout.rt.platform.html.HtmlHelper;
import org.eclipse.scout.rt.platform.html.IHtmlContent;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Buffer for expressions</br>
 * (not thread safe)
 */
public abstract class AbstractExpressionBuilder implements CharSequence, IHtmlContent {
  private static final Logger LOG = LoggerFactory.getLogger(AbstractExpressionBuilder.class);
  private static final long serialVersionUID = 1L;
  
  private StringBuilder m_buf;
  private HtmlBinds m_binds = new HtmlBinds();

  protected StringBuilder validate() {
    if (m_buf == null) {
      m_buf = new StringBuilder();
      build();
    }
    return m_buf;
  }

  protected void invalidate() {
    m_buf = null;
  }

  protected abstract void build();

  @Override
  public int length() {
    return validate().length();
  }

  @Override
  public char charAt(int index) {
    return validate().charAt(index);
  }

  @Override
  public CharSequence subSequence(int start, int end) {
    return validate().subSequence(start, end);
  }

  @Override
  public String toString() {
    return validate().toString();
  }

  protected void append(CharSequence arg) {
    m_buf.append(arg);
  }

  @Override
  public HtmlBinds getBinds() {
    return m_binds;
  }

  @Override
  public void setBinds(HtmlBinds binds) {
    m_binds = binds;
  }

  protected IHtmlContent importHtml(IHtmlContent contentText) {
    HtmlBinds binds = contentText.getBinds();
    Map<String, String> replacements = getBinds().getReplacements(binds);
    if (!replacements.isEmpty()) {
      contentText.replaceBinds(replacements);
    }

    getBinds().putAll(contentText.getBinds());
    return contentText;
  }

  @Override
  public String toEncodedHtml() {
    String res = this.toString();
    List<String> binds = getBinds().getBindParameters(res);
    Collections.sort(binds, new ReversedLengthComparator());
    for (String b : binds) {
      Object value = getBinds().getBindValue(b);
      if (value == null) {
        LOG.error("No bind value found for ", b);
      }
      else {
        String encode = encode(value);
        // quoteReplacement disables special meaning of $ and \ (back-references)
        res = res.replaceAll(b, Matcher.quoteReplacement(encode));
      }
    }
    return res;
  }
  

  @Override
  public String toPlainText() {
    return BEANS.get(HtmlHelper.class).toPlainText(toEncodedHtml());
  }

  /**
   * @return the encoded bind value.
   */
  protected String encode(Object value) {
    return StringUtility.htmlEncode(StringUtility.emptyIfNull(value).toString(), false);
  }

  @Override
  public void replaceBinds(Map<String, String> bindMap) {
    invalidate();
  }
}
