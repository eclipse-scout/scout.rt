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
package org.eclipse.scout.rt.client.ui.basic.table.customizer;

import org.eclipse.scout.rt.client.ui.basic.table.columns.IColumn;
import org.eclipse.scout.rt.platform.util.collection.OrderedCollection;

/**
 * empty implementation
 */
public abstract class AbstractTableCustomizer implements ITableCustomizer {

  @Override
  public void addColumn() {
  }

  @Override
  public void injectCustomColumns(OrderedCollection<IColumn<?>> columnList) {
  }

  @Override
  public void modifyColumn(ICustomColumn<?> col) {
  }

  @Override
  public void removeAllColumns() {
  }

  @Override
  public void removeColumn(ICustomColumn<?> col) {
  }

  @Override
  public byte[] getSerializedData() {
    return null;
  }

  @Override
  public void setSerializedData(byte[] data) {

  }

}
