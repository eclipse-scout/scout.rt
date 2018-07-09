/*
 * Copyright (c) BSI Business Systems Integration AG. All rights reserved.
 * http://www.bsiag.com/
 */
package org.eclipse.scout.rt.platform.events.management;

import java.beans.ConstructorProperties;
import java.util.Arrays;

import javax.management.MXBean;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.CreateImmediately;
import org.eclipse.scout.rt.platform.events.IListenerListWithManagement;

/**
 * Jmx control for event listeners of all implementations of {@link IListenerListWithManagement}
 *
 * @since 8.0
 */
@MXBean
@ApplicationScoped
@CreateImmediately
public interface IListenerListMonitor {

  /**
   * @return the count of registered event listener lists
   */
  int getListenerListCount();

  ListenerListInfo[] getListenerListInfos();

  public class ListenerListInfo {
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
          .mapToInt(l -> l.getListenerCount())
          .sum();
    }

  }

  public class EventType {
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
          .mapToInt(l -> l.getListenerCount())
          .sum();
    }
  }

  public class ListenerInfo {
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
  }
}
