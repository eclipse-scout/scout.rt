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
package org.eclipse.scout.rt.shared.csv;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class ArrayConsumer implements IDataConsumer {

  private List<Object[]> m_rows;

  public ArrayConsumer() {
    m_rows = new ArrayList<Object[]>();
  }

  @Override
  public void processRow(int lineNr, List<Object> row) {
    m_rows.add(row.toArray());
  }

  public Object[][] getData() {
    int maxCol = 0;
    for (int i = 0; i < m_rows.size(); i++) {
      Object[] o = m_rows.get(i);
      if (Array.getLength(o) > maxCol) {
        maxCol = Array.getLength(o);
      }
    }

    return m_rows.toArray(new Object[m_rows.size()][maxCol]);
  }
}
