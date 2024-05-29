/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.desktop.outline;

import java.security.Permission;
import java.util.List;

import org.eclipse.scout.rt.client.context.ClientRunContext;
import org.eclipse.scout.rt.client.ui.IDisplayParent;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.action.menu.TableMenuType;
import org.eclipse.scout.rt.client.ui.action.menu.TreeMenuType;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.basic.tree.ITree;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPage;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPageWithTable;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.platform.IOrdered;
import org.eclipse.scout.rt.shared.dimension.IVisibleDimension;

public interface IOutline extends ITree, IOrdered, IDisplayParent, IVisibleDimension {

  /**
   * The {@link IOutline} which is currently associated with the current thread.
   */
  ThreadLocal<IOutline> CURRENT = new ThreadLocal<>();

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

  String PROP_OUTLINE_OVERVIEW_VISIBLE = "outlineOverviewVisible";

  /**
   * {@link ITable}
   */
  String PROP_DETAIL_TABLE = "detailTable";

  /**
   * {@link IForm}
   */
  String PROP_SEARCH_FORM = "searchForm";

  String PROP_ORDER = "order";

  String PROP_NAVIGATE_BUTTONS_VISIBLE = "navigateButtonsVisible";

  void activate();

  void deactivate();

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
   * Find a specific page by its type in the outline tree, only considering the subtree of the given rootPage.
   *
   * @return the first found occurrence of the page in the subtree
   */
  <T extends IPage> T findPage(final Class<T> pageType, IPage rootPage);

  /**
   * Call this method to refresh all existing pages in this outline<br>
   * If currently active page(s) are affected they reload their data, otherwise the pages is simply marked dirty and
   * reloaded on next activation
   */
  void refreshPages(List<Class<? extends IPage<?>>> pageTypes);

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
   * Resets the outline.
   * <p>
   * This means all expanded and loaded {@link IPage pages} are discarded (disposed). This includes all detail- and
   * search- forms. The first visible and selectable node will be selected if such a node exists.
   */
  void resetOutline();

  /**
   * Resets the outline.
   * <p>
   * This means all expanded and loaded {@link IPage pages} are discarded (disposed). This includes all detail- and
   * search- forms. The first visible and selectable node will be selected if requested and such a node exists.
   *
   * @param selectFirstNode
   *          Specifies if the first visible and selectable {@link IPage} should be selected after the reset. If
   *          {@code false} no {@link IPage} will be selected and the selection of the outline will be {@code null}. In
   *          the UI the {@link IPage} tiles will be displayed instead of a page content.
   */
  void resetOutline(boolean selectFirstNode);

  boolean isVisible();

  void setVisible(boolean b);

  boolean isVisibleGranted();

  void setVisibleGranted(boolean b);

  <T extends IPage> List<T> findPages(Class<T> pageType);

  <T extends IPage> List<T> findPages(Class<T> pageType, IPage rootPage);

  void setVisiblePermission(Permission p);

  boolean isNavigateButtonsVisible();

  void setNavigateButtonsVisible(boolean b);

  /**
   * @return the default detail form if no page is active (selected)
   */
  IForm getDefaultDetailForm();

  void setDefaultDetailForm(IForm form);

  /**
   * @return the detail form of the active (selected) page {@link IPage#getDetailForm()}
   */
  IForm getDetailForm();

  void setDetailForm(IForm form);

  void setOutlineOverviewVisible(boolean visible);

  boolean isOutlineOverviewVisible();

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
   * This method returns all menus available for this page.
   * <p>
   * <b>Note:</b> The menus are not wrapped - so there might be inherited menus with a menu type different than
   * {@link TreeMenuType#SingleSelection}.
   * <p>
   * In the returned list are:
   * <ul>
   * <li>All menus of the {@link IPage page} itself.</li>
   * <li>If the page is a {@link IPageWithTable}: all empty space menus of the pages table.</li>
   * <li>If the parent page is a {@link IPageWithTable}: all {@link TableMenuType#SingleSelection} menus of the parents
   * table page.</li>
   * </ul>
   *
   * @param page
   *          The {@link IPage} for which the menus should be calculated.
   * @return A {@link List} holding the {@link IMenu}s for the specified page.
   */
  List<IMenu> getMenusForPage(IPage<?> page);

  void firePageChanged(IPage<?> page);

  void fireBeforeDataLoaded(IPage<?> page);

  void fireAfterDataLoaded(IPage<?> page);

  void fireAfterTableInit(IPage<?> page);

  void fireAfterPageInit(IPage<?> page);

  void fireAfterSearchFormStart(IPage<?> page);

  void fireAfterPageDispose(IPage<?> page);

  void firePageActivated(IPage<?> page);

  /**
   * Creates a new {@link ClientRunContext} to be used for executing model logic in the context of a suitable display
   * parent.
   *
   * @return Returns a {@link ClientRunContext}, never <code>null</code>.
   * @since 7.0
   */
  ClientRunContext createDisplayParentRunContext();

}
