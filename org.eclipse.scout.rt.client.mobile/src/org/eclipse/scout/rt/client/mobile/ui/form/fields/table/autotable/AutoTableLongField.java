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
package org.eclipse.scout.rt.client.mobile.ui.form.fields.table.autotable;

import org.eclipse.scout.rt.client.mobile.ui.form.fields.table.ColumnToFormFieldPropertyDelegator;
import org.eclipse.scout.rt.client.mobile.ui.form.fields.table.IColumnWrapper;
import org.eclipse.scout.rt.client.ui.basic.table.columns.ILongColumn;
import org.eclipse.scout.rt.client.ui.form.fields.longfield.AbstractLongField;
import org.eclipse.scout.rt.client.ui.form.fields.longfield.ILongField;

/**
 * @since 3.9.0
 */
public class AutoTableLongField extends AbstractLongField implements IColumnWrapper<ILongColumn> {
  private ColumnToFormFieldPropertyDelegator<ILongColumn, ILongField> m_propertyDelegator;

  public AutoTableLongField(ILongColumn column) {
    super(false);
    m_propertyDelegator = new ColumnToFormFieldPropertyDelegator<ILongColumn, ILongField>(column, this);
    callInitializer();
  }

  @Override
  protected void initConfig() {
    super.initConfig();

    m_propertyDelegator.init();
  }

  @Override
  public ILongColumn getWrappedObject() {
    return m_propertyDelegator.getSender();
  }
}
