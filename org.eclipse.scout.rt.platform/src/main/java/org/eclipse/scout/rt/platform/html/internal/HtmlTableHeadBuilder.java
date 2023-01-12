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

import java.util.List;

import org.eclipse.scout.rt.platform.html.IHtmlTableCell;

/**
 * Builder for a HTML table head element (&lt;th&gt;).
 */
public class HtmlTableHeadBuilder extends HtmlNodeBuilder implements IHtmlTableCell {

  private static final long serialVersionUID = 1L;

  public HtmlTableHeadBuilder(List<? extends CharSequence> text) {
    super("th", text);
  }

  @Override
  public IHtmlTableCell colspan(int colspan) {
    addAttribute("colspan", colspan);
    return this;
  }

  @Override
  public IHtmlTableCell cssClass(CharSequence cssClass) {
    return (IHtmlTableCell) super.cssClass(cssClass);
  }

  @Override
  public IHtmlTableCell style(CharSequence style) {
    return (IHtmlTableCell) super.style(style);
  }

  @Override
  public IHtmlTableCell appLink(CharSequence ref) {
    return (IHtmlTableCell) super.appLink(ref);
  }

  @Override
  public IHtmlTableCell addAttribute(String name, CharSequence value) {
    return (IHtmlTableCell) super.addAttribute(name, value);
  }
}
