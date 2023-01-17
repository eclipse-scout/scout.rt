/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.desktop.outline;

import org.eclipse.scout.rt.client.ui.basic.tree.AbstractTree;
import org.eclipse.scout.rt.client.ui.basic.tree.ITreeUIFacade;
import org.eclipse.scout.rt.platform.annotations.ConfigProperty;
import org.eclipse.scout.rt.platform.classid.ClassId;
import org.eclipse.scout.rt.platform.text.TEXTS;
import org.eclipse.scout.rt.shared.AbstractIcons;

@ClassId("57c90097-ba28-414c-8ce6-0ae32bfef803")
public abstract class AbstractSearchOutline extends AbstractOutline implements ISearchOutline {

  private int m_minSearchTokenLength;

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
    if (len > 0) {
      m_minSearchTokenLength = len;
    }
  }

  @Override
  public int getMinSearchTokenLength() {
    if (m_minSearchTokenLength <= 0) {
      return 2;
    }
    return m_minSearchTokenLength;
  }

  @Override
  public void setMaxSearchQueryLength(int len) {
    if (len > 0) {
      propertySupport.setPropertyInt(PROP_MAX_SEARCH_QUERY_LENGTH, len);
    }
  }

  @Override
  public int getMaxSearchQueryLength() {
    int len = propertySupport.getPropertyInt(PROP_MAX_SEARCH_QUERY_LENGTH);
    if (len <= 0) {
      len = 200;
    }
    return len;
  }

  @Override
  public void search() {
    execSearch(getSearchQuery());
  }

  protected void execSearch(String query) {
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
  public void setSearchStatus(String searchStatus) {
    propertySupport.setPropertyString(PROP_SEARCH_STATUS, searchStatus);
  }

  @Override
  public String getSearchStatus() {
    return propertySupport.getPropertyString(PROP_SEARCH_STATUS);
  }

  @Override
  public void requestFocusQueryField() {
    // Always fire property change since it is used as an event. Therefore it does not have a value.
    propertySupport.setPropertyAlwaysFire(PROP_REQUEST_FOCUS_QUERY_FIELD, null);
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
    public void search(String query) {
      try {
        pushUIProcessor();
        setProperty(PROP_SEARCH_QUERY, query);
        AbstractSearchOutline.this.search();
      }
      finally {
        popUIProcessor();
      }
    }

  }
}
