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
import java.util.Map;

import org.eclipse.scout.rt.platform.html.IHtmlBind;
import org.eclipse.scout.rt.platform.html.IHtmlContent;

/**
 * Empty node for HTML fragments: Creates a node that may contain other html content, but does not have a tag name.
 */
public class HtmlContentBuilder extends AbstractExpressionBuilder implements IHtmlContent {
  private final List<? extends IHtmlBind> m_texts;

  public HtmlContentBuilder(CharSequence... texts) {
    this(Arrays.asList(texts));
  }

  public HtmlContentBuilder(List<? extends CharSequence> texts) {
    m_texts = importTexts(texts);
  }

  protected ArrayList<? extends IHtmlBind> importTexts(List<? extends CharSequence> texts) {
    ArrayList<IHtmlBind> bindTexts = new ArrayList<IHtmlBind>();
    for (CharSequence text : texts) {
      if (text == null) {
        continue;
      }
      else if (text instanceof IHtmlContent) {
        bindTexts.add(importHtml((IHtmlContent) text));
      }
      else if (text instanceof IHtmlBind) {
        bindTexts.add((IHtmlBind) text);
      }
      else {
        bindTexts.add(getBinds().put(text));
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

  @Override
  public void replaceBinds(Map<String, String> bindMap) {
    super.replaceBinds(bindMap);
    for (IHtmlBind elem : m_texts) {
      elem.replaceBinds(bindMap);
    }
  }

  protected List<? extends IHtmlBind> getTexts() {
    return m_texts;
  }

}
