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
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ui.basic.tree.ITreeNode;
import org.eclipse.scout.rt.client.ui.desktop.outline.IOutline;
import org.eclipse.scout.rt.client.ui.form.AbstractForm;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.client.ui.form.fields.AbstractFormField;
import org.eclipse.scout.rt.client.ui.form.fields.tablefield.ITableField;

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
public abstract interface IPage extends ITreeNode {

  void initPage() throws ProcessingException;

  /**
   * @deprecated use {@link #getUserPreferenceContext()} instead
   */
  @Deprecated
  String getBookmarkIdentifier();

  /**
   * This method is used to override the bookmark identifier used for this page
   * in case multiple instances of this page are used in the same child list<br>
   * By default this returns null.
   * <p>
   * Note that the value of this id is final and <b>not</b> dynamic to prevent some unexpected behaviours due to id
   * changes during lifetime of the page. Such as persistent store of search context, table width, column order etc.
   */
  String getUserPreferenceContext();

  IOutline getOutline();

  /**
   * Reload this page content.<br>
   * This will first remove all child nodes, add new nodes/table rows and
   * trigger execPageDataLoaded
   */
  void reloadPage() throws ProcessingException;

  /**
   * @return the detail form, the detail form is not automatically started<br>
   *         This returns the detail form for THIS node (page), NOT for child
   *         nodes of this page
   */
  IForm getDetailForm();

  /**
   * set the detail form, the form must either be in non-started state or
   * displayHint must be {@link IForm#DISPLAY_HINT_VIEW} and {@link IForm#isAutoAddRemoveOnDesktop()} must be false
   */
  void setDetailForm(IForm form);

  /**
   * @return the child page at the index
   *         Note that this is <b>not</b> exactly the same as (IPage)getChildNode().
   *         see {@link VirtualPage} for more details.
   */
  IPage getChildPage(int index);

  /**
   * @return all child pages
   *         Note that this is <b>not</b> exactly the same as (IPage[])getChildNodes().
   *         see {@link VirtualPage} for more details.
   *         <p>
   *         Note: Calling this method effectively creates all child page objects and may be expensive on pages with
   *         many child pages.
   */
  IPage[] getChildPages();

  /**
   * Convenience for (IPage)getParentNode()
   */
  IPage getParentPage();

  boolean isTableVisible();

  void setTableVisible(boolean b);

  /**
   * see {@link ITableField#getTablePopulateStatus()}
   * <p>
   * This method is temporary and will be removed in future releases (long term) when the {@link IPage} is legacy and
   * replaced by a simple page with just N forms.
   * 
   * @since 3.8.2
   */
  IProcessingStatus getPagePopulateStatus();

  /**
   * see {@link ITableField#setTablePopulateStatus(IProcessingStatus)}
   * <p>
   * This method is temporary and will be removed in future releases (long term) when the {@link IPage} is legacy and
   * replaced by a simple page with just N forms.
   * 
   * @since 3.8.2
   */
  void setPagePopulateStatus(IProcessingStatus status);

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
}
