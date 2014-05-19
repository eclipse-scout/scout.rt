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
package org.eclipse.scout.rt.ui.html.json.desktop.fixtures;

import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ui.desktop.outline.IOutline;
import org.eclipse.scout.rt.client.ui.form.outline.DefaultOutlineTableForm;
import org.eclipse.scout.rt.client.ui.form.outline.DefaultOutlineTreeForm;

public class DesktopWithOutlineForms extends DesktopWithOneOutline {

  @Override
  protected void execOpened() throws ProcessingException {
    DefaultOutlineTreeForm treeForm = new DefaultOutlineTreeForm();
    treeForm.setAutoAddRemoveOnDesktop(false);
    treeForm.startView();
    addForm(treeForm);

    DefaultOutlineTableForm tableForm = new DefaultOutlineTableForm();
    tableForm.setAutoAddRemoveOnDesktop(false);
    tableForm.startView();
    addForm(tableForm);

    IOutline firstOutline = CollectionUtility.firstElement(getAvailableOutlines());
    setOutline(firstOutline);
  }

}
