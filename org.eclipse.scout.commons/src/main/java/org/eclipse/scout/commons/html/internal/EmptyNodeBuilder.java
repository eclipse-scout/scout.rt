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

import java.util.Arrays;
import java.util.List;

import org.eclipse.scout.commons.html.IHtmlContent;

/**
 *
 */
public class EmptyNodeBuilder extends AbstractExpressionBuilder implements IHtmlContent {
  private final List<? extends CharSequence> m_texts;

  public EmptyNodeBuilder(CharSequence... texts) {
    this(Arrays.asList(texts));
  }

  public EmptyNodeBuilder(List<? extends CharSequence> texts) {
    m_texts = texts;
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
