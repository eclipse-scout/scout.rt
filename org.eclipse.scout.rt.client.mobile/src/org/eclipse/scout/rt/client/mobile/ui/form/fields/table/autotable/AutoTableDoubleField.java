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
import org.eclipse.scout.rt.client.ui.basic.table.columns.IDoubleColumn;
import org.eclipse.scout.rt.client.ui.form.fields.doublefield.AbstractDoubleField;
import org.eclipse.scout.rt.client.ui.form.fields.doublefield.IDoubleField;

/**
 * @since 3.9.0
 */
public class AutoTableDoubleField extends AbstractDoubleField implements IColumnWrapper<IDoubleColumn> {
  private ColumnToFormFieldPropertyDelegator<IDoubleColumn, IDoubleField> m_propertyDelegator;

  public AutoTableDoubleField(IDoubleColumn column) {
    super(false);
    m_propertyDelegator = new ColumnToFormFieldPropertyDelegator<IDoubleColumn, IDoubleField>(column, this);
    callInitializer();
  }

  @Override
  protected void initConfig() {
    super.initConfig();

    m_propertyDelegator.init();
  }

  @Override
  public IDoubleColumn getWrappedObject() {
    return m_propertyDelegator.getSender();
  }
}
