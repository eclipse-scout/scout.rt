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

/**
 * An empty HTML tag without a closing tag (e.g. br)
 */
public class EmptyHtmlNodeBuilder extends HtmlNodeBuilder {

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
    if (getTexts().size() > 0) {
      appendText();
    }
  }

}
