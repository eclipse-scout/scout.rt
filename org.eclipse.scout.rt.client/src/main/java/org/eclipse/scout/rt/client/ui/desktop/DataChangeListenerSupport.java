package org.eclipse.scout.rt.client.ui.desktop;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.scout.rt.client.ui.DataChangeListener;
import org.eclipse.scout.rt.platform.Bean;
import org.eclipse.scout.rt.platform.util.EventListenerList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manages {@link DataChangeListener} instances that may only be interested in certain events.
 */
@Bean
public class DataChangeListenerSupport {

  private static final Logger LOG = LoggerFactory.getLogger(DataChangeListenerSupport.class);

  private final List<Object[]> m_eventBuffer;
  private final Map<Object, EventListenerList> m_listenersByDataType;

  private String m_name;
  private boolean m_buffering;

  public DataChangeListenerSupport() {
    m_eventBuffer = new ArrayList<>();
    m_listenersByDataType = new HashMap<>();
  }

  public DataChangeListenerSupport withName(String name) {
    m_name = name;
    return this;
  }

  public Map<Object, EventListenerList> getListenersByDataType() {
    return m_listenersByDataType;
  }

  public void addDataChangeListener(DataChangeListener listener, Object... dataTypes) {
    LOG.debug("[{}] Add listener {} [dataTypes={}]", m_name, listener, dataTypes);
    if (dataTypes == null || dataTypes.length == 0) {
      EventListenerList list = m_listenersByDataType.get(null);
      if (list == null) {
        list = new EventListenerList();
        m_listenersByDataType.put(null, list);
      }
      list.add(DataChangeListener.class, listener);
    }
    else {
      for (Object dataType : dataTypes) {
        if (dataType != null) {
          EventListenerList list = m_listenersByDataType.get(dataType);
          if (list == null) {
            list = new EventListenerList();
            m_listenersByDataType.put(dataType, list);
          }
          list.add(DataChangeListener.class, listener);
        }
      }
    }
  }

  public void removeDataChangeListener(DataChangeListener listener, Object... dataTypes) {
    LOG.debug("[{}] Remove listener {} [dataTypes={}]", m_name, listener, dataTypes);
    if (dataTypes == null || dataTypes.length == 0) {
      for (Iterator<EventListenerList> it = m_listenersByDataType.values().iterator(); it.hasNext();) {
        EventListenerList list = it.next();
        list.removeAll(DataChangeListener.class, listener);
        if (list.getListenerCount(DataChangeListener.class) == 0) {
          it.remove();
        }
      }
    }
    else {
      for (Object dataType : dataTypes) {
        if (dataType != null) {
          EventListenerList list = m_listenersByDataType.get(dataType);
          if (list != null) {
            list.remove(DataChangeListener.class, listener);
            if (list.getListenerCount(DataChangeListener.class) == 0) {
              m_listenersByDataType.remove(dataType);
            }
          }
        }
      }
    }
  }

  public boolean isBuffering() {
    return m_buffering;
  }

  public void setBuffering(boolean buffering) {
    if (buffering == m_buffering) {
      return;
    }
    m_buffering = buffering;
    LOG.debug("[{}] Buffer data change events: {}", m_name, m_buffering);
    if (!m_buffering) {
      processDataChangeBuffer();
    }
  }

  public void dataChanged(Object... dataTypes) {
    LOG.debug("[{}] dataChanged [dataTypes={}]", m_name, dataTypes);
    if (isBuffering()) {
      if (dataTypes != null && dataTypes.length > 0) {
        m_eventBuffer.add(dataTypes);
      }
    }
    else {
      fireDataChanged(dataTypes);
    }
  }

  protected void processDataChangeBuffer() {
    LOG.debug("[{}] processChangeBuffer", m_name);
    Set<Object> knownEvents = new HashSet<Object>();
    for (Object[] dataTypes : m_eventBuffer) {
      for (Object dataType : dataTypes) {
        knownEvents.add(dataType);
      }
    }
    m_eventBuffer.clear();
    fireDataChanged(knownEvents.toArray(new Object[knownEvents.size()]));
  }

  protected void fireDataChanged(Object... dataTypes) {
    if (dataTypes != null && dataTypes.length > 0) {
      // Important: Use LinkedHashMaps to make event firing deterministic!
      // (If listeners would be called in random order, bugs may not be reproduced very well.)
      HashMap<DataChangeListener, Set<Object>> map = new LinkedHashMap<>();
      for (Object dataType : dataTypes) {
        if (dataType != null) {
          EventListenerList list = m_listenersByDataType.get(dataType);
          if (list != null) {
            for (DataChangeListener listener : list.getListeners(DataChangeListener.class)) {
              Set<Object> typeSet = map.get(listener);
              if (typeSet == null) {
                typeSet = new LinkedHashSet<Object>();
                map.put(listener, typeSet);
              }
              typeSet.add(dataType);
            }
          }
        }
      }
      for (Map.Entry<DataChangeListener, Set<Object>> e : map.entrySet()) {
        DataChangeListener listener = e.getKey();
        Set<Object> typeSet = e.getValue();
        LOG.debug("[{}] notifiying {} [dataTypes={}]", m_name, listener, typeSet);
        listener.dataChanged(typeSet.toArray());
      }
    }
  }
}
