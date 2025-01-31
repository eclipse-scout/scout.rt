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

import java.util.Objects;

import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.platform.util.ChangeStatus;

/**
 * An event object to be used with the {@link IDataChangeManager} holding information about one changed item including
 * its key and optionally the changed item data.
 *
 * @since 8.0
 */
public class ItemDataChangeEvent extends DataChangeEvent {
  private static final long serialVersionUID = 1L;

  private final Object m_key;
  private final Object m_data;

  /**
   * A {@link ItemDataChangeEvent} is fired by calling {@link IDesktop#fireDataChangeEvent(DataChangeEvent)}
   * <p>
   *
   * @param source
   *     the source of the event
   * @param objectType
   *     used so a listener can distinct between various data change event. Typically you'd use the
   *     <code>.class</code> property of that entity as objectType
   * @param changeStatus
   *     a constant value from {@link ChangeStatus}. Specifies the change status of the entity (e.g. inserted,
   *     updated, deleted or not changed)
   * @param key
   *     the key of the entity that has been modified
   * @param data
   *     (optional) data a listener can use, typically the data of an entity that has been inserted or updated or
   *     the entity itself
   */
  public ItemDataChangeEvent(Object source, Class<?> objectType, int changeStatus, Object key, Object data) {
    super(source, objectType, changeStatus);
    m_key = key;
    m_data = data;
  }

  @Override
  public Class<?> getDataType() {
    return (Class<?>) super.getDataType();
  }

  public Object getKey() {
    return m_key;
  }

  public Object getData() {
    return m_data;
  }

  @Override
  public int hashCode() {
    return Objects.hash(getDataType(), getChangeStatus(), m_key, m_data);
  }

  @Override
  public boolean equals(Object obj) {
    if (!super.equals(obj)) {
      return false;
    }
    ItemDataChangeEvent other = (ItemDataChangeEvent) obj;
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
