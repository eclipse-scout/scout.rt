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
package org.eclipse.scout.rt.client.ui.basic.table.columnfilter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.scout.commons.TypeCastUtility;
import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ui.basic.table.AbstractTable;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.basic.table.TableRow;
import org.eclipse.scout.rt.client.ui.basic.table.columnfilter.ColumnFilterForm.MainBox.CloseButton;
import org.eclipse.scout.rt.client.ui.basic.table.columnfilter.ColumnFilterForm.MainBox.DateDetailBox;
import org.eclipse.scout.rt.client.ui.basic.table.columnfilter.ColumnFilterForm.MainBox.DateDetailBox.DateSequenceBox.DateFromField;
import org.eclipse.scout.rt.client.ui.basic.table.columnfilter.ColumnFilterForm.MainBox.DateDetailBox.DateSequenceBox.DateToField;
import org.eclipse.scout.rt.client.ui.basic.table.columnfilter.ColumnFilterForm.MainBox.NumberDetailBox;
import org.eclipse.scout.rt.client.ui.basic.table.columnfilter.ColumnFilterForm.MainBox.NumberDetailBox.NumberSequenceBox.NumberFromField;
import org.eclipse.scout.rt.client.ui.basic.table.columnfilter.ColumnFilterForm.MainBox.NumberDetailBox.NumberSequenceBox.NumberToField;
import org.eclipse.scout.rt.client.ui.basic.table.columnfilter.ColumnFilterForm.MainBox.OkButton;
import org.eclipse.scout.rt.client.ui.basic.table.columnfilter.ColumnFilterForm.MainBox.StringDetailBox;
import org.eclipse.scout.rt.client.ui.basic.table.columnfilter.ColumnFilterForm.MainBox.StringDetailBox.PatternField;
import org.eclipse.scout.rt.client.ui.basic.table.columnfilter.ColumnFilterForm.MainBox.ValuesBox.ValuesTableField;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractObjectColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractStringColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IDateColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IDoubleColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IIntegerColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.ILongColumn;
import org.eclipse.scout.rt.client.ui.form.AbstractForm;
import org.eclipse.scout.rt.client.ui.form.AbstractFormHandler;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.IValueField;
import org.eclipse.scout.rt.client.ui.form.fields.button.AbstractButton;
import org.eclipse.scout.rt.client.ui.form.fields.button.AbstractCloseButton;
import org.eclipse.scout.rt.client.ui.form.fields.button.AbstractLinkButton;
import org.eclipse.scout.rt.client.ui.form.fields.button.AbstractOkButton;
import org.eclipse.scout.rt.client.ui.form.fields.datefield.AbstractDateField;
import org.eclipse.scout.rt.client.ui.form.fields.datefield.IDateField;
import org.eclipse.scout.rt.client.ui.form.fields.doublefield.AbstractDoubleField;
import org.eclipse.scout.rt.client.ui.form.fields.doublefield.IDoubleField;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.sequencebox.AbstractSequenceBox;
import org.eclipse.scout.rt.client.ui.form.fields.stringfield.AbstractStringField;
import org.eclipse.scout.rt.client.ui.form.fields.tablefield.AbstractTableField;
import org.eclipse.scout.rt.shared.ScoutTexts;
import org.eclipse.scout.rt.shared.services.lookup.LookupRow;

public class ColumnFilterForm extends AbstractForm {
  private ITableColumnFilter<?> m_columnFilter;

  public ColumnFilterForm() throws ProcessingException {
  }

  @Override
  protected String getConfiguredTitle() {
    return ScoutTexts.get("ColumnFilter");
  }

  @Override
  protected boolean getConfiguredModal() {
    return true;
  }

  public ITableColumnFilter<?> getColumnFilter() {
    return m_columnFilter;
  }

  public void setColumnFilter(ITableColumnFilter<?> columnFilter) {
    m_columnFilter = columnFilter;
  }

  public MainBox getMainBox() {
    return (MainBox) getRootGroupBox();
  }

  public ValuesTableField getValuesTableField() {
    return getFieldByClass(ValuesTableField.class);
  }

  public StringDetailBox getStringDetailBox() {
    return getFieldByClass(StringDetailBox.class);
  }

  public PatternField getPatternField() {
    return getFieldByClass(PatternField.class);
  }

  public DateDetailBox getDateDetailBox() {
    return getFieldByClass(DateDetailBox.class);
  }

  public DateFromField getDateFromField() {
    return getFieldByClass(DateFromField.class);
  }

  public DateToField getDateToField() {
    return getFieldByClass(DateToField.class);
  }

  public NumberDetailBox getNumberDetailBox() {
    return getFieldByClass(NumberDetailBox.class);
  }

  public NumberFromField getNumberFromField() {
    return getFieldByClass(NumberFromField.class);
  }

  public NumberToField getNumberToField() {
    return getFieldByClass(NumberToField.class);
  }

  public OkButton getOkButton() {
    return getFieldByClass(OkButton.class);
  }

  public CloseButton getCloseButton() {
    return getFieldByClass(CloseButton.class);
  }

  public void startModify() throws ProcessingException {
    startInternal(new ModifyHandler());
  }

  private void setupDateField(IDateField f, IColumn<?> obj) {
    if (obj instanceof IDateColumn) {
      IDateColumn col = (IDateColumn) obj;
      f.setHasTime(col.isHasTime());
      f.setFormat(col.getFormat());
    }
  }

  private void setupNumberField(IDoubleField f, IColumn<?> obj) {
    if (obj instanceof IDoubleColumn) {
      IDoubleColumn col = (IDoubleColumn) obj;
      f.setFormat(col.getFormat());
      f.setGroupingUsed(col.isGroupingUsed());
      f.setMaxFractionDigits(col.getMaxFractionDigits());
      f.setMinFractionDigits(col.getMinFractionDigits());
      f.setMultiplier(col.getMultiplier());
      f.setPercent(col.isPercent());
    }
    else if (obj instanceof IIntegerColumn) {
      IIntegerColumn col = (IIntegerColumn) obj;
      f.setFormat(col.getFormat());
      f.setGroupingUsed(col.isGroupingUsed());
      f.setMaxFractionDigits(0);
      f.setMinFractionDigits(0);
    }
    else if (obj instanceof ILongColumn) {
      ILongColumn col = (ILongColumn) obj;
      f.setFormat(col.getFormat());
      f.setGroupingUsed(col.isGroupingUsed());
      f.setMaxFractionDigits(0);
      f.setMinFractionDigits(0);
    }
  }

  @Order(10)
  public class MainBox extends AbstractGroupBox {

    @Override
    protected int getConfiguredGridW() {
      return 1;
    }

    @Order(20)
    public class ValuesBox extends AbstractGroupBox {
      @Override
      protected String getConfiguredLabel() {
        return ScoutTexts.get("ColumnFilterValuesSection");
      }

      @Override
      protected int getConfiguredGridColumnCount() {
        return 1;
      }

      @Order(10)
      public class ButtonsSequenceBox extends AbstractSequenceBox {
        @Override
        protected boolean getConfiguredLabelVisible() {
          return false;
        }

        @Override
        protected boolean getConfiguredAutoCheckFromTo() {
          return false;
        }

        @Order(10)
        public class CheckAllButton extends AbstractLinkButton {

          @Override
          protected String getConfiguredLabel() {
            return ScoutTexts.get("CheckAll");
          }

          @Override
          protected boolean getConfiguredProcessButton() {
            return false;
          }

          @Override
          protected void execClickAction() throws ProcessingException {
            for (ITableRow row : getValuesTableField().getTable().getRows()) {
              row.setChecked(true);
            }
          }
        }

        @Order(20)
        public class UncheckAllButton extends AbstractLinkButton {

          @Override
          protected String getConfiguredLabel() {
            return ScoutTexts.get("UncheckAll");
          }

          @Override
          protected boolean getConfiguredProcessButton() {
            return false;
          }

          @Override
          protected void execClickAction() throws ProcessingException {
            for (ITableRow row : getValuesTableField().getTable().getRows()) {
              row.setChecked(false);
            }
          }
        }
      }

      @Order(30)
      public class ValuesTableField extends AbstractTableField<ValuesTableField.Table> {
        @Override
        protected int getConfiguredGridW() {
          return 1;
        }

        @Override
        protected int getConfiguredGridH() {
          return 8;
        }

        @Override
        protected boolean getConfiguredLabelVisible() {
          return false;
        }

        @Override
        protected void execReloadTableData() throws ProcessingException {
          List<LookupRow> hist = getColumnFilter().createHistogram();
          ArrayList<ITableRow> rowList = new ArrayList<ITableRow>(hist.size() + 1);
          for (LookupRow histRow : hist) {
            TableRow tableRow = new TableRow(getTable().getColumnSet(), new Object[]{histRow.getKey(), histRow.getText()});
            tableRow.setIconId(histRow.getIconId());
            tableRow.setForegroundColor(histRow.getForegroundColor());
            tableRow.setBackgroundColor(histRow.getBackgroundColor());
            tableRow.setFont(histRow.getFont());
            rowList.add(tableRow);
          }
          getTable().discardAllRows();
          getTable().addRows(rowList.toArray(new ITableRow[rowList.size()]));
          //set checks
          Set<?> selectedKeys = getColumnFilter().getSelectedValues();
          if (selectedKeys != null) {
            Table.KeyColumn keyCol = getTable().getKeyColumn();
            for (ITableRow row : getTable().getRows()) {
              if (selectedKeys.contains(keyCol.getValue(row))) {
                row.setChecked(true);
              }
            }
          }
        }

        public class Table extends AbstractTable {
          @Override
          protected boolean getConfiguredCheckable() {
            return true;
          }

          @Override
          protected boolean getConfiguredHeaderVisible() {
            return false;
          }

          @Override
          protected boolean getConfiguredAutoResizeColumns() {
            return true;
          }

          public KeyColumn getKeyColumn() {
            return getColumnSet().getColumnByClass(KeyColumn.class);
          }

          public TextColumn getTextColumn() {
            return getColumnSet().getColumnByClass(TextColumn.class);
          }

          @Order(10)
          public class KeyColumn extends AbstractObjectColumn {

            @Override
            protected boolean getConfiguredDisplayable() {
              return false;
            }

            @Override
            protected boolean getConfiguredPrimaryKey() {
              return true;
            }
          }

          @Order(20)
          public class TextColumn extends AbstractStringColumn {
            @Override
            protected String getConfiguredHeaderText() {
              return ScoutTexts.get("ColumnValues");
            }

            @Override
            protected int getConfiguredHorizontalAlignment() {
              return -1;
            }

            @Override
            protected int getConfiguredWidth() {
              return 200;
            }
          }

        }
      }
    }

    @Order(30)
    public class StringDetailBox extends AbstractGroupBox {

      @Override
      protected String getConfiguredLabel() {
        return ScoutTexts.get("ColumnFilterStringSection");
      }

      @Override
      protected boolean getConfiguredVisible() {
        return false;
      }

      @Override
      protected int getConfiguredGridColumnCount() {
        return 1;
      }

      @Order(10)
      public class PatternField extends AbstractStringField {
        @Override
        protected String getConfiguredLabel() {
          return ScoutTexts.get("StringPattern");
        }

        @Override
        protected boolean getConfiguredLabelVisible() {
          return false;
        }

        @Override
        public boolean isSpellCheckEnabled() {
          return false;
        }
      }
    }

    @Order(40)
    public class DateDetailBox extends AbstractGroupBox {

      @Override
      protected String getConfiguredLabel() {
        return ScoutTexts.get("ColumnFilterDateSection");
      }

      @Override
      protected boolean getConfiguredVisible() {
        return false;
      }

      @Order(10)
      public class DateSequenceBox extends AbstractSequenceBox {
        @Override
        protected boolean getConfiguredLabelVisible() {
          return false;
        }

        @Order(10)
        public class DateFromField extends AbstractDateField {
          @Override
          protected String getConfiguredLabel() {
            return ScoutTexts.get("from");
          }
        }

        @Order(20)
        public class DateToField extends AbstractDateField {
          @Override
          protected String getConfiguredLabel() {
            return ScoutTexts.get("to");
          }
        }
      }
    }

    @Order(40)
    public class NumberDetailBox extends AbstractGroupBox {

      @Override
      protected String getConfiguredLabel() {
        return ScoutTexts.get("ColumnFilterNumberSection");
      }

      @Override
      protected boolean getConfiguredVisible() {
        return false;
      }

      @Order(10)
      public class NumberSequenceBox extends AbstractSequenceBox {
        @Override
        protected boolean getConfiguredLabelVisible() {
          return false;
        }

        @Order(10)
        public class NumberFromField extends AbstractDoubleField {
          @Override
          protected String getConfiguredLabel() {
            return ScoutTexts.get("from");
          }
        }

        @Order(20)
        public class NumberToField extends AbstractDoubleField {
          @Override
          protected String getConfiguredLabel() {
            return ScoutTexts.get("to");
          }
        }
      }
    }

    /**
     * Button "ok"
     */
    @Order(100)
    public class OkButton extends AbstractOkButton {
    }

    /**
     * Button "close"
     */
    @Order(110)
    public class CloseButton extends AbstractCloseButton {
    }

    /**
     * Button "reset"
     */
    @Order(120)
    public class RemoveButton extends AbstractButton {
      @Override
      protected String getConfiguredLabel() {
        return ScoutTexts.get("ColumnFilterRemoveButton");
      }

      @Override
      protected void execClickAction() throws ProcessingException {
        for (ITableRow r : getValuesTableField().getTable().getRows()) {
          r.setChecked(false);
        }
        for (IFormField f : getAllFields()) {
          if (f instanceof IValueField<?>) {
            ((IValueField<?>) f).setValue(null);
          }
        }
        doOk();
      }
    }

  }

  /**
   * Handler for "Modify"
   */
  public class ModifyHandler extends AbstractFormHandler {

    @Override
    protected void execLoad() throws ProcessingException {
      setTitle(getColumnFilter().getColumn().getHeaderCell().getText());
      getValuesTableField().reloadTableData();
      //
      if (getColumnFilter() instanceof StringColumnFilter) {
        StringColumnFilter filter = (StringColumnFilter) getColumnFilter();
        getStringDetailBox().setVisible(true);
        getPatternField().setValue(filter.getPattern());
      }
      else if (getColumnFilter() instanceof ComparableColumnFilter) {
        ComparableColumnFilter filter = (ComparableColumnFilter) getColumnFilter();
        Class dataType = filter.getColumn().getDataType();
        if (Date.class.isAssignableFrom(dataType)) {
          getDateDetailBox().setVisible(true);
          setupDateField(getDateFromField(), getColumnFilter().getColumn());
          setupDateField(getDateToField(), getColumnFilter().getColumn());
          getDateFromField().setValue((Date) filter.getMinimumValue());
          getDateToField().setValue((Date) filter.getMaximumValue());
        }
        else if (Number.class.isAssignableFrom(dataType)) {
          getNumberDetailBox().setVisible(true);
          setupNumberField(getNumberFromField(), getColumnFilter().getColumn());
          setupNumberField(getNumberToField(), getColumnFilter().getColumn());
          getNumberFromField().setValue(TypeCastUtility.castValue(filter.getMinimumValue(), Double.class));
          getNumberToField().setValue(TypeCastUtility.castValue(filter.getMaximumValue(), Double.class));
        }
      }
    }

    @Override
    protected void execPostLoad() throws ProcessingException {
      touch();
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void execStore() throws ProcessingException {
      Object[] checkedKeys = getValuesTableField().getTable().getKeyColumn().getValues(getValuesTableField().getTable().getCheckedRows());
      if (checkedKeys.length > 0) {
        getColumnFilter().setSelectedValues(new HashSet(Arrays.asList(checkedKeys)));
      }
      else {
        getColumnFilter().setSelectedValues(null);
      }
      //
      if (getColumnFilter() instanceof StringColumnFilter) {
        StringColumnFilter filter = (StringColumnFilter) getColumnFilter();
        filter.setPattern(getPatternField().getValue());
      }
      else if (getColumnFilter() instanceof ComparableColumnFilter) {
        ComparableColumnFilter filter = (ComparableColumnFilter) getColumnFilter();
        Class dataType = filter.getColumn().getDataType();
        if (Date.class.isAssignableFrom(dataType)) {
          filter.setMinimumValue(getDateFromField().getValue());
          filter.setMaximumValue(getDateToField().getValue());
        }
        else if (Number.class.isAssignableFrom(dataType)) {
          filter.setMinimumValue((Comparable) TypeCastUtility.castValue(getNumberFromField().getValue(), dataType));
          filter.setMaximumValue((Comparable) TypeCastUtility.castValue(getNumberToField().getValue(), dataType));
        }
      }
    }
  }
}
