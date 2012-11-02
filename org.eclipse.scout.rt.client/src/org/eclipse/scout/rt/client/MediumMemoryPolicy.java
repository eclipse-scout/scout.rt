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
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.scout.commons.LRUCache;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.basic.table.columnfilter.ITableColumnFilter;
import org.eclipse.scout.rt.client.ui.basic.table.columnfilter.ITableColumnFilterManager;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IColumn;
import org.eclipse.scout.rt.client.ui.basic.tree.ITreeNode;
import org.eclipse.scout.rt.client.ui.basic.tree.ITreeVisitor;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.client.ui.desktop.outline.IOutline;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPage;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPageWithTable;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.shared.services.common.jdbc.SearchFilter;

/**
 * Cache only last 5 table page search form contents, last 5 table filter settings, releaseUnusedPages after every page
 * reload and force gc do free memory.
 */
public class MediumMemoryPolicy extends AbstractMemoryPolicy {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(MediumMemoryPolicy.class);

  private boolean m_release = false;
  //cache last 5 search form contents and table filters
  private final LRUCache<String/*pageFormIdentifier*/, SearchFormState> m_searchFormCache;
  private final LRUCache<String /*pageTableIdentifier*/, Map<String, byte[]>> m_tableColumnFilterManagerState;

  public MediumMemoryPolicy() {
    m_searchFormCache = new LRUCache<String, SearchFormState>(5, 0L);
    m_tableColumnFilterManagerState = new LRUCache<String, Map<String, byte[]>>(5, 0L);
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
    try {
      final AtomicLong nodeCount = new AtomicLong();
      if (desktop.getOutline() != null && desktop.getOutline().getSelectedNode() != null) {
        final HashSet<IPage> preservationSet = new HashSet<IPage>();
        IPage p = (IPage) desktop.getOutline().getSelectedNode();
        while (p != null) {
          // the tree in the selection is not the topic
          // of the analysis whether we should free up the memory
          // so we calculate only the other ones.
          preservationSet.add(p);
          p = p.getParentPage();
        }
        ITreeVisitor v = new ITreeVisitor() {
          @Override
          public boolean visit(ITreeNode node) {
            IPage page = (IPage) node;
            if (preservationSet.contains(page)) {
              // nop
            }
            else if (page.getParentPage() == null) {
              // nop, InvisibleRootPage
            }
            else if (page.isChildrenLoaded()) {
              nodeCount.getAndAdd(page.getChildNodeCount());
            }
            return true;
          }
        };
        for (IOutline outline : desktop.getAvailableOutlines()) {
          outline.visitNode(outline.getRootNode(), v);
        }
      }
      long memTotal = Runtime.getRuntime().totalMemory();
      long memUsed = (memTotal - Runtime.getRuntime().freeMemory());
      long memMax = Runtime.getRuntime().maxMemory();
      if (memUsed > memMax * 80L / 100L || nodeCount.get() > 10000) {
        m_release = true;
      }
    }
    catch (Exception e) {
      LOG.error(null, e);
    }
  }

  /**
   * when table contains 1000+ rows clear table before loading new data, thus disabling "replaceRow" mechanism
   */
  @Override
  public void beforeTablePageLoadData(IPageWithTable<?> page) {
    if (m_release) {
      //make sure inactive outlines have no selection that "keeps" the pages
      IDesktop desktop = ClientJob.getCurrentSession().getDesktop();
      for (IOutline o : desktop.getAvailableOutlines()) {
        if (o != desktop.getOutline()) {
          o.selectNode(null);
        }
      }
      ClientJob.getCurrentSession().getDesktop().releaseUnusedPages();
      System.gc();
      for (Job j : Job.getJobManager().find(ClientJob.class)) {
        if (j instanceof ForceGCJob) {
          j.cancel();
        }
      }
      new ForceGCJob().schedule();
      m_release = false;
    }
    if (page.getTable() != null && page.getTable().getRowCount() > 1000) {
      page.getTable().discardAllRows();
    }
  }

  @Override
  public String toString() {
    return "Medium";
  }
}
