/*
 * Copyright (c) 2021 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.ui.html.json.table;

import org.eclipse.scout.rt.client.ui.basic.cell.ICell;
import org.eclipse.scout.rt.client.ui.basic.table.columns.ISmartColumn;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.shared.services.common.code.ICodeType;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.basic.cell.JsonCell;

public class JsonSmartColumn<T extends ISmartColumn<VALUE>, VALUE> extends JsonColumn<T> {

  private ICodeType<?, VALUE> m_codeType;

  public JsonSmartColumn(T model) {
    super(model);
  }

  @Override
  public JsonCell createJsonCell(ICell cell, IJsonAdapter<?> parentAdapter) {
    if (cell.getValue() == null || getColumn().isSortCodesByDisplayText() || getColumn().getCodeTypeClass() == null) {
      return super.createJsonCell(cell, parentAdapter);
    }
    if (m_codeType == null || !m_codeType.getClass().equals(getColumn().getCodeTypeClass())) {
      m_codeType = BEANS.opt(getColumn().getCodeTypeClass());
    }
    if (m_codeType == null) {
      return super.createJsonCell(cell, parentAdapter);
    }
    @SuppressWarnings("unchecked")
    int codeIndex = m_codeType.getCodeIndex((VALUE) cell.getValue());
    return new JsonCell(cell, parentAdapter, createCellValueReader(cell), codeIndex);
  }
}
