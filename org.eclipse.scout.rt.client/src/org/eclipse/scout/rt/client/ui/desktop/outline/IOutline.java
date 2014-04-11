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
package org.eclipse.scout.rt.client.ui.desktop.outline;

import java.security.Permission;

import org.eclipse.scout.commons.ITypeWithClassId;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.basic.tree.ITree;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPage;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPageWithTable;
import org.eclipse.scout.rt.client.ui.form.IForm;

public interface IOutline extends ITree, ITypeWithClassId {

  /**
   * {@link Boolean}
   */
  String PROP_ENABLED = "enabled";

  /**
   * {@link Boolean}
   */
  String PROP_VISIBLE = "visible";

  /**
   * {@link IForm}
   */
  String PROP_DETAIL_FORM = "detailForm";

  /**
   * {@link ITable}
   */
  String PROP_DETAIL_TABLE = "detailTable";

  /**
   * {@link IForm}
   */
  String PROP_SEARCH_FORM = "searchForm";

  /**
   * alias to {@link ITree#getSelectedNode()}
   */
  IPage getActivePage();

  void makeActivePageToContextPage();

  void clearContextPage();

  /**
   * Find a specific page by its type in the outline tree
   * 
   * @return the first found occurrence of the page
   */
  <T extends IPage> T findPage(final Class<T> pageType);

  /**
   * Call this method to refresh all existing pages in this outline<br>
   * If currently active page(s) are affected they reload their data, otherwise
   * the pages is simply marked dirty and reloaded on next activation
   */
  void refreshPages(Class... pageTypes);

  /**
   * Unload and release unused pages, such as closed and non-selected nodes
   */
  void releaseUnusedPages();

  /**
   * Reset outline as it would have been started again from scratch
   */
  void resetOutline() throws ProcessingException;

  boolean isVisible();

  void setVisible(boolean b);

  boolean isVisibleGranted();

  void setVisibleGranted(boolean b);

  void setVisiblePermission(Permission p);

  /**
   * @return the detail form of the active (selected) page {@link IPage#getDetailForm()}
   */
  IForm getDetailForm();

  void setDetailForm(IForm form);

  /**
   * @return the detail table of the active (selected) page {@link IPage#getTable()}
   */
  ITable getDetailTable();

  void setDetailTable(ITable table);

  /**
   * @return the search form of the active (selected) page {@link IPageWithTable#getSearchFormInternal()}
   */
  IForm getSearchForm();

  void setSearchForm(IForm form);

  /**
   * Convenience for (IPage)getRootNode()
   */
  IPage getRootPage();

  OutlineMediator getOutlineMediator();

  void setPageChangeStrategy(IPageChangeStrategy pageChangeStrategy);

  IPageChangeStrategy getPageChangeStrategy();
}
