/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.ui.basic.table.userfilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.scout.rt.client.ui.basic.table.AbstractTable;
import org.eclipse.scout.rt.client.ui.basic.table.TableEvent;
import org.eclipse.scout.rt.client.ui.basic.userfilter.IUserFilterState;
import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.platform.serialization.SerializationUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @since 5.1 replaces ITableColumnFilterManager
 */
public class TableUserFilterManager {
  private static final Logger LOG = LoggerFactory.getLogger(TableUserFilterManager.class);

  private final Map<Object, IUserFilterState> m_filterMap = new HashMap<>();
  private final AbstractTable m_table;

  public TableUserFilterManager(AbstractTable table) {
    m_table = table;
  }

  public void addFilter(IUserFilterState filter) {
    m_filterMap.put(filter.createKey(), filter);
    fireFilterAdded(filter);
    LOG.info("Filter added {}", filter);
  }

  public void removeFilter(IUserFilterState filter) {
    removeFilterByKey(filter.createKey());
  }

  public void removeFilterByKey(Object key) {
    IUserFilterState filter = m_filterMap.remove(key);
    fireFilterRemoved(filter);
    LOG.info("Filter removed {}", filter);
  }

  public void reset() {
    for (IUserFilterState filter : new ArrayList<>(m_filterMap.values())) {
      removeFilter(filter);
    }
  }

  public boolean isEmpty() {
    return m_filterMap.isEmpty();
  }

  public IUserFilterState getFilter(Object key) {
    return m_filterMap.get(key);
  }

  public Collection<IUserFilterState> getFilters() {
    return Collections.unmodifiableCollection(m_filterMap.values());
  }

  public List<String> getDisplayTexts() {
    List<String> list = new ArrayList<>();
    for (IUserFilterState filter : m_filterMap.values()) {
      list.add(filter.getDisplayText());
    }
    return list;
  }

  private void fireFilterAdded(IUserFilterState filter) {
    TableEvent event = new TableEvent(m_table, TableEvent.TYPE_USER_FILTER_ADDED);
    event.setUserFilter(filter);
    m_table.fireTableEventInternal(event);
  }

  private void fireFilterRemoved(IUserFilterState filter) {
    TableEvent event = new TableEvent(m_table, TableEvent.TYPE_USER_FILTER_REMOVED);
    event.setUserFilter(filter);
    m_table.fireTableEventInternal(event);
  }

  /**
   * Get the serialized data of the UserFilterManager for further processing (e.g. storing a bookmark)
   */
  public byte[] getSerializedData() {
    byte[] data = null;
    try {
      // Create array list because m_filterMap.values() is not serializable
      Collection<IUserFilterState> filterStates = new ArrayList<>(m_filterMap.values());
      data = SerializationUtility.createObjectSerializer().serialize(filterStates);
    }
    catch (Exception t) {
      throw new ProcessingException("Failed creating user filter data.", t);
    }
    return data;
  }

  /**
   * Import the serialized data, e.g. after restoring from a bookmark
   */
  public void setSerializedData(byte[] data) {
    try {
      reset();
      Collection<IUserFilterState> filterStates = SerializationUtility.createObjectSerializer().deserialize(data, null);
      for (IUserFilterState filterState : filterStates) {
        boolean success = filterState.notifyDeserialized(m_table);
        if (success) {
          addFilter(filterState);
        }
        else {
          LOG.info("User filter state of table '{}' cannot be deserialized because the column could not be found. Ignoring element.", m_table.getClass().getName());
        }
      }
    }
    catch (IOException | ClassNotFoundException e) {
      throw new ProcessingException("Failed reading user filter data.", e);
    }
  }
}
