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

import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.html.IHtmlInput;

public class HtmlInputBuilder extends EmptyHtmlNodeBuilder implements IHtmlInput {

  public HtmlInputBuilder() {
    super("input");
  }

  @Override
  public IHtmlInput id(String id) {
    addAttribute("id", id);
    return this;
  }

  @Override
  public IHtmlInput name(String name) {
    addAttribute("name", name);
    return this;
  }

  @Override
  public IHtmlInput type(String type) {
    addAttribute("type", type);
    return this;
  }

  @Override
  public IHtmlInput value(Object value) {
    addAttribute("value", StringUtility.nvl(value, ""));
    return this;
  }

  @Override
  public IHtmlInput maxlength(int maxlength) {
    addAttribute("maxlength", maxlength);
    return this;
  }

  @Override
  public IHtmlInput checked() {
    addAttribute("checked", "checked");
    return this;
  }

  @Override
  public IHtmlInput cssClass(CharSequence cssClass) {
    return (IHtmlInput) super.cssClass(cssClass);
  }

  @Override
  public IHtmlInput style(CharSequence style) {
    return (IHtmlInput) super.style(style);
  }

  @Override
  public IHtmlInput appLink(CharSequence ref) {
    return (IHtmlInput) super.appLink(ref);
  }

  @Override
  public IHtmlInput addAttribute(String name, CharSequence value) {
    return (IHtmlInput) super.addAttribute(name, value);
  }
}
