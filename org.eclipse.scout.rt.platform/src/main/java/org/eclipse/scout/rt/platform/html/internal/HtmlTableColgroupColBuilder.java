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

import org.eclipse.scout.rt.platform.html.IHtmlTableColgroupCol;

/**
 * Builder for a html table colgroup col.
 */
public class HtmlTableColgroupColBuilder extends EmptyHtmlNodeBuilder implements IHtmlTableColgroupCol {

  private static final long serialVersionUID = 1L;

  public HtmlTableColgroupColBuilder() {
    super("col");
  }

  /**
   * Add a css class
   */
  @Override
  public IHtmlTableColgroupCol cssClass(CharSequence cssClass) {
    return (IHtmlTableColgroupCol) super.cssClass(cssClass);
  }

  /**
   * Add a css style
   */
  @Override
  public IHtmlTableColgroupCol style(CharSequence style) {
    return (IHtmlTableColgroupCol) super.style(style);
  }

  /**
   * Add an application local link
   *
   * @param ref
   *          path to identify what is the link referring to.
   */
  @Override
  public IHtmlTableColgroupCol appLink(CharSequence ref) {
    return (IHtmlTableColgroupCol) super.appLink(ref);
  }

  @Override
  public IHtmlTableColgroupCol addAttribute(String name, CharSequence value) {
    return (IHtmlTableColgroupCol) super.addAttribute(name, value);
  }
}
