/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.basic.table.columnfilter.ITableColumnFilter;
import org.eclipse.scout.rt.client.ui.basic.table.columnfilter.ITableColumnFilterManager;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IColumn;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.shared.services.common.jdbc.SearchFilter;

/**
 * No specific restrictions, cache all table page search form contents and all page table filter settings. Check memory
 * limits after page reload.
 */
public class LargeMemoryPolicy extends AbstractMemoryPolicy {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(LargeMemoryPolicy.class);

  //cache all search form contents
  private final Map<String/*pageFormIdentifier*/, SearchFormState> m_searchFormCache;
  private final Map<String /*pageTableIdentifier*/, Map<String, byte[]>> m_tableColumnFilterManagerState;

  public LargeMemoryPolicy() {
    m_searchFormCache = new HashMap<String, SearchFormState>();
    m_tableColumnFilterManagerState = new HashMap<String, Map<String, byte[]>>();
  }

  @Override
  protected void loadSearchFormState(IForm f, String pageFormIdentifier) throws ProcessingException {
    //check if there is stored search form data
    SearchFormState state = m_searchFormCache.get(pageFormIdentifier);
    if (state != null) {
      if (state.formContentXml != null) {
        f.setXML(state.formContentXml);
      }
      if (state.searchFilter != null) {
        f.setSearchFilter(state.searchFilter);
      }
    }
  }

  @Override
  protected void storeSearchFormState(IForm f, String pageFormIdentifier) throws ProcessingException {
    //cache search form data
    if (f.isEmpty()) {
      m_searchFormCache.remove(pageFormIdentifier);
    }
    else {
      String xml = f.getXML("UTF-8");
      SearchFilter filter = f.getSearchFilter();
      m_searchFormCache.put(pageFormIdentifier, new SearchFormState(xml, filter));
    }
  }

  @Override
  protected void storeColumnFilterState(ITable t, String pageTableIdentifier) throws ProcessingException {
    ITableColumnFilterManager filterManager = t.getColumnFilterManager();
    if (filterManager == null || filterManager.getFilters() == null || filterManager.getFilters().isEmpty()) {
      m_tableColumnFilterManagerState.remove(pageTableIdentifier);
      return;
    }
    Map<String, byte[]> state = m_tableColumnFilterManagerState.get(pageTableIdentifier);
    if (state == null) {
      state = new HashMap<String, byte[]>();
      m_tableColumnFilterManagerState.put(pageTableIdentifier, state);
    }
    for (ITableColumnFilter<?> filter : filterManager.getFilters()) {
      IColumn<?> col = filter.getColumn();
      if (col.getColumnId() != null) {
        byte[] data = filterManager.getSerializedFilter(col);
        if (data == null || data.length == 0) {
          state.remove(col.getColumnId());
        }
        else {
          state.put(col.getColumnId(), data);
        }
      }
    }
  }

  @Override
  protected void loadColumnFilterState(ITable t, String pageTableIdentifier) throws ProcessingException {
    if (t == null || t.getColumnFilterManager() == null) {
      return;
    }
    Map<String, byte[]> state = m_tableColumnFilterManagerState.get(pageTableIdentifier);
    if (state != null) {
      for (Map.Entry<String, byte[]> entry : state.entrySet()) {
        IColumn col = t.getColumnSet().getColumnById(entry.getKey());
        if (col != null) {
          t.getColumnFilterManager().setSerializedFilter(entry.getValue(), col);
        }
      }
    }
  }

  @Override
  public void afterOutlineSelectionChanged(final IDesktop desktop) {
    long memTotal = Runtime.getRuntime().totalMemory();
    long memUsed = (memTotal - Runtime.getRuntime().freeMemory());
    long memMax = Runtime.getRuntime().maxMemory();
    if (memUsed > memMax * 80L / 100L) {
      new ClientSyncJob("Check memory", ClientSyncJob.getCurrentSession()) {
        @Override
        protected void runVoid(IProgressMonitor monitor) throws Throwable {
          desktop.releaseUnusedPages();
          System.gc();
        }
      }.schedule();
    }
  }

  @Override
  public String toString() {
    return "Large";
  }
}
