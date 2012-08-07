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

import org.eclipse.scout.rt.client.mobile.ui.form.fields.table.IColumnWrapper;
import org.eclipse.scout.rt.client.mobile.ui.form.fields.table.SmartColumnToSmartFieldPropertyDelegator;
import org.eclipse.scout.rt.client.ui.basic.table.columns.ISmartColumn;
import org.eclipse.scout.rt.client.ui.form.fields.smartfield.AbstractSmartField;

/**
 * @since 3.9.0
 */
public class TableRowFormSmartField extends AbstractSmartField implements IColumnWrapper<ISmartColumn<?>> {
  private SmartColumnToSmartFieldPropertyDelegator m_propertyDelegator;

  public TableRowFormSmartField(ISmartColumn<?> column) {
    super(false);
    m_propertyDelegator = new SmartColumnToSmartFieldPropertyDelegator(column, this);
    callInitializer();
  }

  @Override
  protected void initConfig() {
    super.initConfig();

    m_propertyDelegator.init();
  }

  @Override
  public ISmartColumn<?> getWrappedObject() {
    return m_propertyDelegator.getSender();
  }
}
