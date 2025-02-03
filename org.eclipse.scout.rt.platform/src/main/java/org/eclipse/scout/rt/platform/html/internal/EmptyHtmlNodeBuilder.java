/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.html.internal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * An empty HTML tag without a closing tag (e.g. br)
 */
public class EmptyHtmlNodeBuilder extends HtmlNodeBuilder {

  private static final long serialVersionUID = 1L;

  public EmptyHtmlNodeBuilder(String tag, CharSequence... texts) {
    this(tag, Arrays.asList(texts));
  }

  public EmptyHtmlNodeBuilder(String tag) {
    this(tag, new ArrayList<String>());
  }

  public EmptyHtmlNodeBuilder(String tag, List<? extends CharSequence> texts) {
    super(tag, texts);
  }

  @Override
  public void build() {
    appendStartTag();
    if (!getTexts().isEmpty()) {
      appendText();
    }
  }
}
