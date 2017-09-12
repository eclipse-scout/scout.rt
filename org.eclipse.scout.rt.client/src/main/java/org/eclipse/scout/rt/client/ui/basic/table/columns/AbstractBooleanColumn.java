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
package org.eclipse.scout.rt.client.ui.basic.table.columns;

import org.eclipse.scout.rt.client.extension.ui.basic.table.columns.IBooleanColumnExtension;
import org.eclipse.scout.rt.client.ui.basic.cell.Cell;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.IValueField;
import org.eclipse.scout.rt.client.ui.form.fields.booleanfield.AbstractBooleanField;
import org.eclipse.scout.rt.client.ui.form.fields.booleanfield.IBooleanField;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.annotations.ConfigProperty;
import org.eclipse.scout.rt.platform.classid.ClassId;
import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.platform.util.BooleanUtility;

/**
 * Column holding Boolean values
 */
@ClassId("62fcae6b-6b75-4e8c-bb3f-ea3b400e7e30")
public abstract class AbstractBooleanColumn extends AbstractColumn<Boolean> implements IBooleanColumn {
  // DO NOT init members, this has the same effect as if they were set AFTER
  // initConfig()

  public AbstractBooleanColumn() {
    this(true);
  }

  public AbstractBooleanColumn(boolean callInitializer) {
    super(callInitializer);
  }

  @Override
  protected void initConfig() {
    super.initConfig();
    setVerticalAlignment(getConfiguredVerticalAlignment());
    setTriStateEnabled(getConfiguredTriStateEnabled());
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
  public void setTriStateEnabled(boolean triStateEnabled) {
    propertySupport.setPropertyBool(PROP_TRI_STATE_ENABLED, triStateEnabled);
    if (!triStateEnabled && getTable() != null) {
      // Validate value again (converts null to false)
      for (ITableRow row : getTable().getRows()) {
        setValue(row, getValue(row));
      }
    }
  }

  @Override
  public boolean isTriStateEnabled() {
    return propertySupport.getPropertyBool(PROP_TRI_STATE_ENABLED);
  }

  @Override
  protected void decorateCellInternal(Cell cell, ITableRow row) {
    super.decorateCellInternal(cell, row);
    updateDisplayText(row, cell);
  }

  @Override
  protected String formatValueInternal(ITableRow row, Boolean value) {
    if (isTriStateEnabled() && value == null) {
      return UNDEFINED_TEXT;
    }
    return (BooleanUtility.nvl(value)) ? TRUE_TEXT : "";
  }

  @Override
  protected Boolean parseValueInternal(ITableRow row, Object rawValue) {
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
  protected Boolean validateValueInternal(ITableRow row, Boolean rawValue) {
    rawValue = super.validateValueInternal(row, rawValue);
    if (!isTriStateEnabled() && rawValue == null) {
      rawValue = Boolean.FALSE;
    }
    return rawValue;
  }

  @Override
  protected IValueField<Boolean> createDefaultEditor() {
    return new AbstractBooleanField() {
    };
  }

  @Override
  protected void mapEditorFieldProperties(IFormField f) {
    super.mapEditorFieldProperties(f);
    if (f instanceof IBooleanField) {
      ((IBooleanField) f).setTriStateEnabled(isTriStateEnabled());
    }
  }

  /**
   * Configures the horizontal alignment of text inside this column (including header text).
   * <p>
   * Subclasses can override this method. For boolean columns, the default is {@code 0} (center alignment).
   *
   * @return {@code -1} for left, {@code 0} for center and {@code 1} for right alignment.
   */
  @Override
  protected int getConfiguredHorizontalAlignment() {
    return 0; // center position
  }

  /**
   * Configures the vertical alignment of text inside this column (including header text).
   * <p>
   * Subclasses can override this method. Default is {@code -1} (top alignment).
   *
   * @return {@code -1} for top, {@code 0} for center and {@code 1} for bottom alignment.
   */
  @ConfigProperty(ConfigProperty.INTEGER)
  @Order(200)
  protected int getConfiguredVerticalAlignment() {
    return -1; // top position
  }

  /**
   * <ul>
   * <li><b>true:</b> the check box can have a {@link #getValue()} of <code>true</code>, <code>false</code> and
   * <code>null</code>. <code>null</code> is the third state that represents "undefined" and is typically displayed
   * using a filled rectangular area.
   * <li><b>false:</b> the check box can have a {@link #getValue()} of <code>true</code> and <code>false</code>. The
   * value is never <code>null</code> (setting the value to <code>null</code> will automatically convert it to
   * <code>false</code>).
   * </ul>
   * The default is <code>false</code>.
   *
   * @since 6.1
   * @return <code>true</code> if this check box supports the so-called "tri-state" and allows setting the value to
   *         <code>null</code> to represent the "undefined" value.
   */
  @Order(220)
  @ConfigProperty(ConfigProperty.BOOLEAN)
  protected boolean getConfiguredTriStateEnabled() {
    return false;
  }

  @Override
  protected int getConfiguredMinWidth() {
    return NARROW_MIN_WIDTH;
  }

  protected static class LocalBooleanColumnExtension<OWNER extends AbstractBooleanColumn> extends LocalColumnExtension<Boolean, OWNER> implements IBooleanColumnExtension<OWNER> {

    public LocalBooleanColumnExtension(OWNER owner) {
      super(owner);
    }
  }

  @Override
  protected IBooleanColumnExtension<? extends AbstractBooleanColumn> createLocalExtension() {
    return new LocalBooleanColumnExtension<>(this);
  }
}
