package org.eclipse.scout.rt.client.mobile.ui.form.outline;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.mobile.ui.action.ActionButtonBarUtility;
import org.eclipse.scout.rt.client.mobile.ui.desktop.MobileDesktopUtility;
import org.eclipse.scout.rt.client.mobile.ui.form.fields.table.autotable.AutoTableForm;
import org.eclipse.scout.rt.client.mobile.ui.form.outline.OutlinePreviewForm.MainBox.PageDetailFormField;
import org.eclipse.scout.rt.client.mobile.ui.form.outline.OutlinePreviewForm.MainBox.PageTableGroupBox;
import org.eclipse.scout.rt.client.mobile.ui.form.outline.OutlinePreviewForm.MainBox.PageTableGroupBox.PageTableField;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.basic.table.AbstractTable;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.basic.table.TableAdapter;
import org.eclipse.scout.rt.client.ui.basic.table.TableEvent;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractStringColumn;
import org.eclipse.scout.rt.client.ui.basic.tree.ITreeNode;
import org.eclipse.scout.rt.client.ui.desktop.outline.AbstractOutline;
import org.eclipse.scout.rt.client.ui.desktop.outline.AbstractOutlineTableField;
import org.eclipse.scout.rt.client.ui.desktop.outline.IOutline;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPage;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPageWithNodes;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPageWithTable;
import org.eclipse.scout.rt.client.ui.form.AbstractForm;
import org.eclipse.scout.rt.client.ui.form.AbstractFormHandler;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.button.IButton;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.wrappedform.AbstractWrappedFormField;
import org.eclipse.scout.rt.shared.ScoutTexts;

public class OutlinePreviewForm extends AbstractForm {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(OutlinePreviewForm.class);
  private List<IButton> m_mainboxButtons;
  private IPage m_page;
  private IPage m_parentPage;
  private P_PageTableListener m_pageTableListener;
  private PreviewOutline m_outline;

  public OutlinePreviewForm(IPage page) throws ProcessingException {
    super(false);

    //Init (order is important)
    setPageInternal(page);
    initMainButtons();
    callInitializer();
    initFields();
  }

  @Override
  protected boolean getConfiguredAskIfNeedSave() {
    return false;
  }

  @Override
  protected int getConfiguredDisplayHint() {
    return DISPLAY_HINT_VIEW;
  }

  @Override
  protected String getConfiguredDisplayViewId() {
    return VIEW_ID_PAGE_DETAIL;
  }

  public PageTableField getPageTableField() {
    return getFieldByClass(PageTableField.class);
  }

  public PageDetailFormField getPageDetailFormField() {
    return getFieldByClass(PageDetailFormField.class);
  }

  public PageTableGroupBox getPageTableGroupBox() {
    return getFieldByClass(PageTableGroupBox.class);
  }

  public final IPage getPage() {
    return m_page;
  }

  private void setPageInternal(IPage page) throws ProcessingException {
    if (m_page == page) {
      return;
    }
    if (m_page != null) {
//FIXME CGU destroy on dispose?
      getPageDetailFormField().setInnerForm(null);
      getPageTableField().setTable(null, true);
      m_outline.disposeTree();
      m_outline = null;
      m_page = null;
    }

    m_page = page;
    if (m_page != null) {
      m_page = (IPage) m_page.getTree().resolveVirtualNode(m_page);
      m_parentPage = m_page.getParentPage();

      m_outline = new PreviewOutline();
      m_outline.setRootNode(m_page);
      m_outline.selectNode(m_page);

      if (m_page.getDetailForm() == null) {
        AutoTableForm autoDetailForm = createAutoDetailForm();
        if (autoDetailForm != null) {
          m_page.setDetailForm(autoDetailForm);
          autoDetailForm.start();
        }
      }
      if (!m_page.isTableVisible()) {
        //Make sure detail table is set on outline even if it is supposed to be invisible
        m_page.setTableVisible(true);
        m_outline.setDetailTable(MobileDesktopUtility.getPageTable(m_page));
      }

    }
  }

  private void setPageTable(ITable table) {
    if (m_pageTableListener == null) {
      m_pageTableListener = new P_PageTableListener();
    }

    if (getPageTableField().getTable() != null) {
      getPageTableField().getTable().removeTableListener(m_pageTableListener);
    }

    getPageTableField().setTable(table, true);
    getPageTableField().setVisible(table != null && table.getRowCount() > 0);

    if (table != null) {
      table.addTableListener(m_pageTableListener);
    }
  }

  private AutoTableForm createAutoDetailForm() throws ProcessingException {
    ITable table = null;
    IPage parentPage = m_parentPage;
    if (parentPage instanceof IPageWithTable) {
      table = ((IPageWithTable) parentPage).getTable();
    }
    else if (parentPage instanceof IPageWithNodes) {
//      table = ((IPageWithNodes) parentPage).getInternalTable();
    }
    if (table != null && table.getSelectedRow() != null) {
      return new AutoTableForm(table.getSelectedRow());
    }

    return null;
  }

  private void initMainButtons() {
    List<IButton> buttonList = new LinkedList<IButton>();
    buttonList.addAll(fetchNodeActionsAndConvertToButtons());

    if (m_outline.getDetailForm() != null) {
      //Buttons of the auto table form are the same as the node actions, so only the buttons of regular detail forms are added
      if (!(m_outline.getDetailForm() instanceof AutoTableForm) || m_page instanceof OutlinePreviewLeafPage) {
        IButton[] detailFormCustomButtons = m_outline.getDetailForm().getRootGroupBox().getCustomProcessButtons();
        buttonList.addAll(Arrays.asList(detailFormCustomButtons));
      }
    }

    m_mainboxButtons = buttonList;
  }

  private void initFields() throws ProcessingException {
    getPageDetailFormField().setInnerForm(m_outline.getDetailForm());
    getPageDetailFormField().setVisible(m_outline.getDetailForm() != null);
    getPageTableGroupBox().setBorderVisible(m_outline.getDetailForm() != null);
    IPage page = m_page;

    ITable pageTable = MobileDesktopUtility.getPageTable(page);

    //Make sure the preview form does only contain folder pages.
    if (m_page instanceof IPageWithTable) {
      pageTable = new PlaceholderTable();
      pageTable.initTable();
      pageTable.addRowByArray(new Object[]{"Details"});//FIXME CGU
    }

    setPageTable(pageTable);
  }

  private List<IButton> fetchNodeActionsAndConvertToButtons() {

    IMenu[] nodeActions = m_outline.getUIFacade().fireNodePopupFromUI();
    List<IMenu> nodeActionList = new LinkedList<IMenu>();

    //Remove separators
    for (IMenu action : nodeActions) {
      if (!action.isSeparator()) {
        nodeActionList.add(action);
      }
    }

    return ActionButtonBarUtility.convertActionsToMainButtons(nodeActionList.toArray(new IMenu[nodeActionList.size()]));
  }

  @Order(10.0f)
  public class MainBox extends AbstractGroupBox {

    @Override
    protected boolean getConfiguredBorderVisible() {
      return false;
    }

    @Override
    protected int getConfiguredGridColumnCount() {
      return 1;
    }

    @Override
    protected void injectFieldsInternal(List<IFormField> fieldList) {
      if (m_mainboxButtons != null) {
        fieldList.addAll(m_mainboxButtons);
      }

      super.injectFieldsInternal(fieldList);
    }

    @Order(5.0f)
    public class PageDetailFormField extends AbstractWrappedFormField<IForm> {

      @Override
      protected int getConfiguredGridW() {
        return 2;
      }

      @Override
      protected int getConfiguredGridH() {
        return 2;
      }

      @Override
      protected double getConfiguredGridWeightY() {
        return 0;
      }

    }

    @Order(10.0f)
    public class PageTableGroupBox extends AbstractGroupBox {

      @Order(10.0f)
      public class PageTableField extends AbstractOutlineTableField {

        @Override
        protected boolean getConfiguredTableStatusVisible() {
          return false;
        }

        @Override
        protected boolean getConfiguredGridUseUiHeight() {
          return true;
        }

      }

    }
  }

  public void start() throws ProcessingException {
    startInternal(new FormHandler());
  }

  @Order(10.0f)
  public class FormHandler extends AbstractFormHandler {
  }

  private static class PreviewOutline extends AbstractOutline {

    @Override
    protected boolean getConfiguredRootNodeVisible() {
      return true;
    }

  }

  private class P_PageTableListener extends TableAdapter {
    @Override
    public void tableChanged(TableEvent e) {
      switch (e.getType()) {
        case TableEvent.TYPE_ROW_CLICK: {
          handleRowClick(e);
          break;
        }
      }
    }

    private void handleRowClick(TableEvent event) {
      mediateTableRowClick(event, m_page);
    }
  }

  private void mediateTableRowClick(TableEvent e, IPage page) {
    if (e.isConsumed()) {
      return;
    }

    ITableRow tableRow = e.getFirstRow();
    ITreeNode node = null;
    if (page instanceof IPageWithNodes) {
      node = ((IPageWithNodes) page).getTreeNodeFor(tableRow);
    }
    else if (page instanceof IPageWithTable<?>) {
      node = ((IPageWithTable<?>) page).getTreeNodeFor(tableRow);
    }

    if (node == null) {
      OutlinePreviewLeafPage autoPage = new OutlinePreviewLeafPage(tableRow);
      page.getTree().addChildNode(page, autoPage);
      node = autoPage;
    }

    e.consume();
    IOutline outline = getDesktop().getOutline();
    if (outline != null) {
      //If it's a page with nodes show it on the left side (tablet)
      if (node instanceof IPageWithNodes) {
        node = node.getParentNode();
      }
      node.setTreeInternal(outline, true);
      outline.getUIFacade().setNodeSelectedAndExpandedFromUI(node);
    }
  }

  private class PlaceholderTable extends AbstractTable {

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

    }
  }
}
