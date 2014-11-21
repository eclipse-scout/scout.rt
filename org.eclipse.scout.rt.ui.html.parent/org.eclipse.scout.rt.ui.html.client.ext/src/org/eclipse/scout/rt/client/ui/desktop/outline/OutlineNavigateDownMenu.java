package org.eclipse.scout.rt.client.ui.desktop.outline;

import org.eclipse.scout.rt.client.ui.action.menu.IMenuType;
import org.eclipse.scout.rt.client.ui.action.menu.TableMenuType;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.basic.tree.ITreeNode;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPage;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPage5;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPageWithNodes;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPageWithTable;

public class OutlineNavigateDownMenu extends AbstractOutlineNavigationMenu {

  public OutlineNavigateDownMenu(IOutline outline) {
    super(outline, "Show", "Continue");
  }

  @Override
  boolean isDetail(IPage5 page5) {
    return page5.isDetailFormVisible();
  }

  @Override
  public boolean isDefault() {
    return true;
  }

  @Override
  void showDetail(IPage page) {
    ((IPage5) page).setDetailFormVisible(false);
  }

  @Override
  IMenuType getMenuType() {
    return TableMenuType.SingleSelection;
  }

  @Override
  protected void doDrill(IPage page) {
    ITreeNode node;
    ITableRow selectedRow;
    // TODO AWE: (scout) find common interface for pages with tables, currently the methods get(Internal)Table and getTreeNodeFor()
    // are on IPageWithTable and IPageWithNodes but the have no common super class. That's why we must duplicate code here.
    // FIXME CGU: UIFacade seems wrong...  copied from OutlineMediator
    if (page instanceof IPageWithTable<?>) {
      IPageWithTable<?> pageWt = (IPageWithTable<?>) page;
      selectedRow = pageWt.getTable().getSelectedRow();
      node = pageWt.getTreeNodeFor(selectedRow);
      getOutline().getUIFacade().setNodeSelectedAndExpandedFromUI(node);
    }
    else if (page instanceof IPageWithNodes) {
      IPageWithNodes pageWn = (IPageWithNodes) page;
      selectedRow = pageWn.getInternalTable().getSelectedRow();
      node = pageWn.getTreeNodeFor(selectedRow);
      getOutline().getUIFacade().setNodeSelectedAndExpandedFromUI(node);
    }
    else {
      throw new IllegalStateException("Navigate down only works on pages with (internal) table");
    }
  }

}
