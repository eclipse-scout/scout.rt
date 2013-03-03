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
package org.eclipse.scout.rt.ui.swt.basic.table;

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.ui.swt.basic.ISwtScoutComposite;
import org.eclipse.swt.widgets.TableColumn;

public interface ISwtScoutTable extends ISwtScoutComposite<ITable> {
  /**
   * Key for mapping UI and model column: All {@link TableColumn}s contain a {@link TableColumn#getData()} with the
   * scout {@link org.eclipse.scout.rt.client.ui.basic.table.columns.IColumn IColumn}
   */
  String KEY_SCOUT_COLUMN = "scoutColumn";

  void setEnabledFromScout(boolean enabled);

  TableViewer getSwtTableViewer();

}
