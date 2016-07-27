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
package org.eclipse.scout.rt.client.ui.form.fields.smartfield;

import java.util.Collection;
import java.util.List;

import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.basic.table.TableAdapter;
import org.eclipse.scout.rt.client.ui.basic.table.TableEvent;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.annotations.ConfigOperation;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.shared.services.lookup.ILookupRow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Proposal chooser with a table to choose proposals. You can provide your own table implementation when your
 * smart-field defines an inner ContentAssistFieldTable class.
 *
 * @since 6.0.0
 * @param <LOOKUP_KEY>
 */
public class TableProposalChooser<LOOKUP_KEY> extends AbstractProposalChooser<IContentAssistFieldTable<LOOKUP_KEY>, LOOKUP_KEY> {

  private static final Logger LOG = LoggerFactory.getLogger(TableProposalChooser.class);

  private LOOKUP_KEY m_lastSelectedKey;

  public TableProposalChooser(IContentAssistField<?, LOOKUP_KEY> contentAssistField, boolean allowCustomText) {
    super(contentAssistField, allowCustomText);
  }

  @Override
  protected IContentAssistFieldTable createModel() {
    IContentAssistFieldTable<LOOKUP_KEY> table = createConfiguredOrDefaultModel(IContentAssistFieldTable.class);
    table.setMultilineText(m_contentAssistField.isMultilineText());
    table.addTableListener(new TableAdapter() {
      @Override
      public void tableChanged(TableEvent e) {
        if (e.getType() == TableEvent.TYPE_ROW_CLICK) {
          try {
            execResultTableRowClicked(CollectionUtility.firstElement(e.getRows()));
          }
          catch (RuntimeException e1) {
            LOG.warn("could not handle smart table selection.", e);
          }
        }
      }
    });
    return table;
  }

  @Override
  protected IContentAssistFieldTable<LOOKUP_KEY> createDefaultModel() {
    return new P_DefaultProposalTable<>();
  }

  @Override
  public ILookupRow<LOOKUP_KEY> getSelectedLookupRow() {
    ILookupRow<LOOKUP_KEY> row = null;
    if (m_model.isCheckable()) {
      Collection<ITableRow> checkedRows = m_model.getCheckedRows();
      if (CollectionUtility.hasElements(checkedRows)) {
        row = m_model.getCheckedLookupRow();
      }
    }
    else {
      row = m_model.getSelectedLookupRow();
    }
    return row;
  }

  @Override
  public void dispose() {
    m_model.disposeTable();
    m_model = null;
  }

  @Override
  public void forceProposalSelection() {
    m_model.selectNextRow();
  }

  @Override
  protected void dataFetchedDelegateImpl(IContentAssistFieldDataFetchResult<LOOKUP_KEY> result, int maxCount) {
    List<? extends ILookupRow<LOOKUP_KEY>> rows = null;
    boolean selectCurrentValue = false;
    String searchText = null;
    if (result != null) {
      rows = result.getLookupRows();
      selectCurrentValue = result.getSearchParam().isSelectCurrentValue();
      searchText = result.getSearchParam().getSearchText();
    }
    if (rows == null) {
      rows = CollectionUtility.emptyArrayList();
    }
    try {
      // populate table
      m_model.setTableChanging(true);
      m_model.setLookupRows(CollectionUtility.truncateList(rows, maxCount));
      try {
        // restore selection
        LOOKUP_KEY keyToSelect = null;
        if (selectCurrentValue) {
          m_lastSelectedKey = m_contentAssistField.getValueAsLookupKey();
          keyToSelect = m_lastSelectedKey;
        }
        else if (rows.size() == 1
            && !isAllowCustomText()
            && result != null
            && !m_contentAssistField.getWildcard().equals(searchText)) {
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
      updateStatus(result);
    }
    catch (RuntimeException e) {
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
   */
  @ConfigOperation
  @Order(130)
  protected void execResultTableRowClicked(ITableRow row) {
    ILookupRow<LOOKUP_KEY> lrow = m_model.getSelectedLookupRow();
    if (lrow != null && lrow.isEnabled()) {
      m_contentAssistField.acceptProposal();
    }
  }

  @Override
  public void deselect() {
    m_model.deselectAllRows();
  }

  /**
   * Default table class used when smart-field doesn't provide a custom table class.
   */
  static class P_DefaultProposalTable<LOOKUP_KEY> extends ContentAssistFieldTable<LOOKUP_KEY> {
  }

}
