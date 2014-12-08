package org.eclipse.scout.rt.client.ui.desktop.outline;

import org.eclipse.scout.rt.client.ui.action.menu.IMenuType;
import org.eclipse.scout.rt.client.ui.action.menu.TableMenuType;
import org.eclipse.scout.rt.client.ui.basic.tree.ITreeNode;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPage;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPage5;
import org.eclipse.scout.rt.client.ui.form.fields.ModelVariant;

/**
 * When the user presses the "navigate up" button, we want to display either the detail table or the detail form
 * depending on the current state of the node. But when the user clicks on a node we _always_ display the detail
 * form (if one exists). So the behavior is slightly different in these two cases.
 */
@ModelVariant("NavigateUp")
public class OutlineNavigateUpMenu extends AbstractOutlineNavigationMenu {

  public OutlineNavigateUpMenu(IOutline outline) {
    super(outline, "Up", "Back");
  }

  @Override
  protected boolean isDetail(IPage5 page5) {
    return !page5.isDetailFormVisible();
  }

  @Override
  protected void showDetail(IPage page) {
    ((IPage5) page).setDetailFormVisible(true);
  }

  @Override
  protected IMenuType getMenuType() {
    return TableMenuType.EmptySpace;
  }

  @Override
  protected void doDrill(IPage page) {
    ITreeNode parentNode = page.getParentNode();
    if (parentNode instanceof IPage5) {
      ((IPage5) parentNode).setNavigateUp();
    }
    getOutline().selectNode(parentNode);
    parentNode.setExpanded(false);
  }

}
