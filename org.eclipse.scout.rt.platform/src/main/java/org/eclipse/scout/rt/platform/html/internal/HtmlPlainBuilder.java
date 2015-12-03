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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.scout.rt.platform.html.IHtmlContent;

public class HtmlPlainBuilder extends AbstractExpressionBuilder {
  private final List<? extends CharSequence> m_texts;

  public HtmlPlainBuilder(CharSequence... texts) {
    this(Arrays.asList(texts));
  }

  public HtmlPlainBuilder(List<? extends CharSequence> texts) {
    m_texts = importTexts(texts);
  }

  protected ArrayList<? extends CharSequence> importTexts(List<? extends CharSequence> texts) {
    ArrayList<CharSequence> bindTexts = new ArrayList<CharSequence>();
    for (CharSequence text : texts) {
      if (text == null) {
        continue;
      }
      if (text instanceof IHtmlContent) {
        bindTexts.add(importHtml((IHtmlContent) text));
      }
      else {
        bindTexts.add(text);
      }
    }
    return bindTexts;
  }

  @Override
  public void build() {
    if (m_texts.size() > 0) {
      appendText();
    }
  }

  protected void appendText() {
    for (CharSequence t : m_texts) {
      append(t);
    }
  }

}
