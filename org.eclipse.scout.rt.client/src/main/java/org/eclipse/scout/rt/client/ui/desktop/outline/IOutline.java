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

import java.security.Permission;
import java.util.List;

import org.eclipse.scout.rt.client.ui.IDisplayParent;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.action.menu.TableMenuType;
import org.eclipse.scout.rt.client.ui.action.menu.TreeMenuType;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.basic.tree.ITree;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPage;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPageWithNodes;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPageWithTable;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.platform.IOrdered;
import org.eclipse.scout.rt.platform.classid.ITypeWithClassId;

public interface IOutline extends ITree, ITypeWithClassId, IOrdered, IDisplayParent {

  /**
   * The {@link IOutline} which is currently associated with the current thread.
   */
  ThreadLocal<IOutline> CURRENT = new ThreadLocal<>();

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
   * {@link IForm}
   */
  String PROP_DEFAULT_DETAIL_FORM = "defaultDetailForm";

  /**
   * {@link ITable}
   */
  String PROP_DETAIL_TABLE = "detailTable";

  /**
   * {@link IForm}
   */
  String PROP_SEARCH_FORM = "searchForm";

  String PROP_VIEW_ORDER = "viewOrder";

  String PROP_BREADCRUMB_ENABLED = "breadcrumbEnabled";

  String PROP_NAVIGATE_BUTTONS_VISIBLE = "navigateButtonsVisible";

  /**
   * alias to {@link ITree#getSelectedNode()}
   */
  IPage<?> getActivePage();

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
   * If currently active page(s) are affected they reload their data, otherwise the pages is simply marked dirty and
   * reloaded on next activation
   */
  void refreshPages(List<Class<? extends IPage>> pageTypes);

  /**
   * @param pageTypes
   *          Must be classes that implement {@link IPage}.
   * @see #refreshPages(List)
   */
  void refreshPages(Class<?>... pageTypes);

  /**
   * Unload and release unused pages, such as closed and non-selected nodes
   */
  void releaseUnusedPages();

  /**
   * Reset outline as it would have been started again from scratch
   */
  void resetOutline();

  boolean isVisible();

  void setVisible(boolean b);

  boolean isVisibleGranted();

  void setVisibleGranted(boolean b);

  void setVisiblePermission(Permission p);

  boolean isBreadcrumbEnabled();

  void setBreadcrumbEnabled(boolean b);

  boolean isNavigateButtonsVisible();

  void setNavigateButtonsVisible(boolean b);

  /**
   * @return the default detail form if no page is active (selected)
   */
  IForm getDefaultDetailForm();

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
  IPage<?> getRootPage();

  OutlineMediator getOutlineMediator();

  void setPageChangeStrategy(IPageChangeStrategy pageChangeStrategy);

  IPageChangeStrategy getPageChangeStrategy();

  /**
   * This method returns all menus available for this page. The menus are not wrapped - so there might be inherited
   * menus with any different menu type from {@link TreeMenuType#SingleSelection}. In the returned list are:
   * <ul>
   * <li>All menus of the outline itself.</li>
   * <li>If the page is a {@link IPageWithTable} all empty space menus of the pages table will be included.</li>
   * <li>If the page is a {@link IPageWithNodes} and it's parent a {@link IPageWithTable} all
   * {@link TableMenuType#SingleSelection} menus of the parents table page will be included.</li>
   * </ul>
   *
   * @param page
   * @return
   */
  List<IMenu> getMenusForPage(IPage<?> page);

  void firePageChanged(IPage<?> page);
}
