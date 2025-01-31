/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.ui.html.json.form.fields.tablefield.fixtures;

import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.form.fields.tablefield.AbstractTableField;
import org.eclipse.scout.rt.platform.classid.ClassId;

@ClassId("a30ddecf-0ce1-45eb-8ad2-47e79c7b92ef")
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
