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
package org.eclipse.scout.rt.client.ui.basic.table.columns;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.eclipse.scout.commons.annotations.ConfigProperty;
import org.eclipse.scout.commons.annotations.ConfigPropertyValue;
import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.booleanfield.AbstractBooleanField;
import org.eclipse.scout.rt.client.ui.form.fields.booleanfield.IBooleanField;

/**
 * Column holding Boolean values
 */
public abstract class AbstractBooleanColumn extends AbstractColumn<Boolean> implements IBooleanColumn {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(AbstractBooleanColumn.class);

  // DO NOT init members, this has the same effect as if they were set AFTER
  // initConfig()

  public AbstractBooleanColumn() {
    super();
  }

  @Override
  protected void initConfig() {
    super.initConfig();
    setVerticalAlignment(getConfiguredVerticalAlignment());
  }

  @Override
  public int getVerticalAlignment() {
    return propertySupport.getPropertyInt(PROP_VERTICAL_ALIGNMENT);
  }

  @Override
  public void setVerticalAlignment(int verticalAlignment) {
    propertySupport.setProperty(PROP_VERTICAL_ALIGNMENT, verticalAlignment);
  }

  @Override
  protected Boolean parseValueInternal(ITableRow row, Object rawValue) throws ProcessingException {
    Boolean validValue = null;
    if (rawValue == null) {
      validValue = null;
    }
    else if (rawValue instanceof Boolean) {
      validValue = (Boolean) rawValue;
    }
    else if (rawValue instanceof Number) {
      validValue = (((Number) rawValue).intValue() == 1);
    }
    else {
      throw new ProcessingException("invalid Boolean value in column '" + getClass().getSimpleName() + "': " + rawValue + " class=" + rawValue.getClass());
    }
    return validValue;
  }

  @Override
  protected IFormField prepareEditInternal(final ITableRow row) throws ProcessingException {
    final AbstractBooleanField f = new AbstractBooleanField() {
    };
    f.setValue(getValue(row));

    //automatic save when value changes
    f.addPropertyChangeListener(IBooleanField.PROP_VALUE, new PropertyChangeListener() {
      @Override
      public void propertyChange(PropertyChangeEvent e) {
        try {
          completeEdit(row, f);
        }
        catch (ProcessingException e1) {
          LOG.error("failed to complete edit mode", e1);
        }
      }
    });
    return f;
  }

  @Override
  protected int getConfiguredHorizontalAlignment() {
    return 0; // center position
  }

  @ConfigProperty(ConfigProperty.INTEGER)
  @Order(200)
  @ConfigPropertyValue("-1")
  protected int getConfiguredVerticalAlignment() {
    return -1; // top position
  }
}
