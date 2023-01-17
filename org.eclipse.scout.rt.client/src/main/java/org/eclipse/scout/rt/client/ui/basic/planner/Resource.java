/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.basic.planner;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.eclipse.scout.rt.client.ui.basic.cell.Cell;
import org.eclipse.scout.rt.client.ui.basic.cell.ICell;
import org.eclipse.scout.rt.client.ui.basic.cell.ICellObserver;
import org.eclipse.scout.rt.platform.util.CollectionUtility;

public class Resource<RI> implements ICellObserver, IActivityObserver {
  private final RI m_id;
  private final Cell m_cell;
  private final List<Activity<?, ?>> m_activities;
  private int m_resourceChanging = 0;
  private boolean m_resourcePropertiesChanged;
  private IResourceObserver<RI> m_observer;

  public Resource(RI id, String text) {
    m_id = id;
    m_cell = new Cell(this);
    m_cell.setText(text);
    m_cell.setObserver(this);
    m_activities = new ArrayList<>();
  }

  public RI getId() {
    return m_id;
  }

  public Cell getCell() {
    return m_cell;
  }

  public String getText() {
    return m_cell.getText();
  }

  public List<Activity<?, ?>> getActivities() {
    return CollectionUtility.arrayList(m_activities);
  }

  public void addActivities(List<Activity<?, ?>> activities) {
    setResourceChanging(true);
    try {
      for (Activity<?, ?> activity : activities) {
        addActivity(activity);
      }
    }
    finally {
      setResourceChanging(false);
    }
  }

  public void removeAllActivities() {
    setResourceChanging(true);
    try {
      for (Activity<?, ?> activity : getActivities()) {
        removeActivity(activity);
      }
    }
    finally {
      setResourceChanging(false);
    }
  }

  public void removeActivities(List<Activity<?, ?>> activities) {
    setResourceChanging(true);
    try {
      for (Activity<?, ?> activity : activities) {
        removeActivity(activity);
      }
    }
    finally {
      setResourceChanging(false);
    }
  }

  @SuppressWarnings("unchecked")
  public void addActivity(Activity<?, ?> activity) {
    setResourceChanging(true);
    try {
      m_activities.add(activity);
      activity.setObserver(this);
      m_resourcePropertiesChanged = true;
    }
    finally {
      setResourceChanging(false);
    }
  }

  public void removeActivity(Activity<?, ?> activity) {
    setResourceChanging(true);
    try {
      m_activities.remove(activity);
      activity.setObserver(null);
      m_resourcePropertiesChanged = true;
    }
    finally {
      setResourceChanging(false);
    }
  }

  @Override
  public Object validateValue(ICell cell, Object value) {
    return null;
  }

  @Override
  public void cellChanged(ICell cell, int changedBit) {
    try {
      setResourceChanging(true);
      //
      m_resourcePropertiesChanged = true;
    }
    finally {
      setResourceChanging(false);
    }
  }

  @Override
  public void activityChanged(Activity activity, int bitPos) {
    try {
      setResourceChanging(true);
      //
      m_resourcePropertiesChanged = true;
    }
    finally {
      setResourceChanging(false);
    }
  }

  public boolean isResourceChanging() {
    return m_resourceChanging > 0;
  }

  public void setResourceChanging(boolean b) {
    if (b) {
      m_resourceChanging++;
    }
    else {
      m_resourceChanging--;
      if (m_resourceChanging == 0 && m_resourcePropertiesChanged) {
        m_resourcePropertiesChanged = false;
        if (m_observer != null) {
          m_observer.resourceChanged(this);
        }
      }
    }
  }

  public void setObserver(IResourceObserver<RI> observer) {
    m_observer = observer;
  }

  public IResourceObserver<RI> getObserver() {
    return m_observer;
  }

  @Override
  public String toString() {
    return getId() + " " + m_cell.toString();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Resource<?> resource = (Resource<?>) o;
    return Objects.equals(m_id, resource.m_id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(m_id);
  }
}
