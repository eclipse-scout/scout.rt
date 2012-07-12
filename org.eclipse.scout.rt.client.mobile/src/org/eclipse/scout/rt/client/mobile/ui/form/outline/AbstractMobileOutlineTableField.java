package org.eclipse.scout.rt.client.mobile.ui.form.outline;

import org.eclipse.scout.commons.exception.IProcessingStatus;
import org.eclipse.scout.commons.exception.ProcessingStatus;
import org.eclipse.scout.rt.client.ClientJob;
import org.eclipse.scout.rt.client.ClientSyncJob;
import org.eclipse.scout.rt.client.ui.basic.tree.ITreeNode;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.client.ui.desktop.outline.AbstractOutlineTableField;
import org.eclipse.scout.rt.client.ui.desktop.outline.IOutline;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPageWithTable;

public abstract class AbstractMobileOutlineTableField extends AbstractOutlineTableField {

  @Override
  protected int getConfiguredGridW() {
    return 2;
  }

  @Override
  protected int getConfiguredGridH() {
    return 2;
  }

  @Override
  protected boolean getConfiguredTableStatusVisible() {
    return true;
  }

  @Override
  protected void execUpdateTableStatus() {
    if (!isTableStatusVisible()) {
      return;
    }
    IOutline outline = ClientJob.getCurrentSession().getDesktop().getOutline();
    if (outline != null && outline.getActivePage() instanceof IPageWithTable<?>) {
      //popuplate status
      IPageWithTable<?> tablePage = (IPageWithTable<?>) outline.getActivePage();
      IProcessingStatus populateStatus = tablePage.getTablePopulateStatus();
      setTablePopulateStatus(populateStatus);
      //selection status
      if (tablePage.isSearchActive() && tablePage.getSearchFilter() != null && (!tablePage.getSearchFilter().isCompleted()) && tablePage.isSearchRequired()) {
        setTableSelectionStatus(null);
      }
      else if (populateStatus != null && populateStatus.getSeverity() == IProcessingStatus.WARNING) {
        setTableSelectionStatus(null);
      }
      else {
        setTableSelectionStatus(new ProcessingStatus(createDefaultTableStatus(), IProcessingStatus.INFO));
      }
    }
    else {
      setTablePopulateStatus(null);
      setTableSelectionStatus(null);
    }
  }

  @Override
  protected void execTableTitleChanged() {
    IDesktop desktop = ClientSyncJob.getCurrentSession().getDesktop();
    ITreeNode node = null;
    if (getTable() != null) {
      if (desktop.getOutline() != null) {
        node = desktop.getOutline().getSelectedNode();
      }
    }
    // set form title and form icon
    if (node != null) {
      getForm().setTitle(desktop.getOutline().getPathText(node));
      getForm().setIconId(node.getCell().getIconId());
    }
    else {
      getForm().setTitle(null);
      getForm().setIconId(null);
    }
  }

}
