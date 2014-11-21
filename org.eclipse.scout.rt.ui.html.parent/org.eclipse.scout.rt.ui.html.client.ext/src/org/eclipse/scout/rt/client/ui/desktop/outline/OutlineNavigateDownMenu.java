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
    super(outline, "Anzeigen", "Weiter"); // TODO AWE: i18n (auch oben)
  }

  @Override
  boolean isDetail(IPage5 page5) {
    return page5.isDetailFormVisible();
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
    if (page instanceof IPageWithTable<?>) {
      IPageWithTable<?> selectedTablePage = (IPageWithTable<?>) page;
      selectedRow = selectedTablePage.getTable().getSelectedRow();
      node = selectedTablePage.getTreeNodeFor(selectedRow);
      // FIXME CGU: UIFacade seems wrong...  copied from OutlineMediator
      getOutline().getUIFacade().setNodeSelectedAndExpandedFromUI(node);
    }
    else if (page instanceof IPageWithNodes) {
      IPageWithNodes pwnPage = (IPageWithNodes) page;
      selectedRow = pwnPage.getInternalTable().getSelectedRow();
      node = pwnPage.getTreeNodeFor(selectedRow);
      getOutline().getUIFacade().setNodeSelectedAndExpandedFromUI(node);
    }
    else {
      throw new IllegalStateException("Navigate down only works on table pages");
    }
  }

}
