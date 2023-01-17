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

import org.eclipse.scout.rt.platform.html.IHtmlElement;
import org.eclipse.scout.rt.platform.html.IHtmlTable;
import org.eclipse.scout.rt.platform.html.IHtmlTableColgroup;
import org.eclipse.scout.rt.platform.html.IHtmlTableRow;
import org.eclipse.scout.rt.platform.util.CollectionUtility;

/**
 * Builder for a HTML table (&lt;table&gt;).
 */
public class HtmlTableBuilder extends HtmlNodeBuilder implements IHtmlTable {

  private static final long serialVersionUID = 1L;

  public HtmlTableBuilder(List<IHtmlTableRow> rows) {
    this(null, rows);
  }

  public HtmlTableBuilder(IHtmlTableColgroup colgroup, List<IHtmlTableRow> rows) {
    super("table", merge(colgroup, rows));
  }

  protected static List<? extends IHtmlElement> merge(IHtmlTableColgroup colgroup, List<IHtmlTableRow> rows) {
    if (colgroup == null) {
      return rows;
    }
    if (rows == null) {
      return CollectionUtility.arrayList(colgroup);
    }

    List<IHtmlElement> result = CollectionUtility.arrayList(colgroup);
    result.addAll(rows);
    return result;
  }

  @Override
  public IHtmlTable cssClass(CharSequence cssClass) {
    return (IHtmlTable) super.cssClass(cssClass);
  }

  @Override
  public IHtmlTable style(CharSequence style) {
    return (IHtmlTable) super.style(style);
  }

  @Override
  public IHtmlTable appLink(CharSequence ref) {
    return (IHtmlTable) super.appLink(ref);
  }

  @Override
  public IHtmlTable addAttribute(String name, CharSequence value) {
    return (IHtmlTable) super.addAttribute(name, value);
  }
}
