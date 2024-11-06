/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.basic.table.customizer;

import org.eclipse.scout.rt.client.ui.basic.table.columns.IColumn;
import org.eclipse.scout.rt.platform.util.collection.OrderedCollection;

public abstract class AbstractTableCustomizerWrapper implements ITableCustomizer {

  private final ITableCustomizer m_delegate;

  public AbstractTableCustomizerWrapper(ITableCustomizer delegate) {
    m_delegate = delegate;
  }

  public ITableCustomizer getDelegate() {
    return m_delegate;
  }

  @Override
  public void injectCustomColumns(OrderedCollection<IColumn<?>> columns) {
    getDelegate().injectCustomColumns(columns);
  }

  @Override
  public void addColumn(IColumn<?> insertAfterColumn) {
    getDelegate().addColumn(insertAfterColumn);
  }

  @Override
  public boolean isCustomizable(IColumn<?> column) {
    return getDelegate().isCustomizable(column);
  }

  @Override
  public void modifyColumn(IColumn<?> col) {
    getDelegate().modifyColumn(col);
  }

  @Override
  public void removeColumn(IColumn<?> col) {
    getDelegate().removeColumn(col);
  }

  @Override
  public void removeAllColumns() {
    getDelegate().removeAllColumns();
  }

  @Override
  public byte[] getSerializedData() {
    return getDelegate().getSerializedData();
  }

  @Override
  public void setSerializedData(byte[] data) {
    getDelegate().setSerializedData(data);
  }

  @Override
  public String getPreferencesKey() {
    return getDelegate().getPreferencesKey();
  }
}
