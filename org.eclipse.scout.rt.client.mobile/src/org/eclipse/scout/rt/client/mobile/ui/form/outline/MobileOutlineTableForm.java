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
package org.eclipse.scout.rt.client.mobile.ui.form.outline;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ClientJob;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.desktop.outline.IOutline;
import org.eclipse.scout.rt.client.ui.form.outline.DefaultOutlineTableForm;

public class MobileOutlineTableForm extends DefaultOutlineTableForm {

  public MobileOutlineTableForm() throws ProcessingException {
    super();
  }

  @Override
  public void setCurrentTable(ITable table) {
    if (table != null) {
      getOutlineTableField().setLabel(computeHeaderNameForSingleColumnTable());
    }

    getOutlineTableField().installTable(table);
  }

  private String computeHeaderNameForSingleColumnTable() {
    String headerName = "";
    final IOutline outline = ClientJob.getCurrentSession().getDesktop().getOutline();
    if (outline != null && outline.getActivePage() != null) {
      headerName = outline.getActivePage().getCell().getText();
    }
    return headerName;
  }
}
