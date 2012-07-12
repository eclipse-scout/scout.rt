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
import org.eclipse.scout.rt.client.ui.basic.table.columns.IDateColumn;
import org.eclipse.scout.rt.client.ui.form.fields.datefield.AbstractDateField;
import org.eclipse.scout.rt.client.ui.form.fields.datefield.IDateField;

/**
 * @since 3.9.0
 */
public class AutoTableDateField extends AbstractDateField implements IColumnWrapper<IDateColumn> {
  private ColumnToFormFieldPropertyDelegator<IDateColumn, IDateField> m_propertyDelegator;

  public AutoTableDateField(IDateColumn column) {
    super(false);
    m_propertyDelegator = new ColumnToFormFieldPropertyDelegator<IDateColumn, IDateField>(column, this);
    callInitializer();
  }

  @Override
  protected void initConfig() {
    super.initConfig();

    m_propertyDelegator.init();
  }

  @Override
  public IDateColumn getWrappedObject() {
    return m_propertyDelegator.getSender();
  }
}
