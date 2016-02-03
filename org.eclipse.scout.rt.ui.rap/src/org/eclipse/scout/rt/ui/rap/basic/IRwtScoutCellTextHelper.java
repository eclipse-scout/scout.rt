/*******************************************************************************
 * Copyright (c) 2016 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.ui.rap.basic;

import org.eclipse.scout.rt.client.ui.basic.cell.ICell;
import org.eclipse.scout.rt.ui.rap.basic.table.RwtScoutColumnModel;
import org.eclipse.scout.rt.ui.rap.basic.tree.RwtScoutTreeModel;

/**
 * @since 4.2
 */
public interface IRwtScoutCellTextHelper {

  /**
   * Process the cell text rendering in {@link RwtScoutColumnModel}, {@link RwtScoutTreeModel} or
   * {@link RwtScoutListModel}
   *
   * @param cell
   *          the cell of the column, tree or list model to be processed
   * @return the processed text of cell, i.e. converted to multi-line text or (escaped) html text
   */
  String processCellText(ICell cell);

}
