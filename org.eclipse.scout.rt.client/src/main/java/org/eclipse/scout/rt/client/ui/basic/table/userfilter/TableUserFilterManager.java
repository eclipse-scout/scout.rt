/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.ui.basic.table.userfilter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.commons.serialization.SerializationUtility;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.basic.userfilter.IUserFilterState;

/**
 * @since 5.1 replaces ITableColumnFilterManager
 */
public class TableUserFilterManager {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(TableUserFilterManager.class);

  private Map<Object, IUserFilterState> m_filterMap = new HashMap<Object, IUserFilterState>();
  private ITable m_table;

  public TableUserFilterManager(ITable table) {
    m_table = table;
  }

  public void addFilter(IUserFilterState filter) throws ProcessingException {
    m_filterMap.put(filter.createKey(), filter);
    LOG.info("Filter added " + filter);
  }

  public void removeFilter(IUserFilterState filter) throws ProcessingException {
    m_filterMap.remove(filter.createKey());
    LOG.info("Filter removed " + filter);
  }

  public IUserFilterState getFilter(Object key) {
    return m_filterMap.get(key);
  }

  public Collection<IUserFilterState> getFilters() {
    return Collections.unmodifiableCollection(m_filterMap.values());
  }

  /**
   * Get the serialized data of the UserFilterManager for further processing (e.g. storing a bookmark)
   */
  public byte[] getSerializedData() throws ProcessingException {
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
  public void setSerializedData(byte[] data) throws ProcessingException {
    try {
      Collection<IUserFilterState> filterStates = SerializationUtility.createObjectSerializer().deserialize(data, null);
      for (IUserFilterState filterState : filterStates) {
        filterState.notifyDeserialized(m_table);
        addFilter(filterState);
      }
    }
    catch (Exception t) {
      throw new ProcessingException("Failed reading user filter data.", t);
    }
  }
}
