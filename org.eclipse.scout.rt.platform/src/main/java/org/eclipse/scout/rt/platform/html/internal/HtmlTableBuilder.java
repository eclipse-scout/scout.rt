/*******************************************************************************
 * Copyright (c) 2010-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.platform.html.internal;

import java.util.List;

import org.eclipse.scout.rt.platform.html.IHtmlElement;
import org.eclipse.scout.rt.platform.html.IHtmlTable;
import org.eclipse.scout.rt.platform.html.IHtmlTableColgroup;
import org.eclipse.scout.rt.platform.html.IHtmlTableRow;
import org.eclipse.scout.rt.platform.util.CollectionUtility;

/**
 * Builder for a html table.
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

  /**
   * Add a css class
   */
  @Override
  public IHtmlTable cssClass(CharSequence cssClass) {
    return (IHtmlTable) super.cssClass(cssClass);
  }

  /**
   * Add a css style
   */
  @Override
  public IHtmlTable style(CharSequence style) {
    return (IHtmlTable) super.style(style);
  }

  /**
   * Add an application local link
   *
   * @param ref
   *          path to identify what is the link referring to.
   */
  @Override
  public IHtmlTable appLink(CharSequence ref) {
    return (IHtmlTable) super.appLink(ref);
  }

  @Override
  public IHtmlTable addAttribute(String name, CharSequence value) {
    return (IHtmlTable) super.addAttribute(name, value);
  }
}
