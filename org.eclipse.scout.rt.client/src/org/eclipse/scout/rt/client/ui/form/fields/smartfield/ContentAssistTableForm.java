/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.ui.form.fields.smartfield;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.commons.CompareUtility;
import org.eclipse.scout.commons.TriState;
import org.eclipse.scout.commons.annotations.ConfigOperation;
import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.exception.IProcessingStatus;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.exception.ProcessingStatus;
import org.eclipse.scout.commons.job.JobEx;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.basic.cell.Cell;
import org.eclipse.scout.rt.client.ui.basic.table.AbstractTable;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractStringColumn;
import org.eclipse.scout.rt.client.ui.form.AbstractFormHandler;
import org.eclipse.scout.rt.client.ui.form.fields.button.AbstractButton;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.radiobuttongroup.AbstractRadioButtonGroup;
import org.eclipse.scout.rt.client.ui.form.fields.smartfield.ContentAssistTableForm.MainBox.ActiveStateRadioButtonGroup;
import org.eclipse.scout.rt.client.ui.form.fields.smartfield.ContentAssistTableForm.MainBox.NewButton;
import org.eclipse.scout.rt.client.ui.form.fields.smartfield.ContentAssistTableForm.MainBox.ResultTableField;
import org.eclipse.scout.rt.client.ui.form.fields.smartfield.ContentAssistTableForm.MainBox.ResultTableField.Table;
import org.eclipse.scout.rt.client.ui.form.fields.tablefield.AbstractTableField;
import org.eclipse.scout.rt.shared.ScoutTexts;
import org.eclipse.scout.rt.shared.services.lookup.ILookupRow;

public class ContentAssistTableForm<KEY_TYPE> extends AbstractContentAssistFieldProposalForm<KEY_TYPE> {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(ContentAssistTableForm.class);

  private JobEx m_dataLoadJob;
  private KEY_TYPE m_lastSelectedKey;

  public ContentAssistTableForm(IContentAssistField<?, KEY_TYPE> contentAssistField, boolean allowCustomText) throws ProcessingException {
    super(contentAssistField, allowCustomText);
  }

  @Override
  public void forceProposalSelection() throws ProcessingException {
    @SuppressWarnings("unchecked")
    ContentAssistTableForm<KEY_TYPE>.MainBox.ResultTableField tableField = getResultTableField();
    ContentAssistTableForm.MainBox.ResultTableField.Table table = tableField.getTable();
    table.selectNextRow();
  }

  @SuppressWarnings("unchecked")
  @Override
  protected void dataFetchedDelegateImpl(IContentAssistFieldDataFetchResult<KEY_TYPE> result, int maxCount) {
    List<? extends ILookupRow<KEY_TYPE>> rows = null;
    boolean selectCurrentValue = false;
    ProcessingException failed = null;
    String searchText = null;
    if (result != null) {
      rows = result.getLookupRows();
      selectCurrentValue = result.isSelectCurrentValue();
      failed = result.getProcessingException();
      searchText = result.getSearchText();
    }
    if (rows == null) {
      rows = new ArrayList<ILookupRow<KEY_TYPE>>();
    }
    try {
      // populate table
      ResultTableField.Table table = (Table) getResultTableField().getTable();

      int n = rows.size();
      if (maxCount > 0) {
        n = Math.min(n, maxCount);
      }
      List<ITableRow> tableRows = new ArrayList<ITableRow>();
      for (int i = 0; i < n; i++) {
        ITableRow row = table.createRow(new Object[]{rows.get(i), null});
        tableRows.add(row);
        row.setEnabled(rows.get(i).isEnabled());
      }
      try {
        table.setTableChanging(true);
        table.discardAllRows();
        table.addRows(tableRows);
        //restore selection
        KEY_TYPE keyToSelect = null;
        if (selectCurrentValue) {
          m_lastSelectedKey = getContentAssistField().getValueAsLookupKey();
          keyToSelect = m_lastSelectedKey;
        }
        else if (table.getRowCount() == 1 && !isAllowCustomText()) {
          // select first
          keyToSelect = (KEY_TYPE) table.getKeyColumn().getValue(0).getKey();
        }
        if (keyToSelect != null) {
          for (ITableRow row : table.getRows()) {
            if (CompareUtility.equals(keyToSelect, table.getKeyColumn().getValue(row).getKey())) {
              table.selectRow(row);
              if (table.isCheckable()) {
                table.checkRow(row, true);
              }
              break;
            }
          }
        }
      }
      finally {
        table.setTableChanging(false);
      }
      String statusText = null;
      int severity = ProcessingStatus.INFO;
      if (failed != null) {
        statusText = failed.getStatus().getMessage();
        severity = ProcessingStatus.ERROR;
      }
      else if (rows.size() <= 0) {
        statusText = ScoutTexts.get("SmartFieldCannotComplete", (searchText == null) ? ("") : (searchText));
        severity = ProcessingStatus.WARNING;
      }
      else if (rows.size() > getContentAssistField().getBrowseMaxRowCount()) {
        statusText = ScoutTexts.get("SmartFieldMoreThanXRows", "" + getContentAssistField().getBrowseMaxRowCount());
        severity = ProcessingStatus.INFO;
      }
      if (statusText != null) {
        getResultTableField().setTablePopulateStatus(new ProcessingStatus(statusText, severity));
      }
      else {
        getResultTableField().setTablePopulateStatus(null);
      }
      if (getNewButton().isEnabled()) {
        getNewButton().setVisible(table.getRowCount() <= 0);
      }
      structureChanged(getResultTableField());

    }
    catch (ProcessingException e) {
      LOG.warn("update proposal list", e);
    }
  }

  @Override
  public void setTablePopulateStatus(IProcessingStatus status) {
    getResultTableField().setTablePopulateStatus(status);
  }

  /**
   * Override this method to change that behavior of what is a single match.
   * <p>
   * By default a single match is when there is only one enabled row in the drop down table
   * </p>
   */
  @ConfigOperation
  @Order(120)
  protected ILookupRow<KEY_TYPE> execGetSingleMatch() {
    int matchCount = 0;
    ILookupRow<KEY_TYPE> foundRow = null;
    Table table = (Table) getResultTableField().getTable();
    @SuppressWarnings("unchecked")
    List<ILookupRow<KEY_TYPE>> values = table.getKeyColumn().getValues();
    for (ILookupRow<KEY_TYPE> row : values) {
      if (row.isEnabled()) {
        foundRow = row;
        matchCount++;
      }
    }
    if (matchCount == 1) {
      return foundRow;
    }
    else {
      return null;
    }
  }

  /**
   * Override this method to change the behaviour when a row is clicked in the result {@link Table}.
   * <p>
   * By default the form is closed with {@link #doOk()}.
   * </p>
   * 
   * @param row
   * @throws ProcessingException
   */
  @ConfigOperation
  @Order(130)
  protected void execResultTableRowClicked(ITableRow row) throws ProcessingException {
    @SuppressWarnings("unchecked")
    ILookupRow<KEY_TYPE> lrow = (ILookupRow<KEY_TYPE>) ((Table) getResultTableField().getTable()).getKeyColumn().getSelectedValue();
    if (lrow != null && lrow.isEnabled()) {
      doOk();
    }
  }

  /**
   * Override this method to adapt the menu list of the result {@link Table}
   */
  protected void injectResultTableMenus(List<IMenu> menuList) {
  }

  @Override
  public ILookupRow<KEY_TYPE> getAcceptedProposal() throws ProcessingException {
    ILookupRow<KEY_TYPE> row = getSelectedLookupRow();
    if (row != null && row.isEnabled()) {
      return row;
    }
    else if (isAllowCustomText()) {
      return null;
    }
    else {
      return execGetSingleMatch();
    }
  }

  @SuppressWarnings("unchecked")
  public ILookupRow<KEY_TYPE> getSelectedLookupRow() {
    Table table = (Table) getResultTableField().getTable();
    ILookupRow<KEY_TYPE> row = null;
    if (table.isCheckable()) {
      Collection<ITableRow> checkedRows = table.getCheckedRows();
      if (CollectionUtility.hasElements(checkedRows)) {
        row = (ILookupRow<KEY_TYPE>) table.getKeyColumn().getValue(CollectionUtility.firstElement(checkedRows));
      }
    }
    else {
      row = (ILookupRow<KEY_TYPE>) table.getKeyColumn().getSelectedValue();
    }

    return row;
  }

  /*
   * Dialog start
   */
  @Override
  public void startForm() throws ProcessingException {
    startInternal(new FormHandler());
  }

  @SuppressWarnings("unchecked")
  public MainBox getMainBox() {
    return (MainBox) getRootGroupBox();
  }

  public ResultTableField getResultTableField() {
    return getFieldByClass(ResultTableField.class);
  }

  public ActiveStateRadioButtonGroup getActiveStateRadioButtonGroup() {
    return getFieldByClass(ActiveStateRadioButtonGroup.class);
  }

  /*
   * Fields
   */
  public NewButton getNewButton() {
    return getFieldByClass(NewButton.class);
  }

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
    protected boolean getConfiguredGridUseUiWidth() {
      return true;
    }

    @Override
    protected boolean getConfiguredGridUseUiHeight() {
      return true;
    }

    @Order(10)
    public class ResultTableField extends AbstractTableField<ContentAssistTableForm.MainBox.ResultTableField.Table> {

      public ResultTableField() {
        super();
      }

      @Override
      protected boolean getConfiguredLabelVisible() {
        return false;
      }

      @Override
      protected double getConfiguredGridWeightY() {
        return 1;
      }

      @Override
      protected boolean getConfiguredGridUseUiWidth() {
        return true;
      }

      @Override
      protected boolean getConfiguredGridUseUiHeight() {
        return true;
      }

      @Override
      protected boolean getConfiguredTableStatusVisible() {
        return true;
      }

      @Override
      protected void execUpdateTableStatus() {
        //nop
      }

      /*
       * inner table
       */
      @Order(4)
      public class Table extends AbstractTable {

        @Override
        protected void injectMenusInternal(List<IMenu> menuList) {
          injectResultTableMenus(menuList);
        }

        @Override
        protected boolean getConfiguredAutoResizeColumns() {
          return true;
        }

        @Override
        protected boolean getConfiguredHeaderVisible() {
          return false;
        }

        @Override
        protected boolean getConfiguredMultiSelect() {
          return false;
        }

        @Override
        protected boolean getConfiguredMultiCheck() {
          return false;
        }

        @Override
        protected boolean getConfiguredScrollToSelection() {
          return true;
        }

        @Override
        protected void execRowClick(ITableRow row) throws ProcessingException {
          execResultTableRowClicked(row);
        }

        @SuppressWarnings("unchecked")
        public KeyColumn getKeyColumn() {
          return getColumnSet().getColumnByClass(KeyColumn.class);
        }

        @SuppressWarnings("unchecked")
        public TextColumn getTextColumn() {
          return getColumnSet().getColumnByClass(TextColumn.class);
        }

        @Order(1)
        public class KeyColumn extends AbstractColumn<ILookupRow<KEY_TYPE>> {
          @Override
          protected boolean getConfiguredPrimaryKey() {
            return true;
          }

          @Override
          protected boolean getConfiguredDisplayable() {
            return false;
          }
        }

        @Order(2)
        public class TextColumn extends AbstractStringColumn {
          @Override
          protected void execDecorateCell(Cell cell, ITableRow row) {
            ILookupRow<KEY_TYPE> lookupRow = getKeyColumn().getValue(row);
            cell.setText(lookupRow.getText());
            cell.setTooltipText(lookupRow.getTooltipText());
            cell.setBackgroundColor(lookupRow.getBackgroundColor());
            cell.setForegroundColor(lookupRow.getForegroundColor());
            cell.setFont(lookupRow.getFont());
            if (getResultTableField().getTable().getDefaultIconId() == null) {
              if (lookupRow.getIconId() != null) {
                cell.setIconId(lookupRow.getIconId());
              }
            }
          }
        }
      }
    }

    @Order(30)
    public class ActiveStateRadioButtonGroup extends AbstractRadioButtonGroup<TriState> {

      @Override
      protected boolean getConfiguredLabelVisible() {
        return false;
      }

      @Override
      protected void execChangedValue() throws ProcessingException {
        if (isVisible() && !isFormLoading()) {
          getContentAssistField().setActiveFilter(getValue());
          getContentAssistField().doSearch(false, false);
        }
      }

      @Order(10)
      public class ActiveButton extends AbstractButton {

        @Override
        protected int getConfiguredDisplayStyle() {
          return DISPLAY_STYLE_RADIO;
        }

        @Override
        protected String getConfiguredLabel() {
          return ScoutTexts.get("ActiveStates");
        }

        @Override
        protected Object getConfiguredRadioValue() {
          return TriState.TRUE;
        }
      }

      @Order(20)
      public class InactiveButton extends AbstractButton {

        @Override
        protected int getConfiguredDisplayStyle() {
          return DISPLAY_STYLE_RADIO;
        }

        @Override
        protected String getConfiguredLabel() {
          return ScoutTexts.get("InactiveStates");
        }

        @Override
        protected Object getConfiguredRadioValue() {
          return TriState.FALSE;
        }
      }

      @Order(30)
      public class ActiveAndInactiveButton extends AbstractButton {

        @Override
        protected int getConfiguredDisplayStyle() {
          return DISPLAY_STYLE_RADIO;
        }

        @Override
        protected String getConfiguredLabel() {
          return ScoutTexts.get("ActiveAndInactiveStates");
        }

        @Override
        protected Object getConfiguredRadioValue() {
          return TriState.UNDEFINED;
        }
      }
    }

    @Order(40)
    public class NewButton extends AbstractButton {
      @Override
      protected boolean getConfiguredLabelVisible() {
        return false;
      }

      @Override
      protected boolean getConfiguredVisible() {
        return false;
      }

      @Override
      protected boolean getConfiguredEnabled() {
        return false;
      }

      @Override
      protected boolean getConfiguredFillHorizontal() {
        return false;
      }

      @Override
      protected int getConfiguredDisplayStyle() {
        return DISPLAY_STYLE_LINK;
      }

      @Override
      protected boolean getConfiguredProcessButton() {
        return false;
      }

      @Override
      protected void execClickAction() throws ProcessingException {
        getContentAssistField().doBrowseNew(getSearchText());
      }
    }// end field

  }// end main box

  /*
   * handlers
   */
  private class FormHandler extends AbstractFormHandler {

    @SuppressWarnings("unchecked")
    @Override
    protected void execLoad() throws ProcessingException {
      getResultTableField().getTable().setDefaultIconId(getContentAssistField().getBrowseIconId());
      getActiveStateRadioButtonGroup().setVisible(getContentAssistField().isActiveFilterEnabled());
      getActiveStateRadioButtonGroup().setValue((TriState) getContentAssistField().getActiveFilter());
      getNewButton().setEnabled(getContentAssistField().getBrowseNewText() != null);
      getNewButton().setLabel(getContentAssistField().getBrowseNewText());
    }

    @Override
    protected boolean execValidate() throws ProcessingException {
      return getAcceptedProposal() != null || isAllowCustomText();
    }

    @Override
    protected void execFinally() throws ProcessingException {
      if (m_dataLoadJob != null) {
        m_dataLoadJob.cancel();
      }
    }
  }

}
