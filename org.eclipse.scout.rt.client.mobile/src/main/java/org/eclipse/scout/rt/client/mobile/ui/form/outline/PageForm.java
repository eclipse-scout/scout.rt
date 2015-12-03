/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.mobile.ui.form.outline;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.scout.rt.client.mobile.transformation.DeviceTransformationConfig;
import org.eclipse.scout.rt.client.mobile.transformation.DeviceTransformationUtility;
import org.eclipse.scout.rt.client.mobile.transformation.MobileDeviceTransformation;
import org.eclipse.scout.rt.client.mobile.ui.basic.table.AbstractMobileTable;
import org.eclipse.scout.rt.client.mobile.ui.basic.table.DrillDownStyleMap;
import org.eclipse.scout.rt.client.mobile.ui.basic.table.MobileTable;
import org.eclipse.scout.rt.client.mobile.ui.basic.table.columns.IRowSummaryColumn;
import org.eclipse.scout.rt.client.mobile.ui.basic.table.form.TableRowForm;
import org.eclipse.scout.rt.client.mobile.ui.form.AbstractMobileForm;
import org.eclipse.scout.rt.client.mobile.ui.form.IActionFetcher;
import org.eclipse.scout.rt.client.mobile.ui.form.fields.table.AbstractMobileTableField;
import org.eclipse.scout.rt.client.mobile.ui.form.outline.PageForm.MainBox.PageDetailFormField;
import org.eclipse.scout.rt.client.mobile.ui.form.outline.PageForm.MainBox.PageTableGroupBox;
import org.eclipse.scout.rt.client.mobile.ui.form.outline.PageForm.MainBox.PageTableGroupBox.PageTableField;
import org.eclipse.scout.rt.client.ui.basic.table.AbstractTable;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.basic.table.TableAdapter;
import org.eclipse.scout.rt.client.ui.basic.table.TableEvent;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractStringColumn;
import org.eclipse.scout.rt.client.ui.desktop.outline.IOutline;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPage;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPageWithTable;
import org.eclipse.scout.rt.client.ui.form.AbstractFormHandler;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.client.ui.form.fields.GridData;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.button.IButton;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.wrappedform.AbstractWrappedFormField;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.exception.ExceptionHandler;
import org.eclipse.scout.rt.platform.util.collection.OrderedCollection;
import org.eclipse.scout.rt.shared.ScoutTexts;
import org.eclipse.scout.rt.shared.TEXTS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PageForm extends AbstractMobileForm implements IPageForm {
  private static final Logger LOG = LoggerFactory.getLogger(PageForm.class);
  private List<IButton> m_mainboxButtons;
  private IPage<?> m_page;
  private P_PageTableListener m_pageTableListener;
  private P_PageTableSelectionListener m_pageTableSelectionListener;
  private PageFormConfig m_pageFormConfig;
  private PageFormManager m_pageFormManager;
  private Map<ITableRow, AutoLeafPageWithNodes> m_autoLeafPageMap;
  private boolean m_rowSelectionRequired;

  public PageForm(IPage<?> page, PageFormManager manager, PageFormConfig pageFormConfig) {
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
  public void initForm() {
    // form
    initFormInternal();

    // fields
    PageFormInitFieldVisitor v = new PageFormInitFieldVisitor();
    visitFields(v);
    v.handleResult();

    // custom
    interceptInitForm();
  }

  @Override
  public PageFormConfig getPageFormConfig() {
    return m_pageFormConfig;
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
  public final IPage<?> getPage() {
    return m_page;
  }

  private void setPageInternal(IPage<?> page) {
    m_page = page;
    m_page = (IPage) m_page.getTree().resolveVirtualNode(m_page);

    if (m_pageFormConfig.isDetailFormVisible() && m_page.getDetailForm() == null) {
      TableRowForm autoDetailForm = createAutoDetailForm();
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
  protected void updateTableFieldVisibility() {
    ITable table = getPageTableField().getTable();
    boolean hasDetailForm = getPageDetailFormField().getInnerForm() != null;

    if (hasDetailForm) {
      boolean hasTableRows = table != null && table.getRowCount() > 0;
      getPageTableField().setVisible(hasTableRows);
    }

    //If there is no table make sure the table group box is invisible and the detail form grows and takes all the space.
    //If there is a table, the detail form must not grow because the table does
    if (getPageTableField().isVisible() != getPageTableGroupBox().isVisible()) {
      getPageTableGroupBox().setVisible(getPageTableField().isVisible());

      GridData gridData = getPageDetailFormField().getGridDataHints();
      if (!getPageTableField().isVisible()) {
        gridData.weightY = 1;
      }
      else {
        gridData.weightY = 0;
      }
      getPageDetailFormField().setGridDataHints(gridData);
      getRootGroupBox().rebuildFieldGrid();
    }
  }

  /**
   * Creates a {@link TableRowForm} out of the selected table row if the parent page is a {@link IPageWithTable}.
   */
  private TableRowForm createAutoDetailForm() {
    ITable table = null;
    IPage<?> parentPage = m_page.getParentPage();
    if (parentPage instanceof IPageWithTable) {
      table = ((IPageWithTable) parentPage).getTable();
    }
    if (table != null) {
      if (table.getSelectedRow() == null) {
        //If the parent page has not been selected before there is no row selected -> select it to create the tableRowForm
        ITableRow row = m_page.getParentPage().getTableRowFor(m_page);
        if (row != null) {
          row.getTable().selectRow(row);
        }
      }
      if (table.getSelectedRow() != null) {
        return new TableRowForm(table.getSelectedRow());
      }

    }

    return null;
  }

  @Override
  protected IActionFetcher createHeaderActionFetcher() {
    return new PageFormHeaderActionFetcher(this);
  }

  @Override
  protected IActionFetcher createFooterActionFetcher() {
    return new PageFormFooterActionFetcher(this);
  }

  private void initMainButtons() {
    List<IButton> buttonList = new LinkedList<IButton>();

    //Add buttons of the detail form to the main box
    if (m_page.getDetailForm() != null) {
      buttonList.addAll(m_page.getDetailForm().getRootGroupBox().getCustomProcessButtons());
    }

    m_mainboxButtons = buttonList;
  }

  private void initFields() {
    if (m_pageFormConfig.isDetailFormVisible()) {
      getPageDetailFormField().setInnerForm(m_page.getDetailForm());
    }

    //Don't display detail form field if there is no detail form -> saves space
    boolean hasDetailForm = getPageDetailFormField().getInnerForm() != null;
    getPageDetailFormField().setVisible(hasDetailForm);
    ITable pageTable = m_page.getTable();

    //Make sure the preview form does only contain folder pages.
    if (!m_pageFormConfig.isTablePageAllowed() && m_page instanceof IPageWithTable) {
      pageTable = new PlaceholderTable(m_page);
      pageTable.initTable();
      pageTable.addRowByArray(new Object[]{TEXTS.get("MobilePlaceholderTableTitle")});
      pageTable.setDefaultIconId(m_page.getCell().getIconId());
    }

    AbstractMobileTable.setAutoCreateRowForm(pageTable, false);
    getPageTableField().setTable(pageTable, true);
    getPageTableField().setTableStatusVisible(m_pageFormConfig.isTableStatusVisible());
    addTableListener();
    updateTableFieldVisibility();

    if (getPageTableGroupBox().isVisible() && !hasDetailForm) {
      //If there is a table but no detail form, don't display a border -> make the table as big as the form.
      //If there is a table and a detail form, display a border to make it look better.
      getPageTableGroupBox().setBorderVisible(false);

      //If there is just the table, the form itself does not need to be scrollable because the table already is
      DeviceTransformationConfig config = DeviceTransformationUtility.getDeviceTransformationConfig();
      if (config != null) {
        config.excludeFieldTransformation(getRootGroupBox(), MobileDeviceTransformation.MAKE_MAINBOX_SCROLLABLE);
      }
    }
  }

  @Override
  protected void execDisposeForm() {
    removeTableListener();
    for (AutoLeafPageWithNodes autoLeafPage : m_autoLeafPageMap.values()) {
      disposeAutoLeafPage(autoLeafPage);
    }
  }

  private void updateDrillDownStyle() {
    ITable table = getPageTableField().getTable();
    if (table != null) {
      setTableRowDrillDownStyle(table, table.getRows());
    }
  }

  private void setTableRowDrillDownStyle(ITable table, List<ITableRow> rows) {
    if (rows == null) {
      return;
    }

    DrillDownStyleMap drillDownMap = MobileTable.getDrillDownStyleMap(table);
    if (drillDownMap == null) {
      drillDownMap = new DrillDownStyleMap();
      AbstractMobileTable.setDrillDownStyleMap(table, drillDownMap);
    }

    for (ITableRow row : rows) {
      if (!isDrillDownRow(row)) {
        drillDownMap.put(row, IRowSummaryColumn.DRILL_DOWN_STYLE_NONE);
      }
      else {
        drillDownMap.put(row, IRowSummaryColumn.DRILL_DOWN_STYLE_ICON);
      }
    }

  }

  private boolean isDrillDownRow(ITableRow tableRow) {
    if (!m_pageFormConfig.isKeepSelection()) {
      return true;
    }

    return PageFormManager.isDrillDownPage(getPage().getPageFor(tableRow));
  }

  public void formAddedNotify() {
    LOG.debug(this + " added");

    //Clear selection if form gets visible again. It must not happen earlier, since the actions typically depend on the selected row.
    clearTableSelectionIfNecessary();

    //Make sure the rows display the correct drill down style
    updateDrillDownStyle();

    if (!m_page.isSelectedNode()) {
      selectChildPageTableRowIfNecessary();

      //Make sure the page which belongs to the form is active when the form is shown
      m_page.getOutline().getUIFacade().setNodeSelectedAndExpandedFromUI(m_page);
    }

    addTableSelectionListener();
    processSelectedTableRow();
  }

  @Override
  public void pageSelectedNotify() {
    if (m_rowSelectionRequired) {
      selectFirstChildPageTableRowIfNecessary(getPageTableField().getTable());
      m_rowSelectionRequired = false;
    }
  }

  private void clearTableSelectionIfNecessary() {
    if (getPageTableField().getTable() == null) {
      return;
    }

    ITableRow selectedRow = getPageTableField().getTable().getSelectedRow();
    if (selectedRow != null && isDrillDownRow(selectedRow)) {
      LOG.debug("Clearing row for table " + getPageTableField().getTable());

      getPageTableField().getTable().selectRow(null);
    }
  }

  public void formRemovedNotify() {
    removeTableSelectionListener();
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

  private void addTableSelectionListener() {
    if (m_pageTableSelectionListener != null) {
      return;
    }
    m_pageTableSelectionListener = new P_PageTableSelectionListener();

    ITable table = getPageTableField().getTable();
    if (table != null) {
      table.addTableListener(m_pageTableSelectionListener);
    }
  }

  private void removeTableSelectionListener() {
    if (m_pageTableSelectionListener == null) {
      return;
    }
    ITable table = getPageTableField().getTable();
    if (table != null) {
      table.removeTableListener(m_pageTableSelectionListener);
    }
    m_pageTableSelectionListener = null;
  }

  private void processSelectedTableRow() {
    if (!m_pageFormConfig.isKeepSelection() && !m_pageFormConfig.isAutoSelectFirstChildPage()) {
      return;
    }

    ITable pageTable = getPage().getTable();
    if (pageTable == null) {
      return;
    }

    ITableRow selectedRow = pageTable.getSelectedRow();
    if (!PageFormManager.isDrillDownPage(getPage().getPageFor(selectedRow))) {
      if (selectedRow != null) {
        if (m_pageFormConfig.isKeepSelection()) {
          handleTableRowSelected(pageTable, selectedRow);
        }
      }
      else {
        if (m_pageFormConfig.isAutoSelectFirstChildPage()) {
          selectFirstChildPageTableRowIfNecessary(pageTable);
        }
      }
    }
  }

  @Override
  public boolean isDirty() {
    if (m_pageFormConfig.isDetailFormVisible()) {
      if (m_page.getDetailForm() != getPageDetailFormField().getInnerForm()) {
        return true;
      }
    }
    if (m_pageFormConfig.isTablePageAllowed() && m_page instanceof IPageWithTable) {
      ITable pageTable = ((IPageWithTable) m_page).getTable();
      if (pageTable != getPageTableField().getTable()) {
        return true;
      }
    }

    return false;
  }

  @Order(10)
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
    protected void injectFieldsInternal(OrderedCollection<IFormField> fields) {
      if (m_mainboxButtons != null) {
        fields.addAllLast(m_mainboxButtons);
      }

      super.injectFieldsInternal(fields);
    }

    @Order(5)
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

    @Order(10)
    public class PageTableGroupBox extends AbstractGroupBox {

      @Order(10)
      public class PageTableField extends AbstractMobileTableField<ITable> {

        @Override
        protected boolean getConfiguredLabelVisible() {
          return false;
        }

        @Override
        protected boolean getConfiguredGridUseUiHeight() {
          //If there is a detail form make the table as height as necessary to avoid a second scrollbar.
          //If there is no detail form make the table itself scrollable.
          return m_pageFormConfig.isDetailFormVisible() && m_page.getDetailForm() != null;
        }

        @Override
        protected void setTableInternal(ITable table) {
          super.setTableInternal(table);
          setTableStatusVisible(false);
        }
      }
    }
  }

  @Override
  public Object computeExclusiveKey() {
    return m_page;
  }

  @Override
  public void start() {
    startInternalExclusive(new FormHandler());
  }

  public class FormHandler extends AbstractFormHandler {

    @Override
    protected boolean getConfiguredOpenExclusive() {
      return true;
    }

  }

  private void handleTableRowSelected(ITable table, ITableRow tableRow) {
    LOG.debug("Table row selected: " + tableRow);

    // If children are not loaded rowPage cannot be estimated.
    //This is the case when the rows get replaced which restores the selection before the children are loaded (e.g. executed by a search).
    if (!m_page.isLeaf() && !m_page.isChildrenLoaded()) {
      if (tableRow == null) {
        //Postpone the row selection since it cannot be done if the row page cannot be estimated
        m_rowSelectionRequired = true;
      }
      return;
    }

    if (tableRow == null) {
      //Make sure there always is a selected row. if NodePageSwitch is enabled the same page and therefore the same table is on different pageForms
      selectFirstChildPageTableRowIfNecessary(table);
      return;
    }

    IPage<?> rowPage = null;
    if (table instanceof PlaceholderTable) {
      rowPage = ((PlaceholderTable) table).getActualPage();
    }
    else if (m_autoLeafPageMap.containsKey(tableRow)) {
      rowPage = m_autoLeafPageMap.get(tableRow);
    }
    else {
      rowPage = m_page.getPageFor(tableRow);
    }
    if (rowPage == null) {
      //Create auto leaf page including an outline and activate it.
      //Adding to a "real" outline is not possible because the page to row maps in AbstractPageWithTable resp. AbstractPageWithNodes can only be modified by the page itself.
      AutoLeafPageWithNodes autoPage = new AutoLeafPageWithNodes(tableRow, m_page);
      AutoOutline autoOutline = new AutoOutline(autoPage);
      autoOutline.selectNode(autoPage);
      m_autoLeafPageMap.put(tableRow, autoPage);

      rowPage = autoPage;
    }

    m_pageFormManager.pageSelectedNotify(this, rowPage);
  }

  private void handleTableRowsDeleted(ITable table, Collection<ITableRow> tableRows) {
    if (tableRows == null) {
      return;
    }

    for (ITableRow tableRow : tableRows) {
      AutoLeafPageWithNodes autoPage = m_autoLeafPageMap.remove(tableRow);
      if (autoPage != null) {
        disposeAutoLeafPage(autoPage);

        m_pageFormManager.pageRemovedNotify(this, autoPage);
      }
    }
  }

  private void disposeAutoLeafPage(AutoLeafPageWithNodes page) {
    if (page == null || page.getOutline() == null) {
      return;
    }

    IOutline outline = page.getOutline();
    outline.removeAllChildNodes(outline.getRootNode());
    outline.disposeTree();
  }

  private void handleTableRowsInserted(ITable table, List<ITableRow> tableRows) {
    setTableRowDrillDownStyle(table, tableRows);
  }

  protected void selectFirstChildPageTableRowIfNecessary(final ITable pageDetailTable) {
    if (!m_pageFormConfig.isAutoSelectFirstChildPage() || pageDetailTable == null || pageDetailTable.getRowCount() == 0) {
      return;
    }

    IPage<?> pageToSelect = m_page.getPageFor(pageDetailTable.getRow(0));
    if (pageDetailTable.getSelectedRow() == null) {
      if (!PageFormManager.isDrillDownPage(pageToSelect)) {
        pageDetailTable.selectFirstRow();
      }
    }

  }

  /**
   * If the currently selected page is a child page belonging to this form, make sure the table reflects that -> select
   * the child page in the table
   */
  private void selectChildPageTableRowIfNecessary() {
    if (!m_pageFormConfig.isKeepSelection()) {
      return;
    }

    IPage<?> selectedPage = (IPage) m_page.getOutline().getSelectedNode();
    if (selectedPage != null && selectedPage.getParentPage() == m_page) {
      ITableRow row = m_page.getTableRowFor(selectedPage);
      if (row != null && !isDrillDownRow(row)) {
        row.getTable().selectRow(row);
      }
    }
  }

  private class PlaceholderTable extends AbstractTable {
    private IPage<?> m_actualPage;

    public PlaceholderTable(IPage<?> page) {
      m_actualPage = page;
    }

    public IPage<?> getActualPage() {
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

    @SuppressWarnings("unused")
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
          case TableEvent.TYPE_ALL_ROWS_DELETED:
          case TableEvent.TYPE_ROWS_DELETED: {
            handleTableRowDeleted(event);
            break;
          }
          case TableEvent.TYPE_ROWS_INSERTED:
            handleTableRowsInserted(event);
        }
      }
      catch (RuntimeException e) {
        BEANS.get(ExceptionHandler.class).handle(e);
      }
    }

    private void handleTableRowDeleted(TableEvent event) {
      PageForm.this.handleTableRowsDeleted(event.getTable(), event.getRows());
      updateTableFieldVisibility();
    }

    private void handleTableRowsInserted(TableEvent event) {
      PageForm.this.handleTableRowsInserted(event.getTable(), event.getRows());
      updateTableFieldVisibility();
    }

  }

  private class P_PageTableSelectionListener extends TableAdapter {
    @Override
    public void tableChanged(TableEvent event) {
      try {
        switch (event.getType()) {
          case TableEvent.TYPE_ROWS_SELECTED: {
            handleTableRowSelected(event);
            break;
          }
        }
      }
      catch (RuntimeException e) {
        BEANS.get(ExceptionHandler.class).handle(e);
      }
    }

    private void handleTableRowSelected(TableEvent event) {
      if (event.isConsumed()) {
        return;
      }

      ITableRow tableRow = event.getFirstRow();
      PageForm.this.handleTableRowSelected(event.getTable(), tableRow);
    }

  }

  @Override
  public String toString() {
    return super.toString() + " with page " + m_page;
  }
}
