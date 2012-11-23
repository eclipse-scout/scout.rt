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
package org.eclipse.scout.rt.ui.rap.mobile.form.fields.tablefield;

import org.eclipse.scout.rt.client.mobile.ui.basic.table.IMobileTable;

/**
 * @since 3.9.0
 */
public class RwtScoutMobileList extends RwtScoutList {

  @Override
  protected RwtScoutListModel createUiListModel() {
    return new RwtScoutMobileListModel(getScoutObject(), this);
  }

  @Override
  public IMobileTable getScoutObject() {
    return (IMobileTable) super.getScoutObject();
  }

  protected RwtScoutMobileListModel getUiListModel() {
    return (RwtScoutMobileListModel) getUiTableViewer().getInput();
  }

  @Override
  protected void handleScoutPropertyChange(String propName, Object newValue) {
    super.handleScoutPropertyChange(propName, newValue);

    if (IMobileTable.PROP_PAGING_ENABLED.equals(propName)) {
      getUiListModel().setPagingEnabled((Boolean) newValue);
    }
  }

}
