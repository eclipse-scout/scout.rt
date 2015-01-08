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
package org.eclipse.scout.rt.client.ui.desktop.outline.pages;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.basic.table.control.AbstractTableControl;
import org.eclipse.scout.rt.client.ui.basic.table.control.ITableControl;
import org.eclipse.scout.rt.client.ui.basic.table.control.SearchFormTableControl;

public abstract class AbstractPageWithTable5<T extends ITable> extends AbstractPageWithTable<T> {

  @Override
  protected void initConfig() {
    super.initConfig();
    getTable().setTableStatusVisible(true);
  }

  @Override
  public void initPage() throws ProcessingException {
    super.initPage();
    // FIXME CGU: move to AbstractTable.initConfig where controls are constructed
    for (ITableControl control : getTable().getTableControls()) {
      ((AbstractTableControl) control).setTable(getTable());
    }
  }

  @Override
  public void pageActivatedNotify() {
    super.pageActivatedNotify();
    if (isSearchRequired() && !getSearchFilter().isCompleted()) {
      SearchFormTableControl control = getTable().getTableControl(SearchFormTableControl.class);
      control.setSelected(true);
    }
  }

}
