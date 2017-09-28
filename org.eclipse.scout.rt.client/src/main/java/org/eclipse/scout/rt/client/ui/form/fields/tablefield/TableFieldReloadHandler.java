/*******************************************************************************
 * Copyright (c) 2010-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.ui.form.fields.tablefield;

import org.eclipse.scout.rt.client.ui.basic.table.IReloadHandler;

/**
 * This class triggers the <code>reloadTableData()</code> method of a referenced tableField.
 * <p>
 * The handler is not installed by default since the existence of a reload handler controls the availability of the
 * reload functionality on the ui (reload keystroke, reload button)
 *
 * @since 5.1
 */
public class TableFieldReloadHandler implements IReloadHandler {

  private final ITableField m_tableField;

  public TableFieldReloadHandler(ITableField field) {
    m_tableField = field;
  }

  @Override
  public void reload() {
    m_tableField.reloadTableData();
  }

}
