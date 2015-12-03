/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.mobile.ui.basic.table;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;

public class DrillDownStyleMap {
  private Map<ITableRow, String> m_drillDownStyleMap;

  public DrillDownStyleMap() {
    m_drillDownStyleMap = new HashMap<ITableRow, String>();
  }

  public void put(ITableRow tableRow, String drillDownStyle) {
    m_drillDownStyleMap.put(tableRow, drillDownStyle);
  }

  public String get(ITableRow tableRow) {
    return m_drillDownStyleMap.get(tableRow);
  }

}
