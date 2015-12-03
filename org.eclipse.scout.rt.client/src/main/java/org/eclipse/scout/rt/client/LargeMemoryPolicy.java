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
package org.eclipse.scout.rt.client;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.scout.rt.client.context.ClientRunContexts;
import org.eclipse.scout.rt.client.job.ModelJobs;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.basic.table.userfilter.TableUserFilterManager;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.platform.util.concurrent.IRunnable;
import org.eclipse.scout.rt.shared.services.common.jdbc.SearchFilter;

/**
 * No specific restrictions, cache all table page search form contents and all page table filter settings. Check memory
 * limits after page reload.
 */
public class LargeMemoryPolicy extends AbstractMemoryPolicy {

  //cache all search form contents
  private final Map<String/*pageFormIdentifier*/, SearchFormState> m_searchFormCache;
  private final Map<String /*pageTableIdentifier*/, byte[]> m_tableUserFilterState;
  private long m_maxMemThreshold = 90L;

  public LargeMemoryPolicy() {
    m_searchFormCache = new HashMap<String, SearchFormState>();
    m_tableUserFilterState = new HashMap<String, byte[]>();
  }

  @Override
  protected void loadSearchFormState(IForm f, String pageFormIdentifier) {
    //check if there is stored search form data
    SearchFormState state = m_searchFormCache.get(pageFormIdentifier);
    if (state != null) {
      if (state.formContentXml != null) {
        f.loadFromXmlString(state.formContentXml);
      }
      if (state.searchFilter != null) {
        f.setSearchFilter(state.searchFilter);
      }
    }
  }

  @Override
  protected void storeSearchFormState(IForm f, String pageFormIdentifier) {
    //cache search form data
    if (f.isEmpty()) {
      m_searchFormCache.remove(pageFormIdentifier);
    }
    else {
      String xml = f.storeToXmlString();
      SearchFilter filter = f.getSearchFilter();
      m_searchFormCache.put(pageFormIdentifier, new SearchFormState(xml, filter));
    }
  }

  @Override
  protected void storeUserFilterState(ITable table, String pageTableIdentifier) {
    TableUserFilterManager filterManager = table.getUserFilterManager();
    if (filterManager == null || filterManager.isEmpty()) {
      m_tableUserFilterState.remove(pageTableIdentifier);
      return;
    }
    m_tableUserFilterState.put(pageTableIdentifier, filterManager.getSerializedData());
  }

  @Override
  protected void loadUserFilterState(ITable table, String pageTableIdentifier) {
    TableUserFilterManager filterManager = table.getUserFilterManager();
    if (filterManager == null) {
      return;
    }
    byte[] state = m_tableUserFilterState.get(pageTableIdentifier);
    if (state != null) {
      filterManager.setSerializedData(state);
    }
  }

  @Override
  public void afterOutlineSelectionChanged(final IDesktop desktop) {
    long memTotal = Runtime.getRuntime().totalMemory();
    long memUsed = (memTotal - Runtime.getRuntime().freeMemory());
    long memMax = Runtime.getRuntime().maxMemory();
    if (memUsed > memMax * getMaxMemThreshold() / 100L) {
      ModelJobs.schedule(new IRunnable() {
        @Override
        public void run() throws Exception {
          desktop.releaseUnusedPages();
        }
      }, ModelJobs.newInput(ClientRunContexts.copyCurrent()).withName("Checking memory"));
    }
  }

  @Override
  public String toString() {
    return "Large";
  }

  /**
   * Threshold in percentage. After this threshold the unused pages are released
   */
  public long getMaxMemThreshold() {
    return m_maxMemThreshold;
  }

  public void setMaxMemThreshold(long maxMemThreshold) {
    m_maxMemThreshold = maxMemThreshold;
  }
}
