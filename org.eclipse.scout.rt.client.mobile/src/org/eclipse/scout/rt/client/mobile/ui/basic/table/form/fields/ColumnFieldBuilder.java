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
package org.eclipse.scout.rt.client.mobile.ui.basic.table.form.fields;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IBigDecimalColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IBooleanColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IDateColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IDoubleColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IIntegerColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.ILongColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.ISmartColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IStringColumn;
import org.eclipse.scout.rt.client.ui.form.fields.GridData;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;

/**
 * @since 3.9.0
 */
public class ColumnFieldBuilder {

  public Map<IColumn<?>, IFormField> build(IColumn<?>[] columns, ITableRow row) throws ProcessingException {
    Map<IColumn<?>, IFormField> fields = new HashMap<IColumn<?>, IFormField>();
    if (columns == null) {
      return fields;
    }

    for (IColumn<?> column : columns) {
      IFormField field = createValueField(column, row);
      if (field != null) {
        fields.put(column, field);
      }
    }

    return fields;
  }

  @SuppressWarnings("unchecked")
  protected IFormField createValueField(IColumn<?> column, ITableRow row) throws ProcessingException {
    if (column.isEditable()) {
      IFormField field = createEditableField(column, row);
      if (field != null) {
        //Only
        return field;
      }
    }

    if (column instanceof IStringColumn) {
      return new StringColumnField((IStringColumn) column);
    }
    if (column instanceof ISmartColumn) {
      return new SmartColumnField((ISmartColumn<?>) column);
    }
    if (column instanceof IDoubleColumn) {
      return new DoubleColumnField((IDoubleColumn) column);
    }
    if (column instanceof IDateColumn) {
      return new DateColumnField((IDateColumn) column);
    }
    if (column instanceof IBooleanColumn) {
      return new BooleanColumnField((IBooleanColumn) column);
    }
    if (column instanceof ILongColumn) {
      return new LongColumnField((ILongColumn) column);
    }
    if (column instanceof IIntegerColumn) {
      return new IntegerColumnField((IIntegerColumn) column);
    }
    if (column instanceof IBigDecimalColumn) {
      return new BigDecimalColumnField((IBigDecimalColumn) column);
    }

    return null;
  }

  protected IFormField createEditableField(IColumn<?> column, ITableRow row) throws ProcessingException {
    IFormField field = column.prepareEdit(row);
    if (field != null) {
      //Revert changes which are done in AbstractColumn#prepareEdit
      field.setLabelVisible(true);
      GridData gd = field.getGridDataHints();
      gd.weightY = 0;
      field.setGridDataHints(gd);

      //Set missing properties
      field.setLabel(column.getHeaderCell().getText());
      field.setTooltipText(column.getHeaderCell().getTooltipText());
    }

    return field;
  }
}
