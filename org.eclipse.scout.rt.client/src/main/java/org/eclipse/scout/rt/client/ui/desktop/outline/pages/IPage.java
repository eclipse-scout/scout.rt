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
package org.eclipse.scout.rt.client.ui.desktop.outline.pages;

import java.util.Collection;
import java.util.List;

import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.basic.tree.ITreeNode;
import org.eclipse.scout.rt.client.ui.desktop.outline.IOutline;
import org.eclipse.scout.rt.client.ui.form.AbstractForm;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.client.ui.form.fields.AbstractFormField;
import org.eclipse.scout.rt.platform.classid.ITypeWithClassId;
import org.eclipse.scout.rt.platform.status.IStatus;

/**
 * A page is a tree node for the outline and a table in the main view<br>
 * Depending on the source of your content, you should consider one of the two implementation type.
 * <dl>
 * <dt>Node page</dt>
 * <dd>Node-oriented page: the content is defined as child pages, the table in the main view display the list of child
 * pages see {@link IPageWithNodes}
 * <dt>Table page</dt>
 * <dd>Table-oriented page: the content is loaded from a outline service and displayed in the table of the main view see
 * {@link IPageWithTable} In the outline, it is possible to drill down the content of the node (except if the page is
 * configured as a leaf)
 */
public interface IPage<T extends ITable> extends ITreeNode, ITypeWithClassId {

  /**
   * @return The {@link ITable} of this {@link IPage} or {@code null} if it does not have a table. If this page has a
   *         table but it is not yet created it will be created and initialized. Same as {@link #getTable(true)}.
   */
  T getTable();

  /**
   * Gets the {@link ITable} of this {@link IPage}.
   *
   * @param create
   *          if {@code true} and there is no table yet: the table will be created and initialized.
   * @return The {@link ITable} or {@code null} if this page has no table (this method call can also return {@code null}
   *         if {@code true} is passed. This can be the case if this page does not declare a table).
   */
  T getTable(boolean create);

  void initPage();

  /**
   * This method is used to override the bookmark identifier used for this page in case multiple instances of this page
   * are used in the same child list<br>
   * By default this returns null.
   * <p>
   * Note that the value of this id is final and <b>not</b> dynamic to prevent some unexpected behaviours due to id
   * changes during lifetime of the page. Such as persistent store of search context, table width, column order etc.
   */
  String getUserPreferenceContext();

  IOutline getOutline();

  /**
   * Reload this page content.<br>
   * This will first remove all child nodes, add new nodes/table rows and trigger execPageDataLoaded
   */
  void reloadPage();

  /**
   * @return the detail form, the detail form is not automatically started<br>
   *         This returns the detail form for THIS node (page), NOT for child nodes of this page
   */
  IForm getDetailForm();

  /**
   * set the detail form, the form must either be in non-started state or displayHint must be
   * {@link IForm#DISPLAY_HINT_VIEW} and {@link IForm#isShowOnStart()} must be false
   */
  void setDetailForm(IForm form);

  /**
   * @return the child page at the index Note that this is <b>not</b> exactly the same as (IPage)getChildNode(). see
   *         {@link VirtualPage} for more details.
   */
  IPage<?> getChildPage(int index);

  /**
   * @return all child pages Note that this is <b>not</b> exactly the same as (IPage)getChildNodes(). see
   *         {@link VirtualPage} for more details.
   *         <p>
   *         Note: Calling this method effectively creates all child page objects and may be expensive on pages with
   *         many child pages.
   */
  List<IPage<?>> getChildPages();

  /**
   * Convenience for (IPage)getParentNode()
   */
  IPage<?> getParentPage();

  boolean isTableVisible();

  void setTableVisible(boolean b);

  /**
   * Convenience function for <code>getTable().getTableStatus()</code> (returns <code>null</code> when table is
   * <code>null</code>).
   */
  IStatus getTableStatus();

  /**
   * Convenience function for <code>getTable().setTableStatus(status)</code> (does nothing when table is
   * <code>null</code>).
   */
  void setTableStatus(IStatus tableStatus);

  /**
   * Convenience function for <code>getTable().isTableStatusVisible()</code> (returns <code>false</code> when table is
   * <code>null</code>).
   */
  boolean isTableStatusVisible();

  /**
   * Convenience function for <code>getTable().setTableStatusVisible(b)</code> (does nothing when table is
   * <code>null</code>).
   */
  void setTableStatusVisible(boolean tableStatusVisible);

  /**
   * Call this method to refresh all listeners on that dataTypes.<br>
   * These might include pages, forms, fields etc.<br>
   *
   * @see {@link AbstractForm#execDataChanged(Object...)} {@link AbstractForm#execDataChanged(Object...)}
   *      {@link AbstractFormField#execDataChanged(Object...)} {@link AbstractFormField#execDataChanged(Object...)}
   *      {@link AbstractPage#execDataChanged(Object...)} {@link AbstractPage#execDataChanged(Object...)}
   */
  void dataChanged(Object... dataTypes);

  void pageActivatedNotify();

  void pageDeactivatedNotify();

  /**
   * Adapter pattern. A page may contribute adapters when asked for a given adapter class. This way, functionality can
   * be added to {@link IPage} without changing its lean interface
   *
   * @param clazz
   *          the adapter interface class, usually something like IXxxAdapter
   * @return the contributed adapter instance or <code>null</code>
   */
  <A> A getAdapter(Class<A> clazz);

  boolean isDetailFormVisible();

  void setDetailFormVisible(boolean visible);

  /**
   * @since 3.8.2
   */
  ITreeNode getTreeNodeFor(ITableRow tableRow);

  IPage<?> getPageFor(ITableRow tableRow);

  ITableRow getTableRowFor(ITreeNode treeNode);

  List<ITableRow> getTableRowsFor(Collection<? extends ITreeNode> treeNodes);

  /**
   * The default implementation returns the single selection menus from the parent table page's table.
   * <p>
   * If this behavior is not desired return an empty list instead.
   *
   * @param parentTablePage
   *          Parent table page
   * @return A list (non-null) of single selection menus.
   */
  List<IMenu> computeParentTablePageMenus(IPageWithTable<?> parentTablePage);

  /**
   * @return {@code true} if this page is active (has been clicked by the user). {@code false} otherwise.
   */
  boolean isPageActive();

}
