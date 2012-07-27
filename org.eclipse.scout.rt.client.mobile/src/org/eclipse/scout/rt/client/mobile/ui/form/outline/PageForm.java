package org.eclipse.scout.rt.client.mobile.ui.form.outline;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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
  private PageFormConfig m_pageFormConfig;
  private PageFormManager m_pageFormManager;
  private Map<ITableRow, AutoLeafPageWithNodes> m_autoLeafPageMap;

  public PageForm(IPage page, PageFormManager manager, PageFormConfig pageFormConfig) throws ProcessingException {
    super(false);
    m_pageFormManager = manager;
    m_pageFormConfig = pageFormConfig;
    if (m_pageFormConfig == null) {
      m_pageFormConfig = new PageFormConfig();
    }
    m_autoLeafPageMap = new HashMap<ITableRow, AutoLeafPageWithNodes>();

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

    if (m_pageFormConfig.isDetailFormVisible() && m_page.getDetailForm() == null) {
      AutoTableForm autoDetailForm = createAutoDetailForm();
      if (autoDetailForm != null) {
        m_page.setDetailForm(autoDetailForm);
        autoDetailForm.start();
      }
    }

    setTitle(page.getCellForUpdate().getText());
  }

  /**
   * If there is a detail form the table field is visible depending on its content. If there is no detail form the table
   * field always is visible.
   */
  protected void updateTableFieldVisibility() throws ProcessingException {
    ITable table = getPageTableField().getTable();
    boolean hasDetailForm = getPageDetailFormField().getInnerForm() != null;

    if (hasDetailForm) {
      boolean hasTableRows = table != null && table.getRowCount() > 0;
      getPageTableField().setVisible(hasTableRows);
    }
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
    if (m_pageFormConfig.isDetailFormVisible()) {
      getPageDetailFormField().setInnerForm(m_page.getDetailForm());
    }
    getPageDetailFormField().setVisible(getPageDetailFormField().getInnerForm() != null);
    getPageTableGroupBox().setBorderVisible(getPageDetailFormField().getInnerForm() != null);

    IPage page = m_page;
    ITable pageTable = MobileDesktopUtility.getPageTable(page);

    //Make sure the preview form does only contain folder pages.
    if (!m_pageFormConfig.isTablePageAllowed() && m_page instanceof IPageWithTable) {
      pageTable = new PlaceholderTable(m_page);
      pageTable.initTable();
      pageTable.addRowByArray(new Object[]{"Details"});//FIXME CGU
    }

    getPageTableField().setTable(pageTable, true);
    updateTableFieldVisibility();
    getPageTableField().setTableStatusVisible(m_pageFormConfig.isTableStatusVisible());
  }

  private List<IButton> fetchNodeActionsAndConvertToButtons() throws ProcessingException {
    IMenu[] nodeActions = m_page.getTree().getUIFacade().fireNodePopupFromUI();
    List<IMenu> nodeActionList = new LinkedList<IMenu>();

    //Remove separators
    for (IMenu action : nodeActions) {
      if (!action.isSeparator()) {
        nodeActionList.add(action);
      }
    }

    return ActionButtonBarUtility.convertActionsToMainButtons(nodeActionList.toArray(new IMenu[nodeActionList.size()]));
  }

  public void formAddedNotify() throws ProcessingException {
    clearTableSelectionIfNecessary();

    //Make sure the page which belongs to the form is active when the form is shown
    m_page.getOutline().getUIFacade().setNodeSelectedAndExpandedFromUI(m_page);

    addTableListener();
    processSelectedTableRow();
  }

  public void formRemovedNotify() throws ProcessingException {
    removeTableListener();
  }

  private void addTableListener() {
    if (m_pageTableListener != null) {
      return;
    }
    m_pageTableListener = new P_PageTableListener();

    ITable table = getPageTableField().getTable();
    if (table != null) {
      table.addTableListener(m_pageTableListener);
    }
  }

  private void removeTableListener() {
    if (m_pageTableListener == null) {
      return;
    }
    ITable table = getPageTableField().getTable();
    if (table != null) {
      table.removeTableListener(m_pageTableListener);
    }
    m_pageTableListener = null;
  }

  private void clearTableSelectionIfNecessary() {
    if (getPageTableField().getTable() == null) {
      return;
    }

    ITableRow selectedRow = getPageTableField().getTable().getSelectedRow();
    if (!m_pageFormConfig.isKeepSelection() || PageFormManager.isDrillDownPage(MobileDesktopUtility.getPageFor(getPage(), selectedRow))) {
      getPageTableField().getTable().selectRow(null);
    }
  }

  private void processSelectedTableRow() throws ProcessingException {
    if (!m_pageFormConfig.isKeepSelection()) {
      return;
    }

    ITable pageTable = MobileDesktopUtility.getPageTable(getPage());
    if (pageTable == null) {
      return;
    }

    ITableRow selectedRow = pageTable.getSelectedRow();
    if (!PageFormManager.isDrillDownPage(MobileDesktopUtility.getPageFor(getPage(), selectedRow))) {
      if (selectedRow != null) {
        handleTableRowSelected(pageTable, selectedRow);
      }
      else {
        selectPageTableRowIfNecessary(pageTable);
      }
    }
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
          return m_pageFormConfig.isDetailFormVisible() && m_page.getDetailForm() != null;
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
    if (!m_pageFormConfig.isTableStatusVisible()) {
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

  private void handleTableRowSelected(ITable table, ITableRow tableRow) throws ProcessingException {
    LOG.debug("Table row selected: " + tableRow);
    if (tableRow == null) {
      //Make sure there always is a selected row. if NodePageSwitch is enabled the same page and therefor the is on different pageForms
      if (m_pageFormConfig.isKeepSelection()) {
        selectPageTableRowIfNecessary(table);
      }
      return;
    }

    IPage rowPage = null;
    if (table instanceof PlaceholderTable) {
      rowPage = ((PlaceholderTable) table).getActualPage();
    }
    else if (m_autoLeafPageMap.containsKey(tableRow)) {
      rowPage = m_autoLeafPageMap.get(tableRow);
    }
    else {
      rowPage = MobileDesktopUtility.getPageFor(m_page, tableRow);
    }
    if (rowPage == null) {
      //Create auto leaf page including an outline and activate it.
      //Adding to a "real" outline is not possible because the page to row maps in AbstractPageWithTable resp. AbstractPageWithNodes can only be modified by the page itself.
      AutoLeafPageWithNodes autoPage = new AutoLeafPageWithNodes(tableRow);
      AutoOutline autoOutline = new AutoOutline();
      autoOutline.setRootNode(autoPage);
      autoOutline.selectNode(autoPage);
      m_autoLeafPageMap.put(tableRow, autoPage);

      rowPage = autoPage;
    }

    if (m_pageFormConfig.isNodePageSwitchEnabled() && (rowPage instanceof IPageWithNodes)) {
      //If it's a page with nodes show it on the left side (tablet)
      rowPage = rowPage.getParentPage();
    }

    m_pageFormManager.pageSelectedNotify(this, rowPage);
  }

  private void handleTableRowsDeleted(ITable table, ITableRow[] tableRows) throws ProcessingException {
    if (tableRows == null) {
      return;
    }

    for (ITableRow tableRow : tableRows) {
      AutoLeafPageWithNodes autoPage = m_autoLeafPageMap.get(tableRow);
      if (autoPage != null) {
        m_autoLeafPageMap.remove(autoPage);
        m_pageFormManager.pageRemovedNotify(this, autoPage);
      }
    }
  }

  protected void selectPageTableRowIfNecessary(final ITable pageDetailTable) throws ProcessingException {
    if (!m_pageFormConfig.isKeepSelection() || pageDetailTable == null || pageDetailTable.getRowCount() == 0) {
      return;
    }

    IPage activePage = getDesktop().getOutline().getActivePage();
    IPage pageToSelect = MobileDesktopUtility.getPageFor(activePage, pageDetailTable.getRow(0));
    if (pageDetailTable.getSelectedRow() == null) {
      if (!PageFormManager.isDrillDownPage(pageToSelect)) {
        pageDetailTable.selectFirstRow();
      }
    }

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
    public void tableChanged(TableEvent event) {
      try {
        switch (event.getType()) {
          case TableEvent.TYPE_ROWS_SELECTED: {
            handleTableRowSelected(event);
            break;
          }
          case TableEvent.TYPE_ALL_ROWS_DELETED:
          case TableEvent.TYPE_ROWS_DELETED: {
            handleTableRowDeleted(event);
            break;
          }
          case TableEvent.TYPE_ROWS_INSERTED:
            handleTableRowsInserted(event);
        }
      }
      catch (ProcessingException e) {
        SERVICES.getService(IExceptionHandlerService.class).handleException(e);
      }
    }

    private void handleTableRowSelected(TableEvent event) throws ProcessingException {
      if (event.isConsumed()) {
        return;
      }

      ITableRow tableRow = event.getFirstRow();
      PageForm.this.handleTableRowSelected(event.getTable(), tableRow);
    }

    private void handleTableRowDeleted(TableEvent event) throws ProcessingException {
      PageForm.this.handleTableRowsDeleted(event.getTable(), event.getRows());
      updateTableFieldVisibility();
    }

    private void handleTableRowsInserted(TableEvent event) throws ProcessingException {
      updateTableFieldVisibility();
    }

  }
}
