/*******************************************************************************
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.ui.html.json.form.fields.tablefield.fixtures;

import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.form.fields.tablefield.AbstractTableField;

public class TableField<T extends ITable> extends AbstractTableField<T> {
  private T m_table;

  public TableField() {
    this(null);
  }

  public TableField(T table) {
    super(false);
    m_table = table;
    callInitializer();
  }

  @Override
  protected T createTable() {
    return m_table;
  }

}
