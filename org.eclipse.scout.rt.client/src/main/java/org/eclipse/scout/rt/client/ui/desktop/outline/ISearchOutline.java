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
package org.eclipse.scout.rt.client.ui.desktop.outline;

/**
 * Outline with search capabilities.
 */
public interface ISearchOutline extends IOutline {

  String PROP_SEARCH_STATUS = "searchStatus";

  String PROP_SEARCH_QUERY = "searchQuery";

  String PROP_MAX_SEARCH_QUERY_LENGTH = "maxSearchFieldLength";

  /**
   * Property to request the focus for a query field on a search outline.
   */
  String PROP_REQUEST_FOCUS_QUERY_FIELD = "requestFocusQueryField";

  void search();

  String getSearchQuery();

  void setSearchQuery(String searchQuery);

  String getSearchStatus();

  void setSearchStatus(String searchStatus);

  int getMaxSearchQueryLength();

  void setMaxSearchQueryLength(int len);

  int getMinSearchTokenLength();

  void setMinSearchTokenLength(int len);

  void requestFocusQueryField();

  @Override
  ISearchOutlineUiFacade getUIFacade();

}
