/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.spec.client.gen.extract.column;

import org.eclipse.scout.rt.client.ui.basic.table.IHeaderCell;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IColumn;
import org.eclipse.scout.rt.shared.TEXTS;
import org.eclipse.scout.rt.spec.client.gen.extract.AbstractNamedTextExtractor;
import org.eclipse.scout.rt.spec.client.gen.extract.IDocTextExtractor;
import org.eclipse.scout.rt.spec.client.out.mediawiki.MediawikiUtility;

/**
 * Extracts the text of the {@link IHeaderCell} of the {@link IColumn}.
 */
public class ColumnHeaderTextExtractor extends AbstractNamedTextExtractor<IColumn<?>> implements IDocTextExtractor<IColumn<?>> {

  public ColumnHeaderTextExtractor() {
    super(TEXTS.get("org.eclipse.scout.rt.spec.label"));
  }

  /**
   * @param column
   *          {@link IColumn}
   * @return the text of a columns {@link IHeaderCell}, if available. An empty String otherwise.
   */
  @Override
  public String getText(IColumn<?> column) {
    IHeaderCell headerCell = column.getHeaderCell();
    if (headerCell != null) {
      return MediawikiUtility.transformToWiki(headerCell.getText());
    }
    return "";
  }

}
