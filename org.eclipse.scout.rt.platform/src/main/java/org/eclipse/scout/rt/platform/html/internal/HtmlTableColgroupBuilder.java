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

import org.eclipse.scout.rt.platform.html.IHtmlTableColgroup;
import org.eclipse.scout.rt.platform.html.IHtmlTableColgroupCol;

/**
 * Builder for a HTML table colgroup (&lt;colgroup&gt;).
 */
public class HtmlTableColgroupBuilder extends HtmlNodeBuilder implements IHtmlTableColgroup {

  private static final long serialVersionUID = 1L;

  public HtmlTableColgroupBuilder(List<IHtmlTableColgroupCol> cols) {
    super("colgroup", cols);
  }

  @Override
  public IHtmlTableColgroup addAttribute(String name, CharSequence value) {
    return (IHtmlTableColgroup) super.addAttribute(name, value);
  }
}
