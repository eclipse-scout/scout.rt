/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.desktop.hybrid;

import java.util.EventObject;
import java.util.Objects;

import org.eclipse.scout.rt.client.ui.IModelEvent;
import org.eclipse.scout.rt.dataobject.IDoEntity;

public class HybridEvent extends EventObject implements IModelEvent {

  private static final long serialVersionUID = 1L;

  public static final int TYPE_EVENT = 13;
  public static final int TYPE_WIDGET_EVENT = 42;

  public static final String HYBRID_ACTION_END = "hybridActionEnd";

  private final int m_type;
  private final String m_id;
  private final String m_eventType;
  private final IDoEntity m_data;
  private final HybridActionContextElement m_contextElement;

  protected HybridEvent(Object source, int type, String id, String eventType, IDoEntity data, HybridActionContextElement contextElement) {
    super(source);
    m_type = type;
    m_id = id;
    m_eventType = eventType;
    m_data = data;
    m_contextElement = contextElement;
  }

  public static HybridEvent createHybridEvent(Object source, String id, String eventType) {
    return createHybridEvent(source, id, eventType, null, null);
  }

  public static HybridEvent createHybridEvent(Object source, String id, String eventType, IDoEntity data, HybridActionContextElement contextElement) {
    return new HybridEvent(source, HybridEvent.TYPE_EVENT, id, eventType, data, contextElement);
  }

  public static HybridEvent createHybridActionEndEvent(Object source, String id) {
    return createHybridActionEndEvent(source, id, null, null);
  }

  public static HybridEvent createHybridActionEndEvent(Object source, String id, IDoEntity data, HybridActionContextElement contextElement) {
    return createHybridEvent(source, id, HYBRID_ACTION_END, data, contextElement);
  }

  public static HybridEvent createHybridWidgetEvent(Object source, String id, String eventType) {
    return createHybridWidgetEvent(source, id, eventType, null);
  }

  public static HybridEvent createHybridWidgetEvent(Object source, String id, String eventType, IDoEntity data) {
    return new HybridEvent(source, HybridEvent.TYPE_WIDGET_EVENT, id, eventType, data, null);
  }

  @Override
  public int getType() {
    return m_type;
  }

  public String getId() {
    return m_id;
  }

  public String getEventType() {
    return m_eventType;
  }

  public IDoEntity getData() {
    return m_data;
  }

  public HybridActionContextElement getContextElement() {
    return m_contextElement;
  }

  @Override
  public int hashCode() {
    return Objects.hash(source, m_type, m_id, m_eventType, m_data);
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
    HybridEvent other = (HybridEvent) obj;
    if (!source.equals(other.source)) {
      return false;
    }
    if (m_type != other.m_type) {
      return false;
    }
    if (m_id == null) {
      if (other.m_id != null) {
        return false;
      }
    }
    else if (!m_id.equals(other.m_id)) {
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

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("HybridEvent [");
    builder.append("source=").append(source).append(", ");
    builder.append("type=").append(m_type);
    if (m_id != null) {
      builder.append(", ").append("id=").append(m_id);
    }
    if (m_eventType != null) {
      builder.append(", ").append("eventType=").append(m_eventType);
    }
    if (m_data != null) {
      builder.append(", ").append("data=").append(m_data);
    }
    builder.append("]");
    return builder.toString();
  }
}
