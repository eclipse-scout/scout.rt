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

/**
 * empty implementation
 */
public abstract class AbstractTableCustomizer implements ITableCustomizer {

  @Override
  public void addColumn(IColumn<?> insertAfterColumn) {
  }

  @Override
  public void injectCustomColumns(OrderedCollection<IColumn<?>> columnList) {
  }

  @Override
  public boolean isCustomizable(IColumn<?> column) {
    return column instanceof ICustomColumn;
  }

  @Override
  public void modifyColumn(IColumn<?> col) {
  }

  @Override
  public void removeAllColumns() {
  }

  @Override
  public void removeColumn(IColumn<?> col) {
  }

  @Override
  public byte[] getSerializedData() {
    return null;
  }

  @Override
  public void setSerializedData(byte[] data) {
  }
}
