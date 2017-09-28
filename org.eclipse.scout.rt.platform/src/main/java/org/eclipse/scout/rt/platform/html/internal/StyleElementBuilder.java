/*******************************************************************************
 * Copyright (c) 2010-2016 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.platform.html.internal;

import org.eclipse.scout.rt.platform.html.IStyleElement;

public class StyleElementBuilder extends HtmlNodeBuilder implements IStyleElement {

  private static final long serialVersionUID = 1L;

  public StyleElementBuilder(CharSequence... elements) {
    super("style", elements);
  }

  @Override
  public IStyleElement type(String typeName) {
    addAttribute("type", typeName);
    return this;
  }

  /**
   * Add a css class
   */
  @Override
  public IStyleElement cssClass(CharSequence cssClass) {
    return (IStyleElement) super.cssClass(cssClass);
  }

  /**
   * Add a css style
   */
  @Override
  public IStyleElement style(CharSequence style) {
    return (IStyleElement) super.style(style);
  }

  /**
   * Add an application local link
   *
   * @param ref
   *          path to identify what is the link referring to.
   */
  @Override
  public IStyleElement appLink(CharSequence ref) {
    return (IStyleElement) super.appLink(ref);
  }
}
