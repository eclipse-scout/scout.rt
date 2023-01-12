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

import org.eclipse.scout.rt.platform.html.IHtmlTableColgroupCol;

/**
 * Builder for a HTML table colgroup column element (&lt;col&gt;).
 */
public class HtmlTableColgroupColBuilder extends EmptyHtmlNodeBuilder implements IHtmlTableColgroupCol {

  private static final long serialVersionUID = 1L;

  public HtmlTableColgroupColBuilder() {
    super("col");
  }

  @Override
  public IHtmlTableColgroupCol cssClass(CharSequence cssClass) {
    return (IHtmlTableColgroupCol) super.cssClass(cssClass);
  }

  @Override
  public IHtmlTableColgroupCol style(CharSequence style) {
    return (IHtmlTableColgroupCol) super.style(style);
  }

  @Override
  public IHtmlTableColgroupCol appLink(CharSequence ref) {
    return (IHtmlTableColgroupCol) super.appLink(ref);
  }

  @Override
  public IHtmlTableColgroupCol addAttribute(String name, CharSequence value) {
    return (IHtmlTableColgroupCol) super.addAttribute(name, value);
  }
}
