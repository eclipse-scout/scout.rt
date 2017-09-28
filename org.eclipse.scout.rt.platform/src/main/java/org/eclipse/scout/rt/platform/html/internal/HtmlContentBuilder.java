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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.platform.html.IHtmlContent;

/**
 * Empty node for HTML fragments: Creates a node that may contain other html content, but does not have a tag name.
 */
public class HtmlContentBuilder extends AbstractExpressionBuilder {

  private static final long serialVersionUID = 1L;

  private final List<? extends CharSequence> m_texts;

  public HtmlContentBuilder(CharSequence... texts) {
    this(Arrays.asList(texts));
  }

  public HtmlContentBuilder(List<? extends CharSequence> texts) {
    // explicit check for non-serializable objects
    for (CharSequence text : texts) {
      if (text == null) {
        continue;
      }

      if (!(text instanceof Serializable)) {
        throw new ProcessingException("At least one provided char sequence is not serializable: {}", text);
      }
    }

    m_texts = new ArrayList<>(texts);
  }

  @Override
  public void build() {
    if (!m_texts.isEmpty()) {
      appendText();
    }
  }

  protected void appendText() {
    for (CharSequence t : m_texts) {
      append(t, !(t instanceof IHtmlContent));
    }
  }

  protected List<? extends CharSequence> getTexts() {
    return m_texts;
  }
}
