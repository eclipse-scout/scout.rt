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

import java.util.List;

import org.eclipse.scout.commons.html.IHtmlTableCell;

/**
 * Builder for a html table data
 */
public class HtmlTableDataBuilder extends HtmlNodeBuilder implements IHtmlTableCell {

  public HtmlTableDataBuilder(List<? extends CharSequence> text) {
    super("td", text);
  }

  @Override
  public IHtmlTableCell colspan(int colspan) {
    addAttribute("colspan", colspan);
    return this;
  }

  /**
   * Add a css class
   */
  @Override
  public IHtmlTableCell cssClass(CharSequence cssClass) {
    return (IHtmlTableCell) super.cssClass(cssClass);
  }

  /**
   * Add a css style
   */
  @Override
  public IHtmlTableCell style(CharSequence style) {
    return (IHtmlTableCell) super.style(style);
  }

  /**
   * Add an application local link
   *
   * @param ref
   *          path to identify what is the link referring to.
   */
  @Override
  public IHtmlTableCell appLink(CharSequence ref) {
    return (IHtmlTableCell) super.appLink(ref);
  }

  @Override
  public IHtmlTableCell addAttribute(String name, CharSequence value) {
    return (IHtmlTableCell) super.addAttribute(name, value);
  }
}
