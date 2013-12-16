/*******************************************************************************
 * Copyright (c) 2011 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 *******************************************************************************/
package org.eclipse.scout.rt.ui.rap.form.fields.snapbox;

import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.button.IButton;
import org.eclipse.scout.rt.client.ui.form.fields.snapbox.ISnapBox;
import org.eclipse.scout.rt.ui.rap.form.fields.IRwtScoutFormField;
import org.eclipse.scout.rt.ui.rap.form.fields.RwtScoutFieldComposite;
import org.eclipse.scout.rt.ui.rap.form.fields.snapbox.button.RwtScoutMinimizedButton;
import org.eclipse.scout.rt.ui.rap.form.fields.snapbox.layout.SnapBoxLayout;
import org.eclipse.scout.rt.ui.rap.form.fields.snapbox.layout.SnapBoxLayoutData;
import org.eclipse.scout.rt.ui.rap.form.fields.snapbox.layout.SnapBoxMaximizedLayout;
import org.eclipse.scout.rt.ui.rap.form.fields.snapbox.layout.SnapBoxMinimizedLayout;
import org.eclipse.swt.widgets.Composite;

public class RwtScoutSnapBox extends RwtScoutFieldComposite<ISnapBox> implements IRwtScoutSnapBox {

  private Composite m_maximizedItemArea;
  private Composite m_minimizedItemArea;

  @Override
  protected void initializeUi(Composite parent) {
    Composite container = getUiEnvironment().getFormToolkit().createComposite(parent);
    m_maximizedItemArea = getUiEnvironment().getFormToolkit().createComposite(container);
    m_minimizedItemArea = getUiEnvironment().getFormToolkit().createComposite(container);
    setUiContainer(container);
    // create fields
    for (IFormField field : getScoutObject().getFields()) {
      SnapBoxLayoutData data = new SnapBoxLayoutData();
      IRwtScoutFormField uiField = getUiEnvironment().createFormField(m_maximizedItemArea, field);
      uiField.getUiField().setLayoutData(data);
      if (field instanceof IButton) {
        RwtScoutMinimizedButton minButton = new RwtScoutMinimizedButton();
        minButton.createUiField(m_minimizedItemArea, (IButton) field, getUiEnvironment());
        minButton.getUiField().setLayoutData(data);
      }
    }
    // layout
    getUiContainer().setLayout(new SnapBoxLayout());
    SnapBoxMaximizedLayout snapBoxMaximizedLayout = new SnapBoxMaximizedLayout();
    m_maximizedItemArea.setLayout(snapBoxMaximizedLayout);
    m_minimizedItemArea.setLayout(new SnapBoxMinimizedLayout());
  }

  @Override
  protected void updateKeyStrokesFromScout() {
    // nop because the child fields also register the keystrokes of theirs parents
  }
}
