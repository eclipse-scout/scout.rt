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
import java.util.List;

import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.commons.CompareUtility;
import org.eclipse.scout.commons.annotations.ConfigOperation;
import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.exception.IProcessingStatus;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ui.action.ActionUtility;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.action.menu.TableMenuType;
import org.eclipse.scout.rt.client.ui.action.menu.TreeMenuType;
import org.eclipse.scout.rt.client.ui.basic.cell.Cell;
import org.eclipse.scout.rt.client.ui.basic.cell.ICell;
import org.eclipse.scout.rt.client.ui.basic.table.AbstractTable;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.basic.table.TableAdapter;
import org.eclipse.scout.rt.client.ui.basic.table.TableEvent;
import org.eclipse.scout.rt.client.ui.basic.table.TableRow;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractStringColumn;
import org.eclipse.scout.rt.client.ui.basic.table.internal.TablePageTreeMenuWrapper;
import org.eclipse.scout.rt.client.ui.basic.tree.ITree;
import org.eclipse.scout.rt.client.ui.basic.tree.ITreeNode;
import org.eclipse.scout.rt.client.ui.desktop.outline.OutlineMediator;
import org.eclipse.scout.rt.shared.ScoutTexts;

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

  public AbstractPageWithNodes() {
    this(true, null, null);
  }

  public AbstractPageWithNodes(boolean callInitializer) {
    this(callInitializer, null, null);
  }

  /**
   * @deprecated Will be removed in the 6.0 Release.
   *             Use {@link #AbstractPageWithNodes()} in combination with getter and setter on the page
   *             instead.
   */
  @Deprecated
  @SuppressWarnings("deprecation")
  public AbstractPageWithNodes(org.eclipse.scout.rt.shared.ContextMap contextMap) {
    this(true, contextMap, null);
  }

  public AbstractPageWithNodes(String userPreferenceContext) {
    this(true, null, userPreferenceContext);
  }

  public AbstractPageWithNodes(boolean callInitializer, String userPreferenceContext) {
    this(callInitializer, null, userPreferenceContext);
  }

  /**
   * @deprecated Will be removed in the 6.0 Release.
   *             Use {@link #AbstractPageWithNodes(boolean, String)} in combination with getter and setter on the page
   *             instead.
   */
  @Deprecated
  @SuppressWarnings("deprecation")
  public AbstractPageWithNodes(boolean callInitializer, org.eclipse.scout.rt.shared.ContextMap contextMap, String userPreferenceContext) {
    super(callInitializer, contextMap, userPreferenceContext);
  }

  /*
   * Configuration
   */

  /**
   * Called by {@link #loadChildren()} to load data for this page. Allows to add multiple child pages to this page.
   * <p>
   * Subclasses can override this method. The default does nothing.
   * 
   * @param pageList
   *          live collection to add child pages to this page
   * @throws ProcessingException
   */
  @ConfigOperation
  @Order(90)
  protected void execCreateChildPages(Collection<IPage> pageList) throws ProcessingException {
  }

  protected void createChildPagesInternal(Collection<IPage> pageList) throws ProcessingException {
    execCreateChildPages(pageList);
  }

  @Override
  protected void initConfig() {
    super.initConfig();
    m_table = new P_Table();
    if (m_table instanceof AbstractTable) {
      ((AbstractTable) m_table).setContainerInternal(this);
    }
    m_table.addTableListener(new P_TableListener());
    m_table.setAutoDiscardOnDelete(true);
    try {
      m_table.initTable();
    }
    catch (Exception e) {
      LOG.warn(null, e);
    }
  }

  @Override
  public void cellChanged(ICell cell, int changedBit) {
    super.cellChanged(cell, changedBit);
    updateParentTableRow(cell);
  }

  /**
   * If the cell has changed (e.g. the text) we inform our parent as well.
   * If the parent page is a {@link IPageWithNodes}, update its table accordingly.
   * Since the table {@link P_Table} has only one column, we update the first column.
   * 
   * @since 3.10.0-M5
   */
  protected void updateParentTableRow(ICell cell) {
    IPage parent = getParentPage();
    if (parent != null && parent instanceof IPageWithNodes) {
      ITableRow row = ((IPageWithNodes) parent).getTableRowFor(this);
      if (row != null) {
        row.getCellForUpdate(0).setText(cell.getText());
      }
    }
  }

  /*
   * Runtime
   */

  @Override
  public ITable getInternalTable() {
    return m_table;
  }

  @Override
  public void setPagePopulateStatus(IProcessingStatus status) {
    super.setPagePopulateStatus(status);
    getInternalTable().tablePopulated();
  }

  /**
   * Called whenever this page is selected in the outline tree.
   * <p>
   * Subclasses can override this method.<br/>
   * This implementation sets the title of the internal table used by this page to the path from the root node to this
   * page.
   * 
   * @throws ProcessingException
   */
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
    createChildPagesInternal(pageList);
    // load tree
    ITree tree = getTree();
    try {
      if (tree != null) {
        tree.setTreeChanging(true);
      }
      //
      // backup currently selected tree node and its path to root
      boolean oldSelectionOwned = false;
      int oldSelectionDirectChildIndex = -1;
      ITreeNode oldSelectedNode = null;
      if (tree != null) {
        oldSelectedNode = tree.getSelectedNode();
      }
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
      getTree().addChildNodes(this, pageList);
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
      if (tree != null) {
        tree.setTreeChanging(false);
      }
    }
    // copy to table
    try {
      getInternalTable().setTableChanging(true);
      rebuildTableInternal();
      setPagePopulateStatus(null);
    }
    finally {
      getInternalTable().setTableChanging(false);
    }
    super.loadChildren();
  }

  @Override
  public void rebuildTableInternal() throws ProcessingException {
    List<ITreeNode> childNodes = getChildNodes();
    try {
      getInternalTable().setTableChanging(true);
      //
      unlinkAllTableRowWithPage();
      getInternalTable().discardAllRows();
      for (ITreeNode childNode : childNodes) {
        ITableRow row = new TableRow(getInternalTable().getColumnSet());
        row.setCell(0, childNode.getCell());
        ITableRow insertedRow = getInternalTable().addRow(row);
        linkTableRowWithPage(insertedRow, (IPage) childNode);
      }
    }
    finally {
      getInternalTable().setTableChanging(false);
    }
  }

  @Override
  public boolean isFilterAcceptedForChildNode(ITreeNode childPageNode) {
    return m_pageToTableRowMap.get(childPageNode) == null || m_pageToTableRowMap.get(childPageNode).isFilterAccepted();
  }

  private void linkTableRowWithPage(ITableRow tableRow, IPage page) {
    m_tableRowToPageMap.put(tableRow, page);
    m_pageToTableRowMap.put(page, tableRow);
  }

  private void unlinkAllTableRowWithPage() {
    m_tableRowToPageMap.clear();
    m_pageToTableRowMap.clear();
  }

  @Override
  public ITreeNode getTreeNodeFor(ITableRow tableRow) {
    if (tableRow == null) {
      return null;
    }
    else {
      return m_tableRowToPageMap.get(tableRow);
    }
  }

  @Override
  public ITableRow getTableRowFor(ITreeNode childPageNode) {
    return m_pageToTableRowMap.get(childPageNode);
  }

  private List<? extends IMenu> m_pageMenusOfSelection;

  /**
   *
   */
  protected void updateContextMenusForSelection() {
    // remove old
    if (m_pageMenusOfSelection != null) {
      getInternalTable().getContextMenu().removeChildActions(m_pageMenusOfSelection);
      m_pageMenusOfSelection = null;
    }

    List<IMenu> pageMenus = new ArrayList<IMenu>();
    List<ITableRow> selectedRows = getInternalTable().getSelectedRows();
    if (CollectionUtility.size(selectedRows) == 1) {
      ITreeNode node = getTreeNodeFor(CollectionUtility.firstElement(selectedRows));
      if (node instanceof IPageWithTable<?>) {
        IPageWithTable<?> tablePage = (IPageWithTable<?>) node;
        List<IMenu> menus = ActionUtility.getActions(tablePage.getTable().getContextMenu().getChildActions(), ActionUtility.createMenuFilterMenuTypes(TableMenuType.EmptySpace));
        for (IMenu m : menus) {
          pageMenus.add(new TablePageTreeMenuWrapper(m, TableMenuType.SingleSelection));
        }
      }
      else if (node instanceof IPageWithNodes) {
        IPageWithNodes pageWithNodes = (IPageWithNodes) node;
        List<IMenu> menus = ActionUtility.getActions(pageWithNodes.getTree().getContextMenu().getChildActions(), ActionUtility.createMenuFilterMenuTypes(TreeMenuType.SingleSelection));
        for (IMenu m : menus) {
          pageMenus.add(new TablePageTreeMenuWrapper(m, TableMenuType.SingleSelection));
        }
      }
    }
    getInternalTable().getContextMenu().addChildActions(pageMenus);
    m_pageMenusOfSelection = pageMenus;

  }

  /**
   * inner table
   */
  private class P_Table extends AbstractTable {

    @Override
    protected void execRowsSelected(List<? extends ITableRow> rows) throws ProcessingException {
      super.execRowsSelected(rows);
    }

    @Override
    protected boolean getConfiguredSortEnabled() {
      return false;
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
        //if we encounter a cell change, update the tree as well
        IPage page = m_tableRowToPageMap.get(row);
        if (page != null) {
          page.getCellForUpdate().setText(cell.getText());
        }
      }

      @Override
      protected int getConfiguredWidth() {
        return 200;
      }
    }
  }

  private OutlineMediator getOutlineMediator() {
    if (getOutline() == null) {
      return null;
    }

    return getOutline().getOutlineMediator();
  }

  /**
   * Table listener for delegation of actions to tree
   */
  private class P_TableListener extends TableAdapter {
    @Override
    public void tableChanged(TableEvent e) {
      OutlineMediator outlineMediator = getOutlineMediator();
      if (outlineMediator == null) {
        return;
      }

      switch (e.getType()) {
        case TableEvent.TYPE_ROW_ACTION: {
          outlineMediator.mediateTableRowAction(e, AbstractPageWithNodes.this);
          break;
        }
        case TableEvent.TYPE_ROW_DROP_ACTION: {
          outlineMediator.mediateTableRowDropAction(e, AbstractPageWithNodes.this);
          break;
        }
        case TableEvent.TYPE_ROW_FILTER_CHANGED: {
          outlineMediator.mediateTableRowFilterChanged(AbstractPageWithNodes.this);
          break;
        }
        case TableEvent.TYPE_ROWS_SELECTED: {
          updateContextMenusForSelection();
          break;
        }
      }

    }
  }

}
