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

import java.util.List;

import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.basic.table.TableEvent;
import org.eclipse.scout.rt.client.ui.basic.tree.ITree;
import org.eclipse.scout.rt.client.ui.basic.tree.ITreeNode;
import org.eclipse.scout.rt.client.ui.basic.tree.TreeEvent;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPage;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPageWithNodes;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPageWithTable;
import org.eclipse.scout.rt.client.ui.dnd.TransferObject;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.exception.ExceptionHandler;
import org.eclipse.scout.rt.platform.exception.PlatformError;
import org.eclipse.scout.rt.platform.util.CollectionUtility;

/**
 * @since 3.8.0
 */
public class OutlineMediator {

  public void mediateTreeNodesChanged(IPageWithNodes pageWithNodes) {
    try {
      pageWithNodes.rebuildTableInternal();
    }
    catch (RuntimeException | PlatformError e1) {
      BEANS.get(ExceptionHandler.class).handle(e1);
    }
  }

  public void mediateTreeNodeDropAction(TreeEvent e, IPageWithTable<? extends ITable> pageWithTable) {
    ITableRow row = pageWithTable.getTableRowFor(e.getNode());
    ITable table = pageWithTable.getTable();
    if (row != null) {
      table.getUIFacade().fireRowDropActionFromUI(row, e.getDropObject());
    }
  }

  public void mediateTreeNodesDragRequest(TreeEvent e, IPageWithTable<? extends ITable> pageWithTable) {
    List<ITableRow> rows = pageWithTable.getTableRowsFor(e.getNodes());
    ITable table = pageWithTable.getTable();
    table.getUIFacade().setSelectedRowsFromUI(rows);
    TransferObject t = table.getUIFacade().fireRowsDragRequestFromUI();
    if (t != null) {
      e.setDragObject(t);
    }
  }

  public void mediateTreeNodeAction(TreeEvent e, IPageWithTable<? extends ITable> pageWithTable) {
    if (e.isConsumed()) {
      return;
    }

    ITableRow row = pageWithTable.getTableRowFor(e.getNode());
    ITable table = pageWithTable.getTable();
    if (row != null) {
      e.consume();
      /*
       * ticket 78684: this line added
       */
      table.getUIFacade().setSelectedRowsFromUI(CollectionUtility.arrayList(row));
      table.getUIFacade().fireRowActionFromUI(row);
    }
  }

  public void mediateTableRowFilterChanged(IPage<?> page) {
    if (page == null || page.getTree() == null || page.isLeaf()) {
      return;
    }

    page.getTree().applyNodeFilters();
  }

  public void mediateTableRowOrderChanged(TableEvent e, IPageWithTable<?> pageWithTable) {
    if (pageWithTable == null || pageWithTable.getTree() == null || pageWithTable.isLeaf()) {
      return;
    }

    List<IPage<?>> childNodes = pageWithTable.getUpdatedChildPagesFor(e.getRows());
    if (pageWithTable.getTree() != null) {
      pageWithTable.getTree().updateChildNodeOrder(pageWithTable, childNodes);
    }
  }

  public void mediateTableRowsUpdated(TableEvent e, IPageWithTable<?> pageWithTable) {
    if (pageWithTable == null || pageWithTable.getTree() == null || pageWithTable.isLeaf()) {
      return;
    }

    List<IPage<?>> childNodes = pageWithTable.getUpdatedChildPagesFor(e.getRows());
    if (pageWithTable.getTree() != null) {
      pageWithTable.getTree().updateChildNodes(pageWithTable, childNodes);
    }
  }

  public void mediateTableRowsInserted(List<? extends ITableRow> tableRows, List<? extends IPage> childPages, IPageWithTable pageWithTable) {
    if (pageWithTable == null || pageWithTable.getTree() == null || pageWithTable.isLeaf()) {
      return;
    }

    pageWithTable.getTree().addChildNodes(pageWithTable, childPages);
  }

  public void mediateTableRowsDeleted(List<? extends IPage> childNodes, IPageWithTable pageWithTable) {
    if (pageWithTable == null || pageWithTable.getTree() == null || pageWithTable.isLeaf()) {
      return;
    }

    pageWithTable.getTree().removeChildNodes(pageWithTable, childNodes);
  }

  public void mediateTableRowAction(TableEvent e, IPage<?> page) {
    if (e.isConsumed()) {
      return;
    }
    ITreeNode node = page.getTreeNodeFor(e.getFirstRow());
    if (node != null) {
      e.consume();
      ITree tree = page.getTree();
      if (tree != null) {
        tree.getUIFacade().setNodeSelectedAndExpandedFromUI(node);
      }
    }
  }

  public void mediateTableRowDropAction(TableEvent e, IPageWithNodes pageWithNodes) {
    if (pageWithNodes == null || pageWithNodes.getTree() == null) {
      return;
    }

    ITreeNode node = pageWithNodes.getTreeNodeFor(e.getFirstRow());
    if (node != null) {
      pageWithNodes.getTree().getUIFacade().fireNodeDropActionFromUI(node, e.getDropObject());
    }
  }

}
