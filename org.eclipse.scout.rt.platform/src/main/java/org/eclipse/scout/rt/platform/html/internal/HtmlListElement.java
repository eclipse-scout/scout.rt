/*
 * Copyright (c) 2010-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.platform.html.internal;

import org.eclipse.scout.rt.platform.html.IHtmlListElement;

/**
 * Builder for a HTML list element (&lt;li&gt;).
 */
public class HtmlListElement extends HtmlNodeBuilder implements IHtmlListElement {

  private static final long serialVersionUID = 1L;

  public HtmlListElement(CharSequence text) {
    super("li", text);
  }

  @Override
  public IHtmlListElement cssClass(CharSequence cssClass) {
    return (IHtmlListElement) super.cssClass(cssClass);
  }

  @Override
  public IHtmlListElement style(CharSequence style) {
    return (IHtmlListElement) super.style(style);
  }

  @Override
  public IHtmlListElement appLink(CharSequence ref) {
    return (IHtmlListElement) super.appLink(ref);
  }

  @Override
  public IHtmlListElement addAttribute(String name, CharSequence value) {
    return (IHtmlListElement) super.addAttribute(name, value);
  }
}
