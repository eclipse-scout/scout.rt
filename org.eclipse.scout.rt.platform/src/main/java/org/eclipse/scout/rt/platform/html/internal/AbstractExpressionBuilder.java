/*******************************************************************************
 * Copyright (c) 2010-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.platform.html.internal;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.html.HtmlHelper;
import org.eclipse.scout.rt.platform.html.IHtmlContent;

/**
 * Buffer for expressions</br>
 * (not thread safe)
 */
public abstract class AbstractExpressionBuilder implements IHtmlContent {

  private static final long serialVersionUID = 1L;

  private StringBuilder m_buf;

  private boolean m_newLineToBr = true;

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

  @Override
  public boolean isNewLineToBr() {
    return m_newLineToBr;
  }

  @Override
  public IHtmlContent withNewLineToBr(boolean newLineToBr) {
    m_newLineToBr = newLineToBr;
    return this;
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

  protected void append(CharSequence arg, boolean escape) {
    String toAppend = null;
    if (arg == null) {
      toAppend = "";
    }
    else if (escape) {
      if (isNewLineToBr()) {
        toAppend = escapeAndNewLineToBr(arg);
      }
      else {
        toAppend = escape(arg);
      }
    }
    else {
      toAppend = arg.toString();
    }
    m_buf.append(toAppend);
  }

  @Override
  public String toHtml() {
    return toString();
  }

  @Override
  public String toPlainText() {
    return BEANS.get(HtmlHelper.class).toPlainText(toHtml());
  }

  /**
   * @return the encoded bind value.
   */
  protected String escape(Object value) {
    return BEANS.get(HtmlHelper.class).escape(value.toString());
  }

  protected String escapeAndNewLineToBr(Object value) {
    return BEANS.get(HtmlHelper.class).escapeAndNewLineToBr(value.toString());
  }
}
