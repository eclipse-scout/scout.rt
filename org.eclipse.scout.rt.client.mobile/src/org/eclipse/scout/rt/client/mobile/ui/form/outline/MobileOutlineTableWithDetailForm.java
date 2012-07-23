//package org.eclipse.scout.rt.client.mobile.ui.form.outline;
//
//import java.util.List;
//
//import org.eclipse.scout.commons.annotations.InjectFieldTo;
//import org.eclipse.scout.commons.annotations.Order;
//import org.eclipse.scout.commons.exception.ProcessingException;
//import org.eclipse.scout.commons.logger.IScoutLogger;
//import org.eclipse.scout.commons.logger.ScoutLogManager;
//import org.eclipse.scout.rt.client.mobile.ui.form.outline.MobileOutlineTableWithDetailForm.GroupBox.OutlineTableField;
//import org.eclipse.scout.rt.client.ui.basic.table.ITable;
//import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
//import org.eclipse.scout.rt.client.ui.basic.table.TableAdapter;
//import org.eclipse.scout.rt.client.ui.basic.table.TableEvent;
//import org.eclipse.scout.rt.client.ui.basic.tree.ITreeNode;
//import org.eclipse.scout.rt.client.ui.desktop.outline.IOutline;
//import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPage;
//import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPageWithNodes;
//import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPageWithTable;
//import org.eclipse.scout.rt.client.ui.form.IForm;
//import org.eclipse.scout.rt.client.ui.form.fields.button.IButton;
//import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
//import org.eclipse.scout.rt.client.ui.form.fields.wrappedform.AbstractWrappedFormField;
//
//public class MobileOutlineTableWithDetailForm extends AbstractMobileOutlineTableForm implements IMobileOutlineTableForm {
//  private static final IScoutLogger LOG = ScoutLogManager.getLogger(MobileOutlineTableWithDetailForm.class);
//  private IPage m_page;
//  private P_PageTableListener m_pageTableListener;
//  private boolean m_nodePageSwitchEnabled;
//
//  public MobileOutlineTableWithDetailForm(List<IButton> mainboxButtons) throws ProcessingException {
//    super(mainboxButtons);
//  }
//
//  @Override
//  public ITable getCurrentTable() {
//    return getOutlineTableField().getTable();
//  }
//
//  @Override
//  public void setCurrentTable(ITable table) {
//    if (m_pageTableListener == null) {
//      m_pageTableListener = new P_PageTableListener();
//    }
//
//    if (getPageTableField().getTable() != null) {
//      getPageTableField().getTable().removeTableListener(m_pageTableListener);
//    }
//
//    getPageTableField().setTable(table, true);
//    getPageTableField().setVisible(table != null && table.getRowCount() > 0);
//
//    if (table != null) {
//      table.addTableListener(m_pageTableListener);
//    }
//  }
//
//  public OutlineTableField getPageTableField() {
//    return getFieldByClass(OutlineTableField.class);
//  }
//
//  public WrappedPageDetailForm getWrappedPageDetailForm() {
//    return getFieldByClass(WrappedPageDetailForm.class);
//  }
//
//  public boolean isNodePageSwitchEnabled() {
//    return m_nodePageSwitchEnabled;
//  }
//
//  public void setNodePageSwitchEnabled(boolean nodePageSwitchEnabled) {
//    m_nodePageSwitchEnabled = nodePageSwitchEnabled;
//  }
//
//  @InjectFieldTo(AbstractMobileOutlineTableForm.MainBox.class)
//  @Order(5.0f)
//  public class WrappedPageDetailForm extends AbstractWrappedFormField<IForm> {
//
//    @Override
//    protected int getConfiguredGridW() {
//      return 2;
//    }
//
//    @Override
//    protected int getConfiguredGridH() {
//      return 2;
//    }
//
//  }
//
//  @InjectFieldTo(AbstractMobileOutlineTableForm.MainBox.class)
//  @Order(10.0f)
//  public class GroupBox extends AbstractGroupBox {
//
//    @Order(10.0f)
//    public class OutlineTableField extends AbstractMobileOutlineTableField {
//
//      @Override
//      protected boolean getConfiguredGridUseUiHeight() {
//        return true;
//      }
//
//    }
//
//  }
//
//  @Override
//  public void setCurrentForm(IForm detailForm) {
//    getWrappedPageDetailForm().setInnerForm(detailForm);
//    getWrappedPageDetailForm().setVisible(detailForm != null);
//  }
//
//  @Override
//  public IForm getCurrentForm() {
//    return getWrappedPageDetailForm().getForm();
//  }
//
//  private void mediateTableRowSelected(TableEvent e, IPage page) {
//    if (e.isConsumed()) {
//      return;
//    }
//
//    ITableRow tableRow = e.getFirstRow();
//    ITreeNode node = null;
//    if (page instanceof IPageWithNodes) {
//      node = ((IPageWithNodes) page).getTreeNodeFor(tableRow);
//    }
//    else if (page instanceof IPageWithTable<?>) {
//      node = ((IPageWithTable<?>) page).getTreeNodeFor(tableRow);
//    }
//
//    if (node == null) {
//      OutlinePreviewLeafPage autoPage = new OutlinePreviewLeafPage(tableRow);
//      page.getTree().addChildNode(page, autoPage);
//      node = autoPage;
//    }
//
//    e.consume();
//    IOutline outline = getDesktop().getOutline();
//    if (outline != null) {
//      if (isNodePageSwitchEnabled() && node instanceof IPageWithNodes) {
//        //If it's a page with nodes show it on the left side (tablet)
//        node = node.getParentNode();
//      }
//      node.setTreeInternal(outline, true);
//      outline.getUIFacade().setNodeSelectedAndExpandedFromUI(node);
//    }
//  }
//
//  private class P_PageTableListener extends TableAdapter {
//    @Override
//    public void tableChanged(TableEvent e) {
//      switch (e.getType()) {
//        case TableEvent.TYPE_ROWS_SELECTED: {
//          handleRowsSelected(e);
//          break;
//        }
//      }
//    }
//
//    private void handleRowsSelected(TableEvent event) {
//      mediateTableRowSelected(event, m_page);
//    }
//  }
//}
