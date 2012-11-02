/*******************************************************************************
 * Copyright (c) 2011 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 *******************************************************************************/
package org.eclipse.scout.rt.ui.rap.basic.table;

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.ui.rap.IRwtEnvironment;
import org.eclipse.scout.rt.ui.rap.ext.table.TableEx;

/**
 * <h3>RwtScoutTable</h3> ...
 * knownIssues - multi column sorting is not supported, unable to get any key
 * mask in the selection event.
 * <p>
 * - multi line support in headers is not supported by rwt.
 * <p>
 * - multi line support in row texts is not supported so far. Might probably be done by customized table rows.
 * 
 * @since 3.7.0 June 2011
 */
public interface IRwtScoutTableForPatch extends IRwtScoutTable {

  TableColumnManager getUiColumnManager();

  void initializeUiColumns();

  @Override
  IRwtEnvironment getUiEnvironment();

  @Override
  TableEx getUiField();

  @Override
  TableViewer getUiTableViewer();

  void setUiTableViewer(TableViewer uiViewer);

  ITableRow getUiSelectedRow();

  ITableRow[] getUiSelectedRows();
}
