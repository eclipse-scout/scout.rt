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
package org.eclipse.scout.rt.ui.rap.form.fields.groupbox;

import org.eclipse.rwt.lifecycle.WidgetUtil;
import org.eclipse.scout.rt.client.ui.form.fields.button.IButton;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.IGroupBox;
import org.eclipse.scout.rt.ui.rap.IRwtEnvironment;
import org.eclipse.scout.rt.ui.rap.form.fields.groupbox.layout.ButtonBarLayout;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public class RwtScoutGroupBoxButtonbar implements IRwtScoutGroupBoxButtonbar {

  static final String VARIANT_GROUP_BOX_BUTTON_BAR = "groupBoxButtonBar";
  private boolean m_created;
  private Composite m_container;
  private Composite m_leftPart;
  private Composite m_rightPart;
  private IRwtEnvironment m_uiEnvironment;
  private IGroupBox m_scoutGroupBox;

  public RwtScoutGroupBoxButtonbar() {
  }

  @Override
  public void createUiField(Composite parent, IGroupBox scoutGroupBox, IRwtEnvironment uiEnvironment) {
    m_scoutGroupBox = scoutGroupBox;
    m_uiEnvironment = (IRwtEnvironment) uiEnvironment;
    m_container = getUiEnvironment().getFormToolkit().createComposite(parent);
    m_container.setData(WidgetUtil.CUSTOM_VARIANT, VARIANT_GROUP_BOX_BUTTON_BAR);
    m_leftPart = getUiEnvironment().getFormToolkit().createComposite(m_container);
    m_rightPart = getUiEnvironment().getFormToolkit().createComposite(m_container);

    // layout
    GridLayout buttonBarLayout = new GridLayout(3, false);
    buttonBarLayout.horizontalSpacing = 0;
    buttonBarLayout.verticalSpacing = 0;
    buttonBarLayout.marginTop = 6;
    buttonBarLayout.marginHeight = 0;
    buttonBarLayout.marginWidth = 0;
    m_container.setLayout(buttonBarLayout);

    m_leftPart.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, true, true));
    m_rightPart.setLayoutData(new GridData(SWT.END, SWT.CENTER, true, true));

    m_leftPart.setLayout(new ButtonBarLayout(SWT.LEFT));
    m_rightPart.setLayout(new ButtonBarLayout(SWT.RIGHT));

    // init buttons
    //XXX [imo] add ui listener for invisible to make bar invisible when all buttons invisible
    for (IButton b : getScoutObject().getCustomProcessButtons()) {
      // alignment
      if (b.getGridData().horizontalAlignment <= 0) {
        getUiEnvironment().createFormField(m_leftPart, b);
      }
      else {
        getUiEnvironment().createFormField(m_rightPart, b);
      }
    }
    for (IButton b : getScoutObject().getSystemProcessButtons()) {
      // alignment
      if (b.getGridData().horizontalAlignment <= 0) {
        getUiEnvironment().createFormField(m_leftPart, b);
      }
      else {
        getUiEnvironment().createFormField(m_rightPart, b);
      }
    }
    m_created = true;
  }

  @Override
  public IGroupBox getScoutObject() {
    return m_scoutGroupBox;
  }

  @Override
  public Composite getUiContainer() {
    return m_container;
  }

  @Override
  public IRwtEnvironment getUiEnvironment() {
    return m_uiEnvironment;
  }

  @Override
  public Control getUiField() {
    return null;
  }

  @Override
  public boolean isUiDisposed() {
    return getUiContainer() == null || getUiContainer().isDisposed();
  }

  @Override
  public boolean isCreated() {
    return m_created;
  }

  @Override
  public void dispose() {
    m_created = false;
    getUiContainer().dispose();
  }
}
