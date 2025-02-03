/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.desktop.datachange;

import java.util.EventObject;
import java.util.Objects;

import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.platform.util.ChangeStatus;

/**
 * An event object to be used with the {@link IDataChangeManager}.
 *
 * @since 8.0
 */
public class DataChangeEvent extends EventObject {
  private static final long serialVersionUID = 1L;

  public static final Object DEFAULT_SOURCE = new Object();

  private final Object m_dataType;
  private final int m_changeStatus;

  /**
   * A {@link DataChangeEvent} is fired by calling {@link IDesktop#fireDataChangeEvent(DataChangeEvent)}
   */
  public DataChangeEvent(Object eventType, int changeStatus) {
    this(DEFAULT_SOURCE, eventType, changeStatus);
  }

  /**
   * A {@link DataChangeEvent} is fired by calling {@link IDesktop#fireDataChangeEvent(DataChangeEvent)}
   *
   * @param source
   *     the source of the event, may not be <code>null</code>
   * @param dataType
   *     used so a listener can distinct between various data change event. Typically you'd use the
   *     <code>.class</code> property of that entity as eventType
   * @param changeStatus
   *     a constant value from {@link ChangeStatus}. Specifies the change status of the entity (e.g. inserted,
   *     updated, deleted or not changed)
   */
  public DataChangeEvent(Object source, Object dataType, int changeStatus) {
    super(source);
    m_dataType = dataType;
    m_changeStatus = changeStatus;
  }

  public Object getDataType() {
    return m_dataType;
  }

  public int getChangeStatus() {
    return m_changeStatus;
  }

  @Override
  public int hashCode() {
    return Objects.hash(m_dataType, m_changeStatus);
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
    if (m_dataType == null) {
      if (other.m_dataType != null) {
        return false;
      }
    }
    else if (!m_dataType.equals(other.m_dataType)) {
      return false;
    }
    return true;
  }
}
