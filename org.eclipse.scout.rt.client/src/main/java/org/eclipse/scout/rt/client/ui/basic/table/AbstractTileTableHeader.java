/*
 * Copyright (c) 2010-2021 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.client.ui.basic.table;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.scout.rt.client.ui.ClientUIPreferences;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractBooleanColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractDateColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractIconColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractSmartColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IColumn;
import org.eclipse.scout.rt.client.ui.form.fields.LogicalGridLayoutConfig;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.placeholder.AbstractPlaceholderField;
import org.eclipse.scout.rt.client.ui.form.fields.smartfield.AbstractSmartField;
import org.eclipse.scout.rt.client.ui.form.fields.smartfield.ISmartField;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.classid.ClassId;
import org.eclipse.scout.rt.platform.text.TEXTS;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.ImmutablePair;
import org.eclipse.scout.rt.platform.util.ObjectUtility;
import org.eclipse.scout.rt.shared.data.basic.FontSpec;
import org.eclipse.scout.rt.shared.services.lookup.ILookupCall;
import org.eclipse.scout.rt.shared.services.lookup.ILookupRow;
import org.eclipse.scout.rt.shared.services.lookup.LocalLookupCall;
import org.eclipse.scout.rt.shared.services.lookup.LookupRow;

@ClassId("c6ee18fd-e630-4d92-81b1-cd0147c902d4")
public abstract class AbstractTileTableHeader extends AbstractGroupBox implements ITileTableHeader {

  private boolean m_isGrouping;
  private boolean m_isSorting;

  protected TableListener createTableListener() {
    return new TableAdapter() {

      @Override
      public void tableChanged(TableEvent e) {
        handleTableEvent(e);
      }
    };
  }

  protected void handleTableEvent(TableEvent e) {
    switch (e.getType()) {
      case TableEvent.TYPE_COLUMN_HEADERS_UPDATED:
        syncSortingGroupingFields();
        break;
    }
  }

  protected void syncSortingGroupingFields() {
    try {
      // don't call execChangedValue since it would trigger sort/group again
      getSortByField().setValueChangeTriggerEnabled(false);
      getGroupByField().setValueChangeTriggerEnabled(false);
      if (!m_isSorting) {
        IColumn<?> primarySortColumn = CollectionUtility.firstElement(getTable().getColumnSet().getSortColumns());
        if (primarySortColumn != null) {
          getSortByField().setValue(new ImmutablePair<>(primarySortColumn, primarySortColumn.isSortAscending()));
        }
        else {
          getSortByField().setValue(null);
        }
      }
      if (!m_isGrouping) {
        IColumn<?> primaryGroupColumn = CollectionUtility.firstElement(getTable().getColumnSet().getGroupedColumns());
        getGroupByField().setValue(primaryGroupColumn);
      }
    }
    finally {
      getSortByField().setValueChangeTriggerEnabled(true);
      getGroupByField().setValueChangeTriggerEnabled(true);
    }
  }

  protected ITable getTable() {
    if (getParent() instanceof ITable) {
      return (ITable) getParent();
    }
    // shouldn't happen
    return null;
  }

  @Override
  protected void execInitField() {
    super.execInitField();
    TableListener tableListener = createTableListener();
    getTable().addTableListener(tableListener);
    getGroupByField().setVisible(getTable().getColumnSet().isUserColumnGroupingAllowed());
    getSortByField().setVisible(getTable().isSortEnabled());
    // execute once for default values
    syncSortingGroupingFields();
  }

  @Override
  protected int getConfiguredGridColumnCount() {
    return 7;
  }

  @Override
  protected LogicalGridLayoutConfig getConfiguredBodyLayoutConfig() {
    return super.getConfiguredBodyLayoutConfig()
        .withHGap(8);
  }

  public PlaceholderField getPlaceholderField() {
    return getFieldByClass(PlaceholderField.class);
  }

  public GroupByField getGroupByField() {
    return getFieldByClass(GroupByField.class);
  }

  public SortByField getSortByField() {
    return getFieldByClass(SortByField.class);
  }

  @Order(200)
  @ClassId("30293777-ff47-4cef-a068-408524dcdf1a")
  public class PlaceholderField extends AbstractPlaceholderField {

    @Override
    protected int getConfiguredGridW() {
      return 5;
    }
  }

  @Order(300)
  @ClassId("d2dd9b5a-5944-4a0d-afab-8bc89d31eed5")
  public class GroupByField extends AbstractSmartField<IColumn> {

    @Override
    protected String getConfiguredLabel() {
      return TEXTS.get("GroupBy");
    }

    @Override
    protected boolean getConfiguredVisible() {
      return false;
    }

    @Override
    protected byte getConfiguredLabelPosition() {
      return LABEL_POSITION_ON_FIELD;
    }

    @Override
    protected String getConfiguredClearable() {
      return CLEARABLE_ALWAYS;
    }

    @Override
    protected boolean getConfiguredStatusVisible() {
      return false;
    }

    @Override
    protected String getConfiguredDisplayStyle() {
      return ISmartField.DISPLAY_STYLE_DROPDOWN;
    }

    @Override
    protected void initConfig() {
      super.initConfig();
      setLookupCall(createGroupByLookupCall());
    }

    @Override
    protected void execChangedValue() {
      try {
        m_isGrouping = true;
        if (getValue() == null) {
          getTable().getColumnSet().removeGroupColumn(CollectionUtility.firstElement(getTable().getColumnSet().getGroupedColumns()));
        }
        else {
          getTable().getColumnSet().handleGroupingEvent(getValue(), false, true);
        }
        ClientUIPreferences.getInstance().setAllTableColumnPreferences(getTable());
        getTable().sort();
      }
      finally {
        m_isGrouping = false;
      }
    }
  }

  @Order(400)
  @ClassId("7f20aa82-9059-4eaf-9e9d-fcb537114c63")
  public class SortByField extends AbstractSmartField<ImmutablePair<IColumn, Boolean>> {

    @Override
    protected String getConfiguredLabel() {
      return TEXTS.get("SortBy");
    }

    @Override
    protected boolean getConfiguredVisible() {
      return false;
    }

    @Override
    protected byte getConfiguredLabelPosition() {
      return LABEL_POSITION_ON_FIELD;
    }

    @Override
    protected String getConfiguredClearable() {
      return CLEARABLE_ALWAYS;
    }

    @Override
    protected boolean getConfiguredStatusVisible() {
      return false;
    }

    @Override
    protected String getConfiguredDisplayStyle() {
      return ISmartField.DISPLAY_STYLE_DROPDOWN;
    }

    @Override
    protected void initConfig() {
      super.initConfig();
      setLookupCall(createSortByLookupCall());
    }

    @Override
    protected void execChangedValue() {
      try {
        m_isSorting = true;
        if (getValue() == null) {
          getTable().getColumnSet().removeSortColumn(CollectionUtility.firstElement(getTable().getColumnSet().getSortColumns()));
        }
        else {
          getTable().getColumnSet().handleSortEvent(getValue().getLeft(), false, getValue().getRight());
        }
        ClientUIPreferences.getInstance().setAllTableColumnPreferences(getTable());
        getTable().sort();
      }
      finally {
        m_isSorting = false;
      }
    }
  }

  protected ILookupCall<IColumn> createGroupByLookupCall() {
    return new P_GroupByLookupCall();
  }

  protected ILookupCall<ImmutablePair<IColumn, Boolean>> createSortByLookupCall() {
    return new P_SortByLookupCall();
  }

  protected boolean isColumnTypeAllowedForGrouping(IColumn col) {
    return col instanceof AbstractSmartColumn
        || col instanceof AbstractDateColumn
        || col instanceof AbstractBooleanColumn;
  }

  protected boolean isColumnTypeAllowedForSorting(IColumn col) {
    return !(col instanceof AbstractIconColumn);
  }

  @ClassId("dbf260be-ee6c-4f6f-99c6-9b7bcbdf7d61")
  protected class P_GroupByLookupCall extends LocalLookupCall<IColumn> {
    private static final long serialVersionUID = 1L;

    @Override
    protected List<? extends ILookupRow<IColumn>> execCreateLookupRows() {
      final List<LookupRow<IColumn>> lookupRows = new ArrayList<>();

      lookupRows.add(new LookupRow<IColumn>(null, TEXTS.get("NoGrouping"))
          .withFont(new FontSpec(null, FontSpec.STYLE_BOLD, 0)));

      for (IColumn col : getTable().getColumns()) {
        if (col.isVisible() &&
            isColumnTypeAllowedForGrouping(col) ||
            col.isGroupingActive()) {
          String colLabel = ObjectUtility.nvl(col.getHeaderCell().getText(), col.getHeaderCell().getTooltipText());
          lookupRows.add(new LookupRow<>(col, colLabel));
        }
      }

      return lookupRows;
    }
  }

  @ClassId("1f523a64-cfb9-434d-b47c-40cfc0aa6898")
  protected class P_SortByLookupCall extends LocalLookupCall<ImmutablePair<IColumn, Boolean>> {
    private static final long serialVersionUID = 1L;

    @Override
    protected List<? extends ILookupRow<ImmutablePair<IColumn, Boolean>>> execCreateLookupRows() {
      final List<LookupRow<ImmutablePair<IColumn, Boolean>>> lookupRows = new ArrayList<>();

      for (IColumn col : getTable().getColumns()) {
        if (col.isVisible() && isColumnTypeAllowedForSorting(col)) {
          String colLabel = ObjectUtility.nvl(col.getHeaderCell().getText(), col.getHeaderCell().getTooltipText());
          lookupRows.add(new LookupRow<>(new ImmutablePair<>(col, true), colLabel + " \u2191"));
          lookupRows.add(new LookupRow<>(new ImmutablePair<>(col, false), colLabel + " \u2193"));
        }
      }

      return lookupRows;
    }
  }
}
