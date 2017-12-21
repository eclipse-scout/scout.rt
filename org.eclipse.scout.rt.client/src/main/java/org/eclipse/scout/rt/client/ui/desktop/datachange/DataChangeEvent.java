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

import org.eclipse.scout.rt.platform.util.ChangeStatus;

/**
 * An event object to be used with the {@link IDataChangeManager}.
 *
 * @since 7.1
 */
public class DataChangeEvent extends EventObject {

  private static final long serialVersionUID = 1L;

  private Object m_eventType;
  private int m_changeStatus;
  private Object m_key;
  private Object m_data;

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

}
