/*******************************************************************************
 * Copyright (c) 2010-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.ui.desktop.datachange;

import java.util.EventObject;
import java.util.Objects;

import org.eclipse.scout.rt.platform.util.ChangeStatus;

/**
 * An event object to be used with the {@link IDataChangeManager}.
 *
 * @since 8.0
 */
public class DataChangeEvent extends EventObject {
  private static final long serialVersionUID = 1L;

  private final Object m_eventType;
  private final int m_changeStatus;
  private final Object m_key;
  private final Object m_data;

  /**
   * @param source
   *          the source of the event
   * @param eventType
   *          used so a listener can distinct between various data change event. Typically you'd use the
   *          <code>.class</code> property of that entity as eventType
   * @param changeStatus
   *          a constant value from {@link ChangeStatus}. Specifies the change status of the entity (e.g. inserted,
   *          updated, deleted or not changed)
   * @param key
   *          the key of the entity that has been modified
   * @param data
   *          (optional) data a listener can use, typically the data of an entity that has been inserted or updated or
   *          the entity itself
   */
  public DataChangeEvent(Object source, Object eventType, int changeStatus, Object key, Object data) {
    super(source);
    m_eventType = eventType;
    m_changeStatus = changeStatus;
    m_key = key;
    m_data = data;
  }

  public Object getEventType() {
    return m_eventType;
  }

  public int getChangeStatus() {
    return m_changeStatus;
  }

  public Object getKey() {
    return m_key;
  }

  public Object getData() {
    return m_data;
  }

  @Override
  public int hashCode() {
    return Objects.hash(m_eventType, m_changeStatus, m_key, m_data);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    DataChangeEvent other = (DataChangeEvent) obj;
    if (m_changeStatus != other.m_changeStatus) {
      return false;
    }
    if (m_eventType == null) {
      if (other.m_eventType != null) {
        return false;
      }
    }
    else if (!m_eventType.equals(other.m_eventType)) {
      return false;
    }
    if (m_key == null) {
      if (other.m_key != null) {
        return false;
      }
    }
    else if (!m_key.equals(other.m_key)) {
      return false;
    }
    if (m_data == null) {
      if (other.m_data != null) {
        return false;
      }
    }
    else if (!m_data.equals(other.m_data)) {
      return false;
    }
    return true;
  }
}
