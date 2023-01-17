/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.desktop.outline.pages;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import org.eclipse.scout.rt.client.extension.ui.basic.tree.ITreeNodeExtension;
import org.eclipse.scout.rt.client.extension.ui.desktop.outline.pages.IPageWithNodesExtension;
import org.eclipse.scout.rt.client.extension.ui.desktop.outline.pages.PageWithNodesChains.PageWithNodesCreateChildPagesChain;
import org.eclipse.scout.rt.client.ui.action.ActionUtility;
import org.eclipse.scout.rt.client.ui.action.IAction;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.action.menu.TableMenuType;
import org.eclipse.scout.rt.client.ui.action.menu.TreeMenuType;
import org.eclipse.scout.rt.client.ui.action.menu.root.IFormFieldContextMenu;
import org.eclipse.scout.rt.client.ui.basic.cell.Cell;
import org.eclipse.scout.rt.client.ui.basic.cell.ICell;
import org.eclipse.scout.rt.client.ui.basic.table.AbstractTable;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.basic.table.TableEvent;
import org.eclipse.scout.rt.client.ui.basic.table.TableRow;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractStringColumn;
import org.eclipse.scout.rt.client.ui.basic.tree.AbstractTreeNode;
import org.eclipse.scout.rt.client.ui.basic.tree.ITree;
import org.eclipse.scout.rt.client.ui.basic.tree.ITreeNode;
import org.eclipse.scout.rt.client.ui.desktop.outline.MenuWrapper;
import org.eclipse.scout.rt.client.ui.desktop.outline.OutlineMediator;
import org.eclipse.scout.rt.client.ui.desktop.outline.OutlineMenuWrapper.IMenuTypeMapper;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.annotations.ConfigOperation;
import org.eclipse.scout.rt.platform.classid.ClassId;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.ObjectUtility;
import org.eclipse.scout.rt.platform.util.collection.OrderedCollection;

/**
 * A page containing a list of "menu" entries<br>
 * child pages are explicitly added
 * <p>
 * A NodeManagedPage normally contains no menus, actions, etc. because the TableManagedPage handles node events on
 * itself AND its children
 */
@ClassId("d33a8000-e240-4ed4-9a93-44f168ec1ab8")
public abstract class AbstractPageWithNodes extends AbstractPage<ITable> implements IPageWithNodes {

  private static final IMenuTypeMapper TREE_MENU_TYPE_MAPPER = menuType -> {
    if (menuType == TreeMenuType.SingleSelection) {
      return TableMenuType.SingleSelection;
    }
    return menuType;
  };

  public AbstractPageWithNodes() {
    this(true, null);
  }

  public AbstractPageWithNodes(boolean callInitializer) {
    this(callInitializer, null);
  }

  public AbstractPageWithNodes(String userPreferenceContext) {
    this(true, userPreferenceContext);
  }

  public AbstractPageWithNodes(boolean callInitializer, String userPreferenceContext) {
    super(callInitializer, userPreferenceContext);
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
   */
  @ConfigOperation
  @Order(90)
  protected void execCreateChildPages(List<IPage<?>> pageList) {
  }

  protected void createChildPagesInternal(final List<IPage<?>> pageList) {
    createDisplayParentRunContext()
        .run(() -> interceptCreateChildPages(pageList));
  }

  @Override
  protected ITable createTable() {
    P_Table table = null;
    table = new P_Table();
    table.setContainerInternal(this);
    table.addTableListener(
        e -> {
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
        },
        TableEvent.TYPE_ROW_ACTION,
        TableEvent.TYPE_ROW_DROP_ACTION,
        TableEvent.TYPE_ROW_FILTER_CHANGED,
        TableEvent.TYPE_ROWS_SELECTED);
    table.setAutoDiscardOnDelete(true);
    table.setReloadHandler(new PageReloadHandler(this));
    return table;
  }

  @Override
  public void cellChanged(ICell cell, int changedBit) {
    super.cellChanged(cell, changedBit);
    updateParentTableRow(cell);
  }

  /**
   * If the cell has changed (e.g. the text) we inform our parent as well. If the parent page is a
   * {@link IPageWithNodes}, update its table accordingly. Since the table {@link P_Table} has only one column, we
   * update the first column.
   *
   * @since 3.10.0-M5
   */
  protected void updateParentTableRow(ICell cell) {
    IPage<?> parent = getParentPage();
    if (parent instanceof IPageWithNodes) {
      ITableRow row = ((IPageWithNodes) parent).getTableRowFor(this);
      if (row != null) {
        row.getCellForUpdate(0).setText(cell.getText());
      }
    }
  }

  /*
   * Runtime
   */

  /**
   * Called whenever this page is selected in the outline tree.
   * <p>
   * Subclasses can override this method.<br/>
   * This implementation sets the title of the internal table used by this page to the path from the root node to this
   * page.
   */
  @Override
  protected void execPageActivated() {
    super.execPageActivated();
    // set title of table
    if (getTable() != null && getTree() != null) {
      getTable().setTitle(getTree().getPathText(AbstractPageWithNodes.this));
    }
  }

  /**
   * load tree children and fill table
   */
  @Override
  protected void loadChildrenImpl() {
    List<IPage<?>> pageList = new ArrayList<>();
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
      setChildrenLoaded(false);
      fireBeforeDataLoaded();
      try {
        if (tree != null) {
          tree.removeAllChildNodes(this);
          tree.addChildNodes(this, pageList);
        }
      }
      finally {
        fireAfterDataLoaded();
      }
      setChildrenLoaded(true);
      setChildrenDirty(false);
      // table events will handle automatic tree changes in case table is mirrored in tree.
      // restore currently selected tree node when it was owned by our table rows.
      // in case selection was lost, try to select similar index as before
      if (tree != null && oldSelectionOwned && tree.getSelectedNode() == null) {
        if (oldSelectedNode != null && oldSelectedNode.getTree() == tree) { // NOSONAR
          tree.selectNode(oldSelectedNode);
        }
        else {
          int index = Math.max(-1, Math.min(oldSelectionDirectChildIndex, getChildNodeCount() - 1));
          if (index >= 0 && index < getChildNodeCount() && ObjectUtility.equals(oldSelectedText, getChildNode(index).getCell().getText())) {
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
      getTable().setTableChanging(true);
      rebuildTableInternal();
      setTableStatus(null);
    }
    finally {
      getTable().setTableChanging(false);
    }
  }

  @Override
  public void rebuildTableInternal() {
    List<ITreeNode> childNodes = getChildNodes();
    try {
      getTable().setTableChanging(true);
      //
      unlinkAllTableRowWithPage();
      getTable().discardAllRows();
      for (ITreeNode childNode : childNodes) {
        ITableRow row = new TableRow(getTable().getColumnSet());
        updateCellFromPageCell(row.getCellForUpdate(0), childNode.getCell());
        ITableRow insertedRow = getTable().addRow(row);
        linkTableRowWithPage(insertedRow, (IPage) childNode);
      }
    }
    finally {
      getTable().setTableChanging(false);
    }
  }

  /**
   * Called when the table gets rebuilt.
   * <p>
   * Updates the cell belonging to the newly created table row with the content of the page cell. row.
   */
  protected void updateCellFromPageCell(Cell tableRowCell, ICell pageCell) {
    tableRowCell.updateFrom(pageCell);
    // ensure a value is set
    tableRowCell.setValue(pageCell.getText());
  }

  private List<? extends IMenu> m_pageMenusOfSelection;

  protected void updateContextMenusForSelection() {
    // remove old
    if (m_pageMenusOfSelection != null) {
      getTable().getContextMenu().removeChildActions(m_pageMenusOfSelection);
      m_pageMenusOfSelection = null;
    }

    List<IMenu> pageMenus = new ArrayList<>();
    List<ITableRow> selectedRows = getTable().getSelectedRows();
    if (CollectionUtility.size(selectedRows) == 1) {
      ITreeNode node = getTreeNodeFor(CollectionUtility.firstElement(selectedRows));
      if (node instanceof IPageWithNodes) {
        IPageWithNodes pageWithNodes = (IPageWithNodes) node;
        Predicate<IAction> filter = ActionUtility.createMenuFilterMenuTypes(CollectionUtility.hashSet(TreeMenuType.SingleSelection), false);
        List<IMenu> menus = ActionUtility.getActions(pageWithNodes.getMenus(), filter);
        for (IMenu m : menus) {
          pageMenus.add(MenuWrapper.wrapMenu(m, TREE_MENU_TYPE_MAPPER, filter));
        }
      }
    }
    getTable().getContextMenu().addChildActions(pageMenus);
    m_pageMenusOfSelection = pageMenus;
  }

  @Override
  protected void decorateDetailForm() {
    super.decorateDetailForm();
    enhanceDetailFormWithPageMenus();
  }

  protected void enhanceDetailFormWithPageMenus() {
    if (getOutline() == null) {
      return;
    }

    IForm form = getDetailForm();
    IFormFieldContextMenu mainBoxContextMenu = form.getRootGroupBox().getContextMenu();
    List<IMenu> menus = mainBoxContextMenu.getChildActions();

    for (IMenu menu : getOutline().getMenusForPage(this)) {
      if (!acceptPageMenuForDetailForm(menu, form)) {
        continue;
      }
      menus.add(MenuWrapper.wrapMenuIfNotWrapped(menu));
    }
    if (!CollectionUtility.equalsCollection(menus, mainBoxContextMenu.getChildActions())) {
      mainBoxContextMenu.setChildActions(menus);
    }
  }

  protected boolean acceptPageMenuForDetailForm(IMenu menu, IForm form) {
    if (menu.getMenuTypes().contains(TreeMenuType.Header) && menu.getMenuTypes().size() == 1) {
      // Don't show TreeMenuType.Header. These menus should only be shown on outline title
      return false;
    }
    return true;
  }

  /**
   * inner table
   */
  @ClassId("d657e4bf-e2eb-44ed-9a9b-898db24ff408")
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

    @Override
    protected boolean getConfiguredTableStatusVisible() {
      return true;
    }

    @Override
    protected boolean getConfiguredHeaderVisible() {
      return false;
    }

    @Override
    protected void addHeaderMenus(OrderedCollection<IMenu> menus) {
      // header is invisible -> not necessary to add menus
    }

    @SuppressWarnings("unused")
    public LabelColumn getLabelColumn() {
      return getColumnSet().getColumnByClass(LabelColumn.class);
    }

    @Order(1)
    @ClassId("d30cb908-98b8-4b46-ba5f-62354a9eb969")
    public class LabelColumn extends AbstractStringColumn {

      @Override
      protected void decorateCellInternal(Cell cell, ITableRow row) {
        // if we encounter a cell change, update the tree as well
        IPage<?> page = (IPage) getTreeNodeFor(row);
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

  protected final void interceptCreateChildPages(List<IPage<?>> pageList) {
    List<? extends ITreeNodeExtension<? extends AbstractTreeNode>> extensions = getAllExtensions();
    PageWithNodesCreateChildPagesChain chain = new PageWithNodesCreateChildPagesChain(extensions);
    chain.execCreateChildPages(pageList);
  }

  protected static class LocalPageWithNodesExtension<OWNER extends AbstractPageWithNodes> extends LocalPageExtension<OWNER> implements IPageWithNodesExtension<OWNER> {

    public LocalPageWithNodesExtension(OWNER owner) {
      super(owner);
    }

    @Override
    public void execCreateChildPages(PageWithNodesCreateChildPagesChain chain, List<IPage<?>> pageList) {
      getOwner().execCreateChildPages(pageList);
    }
  }

  @Override
  protected IPageWithNodesExtension<? extends AbstractPageWithNodes> createLocalExtension() {
    return new LocalPageWithNodesExtension<>(this);
  }

}
