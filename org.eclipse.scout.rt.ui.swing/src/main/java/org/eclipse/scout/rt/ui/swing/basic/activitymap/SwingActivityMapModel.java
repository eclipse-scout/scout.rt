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
package org.eclipse.scout.rt.ui.swing.basic.activitymap;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JTable;

import org.eclipse.scout.rt.client.ui.basic.activitymap.ActivityCell;
import org.eclipse.scout.rt.client.ui.basic.activitymap.IActivityMap;
import org.eclipse.scout.rt.ui.swing.ext.activitymap.ActivityComponent;
import org.eclipse.scout.rt.ui.swing.ext.activitymap.ActivityMapModel;

public class SwingActivityMapModel implements ActivityMapModel {
  private final IActivityMap m_map;
  private final JTable m_metricsTable;
  private final SwingActivityComponent[] m_components;

  @SuppressWarnings("unchecked")
  public SwingActivityMapModel(IActivityMap map, JTable metricsTable) {
    m_map = map;
    m_metricsTable = metricsTable;
    List<SwingActivityComponent> list = new ArrayList<SwingActivityComponent>();
    List resourceIds = map.getResourceIds();
    int i = 0;
    for (Object resourceId : resourceIds) {
      for (Object cell : map.getActivityCells(resourceId)) {
        list.add(new SwingActivityComponent((ActivityCell) cell, i));
      }
      i++;
    }
    m_components = list.toArray(new SwingActivityComponent[list.size()]);
  }

  @Override
  public int getRowCount() {
    return m_metricsTable.getRowCount();
  }

  @Override
  public int getRowHeight(int rowIndex) {
    if (rowIndex >= 0 && rowIndex < m_metricsTable.getRowCount()) {
      return m_metricsTable.getRowHeight(rowIndex);
    }
    else {
      return m_metricsTable.getRowHeight();
    }
  }

  @Override
  public int getRowLocation(int rowIndex) {
    if (rowIndex >= 0 && rowIndex < m_metricsTable.getRowCount()) {
      return m_metricsTable.getCellRect(rowIndex, 0, true).y;
    }
    else {
      return 0;
    }
  }

  @Override
  public int getRowAtLocation(int y) {
    int i = m_metricsTable.rowAtPoint(new Point(1, y));
    if (i < 0 && y > 0) {
      i = getRowCount();
    }
    return i;
  }

  @Override
  public int getHeaderHeight() {
    if (m_metricsTable.getTableHeader() != null) {
      return m_metricsTable.getTableHeader().getHeight();
    }
    else {
      return 0;
    }
  }

  @Override
  public ActivityComponent[] getActivities() {
    return m_components;
  }

  @Override
  public double[] getActivityRange(ActivityComponent a) {
    SwingActivityComponent sa = (SwingActivityComponent) a;
    ActivityCell cell = sa.getScoutActivityCell();
    return m_map.getTimeScale().getRangeOf(cell.getBeginTime(), cell.getEndTime());
  }
}
