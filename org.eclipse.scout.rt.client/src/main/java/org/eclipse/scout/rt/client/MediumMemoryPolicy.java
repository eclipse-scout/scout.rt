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

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.eclipse.scout.rt.client.session.ClientSessionProvider;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.basic.table.userfilter.TableUserFilterManager;
import org.eclipse.scout.rt.client.ui.basic.tree.ITreeNode;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.client.ui.desktop.outline.IOutline;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPage;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPageWithTable;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.platform.util.collection.ConcurrentExpiringMap;
import org.eclipse.scout.rt.platform.util.visitor.DepthFirstTreeVisitor;
import org.eclipse.scout.rt.platform.util.visitor.IDepthFirstTreeVisitor;
import org.eclipse.scout.rt.platform.util.visitor.TreeVisitResult;
import org.eclipse.scout.rt.shared.services.common.jdbc.SearchFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Cache only last 5 table page search form contents, last 5 table filter settings, releaseUnusedPages after every page
 * reload.
 */
public class MediumMemoryPolicy extends AbstractMemoryPolicy {
  private static final Logger LOG = LoggerFactory.getLogger(MediumMemoryPolicy.class);

  private boolean m_release = false;
  //cache last 5 search form contents and table filters
  private final ConcurrentExpiringMap<String/*pageFormIdentifier*/, SearchFormState> m_searchFormCache;
  private final ConcurrentExpiringMap<String /*pageTableIdentifier*/, byte[]> m_tableUserFilterState;

  public MediumMemoryPolicy() {
    m_searchFormCache = new ConcurrentExpiringMap<>(0L, TimeUnit.MILLISECONDS, 5);
    m_tableUserFilterState = new ConcurrentExpiringMap<>(0L, TimeUnit.MILLISECONDS, 5);
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
    try {
      final AtomicLong nodeCount = new AtomicLong();
      if (desktop.getOutline() != null && desktop.getOutline().getSelectedNode() != null) {
        final Set<IPage> preservationSet = new HashSet<>();
        IPage<?> p = (IPage) desktop.getOutline().getSelectedNode();
        while (p != null) {
          // the tree in the selection is not the topic
          // of the analysis whether we should free up the memory
          // so we calculate only the other ones.
          preservationSet.add(p);
          p = p.getParentPage();
        }
        IDepthFirstTreeVisitor<ITreeNode> v = new DepthFirstTreeVisitor<ITreeNode>() {
          @Override
          public TreeVisitResult preVisit(ITreeNode node, int level, int index) {
            IPage<?> page = (IPage) node;
            if (preservationSet.contains(page)) {
              // nop
            }
            else if (page.getParentPage() == null) {
              // nop, InvisibleRootPage
            }
            else if (page.isChildrenLoaded()) {
              nodeCount.getAndAdd(page.getChildNodeCount());
            }
            return TreeVisitResult.CONTINUE;
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
      LOG.error("Unexpected Exception", e);
    }
  }

  /**
   * when table contains 1000+ rows clear table before loading new data, thus disabling "replaceRow" mechanism
   */
  @Override
  public void beforeTablePageLoadData(IPageWithTable<?> page) {
    if (m_release) {
      //make sure inactive outlines have no selection that "keeps" the pages
      IDesktop desktop = ClientSessionProvider.currentSession().getDesktop();
      for (IOutline o : desktop.getAvailableOutlines()) {
        if (o != desktop.getOutline()) {
          o.selectNode(null);
        }
      }
      desktop.releaseUnusedPages();

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
