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
package org.eclipse.scout.rt.server.jdbc.fixture;

import org.eclipse.scout.rt.platform.holders.TableBeanHolderFilter;
import org.eclipse.scout.rt.platform.holders.TableHolderFilter;

@SuppressWarnings("deprecation")
public class ContainerBean {
  private TableFieldData m_tableFieldData;
  private TableFieldBeanData m_tableFieldBeanData;
  private TableHolderFilter m_tableHolderFilter;
  private TableBeanHolderFilter m_tableBeanHolderFilter;

  public TableFieldData getTableFieldData() {
    return m_tableFieldData;
  }

  public void setTableFieldData(TableFieldData tableFieldData) {
    m_tableFieldData = tableFieldData;
  }

  public TableFieldBeanData getTableFieldBeanData() {
    return m_tableFieldBeanData;
  }

  public void setTableFieldBeanData(TableFieldBeanData tableFieldBeanData) {
    m_tableFieldBeanData = tableFieldBeanData;
  }

  public TableHolderFilter getTableHolderFilter() {
    return m_tableHolderFilter;
  }

  public void setTableHolderFilter(TableHolderFilter tableHolderFilter) {
    m_tableHolderFilter = tableHolderFilter;
  }

  public TableBeanHolderFilter getTableBeanHolderFilter() {
    return m_tableBeanHolderFilter;
  }

  public void setTableBeanHolderFilter(TableBeanHolderFilter tableBeanHolderFilter) {
    m_tableBeanHolderFilter = tableBeanHolderFilter;
  }
}
