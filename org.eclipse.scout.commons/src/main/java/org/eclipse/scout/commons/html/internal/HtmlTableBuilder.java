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

import org.eclipse.scout.commons.html.IHtmlTable;
import org.eclipse.scout.commons.html.IHtmlTableRow;

/**
 * Builder for a html table.
 */
public class HtmlTableBuilder extends HtmlNodeBuilder implements IHtmlTable {

  public HtmlTableBuilder(List<IHtmlTableRow> rows) {
    super("table", rows);
  }

  @Override
  public IHtmlTable cellspacing(int pixel) {
    addAttribute("cellspacing", pixel);
    return this;
  }

  @Override
  public IHtmlTable cellpadding(int pixel) {
    addAttribute("cellpadding", pixel);
    return this;
  }

}
