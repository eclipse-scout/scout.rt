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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import org.eclipse.scout.commons.CompareUtility;
import org.eclipse.scout.commons.annotations.ConfigOperation;
import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.basic.cell.Cell;
import org.eclipse.scout.rt.client.ui.basic.table.AbstractTable;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.basic.table.TableAdapter;
import org.eclipse.scout.rt.client.ui.basic.table.TableEvent;
import org.eclipse.scout.rt.client.ui.basic.table.TableRow;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractStringColumn;
import org.eclipse.scout.rt.client.ui.basic.tree.ITree;
import org.eclipse.scout.rt.client.ui.basic.tree.ITreeNode;
import org.eclipse.scout.rt.client.ui.basic.tree.TreeAdapter;
import org.eclipse.scout.rt.client.ui.basic.tree.TreeEvent;
import org.eclipse.scout.rt.shared.ContextMap;
import org.eclipse.scout.rt.shared.ScoutTexts;
import org.eclipse.scout.rt.shared.services.common.exceptionhandler.IExceptionHandlerService;
import org.eclipse.scout.service.SERVICES;

/**
 * A page containing a list of "menu" entries<br>
 * child pages are explicitly added
 * <p>
 * A NodeManagedPage normally contains no menus, actions, etc. because the TableManagedPage handles node events on
 * itself AND its children
 */
public abstract class AbstractPageWithNodes extends AbstractPage implements IPageWithNodes {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(AbstractPageWithNodes.class);

  private P_Table m_table;
  private final HashMap<ITableRow, IPage> m_tableRowToPageMap = new HashMap<ITableRow, IPage>();
  private final HashMap<IPage, ITableRow> m_pageToTableRowMap = new HashMap<IPage, ITableRow>();
  private P_ChildNodeListener m_childNodeListener;

  public AbstractPageWithNodes() {
    this(true, null, null);
  }

  public AbstractPageWithNodes(boolean callInitializer) {
    this(callInitializer, null, null);
  }

  public AbstractPageWithNodes(ContextMap contextMap) {
    this(true, contextMap, null);
  }

  public AbstractPageWithNodes(String userPreferenceContext) {
    this(true, null, userPreferenceContext);
  }

  public AbstractPageWithNodes(boolean callInitializer, ContextMap contextMap, String userPreferenceContext) {
    super(callInitializer, contextMap, userPreferenceContext);
  }

  /*
   * Configuration
   */
  @ConfigOperation
  @Order(90)
  protected void execCreateChildPages(Collection<IPage> pageList) throws ProcessingException {
  }

  @Override
  protected void initConfig() {
    super.initConfig();
    m_table = new P_Table();
    m_table.addTableListener(new P_TableListener());
    m_table.setAutoDiscardOnDelete(true);
    try {
      m_table.initTable();
    }
    catch (Exception e) {
      LOG.warn(null, e);
    }
  }

  /*
   * Runtime
   */

  /**
   * override to add/remove local tree listener
   */
  @Override
  public void setTreeInternal(ITree tree, boolean includeSubtree) {
    if (getTree() != null && m_childNodeListener != null) {
      getTree().removeTreeListener(m_childNodeListener);
      m_childNodeListener = null;
    }
    super.setTreeInternal(tree, includeSubtree);
    if (getTree() != null) {
      m_childNodeListener = new P_ChildNodeListener();
      getTree().addTreeListener(m_childNodeListener);
    }
  }

  public ITable getInternalTable() {
    return m_table;
  }

  @Override
  protected void execPageActivated() throws ProcessingException {
    super.execPageActivated();
    // set title of table
    if (getInternalTable() != null && getTree() != null) {
      getInternalTable().setTitle(getTree().getPathText(AbstractPageWithNodes.this));
    }
  }

  /**
   * load tree children and fill table
   */
  @Override
  public void loadChildren() throws ProcessingException {
    ArrayList<IPage> pageList = new ArrayList<IPage>();
    execCreateChildPages(pageList);
    IPage[] pages = pageList.toArray(new IPage[pageList.size()]);
    // load tree
    ITree tree = getTree();
    try {
      if (tree != null) tree.setTreeChanging(true);
      //
      // backup currently selected tree node and its path to root
      boolean oldSelectionOwned = false;
      int oldSelectionDirectChildIndex = -1;
      ITreeNode oldSelectedNode = null;
      if (tree != null) oldSelectedNode = tree.getSelectedNode();
      String oldSelectedText = null;
      if (oldSelectedNode != null) {
        ITreeNode t = oldSelectedNode;
        while (t != null && t.getParentNode() != null) {
          if (t.getParentNode() == this) {
            oldSelectionOwned = true;
            oldSelectedText = t.getCell().getText();
            oldSelectionDirectChildIndex = t.getChildNodeIndex();
            break;
          }
          t = t.getParentNode();
        }
      }
      //
      setChildrenLoaded(false);
      //
      getTree().removeAllChildNodes(this);
      getTree().addChildNodes(this, pages);
      //
      setChildrenLoaded(true);
      setChildrenDirty(false);
      // table events will handle automatic tree changes in case table is mirrored in tree.
      // restore currently selected tree node when it was owned by our table rows.
      // in case selection was lost, try to select similar index as before
      if (tree != null && oldSelectionOwned && tree.getSelectedNode() == null) {
        if (oldSelectedNode != null && oldSelectedNode.getTree() == tree) {
          tree.selectNode(oldSelectedNode);
        }
        else {
          int index = Math.max(-1, Math.min(oldSelectionDirectChildIndex, getChildNodeCount() - 1));
          if (index >= 0 && index < getChildNodeCount() && CompareUtility.equals(oldSelectedText, getChildNode(index).getCell().getText())) {
            tree.selectNode(getChildNode(index));
          }
          else if (index >= 0 && index < getChildNodeCount()) {
            tree.selectNode(getChildNode(index));
          }
          else {
            tree.selectNode(this);
          }
        }
      }
    }
    finally {
      if (tree != null) tree.setTreeChanging(false);
    }
    // copy to table
    rebuildTable();
  }

  private void rebuildTable() throws ProcessingException {
    ITreeNode[] childNodes = getChildNodes();
    try {
      getInternalTable().setTableChanging(true);
      //
      unlinkAllTableRowWithPage();
      getInternalTable().discardAllRows();
      for (int i = 0; i < childNodes.length; i++) {
        ITableRow row = new TableRow(getInternalTable().getColumnSet());
        row.setCell(0, childNodes[i].getCell());
        ITableRow insertedRow = getInternalTable().addRow(row);
        linkTableRowWithPage(insertedRow, (IPage) childNodes[i]);
      }
    }
    finally {
      getInternalTable().setTableChanging(false);
    }
  }

  private void linkTableRowWithPage(ITableRow tableRow, IPage page) {
    m_tableRowToPageMap.put(tableRow, page);
    m_pageToTableRowMap.put(page, tableRow);
  }

  private void unlinkAllTableRowWithPage() {
    m_tableRowToPageMap.clear();
    m_pageToTableRowMap.clear();
  }

  private ITreeNode getTreeNodeFor(ITableRow tableRow) {
    if (tableRow == null) return null;
    else return m_tableRowToPageMap.get(tableRow);
  }

  private ITreeNode[] getTreeNodesFor(ITableRow[] tableRows) {
    ITreeNode[] nodes = new ITreeNode[tableRows.length];
    int missingCount = 0;
    for (int i = 0; i < tableRows.length; i++) {
      nodes[i] = m_tableRowToPageMap.get(tableRows[i]);
      if (nodes[i] == null) {
        missingCount++;
      }
    }
    if (missingCount > 0) {
      ITreeNode[] tmp = new ITreeNode[nodes.length - missingCount];
      int index = 0;
      for (int i = 0; i < nodes.length; i++) {
        if (nodes[i] != null) {
          tmp[index] = nodes[i];
          index++;
        }
      }
      nodes = tmp;
    }
    return nodes;
  }

  /**
   * inner table
   */
  private class P_Table extends AbstractTable {

    // disable sorting
    @Override
    public void sort() {
    }

    @Override
    protected boolean getConfiguredAutoResizeColumns() {
      return true;
    }

    @Override
    protected boolean getConfiguredMultiSelect() {
      return false;
    }

    public LabelColumn getLabelColumn() {
      return getColumnSet().getColumnByClass(LabelColumn.class);
    }

    @Order(1)
    public class LabelColumn extends AbstractStringColumn {
      @Override
      protected String getConfiguredHeaderText() {
        return ScoutTexts.get("Folders");
      }

      @Override
      protected void decorateCellInternal(Cell cell, ITableRow row) {
        // Cells were already decorated by the tree, where they are taken from.
      }

      @Override
      protected int getConfiguredWidth() {
        return 800;
      }
    }
  }

  /**
   * Table listener for delegation of actions to tree
   */
  private class P_TableListener extends TableAdapter {
    @Override
    public void tableChanged(TableEvent e) {
      switch (e.getType()) {
        case TableEvent.TYPE_ROW_ACTION: {
          if (!e.isConsumed()) {
            ITreeNode node = getTreeNodeFor(e.getFirstRow());
            if (node != null) {
              e.consume();
              getTree().getUIFacade().setNodeSelectedAndExpandedFromUI(node);
            }
          }
          break;
        }
        case TableEvent.TYPE_ROW_POPUP: {
          ITreeNode node = getTreeNodeFor(e.getFirstRow());
          if (node instanceof IPageWithTable<?>) {
            try {
              node.ensureChildrenLoaded();
              IPageWithTable<?> tablePage = (IPageWithTable<?>) node;
              IMenu[] menus = tablePage.getTable().getUIFacade().fireEmptySpacePopupFromUI();
              if (menus != null) {
                e.addPopupMenus(menus);
              }
            }
            catch (ProcessingException ex) {
              SERVICES.getService(IExceptionHandlerService.class).handleException(ex);
            }
          }
          break;
        }
          /*
           * case TableEvent.TYPE_ROWS_DRAG_REQUEST:{ ITreeNode[]
           * nodes=getTreeNodesFor(e.getRows()); if(nodes.length>0){
           * getTree().getUIFacade().setNodesSelectedFromUI(nodes);
           * TransferObject
           * t=getTree().getUIFacade().fireNodesDragRequestFromUI();
           * if(t!=null){ e.setDragObject(t); } } break; }
           */
        case TableEvent.TYPE_ROW_DROP_ACTION: {
          ITreeNode node = getTreeNodeFor(e.getFirstRow());
          if (node != null) {
            getTree().getUIFacade().fireNodeDropActionFromUI(node, e.getDropObject());
          }
          break;
        }
      }// end switch
    }
  }

  /**
   * Tree listener on children in order to delegate changes to table rows
   */
  private class P_ChildNodeListener extends TreeAdapter {
    @Override
    public void treeChanged(TreeEvent e) {
      switch (e.getType()) {
        case TreeEvent.TYPE_CHILD_NODE_ORDER_CHANGED:
        case TreeEvent.TYPE_NODES_DELETED:
        case TreeEvent.TYPE_NODES_INSERTED:
        case TreeEvent.TYPE_NODES_UPDATED: {
          try {
            rebuildTable();
          }
          catch (ProcessingException e1) {
            SERVICES.getService(IExceptionHandlerService.class).handleException(e1);
          }
          break;
        }
      }// end switch
    }
  }
}
