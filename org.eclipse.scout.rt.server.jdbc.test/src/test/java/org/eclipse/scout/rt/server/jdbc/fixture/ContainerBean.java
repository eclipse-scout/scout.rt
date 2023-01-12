/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.server.jdbc.fixture;

import org.eclipse.scout.rt.platform.holders.TableBeanHolderFilter;

public class ContainerBean {
  private TableFieldBeanData m_tableFieldBeanData;
  private TableBeanHolderFilter m_tableBeanHolderFilter;

  public TableFieldBeanData getTableFieldBeanData() {
    return m_tableFieldBeanData;
  }

  public void setTableFieldBeanData(TableFieldBeanData tableFieldBeanData) {
    m_tableFieldBeanData = tableFieldBeanData;
  }

  public TableBeanHolderFilter getTableBeanHolderFilter() {
    return m_tableBeanHolderFilter;
  }

  public void setTableBeanHolderFilter(TableBeanHolderFilter tableBeanHolderFilter) {
    m_tableBeanHolderFilter = tableBeanHolderFilter;
  }
}
