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
import org.eclipse.scout.commons.exception.IProcessingStatus;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
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
import org.eclipse.scout.rt.client.ui.desktop.outline.OutlineMediator;
import org.eclipse.scout.rt.shared.ContextMap;
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

  public AbstractPageWithNodes(ContextMap contextMap) {
    this(true, contextMap, null);
  }

  public AbstractPageWithNodes(String userPreferenceContext) {
    this(true, null, userPreferenceContext);
  }

  public AbstractPageWithNodes(boolean callInitializer, String userPreferenceContext) {
    this(callInitializer, null, userPreferenceContext);
  }

  public AbstractPageWithNodes(boolean callInitializer, ContextMap contextMap, String userPreferenceContext) {
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
    IPage[] pages = pageList.toArray(new IPage[pageList.size()]);
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
  }

  @Override
  public void rebuildTableInternal() throws ProcessingException {
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
        // Cells were already decorated by the tree, where they are taken from.
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
        case TableEvent.TYPE_ROW_POPUP: {
          outlineMediator.mediateTableRowPopup(e, AbstractPageWithNodes.this);
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
      }

    }

  }
}
