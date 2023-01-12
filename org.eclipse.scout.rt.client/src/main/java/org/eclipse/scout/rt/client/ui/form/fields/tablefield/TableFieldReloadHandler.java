/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
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
  public void reload(String reloadReason) {
    m_tableField.reloadTableData();
  }
}
