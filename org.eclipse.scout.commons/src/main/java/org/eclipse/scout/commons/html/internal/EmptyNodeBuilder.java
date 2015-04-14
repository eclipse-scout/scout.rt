/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.commons.html.internal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.eclipse.scout.commons.html.IHtmlBind;
import org.eclipse.scout.commons.html.IHtmlContent;

/**
 * Empty node for HTML fragments:
 * Creates a node that may contain other html content, but does not have a tag name.
 */
public class EmptyNodeBuilder extends AbstractExpressionBuilder implements IHtmlContent {
  private final List<? extends IHtmlBind> m_texts;

  public EmptyNodeBuilder(CharSequence... texts) {
    this(Arrays.asList(texts));
  }

  public EmptyNodeBuilder(List<? extends CharSequence> texts) {
    ArrayList<IHtmlBind> bindTexts = new ArrayList<IHtmlBind>();
    for (CharSequence text : texts) {
      if (text instanceof IHtmlContent) {
        getBinds().putAll(((IHtmlContent) text).getBinds());
        bindTexts.add((IHtmlContent) text);
      }
      else {
        bindTexts.add(getBinds().put(text));
      }
    }

    m_texts = bindTexts;
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
    for (IHtmlBind elem : m_texts) {
      elem.replaceBinds(bindMap);
    }
  }

}
