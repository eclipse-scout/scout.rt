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

import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.basic.tree.ITreeNode;
import org.eclipse.scout.rt.client.ui.basic.tree.IVirtualTreeNode;
import org.eclipse.scout.rt.client.ui.basic.tree.VirtualTreeNode;
import org.eclipse.scout.rt.client.ui.desktop.outline.IOutline;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.platform.status.IStatus;
import org.eclipse.scout.rt.platform.util.CollectionUtility;

/**
 * For performance optimizations, child pages are only loaded when needed. Until then they are virtualized using
 * VirtualPage objects.
 * <p>
 * A call to {@link IPage#getChildPage(int)}, {@link IPage#getChildPages()} or selecting a virtual page effectively
 * constructs the child page.
 * <p>
 * This construction involves calling
 * {@link AbstractPageWithTable#execCreateChildPage(org.eclipse.scout.rt.client.ui.basic.table.ITableRow)} resp.
 * {@link AbstractPageWithNodes#execCreateChildPages(java.util.Collection)}
 */
public class VirtualPage extends VirtualTreeNode implements IPage, IVirtualTreeNode {

  @Override
  public void initPage() {
    // NOP
  }

  @Override
  public String getUserPreferenceContext() {
    return null;
  }

  @Override
  public IOutline getOutline() {
    return (IOutline) getTree();
  }

  @Override
  public IPage<?> getParentPage() {
    return (IPage) getParentNode();
  }

  @Override
  public IPage<?> getChildPage(final int childIndex) {
    return null;
  }

  @Override
  public List<IPage<?>> getChildPages() {
    return CollectionUtility.emptyArrayList();
  }

  @Override
  public void pageActivatedNotify() {
    // NOP
  }

  @Override
  public void pageDeactivatedNotify() {
    // NOP
  }

  @Override
  public IForm getDetailForm() {
    return null;
  }

  @Override
  public void setDetailForm(IForm form) {
    // NOP
  }

  @Override
  public void dataChanged(Object... dataTypes) {
    // NOP
  }

  @Override
  public final void reloadPage() {
    // NOP
  }

  @Override
  public List computeParentTablePageMenus(IPageWithTable parentTablePage) {
    return CollectionUtility.emptyArrayList();
  }

  @Override
  public boolean isTableVisible() {
    return false;
  }

  @Override
  public void setTableVisible(boolean b) {
    // NOP
  }

  @Override
  public IStatus getTableStatus() {
    return null;
  }

  @Override
  public void setTableStatus(IStatus tableStatus) {
    // NOP
  }

  @Override
  public boolean isTableStatusVisible() {
    return false;
  }

  @Override
  public void setTableStatusVisible(boolean tableStatusVisible) {
    // NOP
  }

  /**
   * not defined on a virtual pages.
   */
  @Override
  public String classId() {
    return null;
  }

  @Override
  public boolean isDetailFormVisible() {
    return false;
  }

  @Override
  public void setDetailFormVisible(boolean visible) {
    // NOP
  }

  @Override
  public ITable getTable() {
    return null;
  }

  @Override
  public ITreeNode getTreeNodeFor(ITableRow tableRow) {
    return null;
  }

  @Override
  public IPage<?> getPageFor(ITableRow tableRow) {
    return null;
  }

  @Override
  public ITableRow getTableRowFor(ITreeNode treeNode) {
    return null;
  }

  @Override
  public List getTableRowsFor(Collection treeNodes) {
    return null;
  }

  @Override
  public Object getAdapter(Class clazz) {
    return null;
  }

  @Override
  public boolean isLazyExpandingEnabled() {
    return false;
  }

  @Override
  public void setLazyExpandingEnabled(boolean lazyAddChildPagesToOutline) {
    // NOP
  }
}
