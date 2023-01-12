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
import org.eclipse.scout.rt.platform.html.IHtmlTableRow;

/**
 * Builder for a HTML table row (&lt;tr&gt;).
 */
public class HtmlTableRowBuilder extends HtmlNodeBuilder implements IHtmlTableRow {

  private static final long serialVersionUID = 1L;

  public HtmlTableRowBuilder(List<? extends IHtmlTableCell> text) {
    super("tr", text);
  }

  @Override
  public IHtmlTableRow cssClass(CharSequence cssClass) {
    return (IHtmlTableRow) super.cssClass(cssClass);
  }

  @Override
  public IHtmlTableRow style(CharSequence style) {
    return (IHtmlTableRow) super.style(style);
  }

  @Override
  public IHtmlTableRow appLink(CharSequence ref) {
    return (IHtmlTableRow) super.appLink(ref);
  }

  @Override
  public IHtmlTableRow addAttribute(String name, CharSequence value) {
    return (IHtmlTableRow) super.addAttribute(name, value);
  }
}
