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

import org.eclipse.scout.commons.CompareUtility;
import org.eclipse.scout.commons.TriState;
import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.exception.ProcessingStatus;
import org.eclipse.scout.commons.job.JobEx;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ui.basic.cell.Cell;
import org.eclipse.scout.rt.client.ui.basic.table.AbstractTable;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractStringColumn;
import org.eclipse.scout.rt.client.ui.form.AbstractFormHandler;
import org.eclipse.scout.rt.client.ui.form.fields.button.AbstractButton;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.radiobuttongroup.AbstractRadioButtonGroup;
import org.eclipse.scout.rt.client.ui.form.fields.smartfield.SmartTableForm.MainBox.ActiveStateRadioButtonGroup;
import org.eclipse.scout.rt.client.ui.form.fields.smartfield.SmartTableForm.MainBox.NewButton;
import org.eclipse.scout.rt.client.ui.form.fields.smartfield.SmartTableForm.MainBox.ResultTableField;
import org.eclipse.scout.rt.client.ui.form.fields.tablefield.AbstractTableField;
import org.eclipse.scout.rt.shared.ScoutTexts;
import org.eclipse.scout.rt.shared.services.lookup.ILookupCallFetcher;
import org.eclipse.scout.rt.shared.services.lookup.LookupRow;

public class SmartTableForm extends AbstractSmartFieldProposalForm {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(SmartTableForm.class);

  private JobEx m_dataLoadJob;
  private Object m_lastSelectedKey;

  public SmartTableForm(ISmartField<?> smartField) throws ProcessingException {
    super(smartField);
  }

  @Override
  public void forceProposalSelection() throws ProcessingException {
    ResultTableField.Table table = getResultTableField().getTable();
    table.selectNextRow();
  }

  @Override
  public void update(final boolean selectCurrentValue, boolean synchronous) throws ProcessingException {
    String text = getSearchText();
    if (text == null) {
      text = "";
    }
    final String textNonNull = text;
    final int maxCount = getSmartField().getBrowseMaxRowCount();
    getResultTableField().setTablePopulateStatus(new ProcessingStatus(ScoutTexts.get("searchingProposals"), ProcessingStatus.WARNING));
    //async load of data
    if (m_dataLoadJob != null) {
      m_dataLoadJob.cancel();
    }
    ILookupCallFetcher fetcher = new ILookupCallFetcher() {
      @Override
      public void dataFetched(LookupRow[] rows, ProcessingException failed) {
        dataFetchedDelegate(rows, failed, maxCount, selectCurrentValue);
      }
    };
    // go async/sync
    if (synchronous) {
      try {
        LookupRow[] rows;
        if (ISmartField.BROWSE_ALL_TEXT.equals(text)) {
          rows = getSmartField().callBrowseLookup(text, maxCount > 0 ? maxCount + 1 : 0);
        }
        else if (text.length() == 0) {
          rows = getSmartField().callBrowseLookup(text, maxCount > 0 ? maxCount + 1 : 0);
        }
        else {
          rows = getSmartField().callTextLookup(text, maxCount > 0 ? maxCount + 1 : 0);
        }
        fetcher.dataFetched(rows, null);
      }
      catch (ProcessingException e) {
        fetcher.dataFetched(null, e);
      }
    }
    else {

      if (ISmartField.BROWSE_ALL_TEXT.equals(textNonNull)) {
        m_dataLoadJob = getSmartField().callBrowseLookupInBackground(textNonNull, maxCount > 0 ? maxCount + 1 : 0, fetcher);
      }
      else if (textNonNull.length() == 0) {
        m_dataLoadJob = getSmartField().callBrowseLookupInBackground(textNonNull, maxCount > 0 ? maxCount + 1 : 0, fetcher);
      }
      else {
        m_dataLoadJob = getSmartField().callTextLookupInBackground(textNonNull, maxCount > 0 ? maxCount + 1 : 0, fetcher);
      }
    }
  }

  private void dataFetchedDelegate(LookupRow[] rows, ProcessingException failed, int maxCount, boolean selectCurrentValue) {
    try {
      // populate table
      ResultTableField.Table table = getResultTableField().getTable();
      if (rows == null) {
        rows = new LookupRow[0];
      }
      int n = rows.length;
      if (maxCount > 0) n = Math.min(n, maxCount);
      ITableRow[] tableRows = new ITableRow[n];
      for (int i = 0; i < n; i++) {
        tableRows[i] = table.createRow(new Object[]{rows[i], null});
        tableRows[i].setEnabled(rows[i].isEnabled());
      }
      try {
        table.setTableChanging(true);
        table.discardAllRows();
        table.addRows(tableRows);
        //restore selection
        Object keyToSelect = null;
        if (selectCurrentValue) {
          m_lastSelectedKey = getSmartField().getValue();
          keyToSelect = m_lastSelectedKey;
        }
        else if (table.getRowCount() == 1 && !getSmartField().isAllowCustomText()) {
          // select first
          keyToSelect = table.getKeyColumn().getValue(0).getKey();
        }
        if (keyToSelect != null) {
          for (ITableRow row : table.getRows()) {
            if (CompareUtility.equals(keyToSelect, table.getKeyColumn().getValue(row).getKey())) {
              table.selectRow(row);
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
      else if (rows.length <= 0) {
        statusText = ScoutTexts.get("SmartFieldCannotComplete", getSearchText());
        severity = ProcessingStatus.WARNING;
      }
      else if (rows.length > getSmartField().getBrowseMaxRowCount()) {
        statusText = ScoutTexts.get("SmartFieldMoreThanXRows", "" + getSmartField().getBrowseMaxRowCount());
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

  private LookupRow getSingleMatch() {
    int matchCount = 0;
    LookupRow foundRow = null;
    for (LookupRow row : getResultTableField().getTable().getKeyColumn().getValues()) {
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

  @Override
  public LookupRow getAcceptedProposal() throws ProcessingException {
    LookupRow row = getResultTableField().getTable().getKeyColumn().getSelectedValue();
    if (row != null && row.isEnabled()) {
      return row;
    }
    else if (getSmartField().isAllowCustomText()) {
      return null;
    }
    else {
      return getSingleMatch();
    }
  }

  /*
   * Dialog start
   */
  @Override
  public void startForm() throws ProcessingException {
    startInternal(new FormHandler());
  }

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
    public class ResultTableField extends AbstractTableField<ResultTableField.Table> {

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
        protected boolean getConfiguredScrollToSelection() {
          return true;
        }

        @Override
        protected void execRowClick(ITableRow row) throws ProcessingException {
          LookupRow lrow = getKeyColumn().getSelectedValue();
          if (lrow != null && lrow.isEnabled()) {
            doOk();
          }
        }

        public KeyColumn getKeyColumn() {
          return getColumnSet().getColumnByClass(KeyColumn.class);
        }

        public TextColumn getTextColumn() {
          return getColumnSet().getColumnByClass(TextColumn.class);
        }

        @Order(1)
        public class KeyColumn extends AbstractColumn<LookupRow> {
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
            LookupRow lookupRow = getKeyColumn().getValue(row);
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
          getSmartField().setActiveFilter(getValue());
          update(false, false);
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
        getSmartField().doBrowseNew(getSearchText());
      }
    }// end field

  }// end main box

  /*
   * handlers
   */
  private class FormHandler extends AbstractFormHandler {

    @Override
    protected void execLoad() throws ProcessingException {
      getResultTableField().getTable().setDefaultIconId(getSmartField().getBrowseIconId());
      getActiveStateRadioButtonGroup().setVisible(getSmartField().isActiveFilterEnabled());
      getActiveStateRadioButtonGroup().setValue(getSmartField().getActiveFilter());
      getNewButton().setEnabled(getSmartField().getBrowseNewText() != null);
      getNewButton().setLabel(getSmartField().getBrowseNewText());
    }

    @Override
    protected boolean execValidate() throws ProcessingException {
      return getAcceptedProposal() != null;
    }

    @Override
    protected void execFinally() throws ProcessingException {
      if (m_dataLoadJob != null) {
        m_dataLoadJob.cancel();
      }
    }
  }

}
