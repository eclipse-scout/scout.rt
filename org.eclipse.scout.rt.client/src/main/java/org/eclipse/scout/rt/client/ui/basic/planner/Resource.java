/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.ui.basic.planner;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ui.basic.cell.Cell;
import org.eclipse.scout.rt.client.ui.basic.cell.ICell;
import org.eclipse.scout.rt.client.ui.basic.cell.ICellObserver;

public class Resource<RI> implements ICellObserver {
  private RI m_id;
  private Cell m_cell;
  private List<Activity<?, ?>> m_activities;

  public Resource(RI id, String text, List<Activity<?, ?>> activities) {
    m_id = id;
    m_cell = new Cell(this);
    m_cell.setText(text);
    m_activities = activities;
    if (m_activities == null) {
      m_activities = new ArrayList<Activity<?, ?>>();
    }
  }

  public Resource(RI id, String text) {
    this(id, text, null);
  }

  public RI getId() {
    return m_id;
  }

  public ICell getCell() {
    return m_cell;
  }

  public String getText() {
    return m_cell.getText();
  }

  public List<Activity<?, ?>> getActivities() {
    return m_activities;
  }

  public void setActivities(List<Activity<?, ?>> activities) {
    //FIXME CGU copy list?
    m_activities = activities;
  }

  @Override
  public Object validateValue(ICell cell, Object value) throws ProcessingException {
    return null;
  }

  @Override
  public void cellChanged(ICell cell, int changedBit) {
  }

  @Override
  public String toString() {
    return getId() + " " + m_cell.toString();
  }
}
