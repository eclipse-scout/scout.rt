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

import java.util.List;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IColumn;

/**
 * empty implementation
 */
public class AbstractTableCustomizer implements ITableCustomizer {

  @Override
  public void addColumn() throws ProcessingException {
  }

  @Override
  public void injectCustomColumns(List<IColumn<?>> columnList) {
  }

  @Override
  public void modifyColumn(ICustomColumn<?> col) throws ProcessingException {
  }

  @Override
  public void removeAllColumns() throws ProcessingException {
  }

  @Override
  public void removeColumn(ICustomColumn<?> col) throws ProcessingException {
  }

  @Override
  public byte[] getSerializedData() throws ProcessingException {
    return null;
  }

  @Override
  public void setSerializedData(byte[] data) throws ProcessingException {

  }
}
