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

import org.eclipse.scout.commons.html.IHtmlTableRow;

/**
 * Builder for a html table row.
 */
public class HtmlTableRowBuilder extends HtmlNodeBuilder implements IHtmlTableRow {

  public HtmlTableRowBuilder(List<? extends CharSequence> text) {
    super("tr", text);
  }

}
