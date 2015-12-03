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
package org.eclipse.scout.rt.client.mobile.ui.form.fields.table;

import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.form.fields.tablefield.AbstractTableField;

/**
 * @since 3.9.0
 */
public abstract class AbstractMobileTableField<T extends ITable> extends AbstractTableField<T> implements IMobileTableField<T> {

  @Override
  protected void initConfig() {
    super.initConfig();

    setActionBarVisible(getConfiguredActionBarVisible());
  }

  protected boolean getConfiguredActionBarVisible() {
    return true;
  }

  @Override
  public boolean isActionBarVisible() {
    return propertySupport.getPropertyBool(PROP_ACTION_BAR_VISIBLE);
  }

  @Override
  public void setActionBarVisible(boolean visible) {
    propertySupport.setPropertyBool(PROP_ACTION_BAR_VISIBLE, visible);
  }

}
