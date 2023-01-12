/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
