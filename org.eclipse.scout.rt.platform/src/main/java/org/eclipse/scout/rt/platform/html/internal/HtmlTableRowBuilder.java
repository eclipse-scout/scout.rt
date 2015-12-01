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
package org.eclipse.scout.rt.platform.html.internal;

import java.util.List;

import org.eclipse.scout.rt.platform.html.IHtmlTableCell;
import org.eclipse.scout.rt.platform.html.IHtmlTableRow;

/**
 * Builder for a html table row.
 */
public class HtmlTableRowBuilder extends HtmlNodeBuilder implements IHtmlTableRow {

  public HtmlTableRowBuilder(List<? extends IHtmlTableCell> text) {
    super("tr", text);
  }

  /**
   * Add a css class
   */
  @Override
  public IHtmlTableRow cssClass(CharSequence cssClass) {
    return (IHtmlTableRow) super.cssClass(cssClass);
  }

  /**
   * Add a css style
   */
  @Override
  public IHtmlTableRow style(CharSequence style) {
    return (IHtmlTableRow) super.style(style);
  }

  /**
   * Add an application local link
   *
   * @param ref
   *          path to identify what is the link referring to.
   */
  @Override
  public IHtmlTableRow appLink(CharSequence ref) {
    return (IHtmlTableRow) super.appLink(ref);
  }

  @Override
  public IHtmlTableRow addAttribute(String name, CharSequence value) {
    return (IHtmlTableRow) super.addAttribute(name, value);
  }
}
