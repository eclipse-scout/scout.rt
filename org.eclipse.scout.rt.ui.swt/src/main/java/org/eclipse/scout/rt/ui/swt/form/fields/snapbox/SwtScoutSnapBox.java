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
package org.eclipse.scout.rt.ui.swt.form.fields.snapbox;

import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.button.IButton;
import org.eclipse.scout.rt.client.ui.form.fields.snapbox.ISnapBox;
import org.eclipse.scout.rt.ui.swt.form.fields.ISwtScoutFormField;
import org.eclipse.scout.rt.ui.swt.form.fields.SwtScoutFieldComposite;
import org.eclipse.scout.rt.ui.swt.form.fields.snapbox.button.SwtScoutMinimizedButton;
import org.eclipse.scout.rt.ui.swt.form.fields.snapbox.layout.SnapBoxLayout;
import org.eclipse.scout.rt.ui.swt.form.fields.snapbox.layout.SnapBoxLayoutData;
import org.eclipse.scout.rt.ui.swt.form.fields.snapbox.layout.SnapBoxMaximizedLayout;
import org.eclipse.scout.rt.ui.swt.form.fields.snapbox.layout.SnapBoxMinimizedLayout;
import org.eclipse.swt.widgets.Composite;

public class SwtScoutSnapBox extends SwtScoutFieldComposite<ISnapBox> implements ISwtScoutSnapBox {

  private Composite m_maximizedItemArea;
  private Composite m_minimizedItemArea;

  @Override
  protected void initializeSwt(Composite parent) {
    Composite container = getEnvironment().getFormToolkit().createComposite(parent);
    m_maximizedItemArea = getEnvironment().getFormToolkit().createComposite(container);
    m_minimizedItemArea = getEnvironment().getFormToolkit().createComposite(container);
    setSwtContainer(container);
    // create fields
    for (IFormField field : getScoutObject().getFields()) {
      SnapBoxLayoutData data = new SnapBoxLayoutData();
      ISwtScoutFormField swtField = getEnvironment().createFormField(m_maximizedItemArea, field);
      swtField.getSwtField().setLayoutData(data);
      if (field instanceof IButton) {
        SwtScoutMinimizedButton minButton = new SwtScoutMinimizedButton();
        minButton.createField(m_minimizedItemArea, (IButton) field, getEnvironment());
        minButton.getSwtField().setLayoutData(data);
      }
    }
    // layout
    getSwtContainer().setLayout(new SnapBoxLayout());
    SnapBoxMaximizedLayout snapBoxMaximizedLayout = new SnapBoxMaximizedLayout();
    m_maximizedItemArea.setLayout(snapBoxMaximizedLayout);
    m_minimizedItemArea.setLayout(new SnapBoxMinimizedLayout());
  }

  @Override
  public ISnapBox getScoutObject() {
    return super.getScoutObject();
  }
}
