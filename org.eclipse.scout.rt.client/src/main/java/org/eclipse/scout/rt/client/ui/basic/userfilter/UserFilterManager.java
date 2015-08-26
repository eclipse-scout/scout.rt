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
package org.eclipse.scout.rt.client.ui.basic.userfilter;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;

/**
 * @since 5.1 replaces ITableColumnFilterManager
 */
public class UserFilterManager {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(UserFilterManager.class);

  private final Map<Object, IUserFilterState> m_filterMap = new HashMap<Object, IUserFilterState>();

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

}
