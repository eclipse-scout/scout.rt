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

import java.util.List;

import org.eclipse.scout.rt.platform.html.IHtmlTableColgroup;
import org.eclipse.scout.rt.platform.html.IHtmlTableColgroupCol;

/**
 * Builder for a html table colgroup.
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
