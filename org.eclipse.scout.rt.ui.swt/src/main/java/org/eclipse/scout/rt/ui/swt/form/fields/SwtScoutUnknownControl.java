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
package org.eclipse.scout.rt.ui.swt.form.fields;

import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.ui.swt.LogicalGridLayout;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

public class SwtScoutUnknownControl extends SwtScoutFieldComposite<IFormField> {

  private Label l;

  @Override
  protected void initializeSwt(Composite parent) {
    Composite container = new Composite(parent, SWT.NONE);
    container.setData("ScoutObject", getScoutObject());
    l = new Label(container, SWT.NONE);
    l.setBackground(l.getDisplay().getSystemColor(SWT.COLOR_YELLOW));
    l.setText("name:" + getScoutObject().getClass().getSimpleName());

    setSwtContainer(container);
    setSwtField(l);
    // layout
    container.setLayout(new LogicalGridLayout(1, 0));
  }

}
