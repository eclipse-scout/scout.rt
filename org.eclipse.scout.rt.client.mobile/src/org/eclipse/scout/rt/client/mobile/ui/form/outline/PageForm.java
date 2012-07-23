package org.eclipse.scout.rt.client.mobile.ui.form.outline;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.scout.commons.NumberUtility;
import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.exception.IProcessingStatus;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.exception.ProcessingStatus;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.mobile.ui.action.ActionButtonBarUtility;
import org.eclipse.scout.rt.client.mobile.ui.desktop.MobileDesktopUtility;
import org.eclipse.scout.rt.client.mobile.ui.form.fields.table.autotable.AutoTableForm;
import org.eclipse.scout.rt.client.mobile.ui.form.outline.PageForm.MainBox.PageDetailFormField;
import org.eclipse.scout.rt.client.mobile.ui.form.outline.PageForm.MainBox.PageTableGroupBox;
import org.eclipse.scout.rt.client.mobile.ui.form.outline.PageForm.MainBox.PageTableGroupBox.PageTableField;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.basic.table.AbstractTable;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.basic.table.TableAdapter;
import org.eclipse.scout.rt.client.ui.basic.table.TableEvent;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractStringColumn;
import org.eclipse.scout.rt.client.ui.basic.tree.ITreeNode;
import org.eclipse.scout.rt.client.ui.desktop.outline.AbstractOutlineTableField;
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
import org.eclipse.scout.rt.shared.services.common.exceptionhandler.IExceptionHandlerService;
import org.eclipse.scout.service.SERVICES;

public class PageForm extends AbstractForm implements IPageForm {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(PageForm.class);
  private List<IButton> m_mainboxButtons;
  private IPage m_page;
  private P_PageTableListener m_pageTableListener;
  private boolean m_nodePageSwitchEnabled;
  private boolean m_tablePagesAllowed;
  private boolean m_detailFormVisible;
  private boolean m_keepSelection;

  private PageFormManager m_pageFormManager;

  public PageForm(IPage page, PageFormManager manager, boolean tablePagesAllowed, boolean detailFormVisible) throws ProcessingException {
    super(false);
    m_tablePagesAllowed = tablePagesAllowed;
    m_detailFormVisible = detailFormVisible;
    m_pageFormManager = manager;
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
    return VIEW_ID_CENTER;
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

  @Override
  public final IPage getPage() {
    return m_page;
  }

  private void setPageInternal(IPage page) throws ProcessingException {
    m_page = page;
    m_page = (IPage) m_page.getTree().resolveVirtualNode(m_page);

    if (m_detailFormVisible && m_page.getDetailForm() == null) {
      AutoTableForm autoDetailForm = createAutoDetailForm();
      if (autoDetailForm != null) {
        m_page.setDetailForm(autoDetailForm);
        autoDetailForm.start();
      }
    }

    setTitle(page.getCellForUpdate().getText());
  }

  protected void setPageTable(ITable table) throws ProcessingException {
    if (m_pageTableListener == null) {
      m_pageTableListener = new P_PageTableListener();
    }

    if (getPageTableField().getTable() != null) {
      getPageTableField().getTable().removeTableListener(m_pageTableListener);
    }

    if (table != null) {
      table.addTableListener(m_pageTableListener);
    }

    getPageTableField().setTable(table, true);
    //FIXME CGU when to hide the table?
    getPageTableField().setVisible(table != null);
  }

  private AutoTableForm createAutoDetailForm() throws ProcessingException {
    ITable table = null;
    IPage parentPage = m_page.getParentPage();
    if (parentPage instanceof IPageWithTable) {
      table = ((IPageWithTable) parentPage).getTable();
    }
    if (table != null && table.getSelectedRow() != null) {
      return new AutoTableForm(table.getSelectedRow());
    }

    return null;
  }

  private void initMainButtons() throws ProcessingException {
    List<IButton> buttonList = new LinkedList<IButton>();
    buttonList.addAll(fetchNodeActionsAndConvertToButtons());

    if (m_page.getDetailForm() != null) {
      //Buttons of the auto table form are the same as the node actions, so only the buttons of regular detail forms are added
      if (!(m_page.getDetailForm() instanceof AutoTableForm) || m_page instanceof AutoLeafPageWithNodes) {
        IButton[] detailFormCustomButtons = m_page.getDetailForm().getRootGroupBox().getCustomProcessButtons();
        buttonList.addAll(Arrays.asList(detailFormCustomButtons));
      }
    }

    m_mainboxButtons = buttonList;
  }

  private void initFields() throws ProcessingException {
    if (m_detailFormVisible) {
      getPageDetailFormField().setInnerForm(m_page.getDetailForm());
    }
    getPageDetailFormField().setVisible(getPageDetailFormField().getInnerForm() != null);
    getPageTableGroupBox().setBorderVisible(getPageDetailFormField().getInnerForm() != null);

    IPage page = m_page;
    ITable pageTable = MobileDesktopUtility.getPageTable(page);

    //Make sure the preview form does only contain folder pages.
    if (!m_tablePagesAllowed && m_page instanceof IPageWithTable) {
      pageTable = new PlaceholderTable(m_page);
      pageTable.initTable();
      pageTable.addRowByArray(new Object[]{"Details"});//FIXME CGU
    }

    setPageTable(pageTable);
  }

  private List<IButton> fetchNodeActionsAndConvertToButtons() throws ProcessingException {

//  IMenu[] nodeActions = m_page.getTree().getUIFacade().fireNodePopupFromUI();
    ITreeNode[] nodes = m_page.getTree().resolveVirtualNodes(m_page.getTree().getSelectedNodes());
    IMenu[] nodeActions = m_page.getTree().fetchMenusForNodesInternal(nodes);
    List<IMenu> nodeActionList = new LinkedList<IMenu>();

    //Remove separators
    for (IMenu action : nodeActions) {
      if (!action.isSeparator()) {
        nodeActionList.add(action);
      }
    }

    return ActionButtonBarUtility.convertActionsToMainButtons(nodeActionList.toArray(new IMenu[nodeActionList.size()]));
  }

  public static boolean isDrillDownPage(IPage page) {
    return page instanceof IPageWithTable && page.getParentNode() instanceof IPageWithNodes;
  }

  public void formAddedNotify() throws ProcessingException {
    if (getPageTableField().getTable() != null) {
      ITableRow selectedRow = getPageTableField().getTable().getSelectedRow();
      if (!isKeepSelection() || isDrillDownPage(MobileDesktopUtility.getPageFor(getPage(), selectedRow))) {
        getPageTableField().getTable().selectRow(null);
      }
    }

    //Make sure the page which belongs to the form is active when the form is shown
    m_page.getOutline().getUIFacade().setNodeSelectedAndExpandedFromUI(m_page);
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
          //If there is a detail form make the table as height as necessary to avoid a second scrollbar.
          //If there is no detail form make the table itself scrollable.
          return m_detailFormVisible;
        }

        @Override
        protected void execUpdateTableStatus() {
          execUpdatePageTableStatus();
        }

        @Override
        public String createDefaultTableStatus() {
          return createDefaultPageTableStatus(getTable());
        }
      }

    }
  }

  protected void execUpdatePageTableStatus() {
    if (!isTableStatusVisible()) {
      return;
    }
    if (getPage() instanceof IPageWithTable<?>) {
      //popuplate status
      IPageWithTable<?> tablePage = (IPageWithTable<?>) getPage();
      IProcessingStatus populateStatus = tablePage.getTablePopulateStatus();
      getPageTableField().setTablePopulateStatus(populateStatus);
      //selection status
      if (tablePage.isSearchActive() && tablePage.getSearchFilter() != null && (!tablePage.getSearchFilter().isCompleted()) && tablePage.isSearchRequired()) {
        getPageTableField().setTableSelectionStatus(null);
      }
      else if (populateStatus != null && populateStatus.getSeverity() == IProcessingStatus.WARNING) {
        getPageTableField().setTableSelectionStatus(null);
      }
      else {
        getPageTableField().setTableSelectionStatus(new ProcessingStatus(getPageTableField().createDefaultTableStatus(), IProcessingStatus.INFO));
      }
    }
    else {
      getPageTableField().setTablePopulateStatus(null);
      getPageTableField().setTableSelectionStatus(null);
    }
  }

  protected String createDefaultPageTableStatus(ITable table) {
    StringBuilder statusText = new StringBuilder();
    if (table != null) {
      int nTotal = table.getFilteredRowCount();
      if (nTotal == 1) {
        statusText.append(ScoutTexts.get("OneRow"));
      }
      else {
        statusText.append(ScoutTexts.get("XRows", NumberUtility.format(nTotal)));
      }
    }
    if (statusText.length() == 0) {
      return null;
    }
    return statusText.toString();
  }

  @Override
  public void start() throws ProcessingException {
    startInternal(new FormHandler());
  }

  @Order(10.0f)
  public class FormHandler extends AbstractFormHandler {
  }

  protected void handleTableRowSelected(ITableRow tableRow) throws ProcessingException {
    LOG.debug("Table row selected: " + tableRow);
    if (tableRow == null) {
      return;
    }

    IPage rowPage = null;
    if (tableRow.getTable() instanceof PlaceholderTable) {
      rowPage = ((PlaceholderTable) tableRow.getTable()).getActualPage();
    }
    else {
      rowPage = MobileDesktopUtility.getPageFor(m_page, tableRow);
    }
    if (rowPage == null) {
      AutoLeafPageWithNodes autoPage = new AutoLeafPageWithNodes(tableRow);
      m_page.getTree().addChildNode(m_page, autoPage);
      rowPage = autoPage;
    }

    if (isNodePageSwitchEnabled() && (rowPage instanceof IPageWithNodes)) {
      //If it's a page with nodes show it on the left side (tablet)
      rowPage = rowPage.getParentPage();
    }

    m_pageFormManager.pageSelectedNotify(this, rowPage);
  }

  /**
   * If enabled, clicking on a page with nodes will lead to a selection of the parent node.
   */
  @Override
  public boolean isNodePageSwitchEnabled() {
    return m_nodePageSwitchEnabled;
  }

  @Override
  public void setNodePageSwitchEnabled(boolean nodePageSwitchEnabled) {
    m_nodePageSwitchEnabled = nodePageSwitchEnabled;
  }

  public void setKeepSelection(boolean keepSelection) {
    m_keepSelection = keepSelection;
  }

  public boolean isKeepSelection() {
    return m_keepSelection;
  }

  @Override
  public void setTableStatusVisible(boolean tableStatusVisible) {
    getPageTableField().setTableStatusVisible(tableStatusVisible);
  }

  @Override
  public boolean isTableStatusVisible() {
    return getPageTableField().isTableStatusVisible();
  }

  private class PlaceholderTable extends AbstractTable {
    private IPage m_actualPage;

    public PlaceholderTable(IPage page) {
      m_actualPage = page;
    }

    public IPage getActualPage() {
      return m_actualPage;
    }

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

  private class P_PageTableListener extends TableAdapter {
    @Override
    public void tableChanged(TableEvent e) {
      switch (e.getType()) {
        case TableEvent.TYPE_ROWS_SELECTED: {
          handleTableRowSelected(e);
          break;
        }
      }
    }

    private void handleTableRowSelected(TableEvent event) {
      if (event.isConsumed()) {
        return;
      }

      ITableRow tableRow = event.getFirstRow();
      try {
        PageForm.this.handleTableRowSelected(tableRow);
      }
      catch (ProcessingException e) {
        SERVICES.getService(IExceptionHandlerService.class).handleException(e);
      }
    }

  }
}
