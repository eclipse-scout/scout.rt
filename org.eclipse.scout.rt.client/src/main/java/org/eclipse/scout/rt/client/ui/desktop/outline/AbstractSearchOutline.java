/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.desktop.outline;

import static java.util.Collections.emptyMap;
import static org.eclipse.scout.rt.platform.util.ObjectUtility.nvl;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.eclipse.scout.rt.client.ui.basic.tree.AbstractTree;
import org.eclipse.scout.rt.client.ui.basic.tree.ITreeUIFacade;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPage;
import org.eclipse.scout.rt.platform.annotations.ConfigProperty;
import org.eclipse.scout.rt.platform.classid.ClassId;
import org.eclipse.scout.rt.platform.text.TEXTS;
import org.eclipse.scout.rt.platform.util.event.FastListenerList;
import org.eclipse.scout.rt.shared.AbstractIcons;

@ClassId("57c90097-ba28-414c-8ce6-0ae32bfef803")
public abstract class AbstractSearchOutline extends AbstractOutline implements ISearchOutline {

  private final FastListenerList<SearchOutlineEventListener> m_listeners = new FastListenerList<>();

  @Override
  protected String getConfiguredTitle() {
    return TEXTS.get("Search");
  }

  @ConfigProperty(ConfigProperty.INTEGER)
  protected int getConfiguredMaxSearchQueryLength() {
    return 60;
  }

  @ConfigProperty(ConfigProperty.INTEGER)
  protected int getConfiguredMinSearchTokenLength() {
    return 2;
  }

  @Override
  protected String getConfiguredIconId() {
    return AbstractIcons.Search;
  }

  @Override
  protected void initConfig() {
    super.initConfig();
    setMaxSearchQueryLength(getConfiguredMaxSearchQueryLength());
    setMinSearchTokenLength(getConfiguredMinSearchTokenLength());
  }

  @Override
  public void setMinSearchTokenLength(int len) {
    if (len <= 0) {
      len = 2;
    }
    propertySupport.setPropertyInt(PROP_MIN_SEARCH_TOKEN_LENGTH, len);
  }

  @Override
  public int getMinSearchTokenLength() {
    return propertySupport.getPropertyInt(PROP_MIN_SEARCH_TOKEN_LENGTH);
  }

  @Override
  public void setMaxSearchQueryLength(int len) {
    if (len <= 0) {
      len = 200;
    }
    propertySupport.setPropertyInt(PROP_MAX_SEARCH_QUERY_LENGTH, len);
  }

  @Override
  public int getMaxSearchQueryLength() {
    return propertySupport.getPropertyInt(PROP_MAX_SEARCH_QUERY_LENGTH);
  }

  @Override
  public void search() {
    fireSearch();
  }

  protected void fireSearchOutlineEvent(SearchOutlineEvent event) {
    m_listeners.list().forEach(listener -> listener.handle(event));
  }

  protected void fireSearch() {
    fireSearchOutlineEvent(new SearchOutlineEvent(this, SearchOutlineEvent.TYPE_SEARCH_EVENT));
  }

  protected void execSearch(String query) {
  }

  protected void execResetSearch() {
  }

  protected void addSearchState(ISearchPage page) {
    addSearchStates(List.of(page));
  }

  protected void addSearchStates(Collection<ISearchPage> pages) {
    Map<ISearchPage, ISearchState> result = new HashMap<>(getSearchStates());
    pages.forEach(page -> {
      ISearchState searchState = page.getSearchState();
      if (searchState == null) {
        throw new IllegalArgumentException("Page '" + page + "' has no search state.");
      }
      result.put(page, searchState);
    });
    setSearchStatesInternal(result);
  }

  protected void removeSearchState(ISearchPage page) {
    removeSearchStates(List.of(page));
  }

  protected void removeSearchStates(Collection<ISearchPage> pages) {
    setSearchStatesInternal(getSearchStates().entrySet().stream()
        .filter(entry -> !pages.contains(entry.getKey()))
        .collect(Collectors.toMap(Entry::getKey, Entry::getValue)));
  }

  protected void setSearchStatesInternal(Map<ISearchPage, ISearchState> searchStates) {
    propertySupport.setProperty(PROP_SEARCH_STATES, searchStates);
  }

  @Override
  public Map<ISearchPage, ISearchState> getSearchStates() {
    return Collections.unmodifiableMap(nvl(getSearchStatesInternal(), emptyMap()));
  }

  protected Map<ISearchPage, ISearchState> getSearchStatesInternal() {
    // noinspection unchecked
    return (Map<ISearchPage, ISearchState>) propertySupport.getProperty(PROP_SEARCH_STATES, Map.class);
  }

  @Override
  public void setSearchQuery(String searchQuery) {
    propertySupport.setPropertyString(PROP_SEARCH_QUERY, searchQuery);
  }

  @Override
  public String getSearchQuery() {
    return propertySupport.getPropertyString(PROP_SEARCH_QUERY);
  }

  @Override
  public void requestFocusQueryField() {
    // Always fire property change since it is used as an event. Therefore, it does not have a value.
    propertySupport.setPropertyAlwaysFire(PROP_REQUEST_FOCUS_QUERY_FIELD, null);
  }

  @Override
  public void fireAfterPageInit(IPage<?> page) {
    super.fireAfterPageInit(page);

    if (page instanceof ISearchPage searchPage && page.getParentPage() == getRootPage()) {
      addSearchState(searchPage);
    }
  }

  @Override
  public void fireAfterPageDispose(IPage<?> page) {
    super.fireAfterPageDispose(page);

    if (page instanceof ISearchPage searchPage) {
      removeSearchState(searchPage);
    }
  }

  @Override
  public void addSearchOutlineEventListener(SearchOutlineEventListener listener) {
    m_listeners.add(listener);
  }

  @Override
  public void removeSearchOutlineEventListener(SearchOutlineEventListener listener) {
    m_listeners.remove(listener);
  }

  @Override
  protected ITreeUIFacade createUIFacade() {
    return new P_UIFacade();
  }

  @Override
  public ISearchOutlineUiFacade getUIFacade() {
    return (ISearchOutlineUiFacade) super.getUIFacade();
  }

  protected class P_UIFacade extends AbstractTree.P_UIFacade implements ISearchOutlineUiFacade {

    @Override
    public void setSearchQueryFromUI(String searchQuery) {
      try {
        pushUIProcessor();
        setSearchQuery(searchQuery);
      }
      finally {
        popUIProcessor();
      }
    }

    @Override
    public void fireSearchFromUI() {
      try {
        pushUIProcessor();
        execSearch(getSearchQuery());
      }
      finally {
        popUIProcessor();
      }
    }

    @Override
    public void fireResetSearchFromUI() {
      try {
        pushUIProcessor();
        execResetSearch();
      }
      finally {
        popUIProcessor();
      }
    }
  }
}
