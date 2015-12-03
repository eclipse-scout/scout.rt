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
package org.eclipse.scout.rt.client.mobile.ui.basic.table;

import java.util.Set;

import org.eclipse.scout.rt.client.mobile.ui.basic.table.form.ITableRowFormProvider;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;

/**
 * Delegates the table properties and also the additional properties of the {@link IMobileTable}.
 * <p>
 * The sender does not necessarily need to be of type {@link IMobileTable}, if the properties are set, they will be
 * delegated.
 * 
 * @since 3.9.0
 */
public class MobileTablePropertyDelegator extends TablePropertyDelegator<ITable, IMobileTable> {

  public MobileTablePropertyDelegator(ITable sender, IMobileTable receiver) {
    super(sender, receiver);
  }

  public MobileTablePropertyDelegator(ITable sender, IMobileTable receiver, Set<String> filteredPropertyNames) {
    super(sender, receiver, filteredPropertyNames);
  }

  @Override
  public void init() {
    super.init();

    if (getSender().hasProperty(IMobileTable.PROP_AUTO_CREATE_TABLE_ROW_FORM)) {
      getReceiver().setAutoCreateTableRowForm((Boolean) getSender().getProperty(IMobileTable.PROP_AUTO_CREATE_TABLE_ROW_FORM));
    }
    if (getSender().hasProperty(IMobileTable.PROP_DRILL_DOWN_STYLE_MAP)) {
      getReceiver().setDrillDownStyleMap((DrillDownStyleMap) getSender().getProperty(IMobileTable.PROP_DRILL_DOWN_STYLE_MAP));
    }
    if (getSender().hasProperty(IMobileTable.PROP_PAGING_ENABLED)) {
      getReceiver().setPagingEnabled((Boolean) getSender().getProperty(IMobileTable.PROP_PAGING_ENABLED));
    }
    if (getSender().hasProperty(IMobileTable.PROP_PAGE_SIZE)) {
      getReceiver().setPageSize((Integer) getSender().getProperty(IMobileTable.PROP_PAGE_SIZE));
    }
    if (getSender().hasProperty(IMobileTable.PROP_PAGE_INDEX)) {
      getReceiver().setPageIndex((Integer) getSender().getProperty(IMobileTable.PROP_PAGE_INDEX));
    }
    if (getSender().hasProperty(IMobileTable.PROP_TABLE_ROW_FORM_PROVIDER)) {
      getReceiver().setTableRowFormProvider((ITableRowFormProvider) getSender().getProperty(IMobileTable.PROP_TABLE_ROW_FORM_PROVIDER));
    }
  }

  @Override
  protected void handlePropertyChange(String name, Object newValue) {
    super.handlePropertyChange(name, newValue);

    if (name.equals(IMobileTable.PROP_AUTO_CREATE_TABLE_ROW_FORM)) {
      getReceiver().setAutoCreateTableRowForm((Boolean) newValue);
    }
    else if (name.equals(IMobileTable.PROP_DRILL_DOWN_STYLE_MAP)) {
      getReceiver().setDrillDownStyleMap((DrillDownStyleMap) newValue);
    }
    else if (name.equals(IMobileTable.PROP_PAGING_ENABLED)) {
      getReceiver().setPagingEnabled((Boolean) newValue);
    }
    else if (name.equals(IMobileTable.PROP_PAGE_SIZE)) {
      getReceiver().setPageSize((Integer) newValue);
    }
    else if (name.equals(IMobileTable.PROP_PAGE_INDEX)) {
      getReceiver().setPageIndex((Integer) newValue);
    }
    else if (name.equals(IMobileTable.PROP_TABLE_ROW_FORM_PROVIDER)) {
      getReceiver().setTableRowFormProvider((ITableRowFormProvider) newValue);
    }
  }
}
