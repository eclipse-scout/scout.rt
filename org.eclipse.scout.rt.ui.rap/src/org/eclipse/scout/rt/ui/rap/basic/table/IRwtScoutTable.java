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

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IColumn;
import org.eclipse.scout.rt.ui.rap.basic.IRwtScoutComposite;
import org.eclipse.swt.widgets.TableColumn;

public interface IRwtScoutTable extends IRwtScoutComposite<ITable> {
  /**
   * all {@link TableColumn}s contain a {@link TableColumn#getData()} with the scout {@link IColumn}
   */
  String KEY_SCOUT_COLUMN = "scoutColumn";

  /**
   * Special variant for disabled table columns (as :disabled seems to by ignored by RAP).
   */
  String VARIANT_TABLE_COLUMN_DISABLED = "customDisabled";

  void setEnabledFromScout(boolean enabled);

  Viewer getUiTableViewer();

}
