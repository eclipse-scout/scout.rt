package org.eclipse.scout.rt.client.ui.desktop.outline;

import org.eclipse.scout.rt.client.ui.action.menu.IMenuType;
import org.eclipse.scout.rt.client.ui.action.menu.TableMenuType;
import org.eclipse.scout.rt.client.ui.basic.tree.ITreeNode;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPage;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPage5;

public class OutlineNavigateUpMenu extends AbstractOutlineNavigationMenu {

  public OutlineNavigateUpMenu(IOutline outline) {
    super(outline, "Up", "Back");
  }

  @Override
  boolean isDetail(IPage5 page5) {
    return !page5.isDetailFormVisible();
  }

  @Override
  void showDetail(IPage page) {
    ((IPage5) page).setDetailFormVisible(true);
  }

  @Override
  IMenuType getMenuType() {
    return TableMenuType.EmptySpace;
  }

  @Override
  protected void doDrill(IPage page) {
    ITreeNode parentNode = page.getParentNode();
    getOutline().selectNode(parentNode);
    parentNode.setExpanded(false);
  }
}
