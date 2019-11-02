/*
 * Copyright (c) 2010-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.platform.events.management;

import java.beans.ConstructorProperties;
import java.util.Arrays;

import javax.management.MXBean;

import org.eclipse.scout.rt.platform.events.IListenerListWithManagement;
import org.eclipse.scout.rt.platform.util.StringUtility;

/**
 * Jmx control for event listeners of all implementations of {@link IListenerListWithManagement}
 *
 * @since 8.0
 */
@MXBean
public interface IListenerListMonitorMBean {

  /**
   * @return the count of registered event listener lists
   */
  int getListenerListCount();

  ListenerListInfo[] getListenerListInfos();

  class ListenerListInfo {
    private final String m_listenerListClassName;
    private final int m_listenerListInstances;
    private final EventType[] m_listenerTypes;

    @ConstructorProperties({"listenerListClassName", "listenerListInstances", "listenerTypes"})
    public ListenerListInfo(String listenerListClassName, int listenerListInstances, EventType[] listenerTypes) {
      m_listenerListClassName = listenerListClassName;
      m_listenerListInstances = listenerListInstances;
      m_listenerTypes = listenerTypes;
    }

    public String getListenerListClassName() {
      return m_listenerListClassName;
    }

    public int getListenerListInstances() {
      return m_listenerListInstances;
    }

    public EventType[] getListenerTypes() {
      return m_listenerTypes;
    }

    public int getListenerCount() {
      return Arrays.stream(m_listenerTypes)
          .mapToInt(EventType::getListenerCount)
          .sum();
    }

    @Override
    @SuppressWarnings("findbugs:VA_FORMAT_STRING_USES_NEWLINE")
    public String toString() {
      return String.format("%s [className=%s, numInstances=%s, listenerTypes=\n%s\n]",
          ListenerListInfo.class.getSimpleName(), m_listenerListClassName, m_listenerListInstances, StringUtility.join("\n", Arrays.asList(m_listenerTypes)));
    }
  }

  class EventType {
    private final String m_eventType;
    private final ListenerInfo[] m_listenerInfos;

    @ConstructorProperties({"eventType", "listenerInfos"})
    public EventType(String eventType, ListenerInfo[] listenerInfos) {
      m_eventType = eventType;
      m_listenerInfos = listenerInfos;
    }

    public String getEventType() {
      return m_eventType;
    }

    public ListenerInfo[] getListenerInfos() {
      return m_listenerInfos;
    }

    public int getListenerCount() {
      return Arrays.stream(m_listenerInfos)
          .mapToInt(ListenerInfo::getListenerCount)
          .sum();
    }

    @Override
    public String toString() {
      return String.format("  %s[type=%s, listeners=%s]", EventType.class.getSimpleName(), m_eventType, Arrays.toString(m_listenerInfos));
    }
  }

  class ListenerInfo {
    private final String m_listenerClassName;
    private final int m_listenerCount;

    @ConstructorProperties({"listenerClassName", "listenerCount"})
    public ListenerInfo(String listenerClassName, int listenerCount) {
      m_listenerClassName = listenerClassName;
      m_listenerCount = listenerCount;
    }

    public String getListenerClassName() {
      return m_listenerClassName;
    }

    public int getListenerCount() {
      return m_listenerCount;
    }

    @Override
    public String toString() {
      return String.format("%s[className=%s, count=%s]", ListenerInfo.class.getSimpleName(), m_listenerClassName, m_listenerCount);
    }
  }
}
