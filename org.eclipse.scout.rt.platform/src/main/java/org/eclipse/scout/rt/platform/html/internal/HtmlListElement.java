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

import org.eclipse.scout.rt.platform.html.IHtmlListElement;

public class HtmlListElement extends HtmlNodeBuilder implements IHtmlListElement {

  public HtmlListElement(CharSequence text) {
    super("li", text);
  }

  /**
   * Add a css class
   */
  @Override
  public IHtmlListElement cssClass(CharSequence cssClass) {
    return (IHtmlListElement) super.cssClass(cssClass);
  }

  /**
   * Add a css style
   */
  @Override
  public IHtmlListElement style(CharSequence style) {
    return (IHtmlListElement) super.style(style);
  }

  /**
   * Add an application local link
   *
   * @param ref
   *          path to identify what is the link referring to.
   */
  @Override
  public IHtmlListElement appLink(CharSequence ref) {
    return (IHtmlListElement) super.appLink(ref);
  }

  @Override
  public IHtmlListElement addAttribute(String name, CharSequence value) {
    return (IHtmlListElement) super.addAttribute(name, value);
  }
}
