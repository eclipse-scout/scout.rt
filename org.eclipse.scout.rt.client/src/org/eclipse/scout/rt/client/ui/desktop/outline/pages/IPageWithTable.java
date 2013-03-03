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
package org.eclipse.scout.rt.client.ui.desktop.outline.pages;

import org.eclipse.scout.commons.exception.IProcessingStatus;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.basic.tree.ITreeNode;
import org.eclipse.scout.rt.shared.services.common.jdbc.SearchFilter;

/**
 * Table-oriented page (one of the two types of IPage @see IPage)<br>
 * <p>
 * Main concern is the content of the table.<br>
 * Usually child pages are added (one for each row in the table) to provide the possibility to drill down the content of
 * the table. (defined in the execCreateChildPage(row))<br>
 * <p>
 * tree events are handled manually using exec.. methods<br>
 * table events are delegated to the tree<br>
 */
public interface IPageWithTable<T extends ITable> extends IPage {

  T getTable();

  /**
   * @deprecated use {@link IPage#getPagePopulateStatus()}. Will be removed in Release 3.10.
   */
  @Deprecated
  IProcessingStatus getTablePopulateStatus();

  /**
   * @deprecated use {@link IPage#setPagePopulateStatus(IProcessingStatus)}. Will be removed in Release 3.10.
   */
  @Deprecated
  void setTablePopulateStatus(IProcessingStatus status);

  ITreeNode getTreeNodeFor(ITableRow tableRow);

  ITableRow getTableRowFor(ITreeNode childPageNode);

  ITableRow[] getTableRowsFor(ITreeNode[] childPageNodes);

  /**
   * Computes the list of linked child pages for the given table rows and updates their summary cell.
   */
  public IPage[] getUpdatedChildPagesFor(ITableRow[] tableRows);

  /**
   * @return search form that is used to filter table rows<br>
   *         Note that the form will be started at latest after being added to
   *         the outline tree inside {@link IPage#initPage()} resps in {@link IPage#execInitPage()}
   */
  ISearchForm getSearchFormInternal();

  boolean isSearchRequired();

  void setSearchRequired(boolean b);

  boolean isSearchActive();

  void setSearchActive(boolean b);

  /**
   * Convenience for getting the search filter from the page's search form
   * 
   * @return life reference to the filter (never null)
   */
  SearchFilter getSearchFilter();

  boolean isShowTableRowMenus();

  void setShowTableRowMenus(boolean showTableRowMenus);

  boolean isShowEmptySpaceMenus();

  void setShowEmptySpaceMenus(boolean showEmptySpaceMenus);
}
