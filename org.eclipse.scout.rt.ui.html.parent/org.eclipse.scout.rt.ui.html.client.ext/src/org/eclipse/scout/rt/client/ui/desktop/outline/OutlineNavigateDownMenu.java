package org.eclipse.scout.rt.client.ui.desktop.outline;

import java.util.Set;

import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ui.action.menu.IMenuType;
import org.eclipse.scout.rt.client.ui.action.menu.TableMenuType;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.basic.tree.ITreeNode;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPage;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPageWithTable;
import org.eclipse.scout.rt.shared.ui.menu.AbstractMenu5;

public class OutlineNavigateDownMenu extends AbstractMenu5 {
  private IOutline m_outline;

  public OutlineNavigateDownMenu(IOutline outline) {
    super(false);
    m_outline = outline;
    callInitializer();
  }

  @Override
  protected String getConfiguredText() {
    return "Anzeigen"; //FIXME CGU translation
  }

  @Override
  protected Set<? extends IMenuType> getConfiguredMenuTypes() {
    return CollectionUtility.hashSet(TableMenuType.SingleSelection);
  }

  //FIXME CGU maybe do this in gui to make it more responsive?
  @Override
  protected void execAction() throws ProcessingException {
    ITreeNode node = null;
    ITableRow selectedRow = null;

    IPage selectedPage = (IPage) m_outline.getSelectedNode();
    if (selectedPage instanceof IPageWithTable<?>) {
      IPageWithTable<?> selectedTablePage = ((IPageWithTable<?>) selectedPage);
      selectedRow = selectedTablePage.getTable().getSelectedRow();
      node = selectedTablePage.getTreeNodeFor(selectedRow);

      //FIXME UIFacade seems wrong...  copied from OutlineMediator
      m_outline.getUIFacade().setNodeSelectedAndExpandedFromUI(node);
    }
    else {
      throw new IllegalStateException("Navigate down only works on table pages");
    }
  }
}
