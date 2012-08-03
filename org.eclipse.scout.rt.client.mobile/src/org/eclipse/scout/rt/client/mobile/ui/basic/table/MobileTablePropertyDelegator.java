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
package org.eclipse.scout.rt.client.mobile.ui.basic.table;

import java.util.Set;

/**
 *
 */
public class MobileTablePropertyDelegator extends TablePropertyDelegator<IMobileTable> {

  public MobileTablePropertyDelegator(IMobileTable sender, IMobileTable receiver) {
    super(sender, receiver);
  }

  public MobileTablePropertyDelegator(IMobileTable sender, IMobileTable receiver, Set<String> filteredPropertyNames) {
    super(sender, receiver, filteredPropertyNames);
  }

  @Override
  public void init() {
    super.init();

    getReceiver().setAutoCreateTableRowForm(getSender().isAutoCreateTableRowForm());
    getReceiver().setDrillDownStyleMap(getSender().getDrillDownStyleMap());
  }

  @Override
  protected void handlePropertyChange(String name, Object newValue) {
    super.handlePropertyChange(name, newValue);

    if (name.equals(IMobileTable.PROP_AUTO_CREATE_TABLE_ROW_FORM)) {
      getReceiver().setAutoCreateTableRowForm(getSender().isAutoCreateTableRowForm());
    }
    else if (name.equals(IMobileTable.PROP_DRILL_DOWN_STYLE_MAP)) {
      getReceiver().setDrillDownStyleMap(getSender().getDrillDownStyleMap());
    }
  }

}
