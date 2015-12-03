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

import org.eclipse.scout.rt.client.ui.basic.table.columns.IDateColumn;
import org.eclipse.scout.rt.client.ui.form.fields.datefield.AbstractDateField;

/**
 * @since 3.9.0
 */
public class DateColumnField extends AbstractDateField implements IColumnWrapper<IDateColumn> {
  private DateColumnFieldPropertyDelegator m_propertyDelegator;

  public DateColumnField(IDateColumn column) {
    super(false);
    m_propertyDelegator = new DateColumnFieldPropertyDelegator(column, this);
    callInitializer();
  }

  @Override
  protected void initConfig() {
    super.initConfig();

    m_propertyDelegator.init();
  }

  @Override
  protected void execDisposeField() {
    m_propertyDelegator.dispose();
  }

  @Override
  public IDateColumn getWrappedObject() {
    return m_propertyDelegator.getSender();
  }
}
