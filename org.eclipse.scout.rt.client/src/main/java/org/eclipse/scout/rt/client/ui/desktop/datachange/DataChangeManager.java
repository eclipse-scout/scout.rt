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

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.scout.rt.platform.Bean;
import org.eclipse.scout.rt.platform.util.event.AbstractGroupedListenerList;

@Bean
public class DataChangeManager extends AbstractGroupedListenerList<IDataChangeListener, DataChangeEvent, Object> implements IDataChangeManager {

  private final List<DataChangeEvent> m_dataChangeEventBuffer;
  private boolean m_buffering;

  public DataChangeManager() {
    m_dataChangeEventBuffer = new ArrayList<>();
  }

  @Override
  public void addAll(IDataChangeManager other) {
    super.addAll((DataChangeManager) other);
  }

  @Override
  protected Set<Object> knownEventTypes() {
    return Collections.emptySet();
  }

  @Override
  protected Object otherEventsType() {
    return null;
  }

  @Override
  protected Object eventType(DataChangeEvent event) {
    return event.getDataType();
  }

  @Override
  protected void handleEvent(IDataChangeListener listener, DataChangeEvent event) {
    listener.dataChanged(event);
  }

  @Override
  public void fireEvents(List<DataChangeEvent> events) {
    if (isBuffering()) {
      m_dataChangeEventBuffer.addAll(events);
    }
    else {
      super.fireEvents(events);
    }
  }

  @Override
  public void fireEvent(DataChangeEvent event) {
    if (isBuffering()) {
      m_dataChangeEventBuffer.add(event);
    }
    else {
      super.fireEvent(event);
    }
  }

  @Override
  public boolean isBuffering() {
    return m_buffering;
  }

  @Override
  public void setBuffering(boolean buffering) {
    if (buffering == m_buffering) {
      return;
    }
    m_buffering = buffering;
    if (!m_buffering) {
      processDataChangeBuffer();
    }
  }

  protected void processDataChangeBuffer() {
    if (m_dataChangeEventBuffer.isEmpty()) {
      return;
    }
    LinkedHashSet<DataChangeEvent> coalescedEvents = new LinkedHashSet<>(m_dataChangeEventBuffer);
    m_dataChangeEventBuffer.clear();
    for (DataChangeEvent event : coalescedEvents) {
      super.fireEvent(event);
    }
  }
}
