/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.ui.form.fields.smartfield;

import java.util.Collection;
import java.util.List;

import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.commons.annotations.ConfigOperation;
import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.commons.status.IStatus;
import org.eclipse.scout.commons.status.Status;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.basic.table.TableEvent;
import org.eclipse.scout.rt.client.ui.basic.table.TableListener;
import org.eclipse.scout.rt.shared.ScoutTexts;
import org.eclipse.scout.rt.shared.services.lookup.ILookupRow;

public class TableProposalChooser<LOOKUP_KEY> extends AbstractProposalChooser<IContentAssistFieldTable<LOOKUP_KEY>, LOOKUP_KEY> {

  private static final IScoutLogger LOG = ScoutLogManager.getLogger(TableProposalChooser.class);

  static class P_Table<LOOKUP_KEY> extends ContentAssistFieldTable<LOOKUP_KEY> {
  }

  private LOOKUP_KEY m_lastSelectedKey;

  public TableProposalChooser(IContentAssistField<?, LOOKUP_KEY> contentAssistField, boolean allowCustomText) throws ProcessingException {
    super(contentAssistField, allowCustomText);
  }

  @Override
  protected IContentAssistFieldTable createModel() {
    IContentAssistFieldTable<LOOKUP_KEY> table = new P_Table<LOOKUP_KEY>();
    table.setMultilineText(m_contentAssistField.isMultilineText());
    table.addTableListener(new TableListener() {

      @Override
      public void tableChangedBatch(List<? extends TableEvent> batch) {
      }

      @Override
      public void tableChanged(TableEvent e) {
        if (e.getType() == TableEvent.TYPE_ROW_CLICK) {
          try {
            execResultTableRowClicked(CollectionUtility.firstElement(e.getRows()));
          }
          catch (ProcessingException e1) {
            LOG.warn("could not handle smart table selection.", e);
          }
        }
      }
    });
    // FIXME AWE: (smart-field) check if we must support this method
    // execDecorateTable(table);
    return table;
  }

  @Override
  public ILookupRow<LOOKUP_KEY> getSelectedLookupRow() {
    ILookupRow<LOOKUP_KEY> row = null;
    if (m_model.isCheckable()) {
      Collection<ITableRow> checkedRows = m_model.getCheckedRows();
      if (CollectionUtility.hasElements(checkedRows)) {
        row = (ILookupRow<LOOKUP_KEY>) m_model.getCheckedLookupRow();
      }
    }
    else {
      row = (ILookupRow<LOOKUP_KEY>) m_model.getSelectedLookupRow();
    }
    return row;
  }

  @Override
  public void dispose() {
    m_model.disposeTable();
    m_model = null;
  }

  @Override
  public void forceProposalSelection() throws ProcessingException {
    m_model.selectNextRow();
  }

  @SuppressWarnings("deprecation")
  @Override
  protected void dataFetchedDelegateImpl(IContentAssistFieldDataFetchResult<LOOKUP_KEY> result, int maxCount) {
    List<? extends ILookupRow<LOOKUP_KEY>> rows = null;
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
      rows = CollectionUtility.emptyArrayList();
    }

    try {
      // populate table
      m_model.setTableChanging(true);
      m_model.setLookupRows(CollectionUtility.truncateList(rows, maxCount));
      try {
        //restore selection
        LOOKUP_KEY keyToSelect = null;
        if (selectCurrentValue) {
          m_lastSelectedKey = m_contentAssistField.getValueAsLookupKey();
          keyToSelect = m_lastSelectedKey;
        }
        else if (rows.size() == 1 && !isAllowCustomText()) {
          // select first
          keyToSelect = CollectionUtility.firstElement(rows).getKey();
        }
        if (keyToSelect != null) {
          m_model.select(keyToSelect);
        }
      }
      finally {
        m_model.setTableChanging(false);
      }
      String statusText = null;
      int severity = IStatus.INFO;
      if (failed != null) {
        statusText = failed.getStatus().getMessage();
        severity = IStatus.ERROR;
      }
      else if (rows.size() <= 0) {
        statusText = ScoutTexts.get("SmartFieldCannotComplete", (searchText == null) ? ("") : (searchText));
        severity = IStatus.WARNING;
      }
      else if (rows.size() > m_contentAssistField.getBrowseMaxRowCount()) {
        statusText = ScoutTexts.get("SmartFieldMoreThanXRows", "" + m_contentAssistField.getBrowseMaxRowCount());
        severity = IStatus.INFO;
      }
      if (statusText != null) {
        setStatus(new Status(statusText, severity));
      }
      else {
        setStatus(null);
      }
      fireStructureChanged();
    }
    catch (ProcessingException e) {
      LOG.warn("update proposal list", e);
    }
  }

  /**
   * Override this method to change that behavior of what is a single match.
   * <p>
   * By default a single match is when there is only one enabled row in the drop down table
   * </p>
   */
  @ConfigOperation
  @Order(120)
  @Override
  protected ILookupRow<LOOKUP_KEY> execGetSingleMatch() {
    int matchCount = 0;
    ILookupRow<LOOKUP_KEY> foundRow = null;
    List<ILookupRow<LOOKUP_KEY>> values = m_model.getLookupRows();
    for (ILookupRow<LOOKUP_KEY> row : values) {
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
   * Override this method to change the behavior when a row is clicked in the result {@link IContentAssistFieldTable}.
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
    // FIXME AWE: (smart-field) ausprobieren: diese methode loswerden und alles UI seitig machen...
    // das smartfield schcikt dann einen value-change, der wert wird aus der table übernommen (gui seitig)
    // die tabelle braucht dann keine select/click events mehr an den server zu schicken.
    ILookupRow<LOOKUP_KEY> lrow = m_model.getSelectedLookupRow();
    if (lrow != null && lrow.isEnabled()) {
      doOk();
    }
  }

  @Override
  public void deselect() {
    m_model.deselectAllRows();
    System.out.println("deselect!");
  }

}
