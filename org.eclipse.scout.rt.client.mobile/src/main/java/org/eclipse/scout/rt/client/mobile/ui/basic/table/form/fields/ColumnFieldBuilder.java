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
package org.eclipse.scout.rt.client.mobile.ui.basic.table.form.fields;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IBigDecimalColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IBigIntegerColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IBooleanColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IDateColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IIntegerColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.ILongColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IProposalColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.ISmartColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IStringColumn;
import org.eclipse.scout.rt.client.ui.form.fields.GridData;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.IValueField;
import org.eclipse.scout.rt.client.ui.form.fields.bigdecimalfield.IBigDecimalField;
import org.eclipse.scout.rt.client.ui.form.fields.booleanfield.IBooleanField;
import org.eclipse.scout.rt.client.ui.form.fields.datefield.IDateField;
import org.eclipse.scout.rt.client.ui.form.fields.integerfield.IIntegerField;
import org.eclipse.scout.rt.client.ui.form.fields.longfield.ILongField;
import org.eclipse.scout.rt.client.ui.form.fields.smartfield.IProposalField;
import org.eclipse.scout.rt.client.ui.form.fields.smartfield.ISmartField;
import org.eclipse.scout.rt.client.ui.form.fields.stringfield.IStringField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @since 3.9.0
 */
public class ColumnFieldBuilder {

  private static final String PROP_PROPERTY_DELEGATOR = "propertyDelegator";
  private static final Logger LOG = LoggerFactory.getLogger(ColumnFieldBuilder.class);

  public Map<IColumn<?>, IFormField> build(List<IColumn<?>> columns, ITableRow row) {
    Map<IColumn<?>, IFormField> fields = new HashMap<IColumn<?>, IFormField>();
    if (columns == null) {
      return fields;
    }

    for (IColumn<?> column : columns) {
      IFormField field = createValueField(column, row);
      if (field != null) {
        fields.put(column, field);
      }
      else {
        LOG.warn("No field mapping found for column " + column.getClass());
      }
    }

    return fields;
  }

  @SuppressWarnings("unchecked")
  protected IFormField createValueField(IColumn<?> column, ITableRow row) {
    if (column.isEditable()) {
      IFormField field = createEditableField(column, row);
      if (field != null) {
        return field;
      }
    }

    if (column instanceof IStringColumn) {
      return new StringColumnField((IStringColumn) column);
    }
    if (column instanceof ISmartColumn) {
      return new SmartColumnField((ISmartColumn<?>) column);
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
    if (column instanceof IBigIntegerColumn) {
      return new BigIntegerColumnField((IBigIntegerColumn) column);
    }

    return null;
  }

  @SuppressWarnings("unchecked")
  protected ColumnFieldPropertyDelegator<? extends IColumn<?>, ? extends IFormField> createColumnFieldPropertyDelegator(IColumn<?> column, IFormField formField) {
    if (column instanceof IStringColumn && formField instanceof IStringField) {
      return new StringColumnFieldPropertyDelegator((IStringColumn) column, (IStringField) formField);
    }
    else if (column instanceof ISmartColumn && formField instanceof ISmartField) {
      return new SmartColumnFieldPropertyDelegator((ISmartColumn) column, (ISmartField) formField);
    }
    else if (column instanceof IProposalColumn && formField instanceof IProposalField) {
      return new ProposalColumnFieldPropertyDelegator((IProposalColumn) column, (IProposalField) formField);
    }
    else if (column instanceof IDateColumn && formField instanceof IDateField) {
      return new DateColumnFieldPropertyDelegator((IDateColumn) column, (IDateField) formField);
    }
    else if (column instanceof IBooleanColumn && formField instanceof IBooleanField) {
      return new ColumnFieldPropertyDelegator(column, formField);
    }
    else if (column instanceof ILongColumn && formField instanceof ILongField) {
      return new LongColumnFieldPropertyDelegator((ILongColumn) column, (ILongField) formField);
    }
    else if (column instanceof IIntegerColumn && formField instanceof IIntegerField) {
      return new IntegerColumnFieldPropertyDelegator((IIntegerColumn) column, (IIntegerField) formField);
    }
    else if (column instanceof IBigDecimalColumn && formField instanceof IBigDecimalField) {
      return new BigDecimalColumnFieldPropertyDelegator((IBigDecimalColumn) column, (IBigDecimalField) formField);
    }

    return new ColumnFieldPropertyDelegator(column, formField);
  }

  protected IFormField createEditableField(IColumn<?> column, ITableRow row) {
    IFormField field = column.prepareEdit(row);
    if (field != null) {
      //Revert changes which are done in AbstractColumn#prepareEdit
      field.setLabelVisible(true);
      GridData gd = field.getGridDataHints();
      gd.weightY = 0;
      field.setGridDataHints(gd);

      //Set missing properties
      ColumnFieldPropertyDelegator<? extends IColumn<?>, ? extends IFormField> propertyDelegator = createColumnFieldPropertyDelegator(column, field);
      if (propertyDelegator != null) {
        propertyDelegator.init();
        field.setProperty(PROP_PROPERTY_DELEGATOR, propertyDelegator);
      }

      field.addPropertyChangeListener(new PropertyChangeListener() {

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
          if (IValueField.PROP_VALUE.equals(evt.getPropertyName())) {
            //Enforce setting cell value even if the value has been changed back to initial value (see AbstractColumn#execCompleteEdit
            ((IValueField<?>) evt.getSource()).touch();
          }
        }
      });
    }

    return field;
  }

  public void fieldDisposed(IFormField field) {
    Object value = field.getProperty(PROP_PROPERTY_DELEGATOR);
    if (value instanceof ColumnFieldPropertyDelegator) {
      ((ColumnFieldPropertyDelegator) value).dispose();
    }
  }
}
