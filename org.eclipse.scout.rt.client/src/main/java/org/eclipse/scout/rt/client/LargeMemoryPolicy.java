/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.scout.rt.client.context.ClientRunContexts;
import org.eclipse.scout.rt.client.job.ModelJobs;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.basic.table.userfilter.TableUserFilterManager;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.shared.services.common.jdbc.SearchFilter;

/**
 * No specific restrictions, cache all table page search form contents and all page table filter settings. Check memory
 * limits after page reload.
 */
public class LargeMemoryPolicy extends AbstractMemoryPolicy {

  //cache all search form contents
  private final Map<String/*pageFormIdentifier*/, SearchFormState> m_searchFormCache;
  private final Map<String /*pageTableIdentifier*/, byte[]> m_tableUserFilterState;
  private long m_maxMemThreshold = 100L;

  public LargeMemoryPolicy() {
    m_searchFormCache = new HashMap<>();
    m_tableUserFilterState = new HashMap<>();
  }

  @Override
  protected void loadSearchFormState(IForm f, String pageFormIdentifier) {
    //check if there is stored search form data
    SearchFormState state = m_searchFormCache.get(pageFormIdentifier);
    if (state != null) {
      if (state.m_formContentXml != null) {
        f.loadFromXmlString(state.m_formContentXml);
      }
      if (state.m_searchFilter != null) {
        f.setSearchFilter(state.m_searchFilter);
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
    if (getMaxMemThreshold() >= 100) {
      return;
    }
    long memTotal = Runtime.getRuntime().totalMemory();
    long memUsed = (memTotal - Runtime.getRuntime().freeMemory());
    long memMax = Runtime.getRuntime().maxMemory();
    if (memUsed > memMax * getMaxMemThreshold() / 100L) {
      ModelJobs.schedule(desktop::releaseUnusedPages, ModelJobs.newInput(ClientRunContexts.copyCurrent()).withName("Checking memory"));
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
